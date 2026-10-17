package frc.robot.subsystems.indexer;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;

public class IndexerIOSpark implements IndexerIO {
  private final SparkBase leftLeader =
      new SparkFlex(IndexerConstants.leftLeaderCanId, MotorType.kBrushless);
  private final SparkBase leftFollower =
      new SparkFlex(IndexerConstants.leftFollowerCanId, MotorType.kBrushless);
  private final SparkBase rightMotor =
      new SparkFlex(IndexerConstants.rightMotorCanId, MotorType.kBrushless);

  public IndexerIOSpark() {
    var leftLeaderConfig = new SparkFlexConfig();
    leftLeaderConfig
        .inverted(IndexerConstants.invertLeftBank)
        .idleMode(IndexerConstants.idleMode)
        .smartCurrentLimit(IndexerConstants.currentLimitAmps);

    var leftFollowerConfig = new SparkFlexConfig();
    leftFollowerConfig
        .idleMode(IndexerConstants.idleMode)
        .smartCurrentLimit(IndexerConstants.currentLimitAmps)
        .follow(leftLeader, false);

    var rightMotorConfig = new SparkFlexConfig();
    rightMotorConfig
        .inverted(IndexerConstants.invertRightBank)
        .idleMode(IndexerConstants.idleMode)
        .smartCurrentLimit(IndexerConstants.currentLimitAmps);

    leftLeader.configure(
        leftLeaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    leftFollower.configure(
        leftFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightMotor.configure(
        rightMotorConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  @Override
  public void updateInputs(IndexerIOInputs inputs) {
    inputs.leftAppliedVolts = leftLeader.getAppliedOutput() * leftLeader.getBusVoltage();
    inputs.leftLeaderCurrentAmps = leftLeader.getOutputCurrent();
    inputs.leftFollowerCurrentAmps = leftFollower.getOutputCurrent();
    inputs.rightAppliedVolts = rightMotor.getAppliedOutput() * rightMotor.getBusVoltage();
    inputs.rightCurrentAmps = rightMotor.getOutputCurrent();
  }

  @Override
  public void setDutyCycle(double speed) {
    leftLeader.set(speed);
    rightMotor.set(speed);
  }
}
