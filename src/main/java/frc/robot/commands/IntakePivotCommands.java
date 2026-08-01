package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.intake.IntakeConstants;
import frc.robot.subsystems.intake.IntakePivot;

public class IntakePivotCommands {
  private IntakePivotCommands() {}

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
