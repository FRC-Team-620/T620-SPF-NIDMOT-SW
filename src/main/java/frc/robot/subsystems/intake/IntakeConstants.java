package frc.robot.subsystems.intake;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

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
  public static final int currentLimitAmps = 40;

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
}
