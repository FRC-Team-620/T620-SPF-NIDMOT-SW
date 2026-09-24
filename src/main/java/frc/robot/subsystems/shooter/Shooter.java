package frc.robot.subsystems.shooter;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class Shooter extends SubsystemBase {
  private final ShooterIO io;
  private final ShooterIOInputsAutoLogged inputs = new ShooterIOInputsAutoLogged();

  // Not used to calculate output: the velocity loop runs on the Spark Flexes. This only holds
  // the gains so they stay tunable from the dashboard; changes are pushed to the motors.
  private final PIDController gains =
      new PIDController(
          ShooterConstants.shooterKP, ShooterConstants.shooterKI, ShooterConstants.shooterKD);
  private double appliedKP = gains.getP();
  private double appliedKI = gains.getI();
  private double appliedKD = gains.getD();

  private boolean velocityControlEnabled = false;
  private double targetVelocityRPM = 0.0;

  public Shooter(ShooterIO io) {
    this.io = io;
    SmartDashboard.putData("Shooter/pidController", gains);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Shooter", inputs);

    if (gains.getP() != appliedKP || gains.getI() != appliedKI || gains.getD() != appliedKD) {
      appliedKP = gains.getP();
      appliedKI = gains.getI();
      appliedKD = gains.getD();
      io.setPID(appliedKP, appliedKI, appliedKD);
    }

    if (velocityControlEnabled) {
      io.setVelocity(targetVelocityRPM);
    }
    Logger.recordOutput(
        "Shooter/TargetVelocityRPM", velocityControlEnabled ? targetVelocityRPM : 0.0);
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

  /** True when both banks are within tolerance, since each bank now closes its own loop. */
  public boolean isAtTargetVelocity() {
    return velocityControlEnabled
        && Math.abs(inputs.leftVelocityRPM - targetVelocityRPM)
            < ShooterConstants.shooterVelocityToleranceRPM
        && Math.abs(inputs.rightVelocityRPM - targetVelocityRPM)
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
