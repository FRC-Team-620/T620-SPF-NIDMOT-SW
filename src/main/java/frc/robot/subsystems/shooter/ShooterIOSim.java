package frc.robot.subsystems.shooter;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

public class ShooterIOSim implements ShooterIO {
  private static final DCMotor gearbox = DCMotor.getNeoVortex(4);
  private static final double gearing = (4.0/3.0);
  private static final double moi = 0.008;
  private static final double fuelMassKg = 0.203;

  private final FlywheelSim sim =
      new FlywheelSim(LinearSystemId.createFlywheelSystem(gearbox, moi, gearing), gearbox);

  private double appliedVolts = 0.0;

  @Override
  public void updateInputs(ShooterIOInputs inputs) {
    sim.setInputVoltage(appliedVolts);
    sim.update(0.02);

    double velocityRPM = sim.getAngularVelocityRPM();
    double motorCurrent = Math.abs(sim.getCurrentDrawAmps()) / 4.0;

    inputs.leftAppliedVolts = appliedVolts;
    inputs.leftVelocityRPM = velocityRPM;
    inputs.leftLeaderCurrentAmps = motorCurrent;
    inputs.leftFollowerCurrentAmps = motorCurrent;
    inputs.rightAppliedVolts = appliedVolts;
    inputs.rightVelocityRPM = velocityRPM;
    inputs.rightLeaderCurrentAmps = motorCurrent;
    inputs.rightFollowerCurrentAmps = motorCurrent;
  }

  @Override
  public void setDutyCycle(double speed) {
    appliedVolts = MathUtil.clamp(speed, -1.0, 1.0) * 12.0;
  }

  @Override
  public void setVoltage(double volts) {
    appliedVolts = MathUtil.clamp(volts, -12.0, 12.0);
  }

  @Override
  public void simulateShot(double fuelSpeedMetersPerSec) {
    applyFuelLoad(fuelSpeedMetersPerSec);
  }

  private void applyFuelLoad(double fuelSpeedMetersPerSec) {
    double omega = sim.getAngularVelocityRadPerSec();
    double fuelEnergy = 0.5 * fuelMassKg * fuelSpeedMetersPerSec * fuelSpeedMetersPerSec;
    double remaining = Math.max(0.0, omega * omega - 2.0 * fuelEnergy / moi);
    sim.setAngularVelocity(Math.copySign(Math.sqrt(remaining), omega));
  }
}
