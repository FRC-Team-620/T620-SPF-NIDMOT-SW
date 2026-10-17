package frc.robot.subsystems.indexer;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

public class IndexerIOSim implements IndexerIO {
  private static final DCMotor gearbox = DCMotor.getNeoVortex(3);
  private static final double gearing = 3.0;
  private static final double moi = 0.002;

  private final FlywheelSim sim =
      new FlywheelSim(LinearSystemId.createFlywheelSystem(gearbox, moi, gearing), gearbox);

  private double appliedVolts = 0.0;

  @Override
  public void updateInputs(IndexerIOInputs inputs) {
    sim.setInputVoltage(appliedVolts);
    sim.update(0.02);

    double motorCurrent = Math.abs(sim.getCurrentDrawAmps()) / 3.0;

    inputs.leftAppliedVolts = appliedVolts;
    inputs.leftLeaderCurrentAmps = motorCurrent;
    inputs.leftFollowerCurrentAmps = motorCurrent;
    inputs.rightAppliedVolts = appliedVolts;
    inputs.rightCurrentAmps = motorCurrent;
  }

  @Override
  public void setDutyCycle(double speed) {
    appliedVolts = MathUtil.clamp(speed, -1.0, 1.0) * 12.0;
  }
}
