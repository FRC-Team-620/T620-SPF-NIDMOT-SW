package frc.robot.commands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.intake.IntakePivot;
import frc.robot.subsystems.shooter.Hood;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;

public class Autos {
  private Autos() {}

  public static Command centerFrontShoot(
      Shooter shooter, Hood hood, IntakePivot intakePivot, Indexer indexer) {
    double autoHoodPosition = ShooterConstants.hoodNearPosition;
    double shooterRPM = ShooterConstants.shooterNearRPM;
    // double shooterRPM = 1250; // testing RPM
    return Commands.sequence(
        Commands.runOnce(() -> hood.setTargetPosition(autoHoodPosition), hood),
        Commands.deadline(
            Commands.waitUntil(shooter::isAtTargetVelocity).withTimeout(3.0),
            ShooterCommands.runAtVelocity(shooter, shooterRPM),
            IntakePivotCommands.extend(intakePivot)),
        Commands.parallel(
                ShooterCommands.runAtVelocity(shooter, shooterRPM),
                IndexerCommands.runAtDutyCycle(indexer, 0.85))
            .withTimeout(5.0));
  }

  // -------------------------------------------------------------
  // HOLY SHIT REALLY BIG TO-DO: make sure these are not flipped
  // -------------------------------------------------------------

  public static Command blueShooterForward(Drive drive) {
    return Commands.runOnce(
            () -> drive.setPose(new Pose2d(drive.getPose().getTranslation(), Rotation2d.k180deg)),
            drive)
        .ignoringDisable(true);
  }

  public static Command blueIntakeForward(Drive drive) {
    return Commands.runOnce(
            () -> drive.setPose(new Pose2d(drive.getPose().getTranslation(), Rotation2d.kZero)),
            drive)
        .ignoringDisable(true);
  }

  public static Command redShooterForward(Drive drive) {
    return Commands.runOnce(
            () -> drive.setPose(new Pose2d(drive.getPose().getTranslation(), Rotation2d.kZero)),
            drive)
        .ignoringDisable(true);
  }

  public static Command redIntakeForward(Drive drive) {
    return Commands.runOnce(
            () -> drive.setPose(new Pose2d(drive.getPose().getTranslation(), Rotation2d.k180deg)),
            drive)
        .ignoringDisable(true);
  }
}
