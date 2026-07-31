package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;

public class ShooterIOSpark implements ShooterIO {
  private final SparkBase leftLeader =
      new SparkMax(ShooterConstants.leftLeaderCanId, MotorType.kBrushless);
  private final SparkBase leftFollower =
      new SparkMax(ShooterConstants.leftFollowerCanId, MotorType.kBrushless);
  private final SparkBase rightLeader =
      new SparkMax(ShooterConstants.rightLeaderCanId, MotorType.kBrushless);
  private final SparkBase rightFollower =
      new SparkMax(ShooterConstants.rightFollowerCanId, MotorType.kBrushless);

  public ShooterIOSpark() {
    var leftLeaderConfig = new SparkMaxConfig();
    leftLeaderConfig
        .inverted(ShooterConstants.invertLeftBank)
        .idleMode(ShooterConstants.idleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps);

    var leftFollowerConfig = new SparkMaxConfig();
    leftFollowerConfig
        .idleMode(ShooterConstants.idleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps)
        .follow(leftLeader, false);

    var rightLeaderConfig = new SparkMaxConfig();
    rightLeaderConfig
        .inverted(ShooterConstants.invertRightBank)
        .idleMode(ShooterConstants.idleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps);

    var rightFollowerConfig = new SparkMaxConfig();
    rightFollowerConfig
        .idleMode(ShooterConstants.idleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps)
        .follow(rightLeader, false);

    leftLeader.configure(
        leftLeaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    leftFollower.configure(
        leftFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightLeader.configure(
        rightLeaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightFollower.configure(
        rightFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  @Override
  public void updateInputs(ShooterIOInputs inputs) {
    inputs.leftAppliedVolts = leftLeader.getAppliedOutput() * leftLeader.getBusVoltage();
    inputs.leftLeaderCurrentAmps = leftLeader.getOutputCurrent();
    inputs.leftFollowerCurrentAmps = leftFollower.getOutputCurrent();
    inputs.rightAppliedVolts = rightLeader.getAppliedOutput() * rightLeader.getBusVoltage();
    inputs.rightLeaderCurrentAmps = rightLeader.getOutputCurrent();
    inputs.rightFollowerCurrentAmps = rightFollower.getOutputCurrent();
  }

  @Override
  public void setDutyCycle(double speed) {
    leftLeader.set(speed);
    rightLeader.set(speed);
  }
}
