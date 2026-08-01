package frc.robot.subsystems.indexer;

import org.littletonrobotics.junction.AutoLog;

public interface IndexerIO {
  @AutoLog
  class IndexerIOInputs {
    public double leftAppliedVolts = 0.0;
    public double leftLeaderCurrentAmps = 0.0;
    public double leftFollowerCurrentAmps = 0.0;
    public double rightAppliedVolts = 0.0;
    public double rightCurrentAmps = 0.0;
  }

  default void updateInputs(IndexerIOInputs inputs) {}

  default void setDutyCycle(double speed) {}
}
