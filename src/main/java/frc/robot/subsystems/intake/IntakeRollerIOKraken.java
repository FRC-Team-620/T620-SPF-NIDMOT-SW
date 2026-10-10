package frc.robot.subsystems.intake;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.ParentDevice;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;
import frc.robot.Constants;

public class IntakeRollerIOKraken implements IntakeRollerIO {
  private final TalonFX leader = new TalonFX(IntakeConstants.leaderCanId);
  private final TalonFX follower = new TalonFX(IntakeConstants.followerCanId);

  private final StatusSignal<Voltage> leaderAppliedVolts;
  private final StatusSignal<Current> leaderCurrent;
  private final StatusSignal<Current> followerCurrent;

  public IntakeRollerIOKraken() {
    var leaderConfig = new TalonFXConfiguration();
    leaderConfig.MotorOutput.NeutralMode = IntakeConstants.krakenNeutralMode;
    leaderConfig.CurrentLimits.StatorCurrentLimit = IntakeConstants.currentLimitAmps;
    leaderConfig.CurrentLimits.StatorCurrentLimitEnable = true;
    leaderConfig.CurrentLimits.SupplyCurrentLimit = IntakeConstants.currentLimitAmps + Constants.stallCurrentBuffer;
    leaderConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
    leader.getConfigurator().apply(leaderConfig);

    var followerConfig = new TalonFXConfiguration();
    followerConfig.MotorOutput.NeutralMode = IntakeConstants.krakenNeutralMode;
    followerConfig.CurrentLimits.StatorCurrentLimit = IntakeConstants.currentLimitAmps;
    followerConfig.CurrentLimits.StatorCurrentLimitEnable = true;
    followerConfig.CurrentLimits.SupplyCurrentLimit = IntakeConstants.currentLimitAmps + Constants.stallCurrentBuffer;
    followerConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
    follower.getConfigurator().apply(followerConfig);
    follower.setControl(new Follower(IntakeConstants.leaderCanId, MotorAlignmentValue.Opposed));

    leaderAppliedVolts = leader.getMotorVoltage();
    leaderCurrent = leader.getStatorCurrent();
    followerCurrent = follower.getStatorCurrent();

    BaseStatusSignal.setUpdateFrequencyForAll(50.0, leaderAppliedVolts, leaderCurrent, followerCurrent);
    ParentDevice.optimizeBusUtilizationForAll(leader, follower);
  }

  @Override
  public void updateInputs(IntakeRollerIOInputs inputs) {
    BaseStatusSignal.refreshAll(leaderAppliedVolts, leaderCurrent, followerCurrent);

    inputs.appliedVolts = leaderAppliedVolts.getValueAsDouble();
    inputs.leaderCurrentAmps = leaderCurrent.getValueAsDouble();
    inputs.followerCurrentAmps = followerCurrent.getValueAsDouble();
  }

  @Override
  public void setSpeed(double speed) {
    leader.set(speed);
  }
}
