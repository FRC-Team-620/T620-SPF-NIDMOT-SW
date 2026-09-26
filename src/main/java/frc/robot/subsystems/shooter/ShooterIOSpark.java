package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;

import frc.robot.Constants;

public class ShooterIOSpark implements ShooterIO {
  private final SparkBase leftLeader =
      new SparkFlex(ShooterConstants.leftLeaderCanId, MotorType.kBrushless);
  private final SparkBase leftFollower =
      new SparkFlex(ShooterConstants.leftFollowerCanId, MotorType.kBrushless);
  private final SparkBase rightLeader =
      new SparkFlex(ShooterConstants.rightLeaderCanId, MotorType.kBrushless);
  private final SparkBase rightFollower =
      new SparkFlex(ShooterConstants.rightFollowerCanId, MotorType.kBrushless);

  public ShooterIOSpark() {
    var leftLeaderConfig = new SparkFlexConfig();
    leftLeaderConfig
        .inverted(ShooterConstants.invertLeftBank)
        .idleMode(ShooterConstants.idleMode)
        .secondaryCurrentLimit(ShooterConstants.currentLimitAmps + Constants.stallCurrentBuffer)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps);
    leftLeaderConfig
        .encoder
        .quadratureMeasurementPeriod(ShooterConstants.velocityMeasurementPeriodMs)
        .quadratureAverageDepth(ShooterConstants.velocityAverageDepth);

    var leftFollowerConfig = new SparkFlexConfig();
    leftFollowerConfig
        .idleMode(ShooterConstants.idleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps)
        .secondaryCurrentLimit(ShooterConstants.currentLimitAmps + Constants.stallCurrentBuffer)
        .follow(leftLeader, false);

    var rightLeaderConfig = new SparkFlexConfig();
    rightLeaderConfig
        .inverted(ShooterConstants.invertRightBank)
        .idleMode(ShooterConstants.idleMode)
        .secondaryCurrentLimit(ShooterConstants.currentLimitAmps + Constants.stallCurrentBuffer)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps);
    rightLeaderConfig
        .encoder
        .quadratureMeasurementPeriod(ShooterConstants.velocityMeasurementPeriodMs)
        .quadratureAverageDepth(ShooterConstants.velocityAverageDepth);

    var rightFollowerConfig = new SparkFlexConfig();
    rightFollowerConfig
        .idleMode(ShooterConstants.idleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps)
        .secondaryCurrentLimit(ShooterConstants.currentLimitAmps + Constants.stallCurrentBuffer)
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
    inputs.leftVelocityRPM = leftLeader.getEncoder().getVelocity();
    inputs.leftLeaderCurrentAmps = leftLeader.getOutputCurrent();
    inputs.leftFollowerCurrentAmps = leftFollower.getOutputCurrent();
    inputs.leftLeaderTempCelsius = leftLeader.getMotorTemperature();
    inputs.leftFollowerTempCelsius = leftFollower.getMotorTemperature();
    inputs.rightAppliedVolts = rightLeader.getAppliedOutput() * rightLeader.getBusVoltage();
    inputs.rightVelocityRPM = rightLeader.getEncoder().getVelocity();
    inputs.rightLeaderCurrentAmps = rightLeader.getOutputCurrent();
    inputs.rightFollowerCurrentAmps = rightFollower.getOutputCurrent();
    inputs.rightLeaderTempCelsius = rightLeader.getMotorTemperature();
    inputs.rightFollowerTempCelsius = rightFollower.getMotorTemperature();
  }

  @Override
  public void setDutyCycle(double speed) {
    leftLeader.set(speed);
    rightLeader.set(speed);
  }

  @Override
  public void setVoltage(double volts) {
    leftLeader.setVoltage(volts);
    rightLeader.setVoltage(volts);
  }
}
