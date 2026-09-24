package frc.robot.subsystems.shooter;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

public class ShooterConstants {

  // -------------------------------------------------------------------------
  // Device CAN IDs
  // -------------------------------------------------------------------------
  public static final int leftLeaderCanId = 41;
  public static final int leftFollowerCanId = 42;
  public static final int rightLeaderCanId = 43;
  public static final int rightFollowerCanId = 44;
  public static final int hoodCanId = 45;

  // -------------------------------------------------------------------------
  // Motor configuration
  // -------------------------------------------------------------------------
  public static final IdleMode idleMode = IdleMode.kCoast;
  public static final IdleMode hoodIdleMode = IdleMode.kBrake;
  public static final int currentLimitAmps = 60; // was 40; limits spin-up/recovery acceleration
  public static final int hoodCurrentLimitAmps = 40;

  // Velocity measurement filter for the built-in Vortex encoder. REV defaults (100 ms period,
  // 64-sample depth) add ~80 ms of lag, so the feedback loop reacts late to shot dips.
  // Lower = less lag but noisier velocity.
  public static final int velocityMeasurementPeriodMs = 16;
  public static final int velocityAverageDepth = 2;

  // -------------------------------------------------------------------------
  // Bank inversion — configure once on the real robot
  // -------------------------------------------------------------------------
  public static final boolean invertLeftBank = true;
  public static final boolean invertRightBank = false;

  // -------------------------------------------------------------------------
  // Hood positions (primary encoder counts)
  // -------------------------------------------------------------------------
  public static final double hoodStowPosition = 0.5;
  public static final double hoodExtendPosition = -7.0;
  public static final double hoodMaxPosition = -8.0;
  public static final double hoodMaxOutput = 0.75;
  public static final double hoodAdjustDelta = 0.5;

  // -------------------------------------------------------------------------
  // Shooter velocity control
  // -------------------------------------------------------------------------
  public static final double shooterIdleRPM = 750.0;
  public static final double shooterPresetRPM = 3700.0;
  // PID + feedforward run onboard the Spark Flexes (1 kHz). PID output is duty cycle, so kP is
  // duty cycle per RPM of error; kS/kV are volts and volts per RPM.
  public static final double shooterKP = 0.002;
  public static final double shooterKI = 0.0;
  public static final double shooterKD = 0.0;
  public static final double shooterKS = 0.190783; // volts, static friction offset
  public static final double shooterKV = 0.00182048; // volts per RPM
  public static final double shooterVelocityToleranceRPM = 150.0;
}
