package frc.robot.subsystems.shooter;

import org.littletonrobotics.junction.AutoLog;

public interface ShooterIO {
  @AutoLog
  class ShooterIOInputs {
    public double leftAppliedVolts = 0.0;
    public double leftLeaderCurrentAmps = 0.0;
    public double leftFollowerCurrentAmps = 0.0;
    public double rightAppliedVolts = 0.0;
    public double rightLeaderCurrentAmps = 0.0;
    public double rightFollowerCurrentAmps = 0.0;
  }

  default void updateInputs(ShooterIOInputs inputs) {}

  default void setDutyCycle(double speed) {}
}
