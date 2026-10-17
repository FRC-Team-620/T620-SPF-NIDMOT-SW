package frc.robot.subsystems.intake;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class IntakePivot extends SubsystemBase {
  private final IntakePivotIO io;
  private final IntakePivotIOInputsAutoLogged inputs = new IntakePivotIOInputsAutoLogged();

  private final PIDController pid = new PIDController(0.2, 0.0, 0.0);
  private final ProfiledPIDController profiledPid =
      new ProfiledPIDController(0.2, 0.0, 0.0, IntakeConstants.pivotFireStowConstraints);
  private boolean positionControlEnabled = false;
  private boolean profiledControlEnabled = false;
  private double targetPosition = 0.0;
  private double maxOutput = IntakeConstants.pivotMaxOutput;

  public IntakePivot(IntakePivotIO io) {
    this.io = io;
    SmartDashboard.putData("IntakePivot/pidController", pid);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("IntakePivot", inputs);

    if (profiledControlEnabled) {
      if (atSetpoint(profiledPid.getGoal().position)) {
        io.setDutyCycle(0.0);
      } else {
        double output = profiledPid.calculate(inputs.encoderPosition);
        io.setDutyCycle(MathUtil.clamp(output, -maxOutput, maxOutput));
      }
    } else if (positionControlEnabled) {
      if (atSetpoint(targetPosition)) {
        io.setDutyCycle(0.0);
      } else {
        double output = pid.calculate(inputs.encoderPosition, targetPosition);
        io.setDutyCycle(MathUtil.clamp(output, -maxOutput, maxOutput));
      }
    }
  }

  public void setTargetPosition(double target) {
    positionControlEnabled = true;
    profiledControlEnabled = false;
    maxOutput = IntakeConstants.pivotMaxOutput;
    targetPosition = target;
  }

  /**
   * Same closed-loop position control as {@link #setTargetPosition}, but clamped to a lower output
   * ceiling so the pivot moves gently — used to stow during the fire sequence without jolting cargo
   * already loaded against the hopper.
   */
  public void setTargetPositionSlow(double target) {
    positionControlEnabled = true;
    profiledControlEnabled = false;
    maxOutput = IntakeConstants.pivotFireStowMaxOutput;
    targetPosition = target;
  }

  /**
   * Drives to the target via a velocity-limited trapezoid profile instead of a reduced output
   * ceiling. Not used by any command yet; see {@link IntakeConstants#pivotFireStowConstraints}.
   */
  public void setTargetPositionProfiled(double target) {
    profiledControlEnabled = true;
    positionControlEnabled = false;
    maxOutput = IntakeConstants.pivotMaxOutput;
    profiledPid.reset(inputs.encoderPosition);
    profiledPid.setGoal(target);
  }

  public void stopPositionControl() {
    positionControlEnabled = false;
    profiledControlEnabled = false;
    io.setDutyCycle(0.0);
  }

  private boolean atSetpoint(double target) {
    return Math.abs(inputs.encoderPosition - target) <= IntakeConstants.pivotPositionTolerance;
  }

  public double getPosition() {
    return inputs.encoderPosition;
  }

  public void resetEncoder() {
    io.resetEncoder();
  }

  public double getCurrentAmps() {
    return inputs.currentAmps;
  }

  public void setRawDutyCycle(double dutyCycle) {
    positionControlEnabled = false;
    io.setDutyCycle(dutyCycle);
  }

  public void setBrakeMode(boolean brake) {
    io.setIdleMode(brake ? IdleMode.kBrake : IdleMode.kCoast);
  }
}
