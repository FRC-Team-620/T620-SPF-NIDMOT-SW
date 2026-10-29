package frc.robot.subsystems.shooter;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

public class HoodIOSim implements HoodIO {
  private static final DCMotor gearbox = DCMotor.getNEO(1);
  private static final double moi = 0.01;
  private static final double upperLimit = 0.0;
  private static final double lowerLimit = ShooterConstants.hoodMaxPosition;

  private final DCMotorSim sim =
      new DCMotorSim(
          LinearSystemId.createDCMotorSystem(gearbox, moi, ShooterConstants.hoodGearing), gearbox);

  private double appliedVolts = 0.0;
  private double encoderOffset = 0.0;

  @Override
  public void updateInputs(HoodIOInputs inputs) {
    sim.setInputVoltage(appliedVolts);
    sim.update(0.02);

    // Hard stops: pin the mechanism and kill velocity when it hits either end of travel
    double motorRotations = getMotorRotations();
    double clamped = MathUtil.clamp(motorRotations, lowerLimit, upperLimit);
    if (clamped != motorRotations) {
      sim.setState(Units.rotationsToRadians(clamped / ShooterConstants.hoodGearing), 0.0);
    }

    inputs.encoderPosition = getMotorRotations() - encoderOffset;
    inputs.appliedVolts = appliedVolts;
    inputs.currentAmps = Math.abs(sim.getCurrentDrawAmps());
  }

  private double getMotorRotations() {
    return sim.getAngularPositionRotations() * ShooterConstants.hoodGearing;
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
