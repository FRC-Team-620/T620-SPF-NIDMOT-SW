# Drive Subsystem Bringup — T620-SPF-NIDMOT-SW

Hardware: REV MAXSwerve | SPARK MAX | NEO (drive) | NEO 550 (turn)
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

> **Note:** The template defaults to NavX (`GyroIONavX`). This project uses a **Pigeon 2** — switch to `GyroIOPigeon2` in `RobotContainer` and confirm `pigeonCanId = 62`.

In REV Hardware Client, also verify:
- Firmware is current on all SPARK MAX units
- No duplicate CAN IDs exist on the bus
- Encoder type per controller: **Through Bore Encoder** on turn, **Hall Effect** on drive

---

## Step 2 — Initial Constants Check

Before first deploy, confirm these values in `DriveConstants.java`:

```java
// Active swerve config — change here only to swap pinion config
public static final MaxSwerveConfig driveConfig = MaxSwerveConfig.HIGH;
// HIGH = 4.71:1 drive ratio, 4.80 m/s free speed, 14T pinion (REV-21-3005-P23)

public static final double trackWidth  = Units.inchesToMeters(26.5); // measure your actual chassis
public static final double wheelBase   = Units.inchesToMeters(26.5); // measure your actual chassis
public static final double wheelRadiusMeters = Units.inchesToMeters(1.5); // MAXSwerve 3in wheel
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

- If it decreases → flip `turnInverted` in `DriveConstants.java`
- If no change → encoder not reading (wiring or CAN issue)

---

## Step 5 — Zero Offset Calibration

**Goal:** Record absolute encoder offsets so each module knows its forward-facing position.

1. Physically align all four modules so the drive wheels point **straight forward** (use a long piece of aluminum tubing laid against the wheels for accuracy)
2. In AdvantageScope, read the current `TurnPosition` values for all four modules
3. Update `DriveConstants.java` with the recorded values:

```java
public static final Rotation2d frontLeftZeroRotation  = new Rotation2d(<recorded_value>);
public static final Rotation2d frontRightZeroRotation = new Rotation2d(<recorded_value>);
public static final Rotation2d backLeftZeroRotation   = new Rotation2d(<recorded_value>);
public static final Rotation2d backRightZeroRotation  = new Rotation2d(<recorded_value>);
```

4. Redeploy and verify: when the robot powers on, all modules should snap to forward-facing.

---

## Step 6 — Drive Direction Verification

**Goal:** Confirm positive drive command → forward wheel motion.

In AdvantageScope, subscribe to:
```
/Drive/Module0/DrivePositionRad
```

Push the robot **forward** by hand. The value must **increase**. Repeat per module.

- If decreasing → the template handles drive inversion via `driveInverted` in `ModuleIOSpark.java`

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
2. In AdvantageScope, select auto routine: **"Drive Simple FF Characterization"**
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
2. Select auto routine: **"Drive Wheel Radius Characterization"**
3. Enable in **Auto** — robot spins in place for at least one full rotation
4. Console prints corrected `wheelRadiusMeters`
5. Update `DriveConstants.java`:

```java
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

1. Select auto routine: **"Drive FF Characterization"** or drive at full stick in Teleop
2. In AdvantageScope plot `/Drive/Module0/DriveVelocityRadPerSec`
3. Record the plateau velocity, convert to m/s: `v_mps = v_rad_per_sec × wheelRadiusMeters`
4. If measured max differs significantly from `4.80 m/s` (HIGH config theoretical), update:

```java
// maxSpeedMetersPerSec is sourced from driveConfig — only override if measured differs
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

Update `DriveConstants.java` with real robot weight and MOI before running any PP autos:

```java
public static final double robotMassKg = <actual_kg>;   // weigh the robot
public static final double robotMOI    = <actual_moi>;  // estimate or measure
public static final double wheelCOF    = 1.2;           // typical for carpet, adjust if needed
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
| Drive goes backwards | Drive motor inverted | Check `driveInverted` in `ModuleIOSpark` |
| Alert: CAN device missing | Wrong CAN ID or disconnected | REV Hardware Client, check bus |
| Oscillating turn | `turnKp` too high | Reduce in `DriveConstants` |
| Robot curves when driving straight | Zero offsets off | Re-run Step 5 with tubing alignment |
