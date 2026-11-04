// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.DriveConstants;
import frc.robot.subsystems.vision.VisionConstants;
import org.littletonrobotics.junction.Logger;
import org.photonvision.PhotonCamera;

/**
 * Drives the robot to a pose {@code standoffMeters} in front of whichever AprilTag the camera
 * currently sees, recomputed from the live PhotonVision detection every cycle rather than the
 * static field layout. This lets the robot track a hand-held tag for bench testing; overrides
 * joystick input while held, and stops once the tag is lost.
 */
public class FollowTag extends Command {
  private static final double standoffMeters = Units.inchesToMeters(12);
  private static final double translationToleranceMeters = 0.03;
  private static final double thetaToleranceRadians = Math.toRadians(2);

  private static final double linearKp = 3.0;
  private static final double linearKd = 0.0;
  private static final double thetaKp = 3.0;
  private static final double thetaKd = 0.0;

  private final Drive drive;
  private final PhotonCamera camera;

  private final PIDController xController;
  private final PIDController yController;
  private final PIDController thetaController;

  private boolean targetVisible = false;
  private Pose2d goalPoseRobotRelative = Pose2d.kZero;

  public FollowTag(Drive drive) {
    this.drive = drive;
    this.camera = new PhotonCamera(VisionConstants.camera0Name);

    xController = new PIDController(linearKp, 0.0, linearKd);
    yController = new PIDController(linearKp, 0.0, linearKd);
    thetaController = new PIDController(thetaKp, 0.0, thetaKd);
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    xController.setTolerance(translationToleranceMeters);
    yController.setTolerance(translationToleranceMeters);
    thetaController.setTolerance(thetaToleranceRadians);

    addRequirements(drive);
  }

  @Override
  public void initialize() {
    targetVisible = false;
    xController.reset();
    yController.reset();
    thetaController.reset();
  }

  @Override
  public void execute() {
    // Recompute the goal from the latest camera frames, if any arrived this cycle. If no new
    // frames arrived, keep driving toward the last known goal rather than stopping every cycle
    // the camera doesn't happen to report a fresh result.
    for (var result : camera.getAllUnreadResults()) {
      if (result.hasTargets()) {
        Transform3d cameraToTag = result.getBestTarget().bestCameraToTarget;
        Transform3d robotToTag = VisionConstants.robotToCamera0.plus(cameraToTag);
        Pose2d tagPoseRobotRelative =
            new Pose2d(
                robotToTag.getTranslation().toTranslation2d(),
                robotToTag.getRotation().toRotation2d());
        goalPoseRobotRelative =
            tagPoseRobotRelative.transformBy(
                new Transform2d(standoffMeters, 0.0, Rotation2d.k180deg));
        targetVisible = true;
      } else {
        targetVisible = false;
      }
    }

    ChassisSpeeds speeds = new ChassisSpeeds();
    if (targetVisible) {
      double xOut = xController.calculate(0.0, -goalPoseRobotRelative.getX());
      double yOut = yController.calculate(0.0, -goalPoseRobotRelative.getY());
      double thetaOut =
          thetaController.calculate(0.0, goalPoseRobotRelative.getRotation().getRadians());

      speeds =
          new ChassisSpeeds(
              MathUtil.clamp(
                  xOut, -DriveConstants.maxSpeedMetersPerSec, DriveConstants.maxSpeedMetersPerSec),
              MathUtil.clamp(
                  yOut, -DriveConstants.maxSpeedMetersPerSec, DriveConstants.maxSpeedMetersPerSec),
              MathUtil.clamp(
                  thetaOut,
                  -DriveConstants.maxSpeedMetersPerSec / DriveConstants.driveBaseRadius,
                  DriveConstants.maxSpeedMetersPerSec / DriveConstants.driveBaseRadius));
    }

    // speeds are already expressed in the robot's own frame (computed relative to the live tag
    // detection), so they're sent directly without a field-relative conversion.
    drive.runVelocity(speeds);

    Logger.recordOutput("AutoAlign/TargetVisible", targetVisible);
    Logger.recordOutput("AutoAlign/GoalPoseRobotRelative", goalPoseRobotRelative);
    Logger.recordOutput("AutoAlign/AtGoal", atGoal());
  }

  @Override
  public void end(boolean interrupted) {
    drive.stop();
  }

  /** Returns true once the robot is within tolerance of the target pose. */
  public boolean atGoal() {
    return targetVisible
        && xController.atSetpoint()
        && yController.atSetpoint()
        && thetaController.atSetpoint();
  }
}
