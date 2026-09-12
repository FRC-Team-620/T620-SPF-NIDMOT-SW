# Vision Subsystem Bringup — T620-SPF-NIDMOT-SW

Hardware: Orange Pi 5 coprocessor | 2× Arducam OV9281 (USB, global shutter) | Ethernet to robot radio
Template: AdvantageKit v26.0.2 Vision Template
Ref: https://docs.advantagekit.org/getting-started/template-projects/vision-template/

---

## Prerequisites

- [ ] Orange Pi 5 imaged with the [PhotonVision image](https://docs.photonvision.org/en/latest/docs/installation/index.html) and powered from PDH (5V or regulated 12V — **not** off a REV/CAN device)
- [ ] Both Arducam OV9281 cameras plugged into the Orange Pi via USB
- [ ] Orange Pi connected to the robot's radio via Ethernet and assigned static IP `10.6.20.9`
- [ ] roboRIO on network (robot radio up)
- [ ] AdvantageScope installed on driver station laptop
- [ ] FAT32-formatted USB stick inserted in roboRIO (required for AdvantageKit log files)

---

## Step 1 — PhotonVision Web UI Verification

Open a browser and navigate to `http://10.6.20.9:5800`.

Confirm:
- Both cameras appear in the **Cameras** dropdown — they should be named `camera_0` and `camera_1`
- Each camera has an **AprilTag** pipeline active (not a reflective tape pipeline)
- The live feed is visible and not blank/frozen
- PhotonVision version matches what the vendordep expects (check `vendordeps/photonlib.json`)

> **Camera naming is critical.** The software connects by name, not by USB port order. If the names differ from `camera_0` / `camera_1`, either rename them in the PhotonVision UI or update `VisionConstants.java` to match.

---

## Step 2 — Camera Name Verification

Confirm `VisionConstants.java` matches the names set in the PhotonVision UI:

```java
public static String camera0Name = "camera_0";
public static String camera1Name = "camera_1";
```

If the coprocessor shows different names, update these constants before deploying.

---

## Step 3 — First Deploy & AdvantageScope Connection

1. Deploy code: `WPILib: Deploy Robot Code` in VS Code
2. Open **AdvantageScope**
3. Connect: `File → Connect to Robot` → enter roboRIO IP (`10.6.20.2`) or use USB
4. Check the **Alerts** panel for any camera disconnection warnings:
   - `"Vision camera 0 is disconnected."` → camera_0 not seen on NetworkTables
   - `"Vision camera 1 is disconnected."` → camera_1 not seen on NetworkTables
5. No alerts = cameras are publishing to NetworkTables and the code is reading them

If a disconnection alert fires, verify:
- The camera name in `VisionConstants` matches the PhotonVision UI exactly (case-sensitive)
- The Orange Pi is reachable from the roboRIO (`ping 10.6.20.9` from DS laptop)
- PhotonVision is running (check the web UI)

---

## Step 4 — Intrinsic Camera Calibration

**Goal:** Give each camera a lens model so PhotonVision can report accurate 3D poses.

Uncalibrated cameras will produce noisy or completely wrong pose estimates. Do this before measuring mount transforms.

In the PhotonVision web UI:

1. Select a camera from the dropdown
2. Navigate to **Calibration** tab
3. Print the calibration target (chessboard or AprilTag board) at the specified size — **do not scale to fit**
4. Hold the target at various distances and angles while clicking **Take Snapshot** — aim for 25–50 images covering the full frame
5. Click **Calibrate** — wait for the process to complete
6. Check the reported reprojection error: **< 1.0 px** is acceptable; **< 0.5 px** is good
7. Repeat for the second camera

Ref: https://docs.photonvision.org/en/latest/docs/calibration/calibration.html

---

## Step 5 — Mount Transform Measurement

**Goal:** Tell the code exactly where each camera sits on the robot so pose estimates are in robot-frame coordinates.

The transforms are currently zero placeholders in `VisionConstants.java`:

```java
public static Transform3d robotToCamera0 = new Transform3d();
public static Transform3d robotToCamera1 = new Transform3d();
```

Once cameras are physically mounted, measure each `Transform3d` from the robot origin (center of the drivebase at floor level) to the camera optical center, using WPILib coordinate conventions (X = forward, Y = left, Z = up):

```java
// Example — replace with measured values
public static Transform3d robotToCamera0 =
    new Transform3d(
        0.30,   // meters forward from robot center
        0.15,   // meters left of robot center
        0.55,   // meters above floor
        new Rotation3d(0.0, Math.toRadians(-20.0), 0.0)); // pitch down 20°

public static Transform3d robotToCamera1 =
    new Transform3d(
        -0.30,  // meters behind robot center
        -0.15,
        0.55,
        new Rotation3d(0.0, Math.toRadians(-20.0), Math.PI)); // facing rearward
```

Measure with calipers or CAD export. A wrong transform produces pose estimates that are offset or rotated relative to the true robot position — visible in AdvantageScope as `RobotPosesAccepted` that don't match `/Drive/Pose`.

---

## Step 6 — Pose Estimation Sanity Check

**Goal:** Confirm accepted pose estimates are plausible and agree with odometry.

Place the robot at a known field location in front of visible AprilTags. In AdvantageScope, open a **3D Field** view and add these signals:

```
/Vision/Camera0/TagPoses           (Pose3d[]) — tags seen by camera 0
/Vision/Camera0/RobotPosesAccepted (Pose3d[]) — poses that passed all filters
/Vision/Camera0/RobotPosesRejected (Pose3d[]) — poses that failed a filter
/Vision/Summary/RobotPosesAccepted (Pose3d[]) — all cameras combined
/Drive/Pose                        (Pose2d)   — odometry estimate
```

Expected behavior:
- `TagPoses` shows field-frame tag locations matching the WPILib field map
- `RobotPosesAccepted` appears near `/Drive/Pose` within ~10–20 cm when the robot is stationary
- `RobotPosesRejected` is empty (or contains only single-tag, far-away estimates)

If all poses land in `RobotPosesRejected`, check `maxAmbiguity` and `maxZError` in `VisionConstants.java` — the defaults are intentionally strict (`0.3` and `0.75 m`).

If accepted poses are consistently offset from odometry by a fixed amount, the mount transform in Step 5 is wrong — re-measure and redeploy.

---

## Step 7 — Pose Fusion Verification

**Goal:** Confirm vision measurements are actually updating the drivetrain pose estimate.

In AdvantageScope, plot:
```
/Drive/Pose    (Pose2d)
```

1. Enable the robot in **Teleop**
2. Drive to face several AprilTags and hold still
3. Move the robot slightly off its known position by hand (or note odometry drift after a few laps)
4. Watch `/Drive/Pose` — it should snap back toward the true field position when tags are in view

If the pose never corrects, check `RobotContainer` wiring: `Vision` must be constructed with `drive::addVisionMeasurement` as the consumer.

---

## Step 8 — Filtering Threshold Tuning

Default thresholds in `VisionConstants.java`:

```java
public static double maxAmbiguity = 0.3;   // reject single-tag estimates above this
public static double maxZError    = 0.75;  // reject estimates where Z is > 0.75 m off floor
```

If too many valid poses are rejected (all landing in `RobotPosesRejected`):
- Increase `maxAmbiguity` toward `0.4`–`0.5` cautiously — higher values let in noisier single-tag estimates
- Confirm camera calibration is complete (Step 4) — uncalibrated cameras produce high ambiguity universally

If odometry is jumping from bad vision measurements:
- Decrease `maxAmbiguity` (stricter)
- Confirm the mount transform (Step 5) is accurate

---

## Step 9 — Standard Deviation Tuning

The code scales pose measurement uncertainty by distance and tag count automatically. The baseline values in `VisionConstants.java`:

```java
public static double linearStdDevBaseline  = 0.02; // meters — at 1 m, 1 tag
public static double angularStdDevBaseline = 0.06; // radians — at 1 m, 1 tag
```

These are the AdvantageKit template defaults and are reasonable starting points. If vision is over-trusted (causing jerky pose jumps at range), increase the baselines. If vision is under-trusted (not correcting odometry drift), decrease them.

Per-camera trust can be adjusted independently without touching the baselines:

```java
public static double[] cameraStdDevFactors = new double[] {
  1.0, // Camera 0
  1.0, // Camera 1 — increase (e.g. 2.0) to trust camera 1 half as much
};
```

---

## AdvantageScope Quick Reference

| Task                              | Signal path                                      |
|-----------------------------------|--------------------------------------------------|
| Camera 0 connected                | `/Vision/Camera0/Connected`                      |
| Camera 1 connected                | `/Vision/Camera1/Connected`                      |
| Tags seen by camera 0             | `/Vision/Camera0/TagPoses`                       |
| All pose observations (camera 0)  | `/Vision/Camera0/RobotPoses`                     |
| Accepted observations (camera 0)  | `/Vision/Camera0/RobotPosesAccepted`             |
| Rejected observations (camera 0)  | `/Vision/Camera0/RobotPosesRejected`             |
| Combined accepted (all cameras)   | `/Vision/Summary/RobotPosesAccepted`             |
| Combined rejected (all cameras)   | `/Vision/Summary/RobotPosesRejected`             |
| Drivetrain pose (for comparison)  | `/Drive/Pose`                                    |
| Active alerts                     | `/Alerts`                                        |

Log files saved to USB stick on roboRIO. Open with `File → Open Log` in AdvantageScope for post-session replay.

---

## Common Issues

| Symptom | Likely Cause | Fix |
|---|---|---|
| Alert: "Vision camera N is disconnected" | Name mismatch or Orange Pi unreachable | Match `cameraXName` in `VisionConstants` to PhotonVision UI; ping `10.6.20.9` |
| All poses in `RobotPosesRejected` | High ambiguity or bad calibration | Complete intrinsic calibration (Step 4); loosen `maxAmbiguity` temporarily to diagnose |
| Accepted poses offset from odometry | Wrong mount transform | Re-measure `robotToCamera` (Step 5) |
| Pose jumps wildly at range | Single-tag ambiguity too high | Lower `maxAmbiguity`; rely on multi-tag results only |
| Vision not correcting odometry drift | Consumer not wired up | Verify `Vision` is constructed with `drive::addVisionMeasurement` in `RobotContainer` |
| Reprojection error > 1.0 px | Poor calibration coverage | Recalibrate with more images at varied distances/angles |
| PhotonVision UI not loading | Orange Pi not on network | Check Ethernet cable; confirm static IP `10.6.20.9`; check PDH channel power |
