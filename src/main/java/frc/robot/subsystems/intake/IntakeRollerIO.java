package frc.robot.subsystems.intake;

import org.littletonrobotics.junction.AutoLog;

public interface IntakeRollerIO {
  @AutoLog
  class IntakeRollerIOInputs {
    public double appliedVolts = 0.0;
    public double velocityRPM = 0.0;
    public double leaderCurrentAmps = 0.0;
    public double followerCurrentAmps = 0.0;
  }

  default void updateInputs(IntakeRollerIOInputs inputs) {}

  default void setSpeed(double speed) {}

  default void simulateIntake() {}

  /** Test-only hook: forces the roller to behave as if physically stuck, for sim testing. */
  default void setSimJammed(boolean jammed) {}
}
