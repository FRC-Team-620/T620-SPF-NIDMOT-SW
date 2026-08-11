package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.shooter.Hood;
import frc.robot.subsystems.shooter.ShooterConstants;

public class HoodCommands {
  private HoodCommands() {}

  public static Command stow(Hood hood) {
    return Commands.runEnd(
        () -> hood.setTargetPosition(ShooterConstants.hoodStowPosition),
        hood::stopPositionControl,
        hood);
  }

  public static Command extend(Hood hood) {
    return Commands.runEnd(
        () -> hood.setTargetPosition(ShooterConstants.hoodExtendPosition),
        hood::stopPositionControl,
        hood);
  }

  public static Command adjustPosition(Hood hood, double delta) {
    return Commands.runOnce(() -> hood.adjustTargetPosition(delta), hood);
  }
}
