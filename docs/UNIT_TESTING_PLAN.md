# Unit Testing Plan — T620 SPF-NIDMOT

Status: proposal / not yet implemented. No test code has been written against this plan yet.

## 0. Current state (baseline)

- Build: Gradle + GradleRIO 2026.2.1, Java 17. `build.gradle` already declares
  `junit-jupiter:5.10.1` + `junit-platform-launcher` and calls
  `wpi.java.configureTestTasks(test)` — **JUnit 5 is fully wired up but unused**.
  There is no `src/test` directory anywhere in the repo.
- CI: `.github/workflows/build.yml` runs `./gradlew build` on every push/PR. Because
  `build` depends on `test`, any tests placed under `src/test/java` will run in CI
  automatically with **no workflow changes required**.
- AdvantageKit 26.0.2. Every subsystem (`Drive`, `Indexer`, `IntakePivot`,
  `IntakeRoller`, `Shooter`, `Hood`) already follows the IO-interface pattern:
  an interface with `@AutoLog` inputs + `default` no-op methods, a `*Spark` real
  implementation, and generated `*IOInputsAutoLogged` classes. This is the exact
  seam unit tests need — every IO interface can already be satisfied with a bare
  `new XIO() {}` or a small hand-written fake.
- Only `Drive`/`ModuleIOSim` has genuine physics simulation (`DCMotorSim`). The
  other five subsystems fall back to anonymous no-op IO stubs in `SIM` mode in
  `RobotContainer` — they compile and run in sim, but move no real physics.
- PID/FF control loops (`Shooter`, `Hood`, `IntakePivot`) run **on the RIO side**,
  inside each subsystem's `periodic()`, using `edu.wpi.first.math.controller.PIDController`
  against IO inputs. This logic is pure Java and doesn't require hardware or even
  simulation to exercise — it's the cheapest, highest-value unit test target in the repo.

---

## 1. Public FRC/AdvantageKit example

