package frc.robot.subsystems.intake;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

public class IntakePivotIOSim implements IntakePivotIO {
  private static final DCMotor gearbox = DCMotor.getNEO(1);
  private static final double moi = 0.05;
  private static final double lowerLimit = 0.0;
  private static final double upperLimit = 25.0;

  private final DCMotorSim sim =
      new DCMotorSim(
          LinearSystemId.createDCMotorSystem(gearbox, moi, IntakeConstants.pivotGearing), gearbox);

  private double appliedVolts = 0.0;
  private double encoderOffset = 0.0;

  @Override
  public void updateInputs(IntakePivotIOInputs inputs) {
    sim.setInputVoltage(appliedVolts);
    sim.update(0.02);

    // Hard stops: pin the mechanism and kill velocity when it hits either end of travel
    double motorRotations = getMotorRotations();
    double clamped = MathUtil.clamp(motorRotations, lowerLimit, upperLimit);
    if (clamped != motorRotations) {
      sim.setState(Units.rotationsToRadians(clamped / IntakeConstants.pivotGearing), 0.0);
    }

    inputs.encoderPosition = getMotorRotations() - encoderOffset;
    inputs.appliedVolts = appliedVolts;
    inputs.currentAmps = Math.abs(sim.getCurrentDrawAmps());
  }

  private double getMotorRotations() {
    return sim.getAngularPositionRotations() * IntakeConstants.pivotGearing;
  }

  @Override
  public void setDutyCycle(double speed) {
    appliedVolts = MathUtil.clamp(speed, -1.0, 1.0) * 12.0;
  }

  @Override
  public void resetEncoder() {
    encoderOffset = getMotorRotations();
  }
}
