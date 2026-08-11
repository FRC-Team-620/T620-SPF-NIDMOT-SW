package frc.robot.subsystems.intake;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class IntakePivot extends SubsystemBase {
  private final IntakePivotIO io;
  private final IntakePivotIOInputsAutoLogged inputs = new IntakePivotIOInputsAutoLogged();

  private final PIDController pid = new PIDController(0.05, 0.0, 0.0);
  private boolean positionControlEnabled = false;
  private double targetPosition = 0.0;

  public IntakePivot(IntakePivotIO io) {
    this.io = io;
    SmartDashboard.putData("IntakePivot/pidController", pid);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("IntakePivot", inputs);

    if (positionControlEnabled) {
      double output = pid.calculate(inputs.encoderPosition, targetPosition);
      io.setDutyCycle(
          MathUtil.clamp(output, -IntakeConstants.pivotMaxOutput, IntakeConstants.pivotMaxOutput));
    }
  }

  public void setTargetPosition(double target) {
    positionControlEnabled = true;
    targetPosition = target;
  }

  public void stopPositionControl() {
    positionControlEnabled = false;
    io.setDutyCycle(0.0);
  }

  public double getPosition() {
    return inputs.encoderPosition;
  }

  public void resetEncoder() {
    io.resetEncoder();
  }

  public void setBrakeMode(boolean brake) {
    io.setIdleMode(brake ? IdleMode.kBrake : IdleMode.kCoast);
  }
}
