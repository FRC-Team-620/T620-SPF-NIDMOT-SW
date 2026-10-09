package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.intake.IntakeConstants;
import frc.robot.subsystems.intake.IntakePivot;
import frc.robot.subsystems.intake.IntakeRoller;
import frc.robot.subsystems.shooter.Hood;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;

/** Driver-facing aim/fire commands that combine the shooter, hood, indexer, and intake. */
public class ShootingCommands {
  private ShootingCommands() {}

  private static Command aim(Shooter shooter, Hood hood, double rpm, double hoodPosition) {
    return Commands.parallel(
        ShooterCommands.runAtVelocity(shooter, rpm),
        Commands.runEnd(
            () -> hood.setTargetPosition(hoodPosition),
            () -> hood.setTargetPosition(ShooterConstants.hoodStowPosition),
            hood));
  }

  /**
   * Spins the shooter and sets the hood for a near shot while held; restows the hood on release.
   */
  public static Command aimNear(Shooter shooter, Hood hood) {
    return aim(shooter, hood, ShooterConstants.shooterNearRPM, ShooterConstants.hoodNearPosition);
  }

  /** Spins the shooter and sets the hood for a far shot while held; restows the hood on release. */
  public static Command aimFar(Shooter shooter, Hood hood) {
    return aim(shooter, hood, ShooterConstants.shooterFarRPM, ShooterConstants.hoodFarPosition);
  }

  /**
   * Stows the intake pivot (slowly, so cargo already loaded against the hopper isn't jolted)
   * immediately on press, then once the shooter reaches its aimed velocity, feeds cargo through the
   * indexer and intake roller. Releasing stops the feed and re-extends the pivot at normal speed.
   * Requires an {@link #aimNear}/{@link #aimFar} to be held concurrently — the feed gate only opens
   * once the shooter is at whatever velocity is currently commanded.
   */
  public static Command fire(
      Shooter shooter, Indexer indexer, IntakePivot intakePivot, IntakeRoller intakeRoller) {
    return Commands.parallel(
        Commands.runEnd(
            () -> intakePivot.setTargetPositionSlow(IntakeConstants.pivotStowPosition),
            () -> intakePivot.setTargetPosition(IntakeConstants.pivotExtendPosition),
            intakePivot),
        Commands.sequence(
            Commands.waitUntil(shooter::isAtTargetVelocity),
            Commands.parallel(
                IndexerCommands.runAtDutyCycle(indexer, 0.85),
                Commands.runEnd(
                    () -> intakeRoller.setSpeed(IntakeConstants.rollerFireSpeed),
                    () -> intakeRoller.setSpeed(0.0),
                    intakeRoller))));
  }
}
