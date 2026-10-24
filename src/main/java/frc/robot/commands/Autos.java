package frc.robot.commands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Constants;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.intake.IntakePivot;
import frc.robot.subsystems.shooter.Hood;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;

public class Autos {
  private Autos() {}

  public static Command centerFrontShoot(
      Shooter shooter, Hood hood, IntakePivot intakePivot, Indexer indexer, Drive drive) {

    // double shooterRPM = 1250; // testing RPM
    return Commands.sequence(
        // This is only getting called once. If you enable/disable, it is not being called.
        Commands.runOnce(
            () -> {
              Alliance ally = DriverStation.getAlliance().get();
              Pose2d fPose =
                  ally == Alliance.Red ? Constants.Poses.hubRED : Constants.Poses.hubBLUE;
              drive.setPose(fPose);
            },
            drive),
        Commands.runOnce(() -> hood.setTargetPosition(ShooterConstants.hoodAutoPosition), hood),
        Commands.deadline(
            Commands.waitUntil(shooter::isAtTargetVelocity).withTimeout(3.0),
            ShooterCommands.runAtVelocity(shooter, ShooterConstants.shooterAutoRPM),
            IntakePivotCommands.extend(intakePivot)),
        Commands.parallel(
                ShooterCommands.runAtVelocity(shooter, ShooterConstants.shooterNearRPM),
                IndexerCommands.runAtDutyCycle(indexer, 0.85))
            .withTimeout(5.0));
  }
}
