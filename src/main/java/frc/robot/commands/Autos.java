package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.intake.IntakePivot;
import frc.robot.subsystems.shooter.Hood;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;

public class Autos {
  private Autos() {}

  public static Command centerFrontShoot(
      Shooter shooter, Hood hood, IntakePivot intakePivot, Indexer indexer) {
    double autoHoodPosition =
        ShooterConstants.hoodStowPosition - 2 * ShooterConstants.hoodAdjustDelta;
    return Commands.sequence(
        Commands.runOnce(() -> hood.setTargetPosition(autoHoodPosition), hood),
        Commands.deadline(
            Commands.waitUntil(shooter::isAtTargetVelocity).withTimeout(5.0),
            ShooterCommands.runAtVelocity(shooter, ShooterConstants.shooterPresetRPM),
            IntakePivotCommands.extend(intakePivot)),
        Commands.parallel(
                ShooterCommands.runAtVelocity(shooter, ShooterConstants.shooterPresetRPM),
                IntakePivotCommands.extend(intakePivot),
                IndexerCommands.runAtDutyCycle(indexer, 0.85))
            .withTimeout(2.0));
  }
}
