# T620-SPF-NIDMOT-SW

FRC Team 620's robot code for the 2026 season. Java, built on [WPILib](https://docs.wpilib.org/) + [AdvantageKit](https://docs.advantagekit.org/) with a REV MAXSwerve drivetrain.

## Tech stack

- **Language/build:** Java 17, Gradle 8.11 via GradleRIO 2026.2.1
- **Framework:** WPILib command-based, [AdvantageKit](https://docs.advantagekit.org/) (IO-interface pattern + replay logging)
- **Vendor libraries** (see [vendordeps/](vendordeps/)): REVLib (SPARK MAX/Flex), Phoenix 6 (Pigeon 2), PathPlannerLib, Studica (NavX), URCL
- **Tooling:** [AdvantageScope](https://docs.advantagescope.org/overview/installation) for log viewing/telemetry, REV Hardware Client for motor controller config

## Prerequisites

1. [WPILib 2026 installer](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html) — bundles the JDK, VS Code, and CLI tools. This repo does not use a standalone JDK/IDE install.
2. [AdvantageScope](https://docs.advantagescope.org/overview/installation) for reading robot telemetry/logs — already bundled with the WPILib installer, though it may lag behind the latest release; grab that directly from Team 6328 if you need newer features.
3. [FRC Game Tools](https://www.ni.com/en/support/downloads/drivers/download.frc-game-tools.html) (NI, Windows-only) — installs the Driver Station app, needed to enable/control the robot on real hardware. Not part of the WPILib installer, and not needed for sim (the sim GUI replaces it, see below).
4. [REV Hardware Client](https://docs.revrobotics.com/rev-hardware-client-2) if you're touching drivetrain or subsystem hardware configuration.
5. GitHub access to this repo (ask in Slack if you don't have it).

## Getting started

```bash
git clone <repo-url>
cd T620-SPF-NIDMOT-SW
./gradlew build
```

`./gradlew build` compiles the code and runs tests — this is the same command CI runs on every push/PR, so if it passes locally it'll pass in CI.

Open the folder in VS Code with the WPILib extension installed to get the "WPILib: ..." commands (build, deploy, simulate) in the command palette.

### Simulate

`WPILib: Simulate Robot Code` (or `./gradlew simulateJava`) runs the robot code against WPILib's simulation GUI without any hardware. `Constants.simMode` controls which mode (`SIM` physics sim vs `REPLAY` log replay) is used when not running on the real RIO — see [Constants.java](src/main/java/frc/robot/Constants.java).

The sim GUI *replaces* the Driver Station app — you don't need the real DS for sim, and launching it won't connect to anything. To actually drive the robot in sim:

1. Once the sim GUI window opens, use the **Teleoperated** / **Autonomous** / **Disabled** buttons in the top-left panel to set robot mode (same as DS state buttons).
2. Plug in controllers before launching sim (or the GUI won't see them without a rescan). This repo expects two Xbox controllers: driver on port 0, operator on port 1 — see `driver`/`op` in [RobotContainer.java](src/main/java/frc/robot/RobotContainer.java).
3. In the **System Joysticks** panel, drag your detected controllers into the **Joysticks** slots at the matching index (0 and 1). If they're not in the right slots, the robot won't respond to input, or will respond to the wrong controller.
4. No physical controllers handy? Expand **Keyboard 0** and assign keys to axes/buttons the same way — useful for a quick smoke test.

### Deploy to the robot

`WPILib: Deploy Robot Code` from VS Code, or `./gradlew deploy`. Connect to the roboRIO first (USB or the field/practice radio). See [DRIVE_BRINGUP.md](src/main/java/frc/robot/subsystems/drive/DRIVE_BRINGUP.md) for first-time drivetrain bringup and calibration.

Unlike sim, real hardware needs the actual Driver Station app (from FRC Game Tools, see Prerequisites) to enable the robot and see faults/battery voltage. Open it, confirm it sees the roboRIO (team number set under Setup) and both driver/operator controllers are listed under USB Devices, then use its Enable/Disable buttons — deploying code alone does not enable the robot.

### VS Code keyboard shortcuts

The WPILib extension only ships a default keybinding for deploy (`Shift+F5`). VS Code doesn't support project-committed keybindings, so add the rest to your own `keybindings.json`: open the Command Palette → **Preferences: Open Keyboard Shortcuts (JSON)**, then merge in:

```json
[
  { "key": "shift+f6", "command": "wpilibcore.buildCode" },
  { "key": "shift+f7", "command": "wpilibcore.simulateCode" },
  { "key": "shift+f8", "command": "wpilibcore.testCode" }
]
```

(`wpilibcore.deployCode` is already bound to `Shift+F5` when `isWPILibProject` is true — no need to add it.) Change the keys if any of them collide with something else you use.

## Project structure

```
src/main/java/frc/robot/
├── Main.java, Robot.java        # entry point, mode-agnostic robot lifecycle
├── RobotContainer.java          # subsystem wiring, controller bindings, autos
├── Constants.java               # REAL / SIM / REPLAY mode switch
├── commands/                    # command factories, one file per subsystem area
├── subsystems/<name>/           # one package per subsystem
│   ├── <Name>.java              # subsystem logic (periodic, control loops)
│   ├── <Name>IO.java            # hardware-agnostic interface (@AutoLog inputs)
│   ├── <Name>IOSpark.java       # real hardware implementation
│   └── <Name>Constants.java     # tunable constants for that subsystem
└── util/
```

Every subsystem follows the AdvantageKit IO pattern: subsystem logic only talks to the `*IO` interface, never directly to a SPARK/Phoenix device. `RobotContainer` picks which `*IO` implementation to construct based on `Constants.currentMode` (real hardware, sim, or no-op for replay). When adding a new subsystem, follow this same interface/impl split — it's what makes sim and replay work for free.

## Testing

JUnit 5 is already wired up in [build.gradle](build.gradle) (`useJUnitPlatform()`); tests live under `src/test/java/frc/robot/...`, mirroring the `src/main/java` package layout. Run them with:

```bash
./gradlew test
```

CI (`.github/workflows/build.yml`) runs `./gradlew build` — which includes `test` — on every push and PR, so opening a PR (a draft is fine) is the easiest way to see tests run without needing hardware. Before adding new tests, check `src/test` for existing ones covering the area you're changing. If a test fails after your change, don't reflexively make it pass — figure out whether your change broke real behavior or the test's assumptions are what's actually outdated, and update whichever one is wrong.

## Troubleshooting

### `./gradlew build` says it can't find a JDK / asks you to set JAVA_HOME

The WPILib installer bundles its own JDK rather than touching your system `JAVA_HOME`, so it doesn't set the environment variable for you — the WPILib VS Code extension points its *internal* terminal at the bundled JDK automatically, but a plain terminal (or a fresh VS Code terminal that isn't going through the extension) has no idea it exists. Point `JAVA_HOME` at it yourself:

- **Windows:** `C:\Users\Public\wpilib\2026\jdk`
- **macOS/Linux:** `~/wpilib/2026/jdk`

PowerShell (persists across terminals):

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Users\Public\wpilib\2026\jdk", "User")
```

macOS/Linux (add to `~/.zshrc` / `~/.bashrc`):

```bash
export JAVA_HOME="$HOME/wpilib/2026/jdk"
```

Restart your terminal (and VS Code, if it's open) after setting it, then re-run `./gradlew build`.

## Contributing

1. Branch off `main`.
2. Open a PR using the [PR template](.github/pull_request_template.md) — fill in the checklist honestly, it's validated by [validate-pr-template.yml](.github/workflows/validate-pr-template.yml).
3. Any other teammate can review and approve — no single required reviewer.
4. CI must pass before merging.

## Getting help

- Slack for day-to-day questions.
- GitHub Issues/Discussions on this repo for anything worth tracking (bugs, design questions, feature proposals).
