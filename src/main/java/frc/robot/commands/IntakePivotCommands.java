package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.intake.IntakeConstants;
import frc.robot.subsystems.intake.IntakePivot;

public class IntakePivotCommands {
  private IntakePivotCommands() {}

  /**
   * Drives the pivot slowly toward the hard stop until a current spike indicates stall, then zeros
   * the encoder. If the timeout elapses before stall is detected, the motor stops without zeroing
   * so position control doesn't use a bad reference.
   */
  public static Command home(IntakePivot pivot) {
    return Commands.run(() -> pivot.setRawDutyCycle(IntakeConstants.pivotHomingDutyCycle), pivot)
        .until(() -> pivot.getCurrentAmps() > IntakeConstants.pivotHomingCurrentThreshold)
        .withTimeout(IntakeConstants.pivotHomingTimeoutSeconds)
        .finallyDo(
            (interrupted) -> {
              pivot.stopPositionControl();
              if (!interrupted) pivot.resetEncoder();
            });
  }

  public static Command stow(IntakePivot pivot) {
    return Commands.runEnd(
        () -> pivot.setTargetPosition(IntakeConstants.pivotStowPosition),
        pivot::stopPositionControl,
        pivot);
  }

  public static Command extend(IntakePivot pivot) {
    return Commands.runEnd(
        () -> pivot.setTargetPosition(IntakeConstants.pivotExtendPosition),
        pivot::stopPositionControl,
        pivot);
  }

  /**
   * Stows at a reduced max output so cargo already loaded against the hopper isn't jolted. Used by
   * the fire sequence instead of {@link #stow}.
   */
  public static Command stowSlow(IntakePivot pivot) {
    return Commands.runEnd(
        () -> pivot.setTargetPositionSlow(IntakeConstants.pivotStowPosition),
        pivot::stopPositionControl,
        pivot);
  }

  /**
   * Velocity-limited stow via a trapezoid motion profile, for smoother motion than {@link
   * #stowSlow}'s reduced-output approach. Not bound to anything yet — verify {@link #stowSlow} on
   * the robot first, then swap {@code ShootingCommands.fire} over to this once tuned.
   */
  public static Command stowProfiled(IntakePivot pivot) {
    return Commands.runEnd(
        () -> pivot.setTargetPositionProfiled(IntakeConstants.pivotStowPosition),
        pivot::stopPositionControl,
        pivot);
  }
}
