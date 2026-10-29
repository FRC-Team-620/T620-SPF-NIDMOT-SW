package frc.robot.subsystems.shooter;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class Hood extends SubsystemBase {
  private final HoodIO io;
  private final HoodIOInputsAutoLogged inputs = new HoodIOInputsAutoLogged();

  private final PIDController pid = new PIDController(0.1, 0.0, 0.0);
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

    Logger.recordOutput("Hood/TargetPosition", getSetpointPosition());
    Logger.recordOutput("Hood/MeasuredAngleDeg", Units.radiansToDegrees(toAngleRad(getPosition())));
    Logger.recordOutput(
        "Hood/SetpointAngleDeg", Units.radiansToDegrees(toAngleRad(getSetpointPosition())));
  }

  public void setTargetPosition(double target) {
    positionControlEnabled = true;
    targetPosition = target;
  }

  public void adjustTargetPosition(double delta) {
    positionControlEnabled = true;
    targetPosition =
        MathUtil.clamp(
            targetPosition + delta,
            ShooterConstants.hoodExtendPosition,
            ShooterConstants.hoodStowPosition);
  }

  public void stopPositionControl() {
    positionControlEnabled = false;
    io.setDutyCycle(0.0);
  }

  public double getPosition() {
    return inputs.encoderPosition;
  }

  public double getSetpointPosition() {
    return positionControlEnabled ? targetPosition : inputs.encoderPosition;
  }

  /** Robot-relative pose of the hood from the encoder, for AdvantageScope. */
  public Pose3d getMeasuredPose() {
    return toPose(getPosition());
  }

  /** Robot-relative pose of the hood at its setpoint, for AdvantageScope. */
  public Pose3d getSetpointPose() {
    return toPose(getSetpointPosition());
  }

  /** Angle from the CAD export pose, in radians, for an encoder position in motor rotations. */
  private static double toAngleRad(double encoderPosition) {
    return ShooterConstants.hoodVisualizationDirection
        * Units.rotationsToRadians(
            (encoderPosition - ShooterConstants.hoodCadPosition) / ShooterConstants.hoodGearing);
  }

  private static Pose3d toPose(double encoderPosition) {
    return new Pose3d(
        ShooterConstants.hoodOrigin, new Rotation3d(0.0, toAngleRad(encoderPosition), 0.0));
  }

  public void resetEncoder() {
    io.resetEncoder();
  }

  public void setBrakeMode(boolean brake) {
    io.setIdleMode(brake ? IdleMode.kBrake : IdleMode.kCoast);
  }
}
