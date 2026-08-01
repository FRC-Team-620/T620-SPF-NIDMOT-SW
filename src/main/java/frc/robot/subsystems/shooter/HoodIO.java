package frc.robot.subsystems.shooter;

import org.littletonrobotics.junction.AutoLog;

public interface HoodIO {
  @AutoLog
  class HoodIOInputs {
    public double encoderPosition = 0.0;
    public double appliedVolts = 0.0;
    public double currentAmps = 0.0;
  }

  default void updateInputs(HoodIOInputs inputs) {}

  default void resetEncoder() {}

  default void setIdleMode(com.revrobotics.spark.config.SparkBaseConfig.IdleMode mode) {}
}
