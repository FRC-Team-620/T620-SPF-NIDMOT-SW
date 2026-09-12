# Vision Subsystem

AprilTag-based pose estimation via [PhotonVision](https://docs.photonvision.org/en/latest/docs/programming/photonlib/index.html), following [AdvantageKit's vision template](https://docs.advantagekit.org/getting-started/template-projects/vision-template/) (adapted from the `v26.0.2` template — no Limelight variant, no MegaTag2, since Spitfire has neither). Pose observations feed `Drive.addVisionMeasurement()` directly; no other subsystem needs to know vision exists.

## Expected hardware

| Component | Expected | Status |
|---|---|---|
| Coprocessor | Orange Pi 5 running the [PhotonVision image](https://docs.photonvision.org/en/latest/docs/installation/index.html) | Imaged and configured |
| Cameras | 2× Arducam OV9281 (USB, global shutter) | Configured — AprilTag pipelines active, named `camera_0` / `camera_1` |
| Power | 5V (Pi) or 12V (regulated) off the robot's PDH, **not** off a Rev/CAN device — vision hardware has no CAN connection at all | — |
| Network | Coprocessor + cameras on the robot's radio network (Ethernet), each camera named to match `VisionConstants.camera0Name`/`camera1Name` | photonvision-front static IP: `10.6.20.9` |
| Mount | 2 locations TBD once the coprocessor/camera enclosure is fabricated | — |

**No CAN involvement.** PhotonVision talks to the robot over NetworkTables/Ethernet, so it doesn't interact with the REV motor controllers or the Pigeon 2 in any way — hardware selection here is independent of the drivetrain.

## What's a placeholder right now

- `VisionConstants.camera0Name` / `camera1Name` — set to `"camera_0"` / `"camera_1"`; confirmed to match the coprocessor configuration.
- `VisionConstants.robotToCamera0` / `robotToCamera1` — dummy `Transform3d` mount offsets. These get measured and replaced once cameras are physically mounted (see Step 5 in `VISION_BRINGUP.md`).
