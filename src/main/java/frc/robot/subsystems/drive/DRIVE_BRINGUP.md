# Drive Subsystem Bringup — T620-SPF-NIDMOT-SW

Hardware: REV MAXSwerve | SPARK Flex (drive) + SPARK Max (turn) | NEO V1 (drive) | NEO 550 (turn)
Template: AdvantageKit v26.0.2 Spark Swerve
Ref: https://docs.advantagekit.org/getting-started/template-projects/spark-swerve-template

---

## Prerequisites

- [ ] roboRIO 2 imaged and on network
- [ ] All SPARK MAX controllers updated to latest firmware via REV Hardware Client
- [ ] FAT32-formatted USB stick inserted in roboRIO (required for AdvantageKit log files)
- [ ] AdvantageScope installed on driver station laptop
- [ ] Robot connected via USB or radio

---

## Step 1 — CAN ID Verification

Open **REV Hardware Client** and confirm every controller is visible and assigned the correct CAN ID matching `DriveConstants.java`:

| Module        | Drive CAN ID | Turn CAN ID |
|---------------|-------------|------------|
| Front Left    | 10          | 20         |
| Front Right   | 11          | 21         |
| Back Left     | 12          | 22         |
| Back Right    | 13          | 23         |
| Pigeon 2 IMU  | 62          | —          |

IDs sourced from Harley-2025 electronics spreadsheet:
https://docs.google.com/spreadsheets/d/1E2qQl7P2I0ImZtpcdnKNVKiHB-iJdpjz9EQkrZBmuj4/edit?usp=sharing

> **Note:** `RobotContainer` is already wired to `GyroIOPigeon2`. Confirm `pigeonCanId = 62` in `DriveConstants.java` matches the physical device.

In REV Hardware Client, also verify:
- Firmware is current on all SPARK MAX units
- No duplicate CAN IDs exist on the bus
- Encoder type per controller: **Through Bore Encoder** on turn, **Hall Effect** on drive

---

## Step 2 — Initial Constants Check

Before first deploy, confirm these values in `DriveConstants.java`:

```java
// Active hardware selection — edit only these three lines to reconfigure
public static final GearConfig driveConfig = GearConfig.HIGH;
// HIGH = 4.71:1 drive ratio, 4.80 m/s free speed, 14T pinion (REV-21-3005-P23)
public static final DriveMotor driveMotorConfig = DriveMotor.NEO_V1;
public static final TurnMotor turnMotorConfig = TurnMotor.NEO_550;

// Chassis outer frame dimensions in inches — trackWidth/wheelBase are derived from these
public static final double frameWidthInches = 26.5; // measure chassis bolt-to-bolt + module housing
public static final double frameLengthInches = 26.5; // measure chassis bolt-to-bolt + module housing
// wheelRadiusMeters is derived from driveConfig — HIGH config = 1.5 in (3 in wheel)
public static final double wheelRadiusMeters = driveConfig.wheelRadiusMeters;
```

Set all zero rotations to `0.0` for now — they get calibrated in Step 5:

```java
public static final Rotation2d frontLeftZeroRotation  = new Rotation2d(0.0);
public static final Rotation2d frontRightZeroRotation = new Rotation2d(0.0);
public static final Rotation2d backLeftZeroRotation   = new Rotation2d(0.0);
public static final Rotation2d backRightZeroRotation  = new Rotation2d(0.0);
```

---

## Step 3 — First Deploy & AdvantageScope Connection

1. Deploy code: `WPILib: Deploy Robot Code` in VS Code
2. Open **AdvantageScope**
3. Connect: `File → Connect to Robot` → enter roboRIO IP (`10.6.20.2`) or use USB
4. Once connected, open the **Log Viewer** tab

Check the **Alerts** panel in AdvantageScope (or Driver Station log) for any startup errors:
- Missing CAN devices → wrong ID or disconnected
- Encoder faults → wiring issue
- No alerts = safe to proceed

---

## Step 4 — Turn Direction Verification

**Goal:** Confirm each module steers counter-clockwise when given a positive command.

In AdvantageScope, subscribe to:
```
/Drive/Module0/TurnPosition   (Front Left)
/Drive/Module1/TurnPosition   (Front Right)
/Drive/Module2/TurnPosition   (Back Left)
/Drive/Module3/TurnPosition   (Back Right)
```

Enable the robot in **Teleop**. Physically rotate each module **counter-clockwise by hand** (when viewed from above). The corresponding `TurnPosition` value must **increase**.

