# Vision Subsystem

AprilTag-based pose estimation via [PhotonVision](https://docs.photonvision.org/en/latest/docs/programming/photonlib/index.html), following [AdvantageKit's vision template](https://docs.advantagekit.org/getting-started/template-projects/vision-template/) (adapted from the `v26.0.2` template — no Limelight variant, no MegaTag2, since Spitfire has neither). Pose observations feed `Drive.addVisionMeasurement()` directly; no other subsystem needs to know vision exists.

## Expected hardware

| Component | Expected | Status |
|---|---|---|
| Coprocessor | Orange Pi 5 / Raspberry Pi 5–class SBC running the [PhotonVision image](https://docs.photonvision.org/en/latest/docs/installation/index.html) | Not yet acquired/imaged |
| Cameras | 2× USB (global shutter preferred for AprilTags at speed) | Not yet chosen |
| Power | 5V (Pi) or 12V (regulated) off the robot's PDH, **not** off a Rev/CAN device — vision hardware has no CAN connection at all | — |
| Network | Coprocessor + cameras on the robot's radio network (Ethernet), each camera named to match `VisionConstants.camera0Name`/`camera1Name` | — |
| Mount | 2 locations TBD once the coprocessor/camera enclosure is fabricated | — |

**No CAN involvement.** PhotonVision talks to the robot over NetworkTables/Ethernet, so it doesn't interact with the REV motor controllers or the Pigeon 2 in any way — hardware selection here is independent of the drivetrain.

## What's a placeholder right now

- `VisionConstants.camera0Name` / `camera1Name` — arbitrary names (`"camera_0"`, `"camera_1"`); must match whatever names get configured on the coprocessor once it's set up.
- `VisionConstants.robotToCamera0` / `robotToCamera1` — dummy `Transform3d` mount offsets. These get measured and replaced once cameras are physically mounted (same idea as `DRIVE_BRINGUP.md`'s zero-offset calibration — the value doesn't exist until the hardware does).

A `VISION_BRINGUP.md` (mirroring `../drive/DRIVE_BRINGUP.md`) will follow once real hardware is in hand — that doc covers imaging the coprocessor, camera calibration, and measuring mount transforms. This README just tracks what hardware the code currently assumes.
