package frc.robot.subsystems.intake;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Constants;

public class IntakeRollerIOSpark implements IntakeRollerIO {
  private final SparkBase leader = new SparkMax(IntakeConstants.leaderCanId, MotorType.kBrushless);
  private final SparkBase follower =
      new SparkMax(IntakeConstants.followerCanId, MotorType.kBrushless);

  private final Debouncer jamDebouncer = 
      new Debouncer(Constants.Intake.kJamDetectionSeconds, DebounceType.kRising);
  private double commandedSpeedDutyCycle;
  private boolean clearingJam = false;
  private double jamClearEndTimeSeconds = 0.0;

  public IntakeRollerIOSpark() {
    var leaderConfig = new SparkMaxConfig();
    leaderConfig
        .idleMode(IntakeConstants.idleMode)
        .secondaryCurrentLimit(IntakeConstants.currentLimitAmps + Constants.stallCurrentBuffer)
        .smartCurrentLimit(IntakeConstants.currentLimitAmps);

    var followerConfig = new SparkMaxConfig();
    followerConfig
        .idleMode(IntakeConstants.idleMode)
        .smartCurrentLimit(IntakeConstants.currentLimitAmps)
        .secondaryCurrentLimit(IntakeConstants.currentLimitAmps + Constants.stallCurrentBuffer)
        .follow(leader, true);

    leader.configure(leaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    follower.configure(
        followerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  @Override
  public void updateInputs(IntakeRollerIOInputs inputs) {
    inputs.appliedVolts = leader.getAppliedOutput() * leader.getBusVoltage();
    inputs.velocityRPM = leader.getEncoder().getVelocity();
    inputs.leaderCurrentAmps = leader.getOutputCurrent();
    inputs.followerCurrentAmps = follower.getOutputCurrent();
  }

  @Override
  public void periodic() {
    updateJamDetection();
  }

  @Override
  public void setSpeed(double speed) {
    leader.set(speed);
  }

  private void updateJamDetection() {
    if (clearingJam) {
      if (Timer.getFPGATimestamp() >= jamClearEndTimeSeconds) {
        clearingJam = false;
        setSpeed(commandedSpeedDutyCycle);
      }
      return;
    }

    boolean stalled =
        Math.abs(commandedSpeedDutyCycle) > Constants.Intake.kJamMinCommandDutyCycle
            && leader.getOutputCurrent() > Constants.Intake.kJamCurrentThresholdAmps
            && Math.abs(leader.getEncoder().getVelocity()) < Constants.Intake.kJamVelocityThresholdRPM;

    if (jamDebouncer.calculate(stalled)) {
      clearingJam = true;
      jamClearEndTimeSeconds = Timer.getFPGATimestamp() + Constants.Intake.kJamReverseSeconds;
      double reverseSpeed =
          -Math.copySign(Constants.Intake.kJamReverseDutyCycle, commandedSpeedDutyCycle);
      setSpeed(reverseSpeed);
    }
  }
}
