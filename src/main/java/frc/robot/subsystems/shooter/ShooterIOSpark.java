package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;

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
        .smartCurrentLimit(ShooterConstants.currentLimitAmps);
    leftLeaderConfig
        .encoder
        .quadratureMeasurementPeriod(ShooterConstants.velocityMeasurementPeriodMs)
        .quadratureAverageDepth(ShooterConstants.velocityAverageDepth);
    leftLeaderConfig
        .closedLoop
        .pid(ShooterConstants.shooterKP, ShooterConstants.shooterKI, ShooterConstants.shooterKD)
        .outputRange(0.0, 1.0) // never drive the flywheel backwards to slow it
        .feedForward
        .kS(ShooterConstants.shooterKS)
        .kV(ShooterConstants.shooterKV);

    var leftFollowerConfig = new SparkFlexConfig();
    leftFollowerConfig
        .idleMode(ShooterConstants.idleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps)
        .follow(leftLeader, false);

    var rightLeaderConfig = new SparkFlexConfig();
    rightLeaderConfig
        .inverted(ShooterConstants.invertRightBank)
        .idleMode(ShooterConstants.idleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps);
    rightLeaderConfig
        .encoder
        .quadratureMeasurementPeriod(ShooterConstants.velocityMeasurementPeriodMs)
        .quadratureAverageDepth(ShooterConstants.velocityAverageDepth);
    rightLeaderConfig
        .closedLoop
        .pid(ShooterConstants.shooterKP, ShooterConstants.shooterKI, ShooterConstants.shooterKD)
        .outputRange(0.0, 1.0) // never drive the flywheel backwards to slow it
        .feedForward
        .kS(ShooterConstants.shooterKS)
        .kV(ShooterConstants.shooterKV);

    var rightFollowerConfig = new SparkFlexConfig();
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
    inputs.leftVelocityRPM = leftLeader.getEncoder().getVelocity();
    inputs.leftLeaderCurrentAmps = leftLeader.getOutputCurrent();
    inputs.leftFollowerCurrentAmps = leftFollower.getOutputCurrent();
    inputs.rightAppliedVolts = rightLeader.getAppliedOutput() * rightLeader.getBusVoltage();
    inputs.rightVelocityRPM = rightLeader.getEncoder().getVelocity();
    inputs.rightLeaderCurrentAmps = rightLeader.getOutputCurrent();
    inputs.rightFollowerCurrentAmps = rightFollower.getOutputCurrent();
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

  @Override
  public void setVelocity(double rpm) {
    leftLeader.getClosedLoopController().setSetpoint(rpm, ControlType.kVelocity);
    rightLeader.getClosedLoopController().setSetpoint(rpm, ControlType.kVelocity);
  }

  @Override
  public void setPID(double kP, double kI, double kD) {
    var config = new SparkFlexConfig();
    config.closedLoop.pid(kP, kI, kD);
    leftLeader.configure(
        config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
    rightLeader.configure(
        config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
  }
}
