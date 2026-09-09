// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.subsystems.vision.LimelightHelpers.PoseEstimate;
import java.util.HashSet;
import java.util.Set;

/** IO implementation for real Limelight hardware. */
public class VisionIOLimelight implements VisionIO {
  private static final double disconnectTimeoutSecs = 0.5;

  protected final String name;
  protected final Transform3d robotToCamera;

  private double lastHeartbeat = -1;
  private double lastHeartbeatTime = 0.0;

  /**
   * Creates a new VisionIOLimelight.
   *
   * @param name The configured name of the camera.
   * @param robotToCamera The 3D position of the camera relative to the robot.
   */
  public VisionIOLimelight(String name, Transform3d robotToCamera) {
    this.name = name;
    this.robotToCamera = robotToCamera;

    // Push the camera's mount offset to the Limelight so it can compute field-relative
    // robot poses on-device. Limelight's robot-space convention matches WPILib's
    // (x forward, y left, z up, degrees).
    LimelightHelpers.setCameraPose_RobotSpace(
        name,
        robotToCamera.getX(),
        robotToCamera.getY(),
        robotToCamera.getZ(),
        Units.radiansToDegrees(robotToCamera.getRotation().getX()),
        Units.radiansToDegrees(robotToCamera.getRotation().getY()),
        Units.radiansToDegrees(robotToCamera.getRotation().getZ()));
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    // A Limelight has no NT-level "connected" flag, so infer it from the heartbeat counter,
    // which increments once per frame the coprocessor publishes.
    double heartbeat = LimelightHelpers.getHeartbeat(name);
    if (heartbeat != lastHeartbeat) {
      lastHeartbeat = heartbeat;
      lastHeartbeatTime = Timer.getFPGATimestamp();
    }
    inputs.connected = Timer.getFPGATimestamp() - lastHeartbeatTime < disconnectTimeoutSecs;

    // Update latest target observation
    if (LimelightHelpers.getTV(name)) {
      inputs.latestTargetObservation =
          new TargetObservation(
              Rotation2d.fromDegrees(LimelightHelpers.getTX(name)),
              Rotation2d.fromDegrees(LimelightHelpers.getTY(name)));
    } else {
      inputs.latestTargetObservation = new TargetObservation(Rotation2d.kZero, Rotation2d.kZero);
    }

    // Read the latest pose estimate (MegaTag1 — no gyro/orientation fusion)
    PoseEstimate poseEstimate = LimelightHelpers.getBotPoseEstimate_wpiBlue(name);

    // Add pose observation
    if (poseEstimate.tagCount > 0) {
      inputs.poseObservations =
          new PoseObservation[] {
            new PoseObservation(
                poseEstimate.timestampSeconds, // Timestamp
                new Pose3d(poseEstimate.pose), // 3D pose estimate
                poseEstimate.tagCount == 1 && poseEstimate.rawFiducials.length == 1
                    ? poseEstimate.rawFiducials[0].ambiguity
                    : 0.0, // Ambiguity
                poseEstimate.tagCount, // Tag count
                poseEstimate.avgTagDist, // Average tag distance
                PoseObservationType.LIMELIGHT) // Observation type
          };
    } else {
      inputs.poseObservations = new PoseObservation[0];
    }

    // Save tag IDs to inputs object
    Set<Integer> tagIds = new HashSet<>();
    for (var fiducial : poseEstimate.rawFiducials) {
      tagIds.add(fiducial.id);
    }
    inputs.tagIds = tagIds.stream().mapToInt(Integer::intValue).toArray();
  }
}
