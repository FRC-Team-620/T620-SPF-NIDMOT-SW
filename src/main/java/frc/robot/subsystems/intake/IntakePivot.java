package frc.robot.subsystems.intake;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class IntakePivot extends SubsystemBase {
  private final IntakePivotIO io;
  private final IntakePivotIOInputsAutoLogged inputs = new IntakePivotIOInputsAutoLogged();

  private final PIDController pid = new PIDController(0.2, 0.0, 0.0);
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

    Logger.recordOutput("IntakePivot/TargetPosition", getSetpointPosition());
    Logger.recordOutput(
        "IntakePivot/MeasuredAngleDeg", Units.radiansToDegrees(toAngleRad(getPosition())));
    Logger.recordOutput(
        "IntakePivot/SetpointAngleDeg", Units.radiansToDegrees(toAngleRad(getSetpointPosition())));
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

  /** Target position while under position control, otherwise the measured position. */
  public double getSetpointPosition() {
    return positionControlEnabled ? targetPosition : inputs.encoderPosition;
  }

  /** Robot-relative pose of the intake pivot from the encoder, for AdvantageScope. */
  public Pose3d getMeasuredPose() {
    return toPose(getPosition());
  }

  /** Robot-relative pose of the intake pivot at its setpoint, for AdvantageScope. */
  public Pose3d getSetpointPose() {
    return toPose(getSetpointPosition());
  }

  /** Angle from the CAD export pose, in radians, for an encoder position in motor rotations. */
  private static double toAngleRad(double encoderPosition) {
    return IntakeConstants.pivotVisualizationDirection
        * Units.rotationsToRadians(
            (encoderPosition - IntakeConstants.pivotCadPosition) / IntakeConstants.pivotGearing);
  }

  private static Pose3d toPose(double encoderPosition) {
    return new Pose3d(
        IntakeConstants.pivotOrigin, new Rotation3d(0.0, toAngleRad(encoderPosition), 0.0));
  }

  public void resetEncoder() {
    io.resetEncoder();
  }

  public void setBrakeMode(boolean brake) {
    io.setIdleMode(brake ? IdleMode.kBrake : IdleMode.kCoast);
  }
}
