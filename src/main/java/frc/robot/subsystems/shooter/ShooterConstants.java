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
  // -----------------------------------`--------------------------------------
  public static final double hoodStowPosition = -0.1;
  public static final double hoodExtendPosition = -1.25; // hub
  public static final double hoodMaxPosition = -8.0;
  public static final double hoodMaxOutput = 0.75;

  public static final double hoodAdjustDelta = 0.5;

  // Aim presets: near/far hood angle + shooter RPM pairs for the aim command.
  public static final double hoodNearPosition = TestedPoints.Hub.hoodPosition;
  public static final double hoodFarPosition = TestedPoints.Tower.hoodPosition;

  // -------------------------------------------------------------------------
  // Shooter velocity control
  // -------------------------------------------------------------------------
  public static final double shooterIdleRPM = 750.0;
  public static final double shooterNearRPM = TestedPoints.Hub.rpm;
  public static final double shooterFarRPM = TestedPoints.Tower.rpm;
  public static final double shooterKP = 0.005; // volts per RPM of error
  public static final double shooterKI = 0.0;
  public static final double shooterKD = 0.0;
  public static final double shooterKS = 0.190783; // volts, static friction offset
  public static final double shooterKV = 0.00182048; // volts per RPM
  public static final double shooterVelocityToleranceRPM = 150.0;

  // -------------------------------------------------------------------------
  // Known tested shot points (not wired to anything yet)
  // -------------------------------------------------------------------------
  public static final class TestedPoints {
    public static final class Tower { // bumper against line
      public static final double hoodPosition = -4.0;
      public static final double rpm = 2200.0;
    }

    public static final class Hub {
      public static final double hoodPosition = -1.25;
      public static final double rpm = 2000.0;
    }
  }
}
