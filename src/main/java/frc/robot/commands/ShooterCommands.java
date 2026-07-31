package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.shooter.Shooter;

public class ShooterCommands {
  private ShooterCommands() {}

  /**
   * Reads "Shooter/Enable" (boolean) and "Shooter/DutyCycle" (number) from SmartDashboard each
   * loop. Set Enable to true and adjust DutyCycle [-1, 1] while the robot is enabled to spin the
   * shooter without deploying code.
   */
  public static Command shooterTuning(Shooter shooter) {
    SmartDashboard.putBoolean("Shooter/Enable", false);
    SmartDashboard.putNumber("Shooter/DutyCycle", 0.0);

    return Commands.runEnd(
        () -> {
          if (SmartDashboard.getBoolean("Shooter/Enable", false)) {
            shooter.setDutyCycle(SmartDashboard.getNumber("Shooter/DutyCycle", 0.0));
          } else {
            shooter.stop();
          }
        },
        shooter::stop,
        shooter);
  }
}
