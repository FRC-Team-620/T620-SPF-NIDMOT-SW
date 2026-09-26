package frc.robot.subsystems.intake;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.trajectory.TrapezoidProfile;

public class IntakeConstants {

  // -------------------------------------------------------------------------
  // Device CAN IDs
  // -------------------------------------------------------------------------
  public static final int leaderCanId = 31;
  public static final int followerCanId = 32;
  public static final int pivotCanId = 33;

  // -------------------------------------------------------------------------
  // Motor configuration
  // -------------------------------------------------------------------------
  public static final IdleMode idleMode = IdleMode.kBrake;
  public static final int currentLimitAmps = 20;

  // -------------------------------------------------------------------------
  // Pivot positions (primary encoder counts)
  // -------------------------------------------------------------------------
  public static final double pivotStowPosition = 0.5;
  public static final double pivotExtendPosition = 24.0;
  public static final double pivotMaxOutput = 0.5;

  // -------------------------------------------------------------------------
  // Pivot homing (current-stall zeroing)
  // -------------------------------------------------------------------------
  // Duty cycle applied while driving toward the hard stop (negative = toward zero)
  public static final double pivotHomingDutyCycle = -0.1; // TODO: tune me
  // Current threshold indicating the motor has stalled against the hard stop (amps, tune
  // empirically)
  public static final double pivotHomingCurrentThreshold = 15.0; // TODO: tune me
  // Failsafe: abort homing if it hasn't completed within this many seconds
  public static final double pivotHomingTimeoutSeconds = 3.0; // TODO: tune me

  // Reduced output ceiling for stowing during the fire sequence, so cargo already loaded against
  // the hopper isn't jolted by a full-speed stow.
  public static final double pivotFireStowMaxOutput = 0.15;

  // Velocity-limited stow profile (max velocity, max acceleration in encoder units/sec[^2]).
  // Not wired into any command yet — verify the reduced-output stow on the robot first, then
  // tune these and switch fire over to IntakePivotCommands.stowProfiled.
  public static final TrapezoidProfile.Constraints pivotFireStowConstraints =
      new TrapezoidProfile.Constraints(20.0, 15.0);

  public static final double rollerFireSpeed = 0.9;
}
