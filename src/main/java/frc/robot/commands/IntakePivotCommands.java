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
}
