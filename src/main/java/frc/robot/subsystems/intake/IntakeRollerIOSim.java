package frc.robot.subsystems.intake;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

public class IntakeRollerIOSim implements IntakeRollerIO {
  private static final DCMotor gearbox = DCMotor.getNEO(2);
  private static final double gearing = 3.0;
  private static final double moi = 0.001;

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
    inputs.leaderCurrentAmps = current / 2.0;
    inputs.followerCurrentAmps = current / 2.0;
  }

  @Override
  public void setSpeed(double speed) {
    appliedVolts = MathUtil.clamp(speed, -1.0, 1.0) * 12.0;
  }
}
