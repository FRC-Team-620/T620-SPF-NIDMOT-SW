package frc.robot.subsystems.indexer;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.Debouncer.DebounceType;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import org.littletonrobotics.junction.Logger;

public class Indexer extends SubsystemBase {
  private final IndexerIO io;
  private final IndexerIOInputsAutoLogged inputs = new IndexerIOInputsAutoLogged();

  private final Debouncer jamDebouncer =
      new Debouncer(Constants.Intake.kJamDetectionSeconds, DebounceType.kRising);
  private double commandedSpeedDutyCycle;
  private boolean clearingJam = false;
  private double jamClearEndTimeSeconds = 0.0;

  public Indexer(IndexerIO io) {
    this.io = io;
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Indexer", inputs);
  }

  public void setDutyCycle(double speed) {
    io.setDutyCycle(speed);
  }

  public void stop() {
    io.setDutyCycle(0.0);
  }

  public void simulateFeed() {
    io.simulateFeed();
  }

  public boolean isFeeding() {
    return inputs.leftAppliedVolts > 1.0;
  }

  private void updateJamDetection() {
    if (clearingJam) {
      if (Timer.getFPGATimestamp() >= jamClearEndTimeSeconds) {
        clearingJam = false;
        setDutyCycle(commandedSpeedDutyCycle);
      }
      return;
    }

    boolean stalled =
        Math.abs(commandedSpeedDutyCycle) > IndexerConstants.kJamMinCommandDutyCycle
            // && inputs.leaderCurrentAmps > IndexerConstants.kJamCurrentThresholdAmps
            && Math.abs(inputs.leftVelocityRPM) < IndexerConstants.kJamVelocityThresholdRPM;

    if (jamDebouncer.calculate(stalled)) {
      clearingJam = true;
      jamClearEndTimeSeconds = Timer.getFPGATimestamp() + IndexerConstants.kJamReverseSeconds;
      double reverseSpeed =
          -Math.copySign(IndexerConstants.kJamReverseDutyCycle, commandedSpeedDutyCycle);
      io.setDutyCycle(reverseSpeed);
    }
  }
}
