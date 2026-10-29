package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.intake.IntakePivot;
import frc.robot.subsystems.intake.IntakeRoller;

public class IntakeCommands {
  private IntakeCommands() {}

  /** Lowers the slapdown and runs the intake roller inward until interrupted, then stops it. */
  public static Command intake(IntakeRoller roller, IntakePivot pivot, double speed) {
    return Commands.parallel(
        IntakePivotCommands.extend(pivot),
        Commands.runEnd(() -> roller.setSpeed(speed), () -> roller.setSpeed(0.0), roller));
  }

  /**
   * Lowers the slapdown and runs the intake roller outward and the indexer in reverse until
   * interrupted, then stops both.
   */
  public static Command extake(
      IntakeRoller roller,
      IntakePivot pivot,
      Indexer indexer,
      double rollerSpeed,
      double indexerSpeed) {
    return Commands.parallel(
        IntakePivotCommands.extend(pivot),
        Commands.runEnd(() -> roller.setSpeed(-rollerSpeed), () -> roller.setSpeed(0.0), roller),
        IndexerCommands.runAtDutyCycle(indexer, -indexerSpeed));
  }
}
