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
  public static Command runAtDutyCycle(Shooter shooter, double dutyCycle) {
    return Commands.runEnd(() -> shooter.setDutyCycle(dutyCycle), shooter::stop, shooter);
  }

  public static Command runAtVelocity(Shooter shooter, double rpm) {
    return Commands.runEnd(
        () -> shooter.setTargetVelocity(rpm), shooter::stopVelocityControl, shooter);
  }

  public static Command stopShooter(Shooter shooter) {
    return Commands.run(shooter::stopVelocityControl, shooter);
  }

  public static Command shooterVoltageTuning(Shooter shooter) {
    SmartDashboard.putBoolean("Shooter/VoltageEnable", false);
    SmartDashboard.putNumber("Shooter/Voltage", 0.0);

    return Commands.runEnd(
        () -> {
          if (SmartDashboard.getBoolean("Shooter/VoltageEnable", false)) {
            shooter.setVoltage(SmartDashboard.getNumber("Shooter/Voltage", 0.0));
          } else {
            shooter.stop();
          }
        },
        shooter::stop,
        shooter);
  }

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
