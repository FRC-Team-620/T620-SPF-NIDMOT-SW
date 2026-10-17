package frc.robot.subsystems.intake;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

public class IntakeRollerIOSim implements IntakeRollerIO {
  private static final DCMotor gearbox = DCMotor.getNEO(2);
  private static final double gearing = 1.5;
  private static final double moi = 0.001;
  private static final double rollerRadiusMeters = 0.0159;
  private static final double fuelMassKg = 0.203;

  private final FlywheelSim sim =
      new FlywheelSim(LinearSystemId.createFlywheelSystem(gearbox, moi, gearing), gearbox);

  private double appliedVolts = 0.0;

  @Override
  public void updateInputs(IntakeRollerIOInputs inputs) {
    sim.setInputVoltage(appliedVolts);
    sim.update(0.02);

    // Leader and follower share the load, so split the current between them
    double current = Math.abs(sim.getCurrentDrawAmps());

    inputs.appliedVolts = appliedVolts;
    inputs.velocityRPM = sim.getAngularVelocityRPM() * gearing;
    inputs.leaderCurrentAmps = current / 2.0;
    inputs.followerCurrentAmps = current / 2.0;
  }

  @Override
  public void setSpeed(double speed) {
    appliedVolts = MathUtil.clamp(speed, -1.0, 1.0) * 12.0;
  }

  @Override
  public void simulateIntake() {
    applyFuelLoad(sim.getAngularVelocityRadPerSec() * rollerRadiusMeters);
  }

  private void applyFuelLoad(double fuelSpeedMetersPerSec) {
    double omega = sim.getAngularVelocityRadPerSec();
    double fuelEnergy = 0.5 * fuelMassKg * fuelSpeedMetersPerSec * fuelSpeedMetersPerSec;
    double remaining = Math.max(0.0, omega * omega - 2.0 * fuelEnergy / moi);
    sim.setAngularVelocity(Math.copySign(Math.sqrt(remaining), omega));
  }
}
