package frc.robot.subsystems.shooter;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class Hood extends SubsystemBase {
  private final HoodIO io;
  private final HoodIOInputsAutoLogged inputs = new HoodIOInputsAutoLogged();

  private final PIDController pid = new PIDController(0.12, 0.0, 0.0);
  private boolean positionControlEnabled = false;
  private double targetPosition = 0.0;

  public Hood(HoodIO io) {
    this.io = io;
    SmartDashboard.putData("Hood/pidController", pid);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Hood", inputs);

    if (positionControlEnabled) {
      double output = pid.calculate(inputs.encoderPosition, targetPosition);
      io.setDutyCycle(
          MathUtil.clamp(output, -ShooterConstants.hoodMaxOutput, ShooterConstants.hoodMaxOutput));
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
