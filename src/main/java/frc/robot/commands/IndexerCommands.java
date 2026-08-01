package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.indexer.Indexer;

public class IndexerCommands {
  private IndexerCommands() {}

  /**
   * Reads "Indexer/Enable" (boolean) and "Indexer/DutyCycle" (number) from SmartDashboard each
   * loop. Set Enable to true and adjust DutyCycle [-1, 1] while the robot is enabled to spin the
   * indexer without deploying code.
   */
  public static Command indexerTuning(Indexer indexer) {
    SmartDashboard.putBoolean("Indexer/Enable", false);
    SmartDashboard.putNumber("Indexer/DutyCycle", 0.0);

    return Commands.runEnd(
        () -> {
          if (SmartDashboard.getBoolean("Indexer/Enable", false)) {
            indexer.setDutyCycle(SmartDashboard.getNumber("Indexer/DutyCycle", 0.0));
          } else {
            indexer.stop();
          }
        },
        indexer::stop,
        indexer);
  }
}
