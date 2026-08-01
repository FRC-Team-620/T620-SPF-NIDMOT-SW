package frc.robot.subsystems.intake;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

public class IntakePivotIOSpark implements IntakePivotIO {
  private final SparkBase motor = new SparkMax(IntakeConstants.pivotCanId, MotorType.kBrushless);

  public IntakePivotIOSpark() {
    var config = new SparkMaxConfig();
    config.idleMode(IntakeConstants.idleMode).smartCurrentLimit(IntakeConstants.currentLimitAmps);

    motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  @Override
  public void updateInputs(IntakePivotIOInputs inputs) {
    inputs.encoderPosition = motor.getEncoder().getPosition();
    inputs.appliedVolts = motor.getAppliedOutput() * motor.getBusVoltage();
    inputs.currentAmps = motor.getOutputCurrent();
  }

  @Override
  public void setDutyCycle(double speed) {
    motor.set(speed);
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