Searched team 6328 Mechanical Advantage (AdvantageKit's author) and several other
public AdvantageKit-based repos (`HuskieRobotics/3061-lib`, `Team-190/2k25-Robot-Code`,
`Team254/FRC-2025-Public`). Findings:

- **6328 `RobotCode2025Public`** has exactly one test:
  `src/test/java/org/littletonrobotics/frc2025/RobotContainerTest.java` — a smoke
  test that just constructs `RobotContainer` and fails if it throws:
  ```java
  @Test
  public void createRobotContainer() {
    try {
      new RobotContainer();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to instantiate RobotContainer, see stack trace above.");
    }
  }
  ```
  Their newer `RobotCode2026Public` repo has **no `src/test` at all** — even the
  framework's own authors don't maintain deep unit tests in-season.
- AdvantageKit's own repo (`akit/` module) has two tests, both for internal logging
  data structures (`LogTable`, `LoggedMechanism2d`) — not robot/subsystem logic.
- **No shipped replay-based test harness exists.** AdvantageKit's "replay mode" is
  documented as a manual, eyeball-diff debugging workflow (compare `RealOutputs` vs
  `ReplayOutputs` in AdvantageScope) — there is no API to script assertions against
  a replayed log.
- The other three public repos checked have **no tests whatsoever**.

**Conclusion:** there is no established community pattern for unit-testing
AdvantageKit IO/Sim layers or driving simulation inside JUnit. The closest usable
precedent is generic WPILib guidance (docs.wpilib.org "Unit Testing" page): JUnit 5,
`HAL.initialize(500, 0)` in `@BeforeEach`, subsystems as `AutoCloseable`, asserting
against vendor sim classes. We will use that as the mechanical foundation and 6328's
`RobotContainerTest` as the floor (bare minimum every FRC codebase should have).

---

## 2. Java/WPILib unit testing standards to adopt

- **Framework:** JUnit 5 (`org.junit.jupiter`), already declared. Use
  `@Test`, `@BeforeEach`/`@AfterEach`, `@DisplayName` for readability, `@Nested` to
  group behavior per method under test.
- **HAL lifecycle:** every test class touching WPILib hardware/sim objects must call
  `HAL.initialize(500, 0)` in `@BeforeEach` and `HAL.shutdown()` (or close resources)
  in `@AfterEach` — WPILib objects (PID controllers are pure Java and don't need
  this, but `SparkMax`/motor sim objects do) require an initialized HAL to avoid
  JNI crashes.
- **Resource cleanup:** subsystems/IO objects that wrap hardware should implement
  `AutoCloseable`; tests should use try-with-resources or close them in `@AfterEach`
  to avoid leaking simulated device handles between tests (WPILib sim device state
  is process-global).
- **Determinism:** never depend on wall-clock time (`Timer.getFPGATimestamp()`)
  inside a unit test. Advance simulated time explicitly
  (`edu.wpi.first.hal.simulation.SimHooks.stepTiming(dt)` /
  `SimHooks.setProgramStarted()`) so tests are deterministic and don't flake in CI.
- **Structure:** one behavior per test method, Arrange-Act-Assert, descriptive
  method names (`atTargetVelocity_returnsFalse_whenVelocityControlDisabled`) rather
  than comments explaining what the test does.
- **Test doubles over mocking frameworks:** because every IO interface already has
  `default` no-op methods, prefer small hand-written fakes (anonymous classes or a
  package-private `FakeXIO implements XIO` with mutable fields the test can poke)
  over introducing Mockito. This matches how `RobotContainer` already builds sim IO
  and keeps the dependency graph unchanged.
- **Package mirroring:** `src/test/java/frc/robot/...` mirrors
  `src/main/java/frc/robot/...` 1:1, per standard Gradle/Java convention (e.g.
  `subsystems/shooter/Shooter.java` → `subsystems/shooter/ShooterTest.java`).
- **No hardware-only assertions:** tests must never require a connected RIO/CAN bus.
  If a test can't run under `./gradlew test` on a laptop, it's not a unit test —
  it belongs in a manual bench-test checklist instead.

---

## 3. High-level per-subsystem test plan

Ordered by effort (cheapest first) so the team can build momentum before tackling
harder subsystems.

| Subsystem | What to test | IO needed |
|---|---|---|
| `RobotContainer` | Smoke test: constructs without throwing in each `Constants.Mode` (mirrors 6328's baseline test). Cheapest, highest-value single test to add first. | none (real construction path) |
| `IntakeRoller`, `Indexer` | Pass-through duty-cycle logic: `setDutyCycle(x)` forwards `x` to IO; `stop()` sends 0; clamping/sign behavior if any exists in the subsystem (currently none — mostly validates there's nothing to regress). | fake IO (no-op is enough) |
| `Shooter` | PID/FF math in isolation: given fake IO reporting a `leftVelocityRPM`, verify `setTargetVelocity` + repeated `periodic()` calls drive `setDutyCycle` toward the setpoint sign-correctly; `isAtTargetVelocity()` true/false at the tolerance boundary; `stopVelocityControl()` zeroes output and disables control. | fake `ShooterIO` with settable `leftVelocityRPM` |
| `IntakePivot`, `Hood` | Same pattern as `Shooter` but for position control: verify direction of `setDutyCycle` output relative to `encoderPosition` vs `targetPosition`, clamping to `pivotMaxOutput`, `resetEncoder()`/`setBrakeMode()` forward to IO. | fake `IntakePivotIO`/`HoodIO` with settable position |
| `commands/*Commands.java` | Command factories (`ShooterCommands`, `IntakePivotCommands`, etc.) are mostly static composition — test that the right subsystem methods get called in the right order/conditions using fake subsystems, without needing the WPILib command scheduler running in real time. | fakes from above |
| `Drive` (swerve) | Highest effort, do last: kinematics math (chassis speeds ↔ module states) can be unit-tested with pure math assertions; pose estimator behavior can use `ModuleIOSim` (already physics-based) driven through several `periodic()` ticks and checked against expected odometry deltas. PathPlanner integration is closer to integration-test territory — lower priority for pure unit tests. | `ModuleIOSim` (exists) + fake `GyroIO` |

**Not recommended as unit tests:** anything requiring a live CAN bus, real sensor
noise characteristics, or driver-station connection — those stay as manual
bench-test checklist items (see §5).

---

## 4. Logging additions needed to support this plan

Current `@AutoLog` inputs classes (`ShooterIOInputs`, `IntakePivotIOInputs`,
`IndexerIOInputs`, `IntakeRollerIOInputs`, `HoodIOInputs`) only capture **raw
hardware feedback**: applied volts, velocity/position, current draw. None of the
control-loop state computed in the subsystem classes (`Shooter.java`,
`IntakePivot.java`, `Hood.java`) is logged anywhere — no setpoint, no PID error, no
enabled/disabled flag, no commanded output. `AutoLogOutput`/`Logger.recordOutput`
usage today is concentrated entirely in `Drive.java`.

This matters for two reasons: (1) it's a real observability gap for tuning/debugging
today, independent of testing, and (2) it's what would let unit tests assert against
the same values you can see live/in replay, instead of only against private fields.

Recommended additions, one per closed-loop subsystem (`Shooter`, `Hood`,
`IntakePivot`), added via `@AutoLogOutput` on getters or `Logger.recordOutput(...)`
calls in `periodic()`:

- `.../TargetVelocityRPM` or `.../TargetPosition` — current setpoint (0 or unused
  value when control is disabled, so it's meaningful even off-target)
  - "Enabled" boolean is derived by whether output is nonzero, but consider logging
    `.../VelocityControlEnabled` or `.../PositionControlEnabled` explicitly — cheap
    and removes ambiguity in replay.
- `.../Error` — `pid.getError()` (or setpoint minus measurement) so tuning and test
  assertions don't need to recompute it.
- `.../OutputDutyCycle` (or `OutputVolts`) — the actual clamped command sent to
  `io.setDutyCycle(...)`, i.e. the value after FF+PID+clamp, not just the raw applied
  volts read back from hardware.
- `.../AtSetpoint` (`Shooter` already computes this via `isAtTargetVelocity()`;
  `Hood`/`IntakePivot` don't currently expose an equivalent — worth adding a
  tolerance check similarly to `Shooter` for consistency).

These are `@AutoLogOutput`-style additions to the subsystem classes, not changes to
the `*IOInputs` structs (they're not hardware feedback — they're commanded/derived
state), so they don't affect the real/sim/replay IO implementations at all. They
directly parallel the fields the unit tests in §3 need to assert on, so writing the
logging and the tests can be the same PR per subsystem.

No new physics-sim classes (`FlywheelSim`, `SingleJointedArmSim`) are required to
execute the §3 plan — the recommended tests use fake IO with directly-set sensor
values, not simulated physics. Physics sim would be a separate, larger effort (see §5).

---

## 5. Effort/necessity estimate and role of SIM

### Effort (rough, per subsystem, assuming one PR per row of §3)

| Item | Effort | Notes |
|---|---|---|
| `RobotContainerTest` smoke test | ~30 min | Copy 6328's pattern almost verbatim. |
| `IntakeRoller`/`Indexer` tests | ~1–2 hrs total | Trivial pass-through logic, minimal fake IO. |
| `Shooter`/`Hood`/`IntakePivot` tests + logging additions (§4) | ~3–4 hrs each | Writing the fake IO once is reusable per subsystem; most time is in the logging additions and picking tolerance-boundary test cases. |
| Command factory tests | ~2–3 hrs total | Depends on how many command factories exist; reuses fakes from above. |
| `Drive` kinematics/pose tests | ~1–2 days | Real math to get right (coordinate frames, wheel radius/gear ratio units); highest payoff for bug-catching but most fiddly to write correctly. |
| Physics-based sim IO (`FlywheelSim`, `SingleJointedArmSim`) for `Shooter`/`Hood`/`IntakePivot` | ~1–2 days each, off-season stretch | Not required for the unit-test plan above; would upgrade sim-mode driver practice fidelity and enable closed-loop-under-physics tests, but is a separate initiative from unit testing correctness. |

**Total for the "pre-deployment gate" tier (RobotContainer + all six subsystems'
logic tests):** roughly 1.5–2 engineering-days, spread across however many students
work on it in parallel (subsystem tests are independent of each other).

### Necessity before robot deployment

- **Not competition-legal-required** — FRC doesn't check for unit tests. The
  question is purely risk/benefit for a small student team on a season clock.
- **High value, low cost** for the RobotContainer smoke test and the PID-logic
  tests (§3 rows 1–4): these catch exactly the bugs that are expensive on a real
  robot — wrong PID sign causing runaway output, wrong clamp direction, a
  `stopVelocityControl()` that doesn't actually zero output, a command factory
  wired to the wrong subsystem method. These are also the bugs hardest to see from
  telemetry alone until something breaks on the field.
- **Lower urgency** for `Drive` kinematics/pose tests and any physics-sim work —
  valuable but higher effort, and `Drive` already has the most existing
  logging/telemetry (`Logger.recordOutput` in `Drive.java`) to debug issues
  manually if something's wrong.
- **Recommendation:** treat §3 rows 1–4 (RobotContainer + Indexer/IntakeRoller +
  Shooter/Hood/IntakePivot logic) as a **pre-deployment gate** — cheap enough to run
  every time in CI (`build.yml` already runs `./gradlew test` for free) and directly
  tied to the "I ran the applicable tests locally" checkbox already required by
  `.github/pull_request_template.md`. Treat `Drive` kinematics tests as **should-do
  before first competition, not before every deploy**. Treat physics-sim IO as
  **off-season/stretch**, not a pre-deployment requirement.

### Can SIM be used for this without physics sim classes?

Yes, and it's important to separate two different meanings of "SIM" here:

1. **`Constants.Mode.SIM` + `RobotContainer`'s existing no-op IO stubs** — this is
   what currently runs when you launch the WPILib simulator GUI. It's good for
   exercising command bindings and driver-station interaction end-to-end, but
   because five of six subsystems have no real physics, sensor values never move on
   their own — not useful for automated assertions, only for manual poking.
2. **JUnit unit tests using fake IO (this plan)** — this doesn't use the WPILib
   simulator GUI or `Constants.Mode.SIM` at all; it directly `new`s a subsystem with
   a hand-written fake IO and calls `periodic()` in a loop from a test method. This
   is faster (no GUI, no driver station, runs in CI headlessly) and is what §3
   actually proposes.

Real physics sim (`DCMotorSim`/`FlywheelSim`/`SingleJointedArmSim`) would let (1)
become useful for automated testing too — e.g. a test that drives a `Shooter` with
`FlywheelSimIO` through many `periodic()` ticks and asserts the flywheel actually
converges to a target RPM given realistic inertia — but that's materially more work
per subsystem than the fake-IO approach and isn't necessary to get real bug-catching
value before the next deployment.

---

## 6. Post-deployment test commands (manual NetworkTables verification)

Separate from the JUnit plan above: `Command`s that run **on the real, deployed
robot** to drive each subsystem through a known setpoint and rely on
NetworkTables/AdvantageScope logging for a human to verify pass/fail. This is a
hardware-in-the-loop bench check, not a substitute for §3's automated tests — it
catches wiring/CAN-ID/mechanical issues JUnit can't see, but requires a person to
run it and read the log, and it doesn't gate CI.

### Pattern

The codebase already has the right shape to extend: `commands/ShooterCommands.java`
has `shooterTuning`/`shooterVoltageTuning`, which read SmartDashboard values in a
`Commands.runEnd` loop, and `RobotContainer` already wires a
`LoggedDashboardChooser<Command>` (`autoChooser`) populated via
`autoChooser.addOption(...)`. Test commands reuse both idioms:

- Each test command commands a subsystem to a setpoint, waits for either
  `waitUntil(subsystem::isAtSetpoint).withTimeout(t)` or a fixed duration, then
  records a boolean/number verdict to NT via `Logger.recordOutput(...)` and leaves
  the subsystem stopped (`.finallyDo(subsystem::stop...)`), the same cleanup pattern
  `runEnd` already uses everywhere.
- A second `LoggedDashboardChooser<Command>` (e.g. `"Test Choices"`), populated the
  same way `autoChooser` is, lets you pick a test from Shuffleboard/AdvantageScope
  and run it like an auto — no redeploy needed to switch which test runs, and it's
  automatically visible in NT like everything else.
- Every test command depends on the `Error`/`OutputDutyCycle`/`AtSetpoint` fields
  proposed in §4 — without them you can only see pass/fail, not *why* something
  failed (oscillating vs. undriven vs. saturated output), which is most of the
  point of running these on hardware instead of just watching the mechanism move.

### Per-subsystem design

**Shooter** (`ShooterTestCommands.spinUpTest(shooter, rpm, timeoutSec)`) — closed-loop,
already has `isAtTargetVelocity()`:
```java
public static Command spinUpTest(Shooter shooter, double rpm, double timeoutSec) {
  return Commands.sequence(
          Commands.runOnce(() -> Logger.recordOutput("Test/Shooter/Active", true)),
          Commands.deadline(
              Commands.waitUntil(shooter::isAtTargetVelocity).withTimeout(timeoutSec),
              ShooterCommands.runAtVelocity(shooter, rpm)),
          Commands.runOnce(
              () -> {
                Logger.recordOutput("Test/Shooter/Passed", shooter.isAtTargetVelocity());
                Logger.recordOutput("Test/Shooter/Active", false);
              }))
      .finallyDo(interrupted -> shooter.stopVelocityControl());
}
```
Run at 2-3 setpoints (idle/preset/max RPM) to catch FF/PID issues that only show at
one end of the range.

**Hood, IntakePivot** (`HoodTestCommands.moveToPositionTest`,
`IntakePivotTestCommands.moveToPositionTest`) — same shape as `Shooter`'s, but both
need the `AtSetpoint` getter from §4 first (neither exposes one today). Test the
two mechanically meaningful positions (stow/extend for the pivot, retracted/deployed
for the hood) rather than an arbitrary setpoint.

**Indexer, IntakeRoller** (`IndexerTestCommands.spinTest`,
`IntakeRollerTestCommands.spinTest`) — open-loop, no setpoint to converge to, so
"pass" means "the motor actually responded": run a fixed duty cycle for a fixed
duration and check current draw exceeds a stall/no-load threshold
(`Test/Indexer/Passed = leaderCurrentAmps > <threshold>` after settling). Simplest
tier — no new §4 fields required, existing current logging is already sufficient.

**Drive** — lowest priority and most complex: command each `Module` to a known
angle/speed and compare against `Drive.java`'s existing `Logger.recordOutput` module
states, or a simpler "drive 1m, check odometry delta" test. `Drive` already has by
far the most existing telemetry of any subsystem, so the incremental value of a
dedicated test command here is lower than for the others — defer until the rest are
in place.

**Full System Test** — one `Commands.sequence(...)` chaining all of the above (each
already self-timeouting), added as the default/first option in the test chooser so
a single selection exercises the whole robot pre-match with it secured on a stand.

---

## 7. Prioritized rollout order

Two independent tracks share the §4 logging work as a prerequisite. Within each
subsystem, do the logging addition and that subsystem's test command in the same
PR (they're the same few lines of `Logger.recordOutput`/getter work read two ways).
Rows refer to the §3 table.

| # | Task | Depends on | Why this order |
|---|---|---|---|
| 1 | `RobotContainerTest` smoke test (row 1) | nothing | Zero dependencies, ~30 min, do it immediately in parallel with everything else below — establishes `src/test/java` and CI actually running tests. |
| 2 | §4 logging additions — **Shooter** (`TargetVelocityRPM`, `Error`, `OutputDutyCycle`, enabled flag; `AtSetpoint` already exists via `isAtTargetVelocity()`) | nothing | Highest-energy mechanism (flywheel) and already has the most control-loop logic to get wrong; also the one PID subsystem with an existing at-setpoint check to build on. |
| 3 | `ShooterTestCommands.spinUpTest` + test chooser wiring in `RobotContainer` | #2 | First real use of the new fields; also stands up the `LoggedDashboardChooser` + chooser-button pattern the rest of the test commands reuse. |
| 4 | §4 logging additions — **Hood** and **IntakePivot** (add `TargetPosition`, `Error`, `OutputDutyCycle`, and a new `AtSetpoint` getter to both — neither has one today) | nothing (parallel to #2/#3) | Same shape as Shooter, can be done by a second person concurrently; both need the `AtSetpoint` getter added, so do them together rather than duplicating the "do we need this?" discussion twice. |
| 5 | `HoodTestCommands`/`IntakePivotTestCommands` position tests | #4, #3 (reuses chooser wiring) | Straightforward once the pattern from #3 exists. |
| 6 | Row 3 JUnit tests — `Shooter`/`Hood`/`IntakePivot` PID/FF logic (fake IO) | #2, #4 (reuses the same getters/fields) | This is the highest bug-catching value in the JUnit plan (§5), and the getters needed for the test's assertions are the same ones just added for logging — cheap to do right after. |
| 7 | Row 2 JUnit tests — `Indexer`/`IntakeRoller` pass-through logic | nothing | Trivial, no new logging needed; low priority only because there's little logic to break, not because it's hard. |
| 8 | `IndexerTestCommands`/`IntakeRollerTestCommands` current-threshold spin tests | #3 (chooser pattern), existing current logging | No new §4 fields required; slot in whenever convenient. |
| 9 | Row 4 JUnit tests — command factories (`ShooterCommands`, `IntakePivotCommands`, etc.) | #6, #7 (reuses their fakes) | Composition-level tests are cheapest once the underlying subsystem fakes already exist from #6/#7. |
| 10 | "Full System Test" sequence command | #3, #5, #8 | Just a `Commands.sequence(...)` over everything already built — do last. |
| 11 | Drive test command + Drive JUnit kinematics/pose tests (§3 last row) | nothing new | Deferred per §5 — highest effort, and `Drive` already has the best existing telemetry of any subsystem. |

Tasks 1, 2/3, and 4/5 have no dependency on each other and can run fully in
parallel across students; 6-9 fan back in once the logging/getters they reuse land.

---

## 8. Keeping tests up to date

Staleness comes from three different triggers, and they don't all deserve the same
fix — some of this should stay manual on purpose.

### 1. Tuning changes (manual — this is the test working as intended)

When a PID gain, tolerance, or setpoint constant changes, the corresponding test's
expected values need updating in the **same PR** as the tuning change — the same
"logging and test in one PR" discipline already recommended in §4/§7. This should
stay a manual PR-hygiene habit, not be automated: a test that silently re-passes
after a real behavior change has stopped doing its job. A failing test after a
tuning change is a prompt to go update the test deliberately, not a bug to
engineer away.

### 2. IO interface changes (already low-maintenance by design)

Every `*IO` interface in this repo uses `default` no-op methods (§0), so adding a
new hardware signal to an IO interface never breaks an existing fake or forces an
unrelated test to be rewritten. No process change needed here — this is a property
of the existing codebase pattern, worth preserving as new IO interfaces are added.

### 3. New subsystems (the real gap — automate the reminder, not the content)

Nothing today prompts a student adding a new subsystem to also add its fake IO,
test class, §4 logging fields, or §6 test command — coverage can silently fall
behind as the robot grows. Two automations close this gap without trying to
generate assertions (which requires knowing what "correct" behavior actually is,
and can't be automated):

- **CI enforcement check**: a step in `.github/workflows/build.yml` (or a small
  Gradle task) that fails the build when a new `src/main/.../*IO.java` interface
  file is added without a matching `src/test/.../*Test.java` file. This is a
  mechanical file-existence check (e.g. `git diff --name-only` against the base
  branch, cross-referenced against `src/test`), not a correctness check — it only
  catches "we forgot," which is the most common way coverage actually erodes.
- **Scaffold generator**: a small script (or Gradle task, e.g.
  `./gradlew newSubsystemTest --name=Climber`) that generates the boilerplate
  skeleton for a new subsystem — a fake IO implementing the interface's `default`
  methods with a few settable fields, and an empty `@Test`-annotated test class
  following the existing naming/package convention — so students aren't
  hand-typing the same ~15 lines every time a subsystem is added. It scaffolds
  structure, not intent; the student still writes the actual assertions.

### Additional habit: prefer parameterized tests over copy-paste

For subsystems tested at multiple setpoints (e.g. `Shooter` at idle/preset/max RPM,
per §6), use JUnit 5's `@ParameterizedTest`/`@ValueSource` instead of one hand-copied
test method per setpoint. Adding a new case then becomes a one-line data addition
instead of a new method to keep in sync with the others — fewer places for drift to
hide as the season's setpoints change.

Both automations in this section (the CI check and the scaffold generator) are
proposed but not yet implemented — flagging as a follow-on task if the team wants
them built out alongside the §7 rollout.

---

## 9. Task breakdown for parallel/agent-based implementation

Each task below is written to be handed to a separate agent (or student) with no
other context beyond this document — it states the concrete deliverable, exact
files, and acceptance criteria rather than assuming the reader has the rest of this
conversation. Tasks are grouped into dependency waves; **everything within a wave
touches disjoint files and can run fully in parallel**. The one deliberate
exception is T11 (RobotContainer wiring), called out below — it's the single
shared-file integration point and should run after, not concurrently with, the
tasks it depends on.

### Wave 0 — no dependencies, run all in parallel

- **T1 — RobotContainer smoke test.** Create
  `src/test/java/frc/robot/RobotContainerTest.java`: a JUnit 5 test that
  constructs `new RobotContainer()` and fails with the exception printed if it
  throws (see §1 for the reference pattern from team 6328). This is also the task
  that establishes `src/test/java` in the repo for the first time.
  Acceptance: `./gradlew test` runs and passes this test; CI (`build.yml`) is green.

- **T2 — Shooter control-loop logging.** In
  `src/main/java/frc/robot/subsystems/shooter/Shooter.java`, add
  `Logger.recordOutput(...)` calls (or equivalent `@AutoLogOutput` getters) for:
  `Shooter/TargetVelocityRPM`, `Shooter/VelocityControlEnabled`, `Shooter/Error`
  (from `pid.getError()`), `Shooter/OutputDutyCycle` (the clamped value passed to
  `io.setDutyCycle`). `Shooter/AtSetpoint` already exists as `isAtTargetVelocity()`
  — just log it too. See §4 for full rationale. No changes to `ShooterIO.java` —
  this is commanded/derived state, not hardware feedback.
  Acceptance: new fields visible in AdvantageScope/NT under `Shooter/` while the
  robot runs in sim or real.

- **T3 — Hood + IntakePivot control-loop logging.** Same pattern as T2, applied to
  `subsystems/shooter/Hood.java` and `subsystems/intake/IntakePivot.java`. Both
  currently lack any at-setpoint check — add a `isAtSetpoint()` method to each
  (tolerance constant alongside the existing PID gains) modeled on `Shooter`'s
  `isAtTargetVelocity()`, then log `TargetPosition`, `PositionControlEnabled`,
  `Error`, `OutputDutyCycle`, `AtSetpoint` for both. Do both subsystems in this one
  task since they share the same new-getter design decision.
  Acceptance: same as T2, for both `Hood/` and `IntakePivot/` NT namespaces.

- **T4 — Indexer/IntakeRoller logic tests.** Create
  `src/test/java/frc/robot/subsystems/indexer/IndexerTest.java` and
  `.../intake/IntakeRollerTest.java`. Use a hand-written fake IO (anonymous class
  or small package-private class implementing `IndexerIO`/`IntakeRollerIO`) to
  verify `setDutyCycle(x)` forwards `x` unchanged and `stop()`/equivalent sends 0.
  See §3 (row 2) for scope; this is intentionally the simplest test in the plan —
  no source changes needed, no dependency on T2/T3.
  Acceptance: tests pass under `./gradlew test`.

- **T5 — CI check for missing subsystem tests.** Add a step to
  `.github/workflows/build.yml` (or a small script it invokes) that fails the
  build when a file matching `src/main/java/frc/robot/subsystems/**/*IO.java` is
  added or modified without a corresponding file under
  `src/test/java/frc/robot/subsystems/**/*Test.java` in the same diff (compare
  against the PR base branch). See §8 for rationale. This is a file-existence
  check only — it does not evaluate test content or quality.
  Acceptance: a test PR that adds a new `*IO.java` with no matching test file
  fails this new CI step; a PR that adds both, or adds neither, passes.

- **T6 — Scaffold generator for new subsystems.** Add a small script (e.g.
  `scripts/new-subsystem-test.<sh|py>`, or a Gradle task) that, given a subsystem
  name, generates: a stub `src/test/.../<Name>Test.java` with an empty `@Test`
  method and a fake-IO skeleton implementing that subsystem's IO interface's
  `default` methods. See §8. This only needs to produce compilable boilerplate,
  not meaningful assertions.
  Acceptance: running the script against one of the existing subsystems (e.g.
  `IntakeRoller`) produces a file that compiles once assertions are filled in.

- **T16 — Dedicated "run tests on PR" workflow, and revisit the checklist gate.**
  Today, `.github/workflows/build.yml` runs `./gradlew build` (which happens to
  run tests as a side effect of `build`, but doesn't surface them as a distinct
  check), and `.github/workflows/validate-pr-template.yml` requires the PR author
  to manually check "I ran the applicable tests locally and they **passed**" in
  the PR description — an honor-system checkbox that GitHub Actions verifies is
  *checked*, not that it's *true*. Once tests actually exist (from the tasks
  above), that checkbox is redundant with something CI can verify directly.
  Add `.github/workflows/test.yml`: triggers on `pull_request`, runs
  `./gradlew test` (not the full `build`) in the same
  `wpilib/roborio-cross-ubuntu:2024-22.04` container as `build.yml`, named
  distinctly (e.g. job name `Unit Tests`) so it shows up as its own PR status
  check instead of being folded into "Build." On failure, upload the JUnit XML
  under `build/test-results` as a workflow artifact so a failure is diagnosable
  from the PR without re-running locally.
  Then update the checklist gate to match: in
  `validate-pr-template.yml`, drop `"I ran the applicable tests locally and they
  **passed**"` from `required_items` (the new `test.yml` check now proves this
  instead of asking the author to self-attest), and remove or reword the
  corresponding line in `.github/pull_request_template.md`. Leave `"I ran the
  code on the robot and it works"` as a required, still-manual item — that one
  isn't automatable. This task is explicitly free to restructure
  `validate-pr-template.yml` further if a cleaner checklist design turns up
  while doing this — the existing script's structure is not sacred.
  Acceptance: opening a PR shows a separate "Unit Tests" check alongside "Build";
  a failing test fails that check (and only that check, not the checklist
  validator); the checklist validator no longer requires the now-redundant
  local-test checkbox; the PR template reflects the same change.

### Wave 1 — depends on Wave 0, still parallel against each other

- **T7 — ShooterTestCommands.** Create
  `src/main/java/frc/robot/commands/ShooterTestCommands.java` with
  `spinUpTest(Shooter shooter, double rpm, double timeoutSec)` per the §6 code
  sample, run at 2-3 RPM setpoints. Depends on T2 (uses the new logged fields for
  diagnosability, though it will compile without them).
  Acceptance: selecting/running the command on a deployed or sim robot logs
  `Test/Shooter/Passed` and `Test/Shooter/Active` to NT.

- **T8 — HoodTestCommands + IntakePivotTestCommands.** Create
  `src/main/java/frc/robot/commands/HoodTestCommands.java` and
  `.../IntakePivotTestCommands.java`, same shape as T7 but for position control,
  using the `isAtSetpoint()` getters T3 adds. Hard dependency on T3 (these
  getters don't exist yet).
  Acceptance: same as T7, for `Test/Hood/` and `Test/IntakePivot/`.

- **T9 — IndexerTestCommands + IntakeRollerTestCommands.** Create the two
  current-threshold spin-test commands described in §6 (run a fixed duty cycle,
  check current exceeds a stall/no-load threshold after settling). No dependency
  on T2/T3 — only needs existing current logging.
  Acceptance: `Test/Indexer/Passed` / `Test/IntakeRoller/Passed` populate in NT.

- **T10 — Shooter/Hood/IntakePivot JUnit logic tests (row 3).** Create
  `src/test/java/frc/robot/subsystems/shooter/ShooterTest.java`,
  `.../shooter/HoodTest.java`, `.../intake/IntakePivotTest.java`. Using fake IOs
  with settable sensor fields, verify: output direction is sign-correct relative
  to setpoint-vs-measurement, `stopVelocityControl()`/`stopPositionControl()`
  zeroes output and disables control, and at-setpoint boundary behavior (using
  the getters from T2/T3). Hard dependency on T2 and T3.
  Acceptance: tests pass under `./gradlew test`.

### Wave 2 — integration (shared file, run after Wave 1 completes)

- **T11 — Wire test commands into RobotContainer.** The only task in this plan
  that touches `RobotContainer.java`, and the reason it's sequenced last: add a
  second `LoggedDashboardChooser<Command>` (e.g. `"Test Choices"`) next to the
  existing `autoChooser`, add `.addOption(...)` entries for every command from
  T7/T8/T9, and add a "Run Selected Test" `SmartDashboard.putData(...)` button
  mirroring how `autoChooser` is consumed. Depends on T7, T8, T9 all being merged
  first to avoid repeated conflicts on this one file.
  Acceptance: the "Test Choices" chooser appears in Shuffleboard/AdvantageScope
  and running a selected entry produces the expected `Test/...` NT output.

- **T12 — Full System Test sequence.** Create
  `src/main/java/frc/robot/commands/TestRoutines.java` with a
  `fullSystemTest(...)` command composing all of T7/T8/T9's commands via
  `Commands.sequence(...)`, and add it as the default option in T11's chooser.
  Depends on T7, T8, T9, and T11.
  Acceptance: selecting "Full System Test" runs every subsystem test in sequence
  and each logs its own pass/fail independently.

### Wave 3 — depends on Waves 0-1 fakes

- **T13 — Command factory JUnit tests (row 4).** Create test files for
  `ShooterCommands`, `IntakePivotCommands`, `HoodCommands`, `IndexerCommands`,
  and `Autos` under `src/test/java/frc/robot/commands/`, reusing the fake IOs
  built for T4 and T10 to verify command factories call the right subsystem
  methods under the right conditions. Depends on T4 and T10.
  Acceptance: tests pass under `./gradlew test`.

### Wave 4 — deferred, no strict dependency but lowest priority (see §5, §6, §7)

- **T14 — Drive test command.** Module-level or straight-line odometry test
  command per §6's `Drive` design. No hard dependency, but intentionally last —
  `Drive` already has the best existing telemetry of any subsystem.

- **T15 — Drive kinematics/pose JUnit tests.** Pure-math kinematics tests plus
  `ModuleIOSim`-driven pose estimator tests per §3's last row. Highest effort in
  the plan; do only once Waves 0-3 are complete.

### Coordination notes

- Every task except T11 creates new files or edits files no other task touches —
  safe to assign to fully independent agents/students without merge coordination.
- T11 is the single integration point; hold it until T7/T8/T9 are merged, or
  expect repeated conflicts on `RobotContainer.java`.
- T5 and T6 are process/tooling tasks, not subsystem code — safe to assign to
  whoever is comfortable with Gradle/CI/scripting rather than robot logic.
- T16 touches `.github/workflows/test.yml` (new), `validate-pr-template.yml`, and
  `pull_request_template.md` — disjoint from T5's `build.yml` change, so the two
  can run in parallel despite both being CI-focused.
