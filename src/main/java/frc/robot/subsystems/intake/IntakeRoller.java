package frc.robot.subsystems.intake;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import org.littletonrobotics.junction.Logger;

public class IntakeRoller extends SubsystemBase {
  private final IntakeRollerIO io;
  private final IntakeRollerIOInputsAutoLogged inputs = new IntakeRollerIOInputsAutoLogged();

  private final Debouncer jamDebouncer =
      new Debouncer(Constants.Intake.kJamDetectionSeconds, DebounceType.kRising);
  private double commandedSpeedDutyCycle;
  private boolean clearingJam = false;
  private double jamClearEndTimeSeconds = 0.0;

  public IntakeRoller(IntakeRollerIO io) {
    this.io = io;
  }

  @Override
  public void periodic() {
    updateJamDetection();
    io.updateInputs(inputs);
    Logger.processInputs("IntakeRoller", inputs);
  }

  public void setSpeed(double speed) {
    commandedSpeedDutyCycle = speed;
    if (!clearingJam) {
      io.setSpeed(speed);
    }
  }

  public void simulateIntake() {
    io.simulateIntake();
  }

  public void setSimJammed(boolean jammed) {
    io.setSimJammed(jammed);
  }

  public boolean isIntaking() {
    return inputs.appliedVolts > 1.0;
  }

  public boolean isExtaking() {
    return inputs.appliedVolts < -1.0;
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
            && inputs.leaderCurrentAmps > Constants.Intake.kJamCurrentThresholdAmps
            && Math.abs(inputs.velocityRPM) < Constants.Intake.kJamVelocityThresholdRPM;

    if (jamDebouncer.calculate(stalled)) {
      clearingJam = true;
      jamClearEndTimeSeconds = Timer.getFPGATimestamp() + Constants.Intake.kJamReverseSeconds;
      double reverseSpeed =
          -Math.copySign(Constants.Intake.kJamReverseDutyCycle, commandedSpeedDutyCycle);
      io.setSpeed(reverseSpeed);
    }
  }
}
