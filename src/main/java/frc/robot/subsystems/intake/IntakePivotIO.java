package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.AutoLog;

public interface IntakePivotIO {
  @AutoLog
  class IntakePivotIOInputs {
    public double encoderPosition = 0.0;
    public double appliedVolts = 0.0;
    public double currentAmps = 0.0;
  }

  default void updateInputs(IntakePivotIOInputs inputs) {}

  default void resetEncoder() {}

  default void setDutyCycle(double speed) {}

  default void setIdleMode(com.revrobotics.spark.config.SparkBaseConfig.IdleMode mode) {}
}
