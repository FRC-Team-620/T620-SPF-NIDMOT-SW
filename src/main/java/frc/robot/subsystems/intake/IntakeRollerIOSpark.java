package frc.robot.subsystems.intake;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;

public class IntakeRollerIOSpark implements IntakeRollerIO {
  private final SparkBase leader = new SparkMax(IntakeConstants.leaderCanId, MotorType.kBrushless);
  private final SparkBase follower =
      new SparkMax(IntakeConstants.followerCanId, MotorType.kBrushless);

  public IntakeRollerIOSpark() {
    var leaderConfig = new SparkMaxConfig();
    leaderConfig
        .idleMode(IntakeConstants.idleMode)
        .smartCurrentLimit(IntakeConstants.currentLimitAmps);

    var followerConfig = new SparkMaxConfig();
    followerConfig
        .idleMode(IntakeConstants.idleMode)
        .smartCurrentLimit(IntakeConstants.currentLimitAmps)
        .follow(leader, true);

    leader.configure(leaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    follower.configure(
        followerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  @Override
  public void updateInputs(IntakeRollerIOInputs inputs) {
    inputs.appliedVolts = leader.getAppliedOutput() * leader.getBusVoltage();
    inputs.leaderCurrentAmps = leader.getOutputCurrent();
    inputs.followerCurrentAmps = follower.getOutputCurrent();
  }

  @Override
  public void setSpeed(double speed) {
    leader.set(speed);
  }
}
