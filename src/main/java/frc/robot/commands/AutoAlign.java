// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.DriveConstants;
import frc.robot.subsystems.vision.VisionConstants;
import org.littletonrobotics.junction.Logger;

/**
 * Drives the robot to a parking pose directly in front of the closest alliance scoring AprilTag
 * (tags 9/10 for red, 25/26 for blue on the 2026 Rebuilt field), overriding joystick input while
 * held. Target tag is chosen once at the start of each run, based on whichever of the two tags is
 * nearer to the robot's current pose.
 */
public class AutoAlign extends Command {
  private static final int[] redTagIds = {9, 10};
  private static final int[] blueTagIds = {25, 26};

  private static final double standoffMeters = 0.6; // distance to park in front of the tag
  private static final double translationToleranceMeters = 0.03;
  private static final double thetaToleranceRadians = Math.toRadians(2);

  private static final double linearKp = 3.0;
  private static final double linearKd = 0.0;
  private static final double thetaKp = 3.0;
  private static final double thetaKd = 0.0;

  private final Drive drive;

  private final ProfiledPIDController xController;
  private final ProfiledPIDController yController;
  private final ProfiledPIDController thetaController;

  private Pose2d targetPose = Pose2d.kZero;

  public AutoAlign(Drive drive) {
    this.drive = drive;

    xController =
        new ProfiledPIDController(
            linearKp,
            0.0,
            linearKd,
            new TrapezoidProfile.Constraints(
                DriveConstants.maxSpeedMetersPerSec, DriveConstants.maxSpeedMetersPerSec * 2));
    yController =
        new ProfiledPIDController(
            linearKp,
            0.0,
            linearKd,
            new TrapezoidProfile.Constraints(
                DriveConstants.maxSpeedMetersPerSec, DriveConstants.maxSpeedMetersPerSec * 2));
    thetaController =
        new ProfiledPIDController(
            thetaKp,
            0.0,
            thetaKd,
            new TrapezoidProfile.Constraints(
                DriveConstants.maxSpeedMetersPerSec / DriveConstants.driveBaseRadius,
                DriveConstants.maxSpeedMetersPerSec / DriveConstants.driveBaseRadius * 2));
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    xController.setTolerance(translationToleranceMeters);
    yController.setTolerance(translationToleranceMeters);
    thetaController.setTolerance(thetaToleranceRadians);

    addRequirements(drive);
  }

  @Override
  public void initialize() {
    boolean isRed =
        DriverStation.getAlliance().isPresent()
            && DriverStation.getAlliance().get() == Alliance.Red;
    targetPose = closestTargetPose(isRed ? redTagIds : blueTagIds, drive.getPose());

    xController.reset(drive.getPose().getX());
    yController.reset(drive.getPose().getY());
    thetaController.reset(drive.getPose().getRotation().getRadians());
  }

  @Override
  public void execute() {
    double xOut = xController.calculate(drive.getPose().getX(), targetPose.getX());
    double yOut = yController.calculate(drive.getPose().getY(), targetPose.getY());
    double thetaOut =
        thetaController.calculate(
            drive.getPose().getRotation().getRadians(), targetPose.getRotation().getRadians());

    drive.runVelocity(
        ChassisSpeeds.fromFieldRelativeSpeeds(
            new ChassisSpeeds(xOut, yOut, thetaOut), drive.getRotation()));

    Logger.recordOutput("AutoAlign/TargetPose", targetPose);
    Logger.recordOutput("AutoAlign/AtGoal", atGoal());
  }

  @Override
  public void end(boolean interrupted) {
    drive.stop();
  }

  /** Returns true once the robot is within tolerance of the target pose. */
  public boolean atGoal() {
    return xController.atGoal() && yController.atGoal() && thetaController.atGoal();
  }

  /**
   * Returns the parking pose {@code standoffMeters} in front of whichever tag in {@code tagIds} is
   * closest to {@code currentPose}, facing back toward the tag. Falls back to {@code currentPose}
   * if none of the tags are present in the field layout.
   */
  private static Pose2d closestTargetPose(int[] tagIds, Pose2d currentPose) {
    Pose2d closest = null;
    double closestDistance = Double.POSITIVE_INFINITY;
    for (int id : tagIds) {
      var tagPose = VisionConstants.aprilTagLayout.getTagPose(id);
      if (tagPose.isEmpty()) continue;
      Pose2d candidate =
          tagPose
              .get()
              .toPose2d()
              .transformBy(new Transform2d(standoffMeters, 0.0, Rotation2d.k180deg));
      double distance = candidate.getTranslation().getDistance(currentPose.getTranslation());
      if (distance < closestDistance) {
        closestDistance = distance;
        closest = candidate;
      }
    }
    return closest != null ? closest : currentPose;
  }
}