- If it decreases → flip `turnInverted` in `DriveConstants.java` (currently `false`)
- If no change → encoder not reading (wiring or CAN issue)

---

## Step 5 — Zero Offset Calibration

**Goal:** Set absolute encoder offsets so each module knows its forward-facing position.

### Option A — REV MAXSwerve Zeroing Tool (recommended)

The REV MAXSwerve Calibration Tool sets a fixed mechanical reference into each through-bore encoder. Because all four modules share the same physical geometry, the expected `zeroRotation` values are constant and already defined in `MAXSwerveModuleConfig.ZeroRotations`.

Full procedure reference: [REV Hardware Client — MAXSwerve Calibration](https://docs.revrobotics.com/rev-hardware-client-2/guides/swerve-calibration#maxswerve-calibration)

**Zeroing procedure (do once per module, or after any encoder replacement):**

Do each module individually — the SPARK must be connected directly to your computer for this step:

1. Fully assemble the MAXSwerve module with the steering SPARK MAX connected to the Through Bore Encoder
2. Connect the **steering SPARK MAX directly to your laptop via USB-C** (do not use CAN for this step)
3. Open **REV Hardware Client** and verify firmware is current under the **Update** tab
4. Navigate to **Utilities Tab → Absolute Encoder**
5. Mount the Calibration Tool on the module with its lip facing the module; rotate the wheel and tool together until **the tool's lip drops firmly into position** — the bevel gear must slot into **the cutout side marked with the orange dot** (the side with additional corners and curves)
6. Once seated, the wheel will be unable to rotate freely — this mechanical lock ensures accurate placement
7. Click **Set Zero Offset** in the software
8. Remove the tool and repeat for the remaining three modules

After zeroing all modules, set `DriveConstants.java` to the known geometry values:

```java
public static final Rotation2d frontLeftZeroRotation  = MAXSwerveModuleConfig.ZeroRotations.FRONT_LEFT;   // -π/2
public static final Rotation2d frontRightZeroRotation = MAXSwerveModuleConfig.ZeroRotations.FRONT_RIGHT;  //  0
public static final Rotation2d backLeftZeroRotation   = MAXSwerveModuleConfig.ZeroRotations.BACK_LEFT;    //  π
public static final Rotation2d backRightZeroRotation  = MAXSwerveModuleConfig.ZeroRotations.BACK_RIGHT;   //  π/2
```

Deploy and verify: all four modules should snap to **straight forward** on enable.

> If a module points slightly off after zeroing, the bevel gear was not perfectly centered during the zeroing step. Fine-tune using Option B below.

### Option B — Manual alignment (fallback / fine-tune)

1. Physically align all four wheels **straight forward** using a long piece of aluminum tubing laid against both wheels on each side
2. In AdvantageScope, read the current `TurnPosition` values for all four modules
3. Update `DriveConstants.java` with the recorded values:

```java
public static final Rotation2d frontLeftZeroRotation  = new Rotation2d(<recorded_value>);
public static final Rotation2d frontRightZeroRotation = new Rotation2d(<recorded_value>);
public static final Rotation2d backLeftZeroRotation   = new Rotation2d(<recorded_value>);
public static final Rotation2d backRightZeroRotation  = new Rotation2d(<recorded_value>);
```

4. Redeploy and verify: all modules snap to forward on enable.

---

## Step 6 — Drive Direction Verification

**Goal:** Confirm positive drive command → forward wheel motion.

In AdvantageScope, subscribe to:
```
/Drive/Module0/DrivePositionRad
```

Push the robot **forward** by hand. The value must **increase**. Repeat per module.

- If decreasing → there is no `driveInverted` constant; add `.inverted(true)` to the `SparkFlexConfig` block in `ModuleIOSpark.java`

---

## Step 7 — Odometry Sanity Check

In AdvantageScope, open the **Odometry** tab or plot:
```
/Drive/Pose
```

1. Place robot at a known field position
2. Drive straight forward ~1 meter
3. Confirm the pose X value increases by ~1.0 m and Y stays flat
4. Drive a full square — robot should return close to origin

Significant drift indicates wrong `wheelRadiusMeters` or `trackWidth`/`wheelBase` — adjust and redeploy.

---

## Step 8 — Feedforward Characterization

**Goal:** Measure real `driveKs` and `driveKv` from hardware.

1. Place robot on carpet with room to drive straight
2. In the Driver Station dashboard, select **"Drive Simple FF Characterization"** from the `Auto Choices` chooser
3. Enable in **Auto** — robot will slowly ramp drive motors for 5–10 seconds
4. Disable when done
5. Check Driver Station console for printed `kS` and `kV` values
6. Update `DriveConstants.java`:

```java
public static final double driveKs = <printed_value>;
public static final double driveKv = <printed_value>;
```

---

## Step 9 — Wheel Radius Characterization

**Goal:** Precisely measure actual wheel radius (accounting for wear/compression).

1. Place robot on carpet
2. In the Driver Station dashboard, select **"Drive Wheel Radius Characterization"** from the `Auto Choices` chooser
3. Enable in **Auto** — robot spins in place for at least one full rotation
4. Console prints corrected `wheelRadiusMeters`
5. Update `DriveConstants.java` — change the derived field to a measured literal:

```java
// Replace: public static final double wheelRadiusMeters = driveConfig.wheelRadiusMeters;
public static final double wheelRadiusMeters = <printed_value>;
```

---

## Step 10 — Drive PID Tuning

In AdvantageScope, plot:
```
/Drive/Module0/DriveVelocityRadPerSec      (measured)
/Drive/Module0/DriveVelocitySetpointRadPerSec  (setpoint)
```

Tune `driveKp` and `driveKd` until measured tracks setpoint cleanly with no oscillation.

For turn PID, plot:
```
/Drive/Module0/TurnPosition      (measured)
/Drive/Module0/TurnPositionSetpoint
```

`turnKp = 2.0` is a reasonable starting point for MAXSwerve. Increase until response is crisp without oscillation.

---

## Step 11 — Max Speed Measurement

1. In the Driver Station dashboard, select **"Drive Simple FF Characterization"** from the `Auto Choices` chooser, or drive at full stick in Teleop
2. In AdvantageScope plot `/Drive/Module0/DriveVelocityRadPerSec`
3. Record the plateau velocity, convert to m/s: `v_mps = v_rad_per_sec × wheelRadiusMeters`
4. If measured max differs significantly from `4.80 m/s` (HIGH + NEO V1 theoretical), update `DriveConstants.java` — change the derived field to a measured literal:

```java
// Replace: public static final double maxSpeedMetersPerSec = driveMotorConfig.freeSpeedMps(driveConfig);
public static final double maxSpeedMetersPerSec = <measured_value>;
```

---

## Step 12 — Current Limit / Slip Verification

1. Place robot against a solid wall
2. Drive full throttle into wall for 1–2 seconds
3. In AdvantageScope monitor `/Drive/Module0/DriveCurrentAmps`
4. Note current at onset of wheel slip
5. Set `driveMotorCurrentLimit` slightly below that value (default 50A is conservative for NEO)

---

## Step 13 — PathPlanner Configuration

Verify/update `DriveConstants.java` with real robot weight and MOI before running any PP autos (initial values are filled in but need confirmation):

```java
public static final double robotMassKg = 74.088; // weigh the robot and update
public static final double robotMOI    = 6.883;  // estimate or measure; update if autos arc badly
public static final double wheelCOF    = 1.2;    // typical for carpet, adjust if needed
```

Verify PathPlanner can plan and follow a straight 1m path before using complex autos.

---

## AdvantageScope Quick Reference

| Task                      | Signal path                                      |
|---------------------------|--------------------------------------------------|
| Turn position (per module)| `/Drive/Module{0-3}/TurnPosition`                |
| Drive velocity            | `/Drive/Module{0-3}/DriveVelocityRadPerSec`      |
| Drive current             | `/Drive/Module{0-3}/DriveCurrentAmps`            |
| Robot pose                | `/Drive/Pose`                                    |
| Gyro heading              | `/Drive/Gyro/YawPosition`                        |
| Active alerts             | `/Alerts`                                        |

Log files saved to USB stick on roboRIO. Open with `File → Open Log` in AdvantageScope for post-session replay.

---

## Common Issues

| Symptom | Likely Cause | Fix |
|---|---|---|
| Module spins to wrong angle | Zero offset not set | Redo Step 5 |
| Odometry drifts sideways | `trackWidth`/`wheelBase` wrong | Measure chassis bolt-to-bolt |
| Drive goes backwards | Drive motor inverted | Add `.inverted(true)` to `SparkFlexConfig` in `ModuleIOSpark` |
| Alert: CAN device missing | Wrong CAN ID or disconnected | REV Hardware Client, check bus |
| Oscillating turn | `turnKp` too high | Reduce in `DriveConstants` |
| Robot curves when driving straight | Zero offsets off | Re-run Step 5 with tubing alignment |
