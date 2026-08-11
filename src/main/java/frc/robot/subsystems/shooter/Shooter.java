package frc.robot.subsystems.shooter;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class Shooter extends SubsystemBase {
  private final ShooterIO io;
  private final ShooterIOInputsAutoLogged inputs = new ShooterIOInputsAutoLogged();

  private final PIDController pid =
      new PIDController(
          ShooterConstants.shooterKP, ShooterConstants.shooterKI, ShooterConstants.shooterKD);
  private boolean velocityControlEnabled = false;
  private double targetVelocityRPM = 0.0;

  public Shooter(ShooterIO io) {
    this.io = io;
    SmartDashboard.putData("Shooter/pidController", pid);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Shooter", inputs);

    if (velocityControlEnabled) {
      double ffDuty =
          (ShooterConstants.shooterKS + ShooterConstants.shooterKV * targetVelocityRPM) / 12.0;
      double pidDuty = pid.calculate(inputs.leftVelocityRPM, targetVelocityRPM);
      io.setDutyCycle(MathUtil.clamp(ffDuty + pidDuty, 0.0, 1.0));
    }
  }

  public void setTargetVelocity(double rpm) {
    velocityControlEnabled = true;
    targetVelocityRPM = rpm;
  }

  public void stopVelocityControl() {
    velocityControlEnabled = false;
    io.setDutyCycle(0.0);
  }

  public double getVelocityRPM() {
    return inputs.leftVelocityRPM;
  }

  public boolean isAtTargetVelocity() {
    return velocityControlEnabled
        && Math.abs(inputs.leftVelocityRPM - targetVelocityRPM)
            < ShooterConstants.shooterVelocityToleranceRPM;
  }

  public void setVoltage(double volts) {
    io.setVoltage(volts);
  }

  public void setDutyCycle(double speed) {
    io.setDutyCycle(speed);
  }

  public void stop() {
    io.setDutyCycle(0.0);
  }
}
