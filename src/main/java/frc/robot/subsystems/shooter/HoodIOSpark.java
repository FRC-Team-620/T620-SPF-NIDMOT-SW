package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

public class HoodIOSpark implements HoodIO {
  private final SparkBase motor = new SparkMax(ShooterConstants.hoodCanId, MotorType.kBrushless);

  public HoodIOSpark() {
    var config = new SparkMaxConfig();
    config
        .idleMode(ShooterConstants.hoodIdleMode)
        .smartCurrentLimit(ShooterConstants.currentLimitAmps);

    motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  @Override
  public void updateInputs(HoodIOInputs inputs) {
    inputs.encoderPosition = motor.getEncoder().getPosition();
    inputs.appliedVolts = motor.getAppliedOutput() * motor.getBusVoltage();
    inputs.currentAmps = motor.getOutputCurrent();
  }

  @Override
  public void resetEncoder() {
    motor.getEncoder().setPosition(0.0);
  }

  @Override
  public void setIdleMode(IdleMode mode) {
    var config = new SparkMaxConfig();
    config.idleMode(mode);
    motor.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
  }
}
