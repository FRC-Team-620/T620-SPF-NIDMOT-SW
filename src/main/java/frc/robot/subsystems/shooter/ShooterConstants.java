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
  public static final int currentLimitAmps = 40;

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
  public static final double shooterKP = 0.002;
  public static final double shooterKI = 0.0;
  public static final double shooterKD = 0.0;
  public static final double shooterKS = 0.190783; // volts, static friction offset
  public static final double shooterKV = 0.00182048; // volts per RPM
  public static final double shooterVelocityToleranceRPM = 150.0;

  // -------------------------------------------------------------------------
  // Robot geometry
  // -------------------------------------------------------------------------
  public static final double robotHeightM = 18.25 / 39.37;
  public static final double shooterWheelRadiusM = 4 / 39.37;

  // -------------------------------------------------------------------------
  // field geometry
  // -------------------------------------------------------------------------
  public static final double hubToTowerDistanceM = 2.9;

  // -------------------------------------------------------------------------
  // tuning offsets
  // -------------------------------------------------------------------------
  public static final double shooterVelocityOffsetRPM = 0.0;
  public static final double hoodPositionOffset = 5.0;
  public static final double distOffset = .762;
  public static final double atHubRPM = 100;
  // scaling number
  public static final double atHubAngle = 0.5 - 0.0;
  public static final double atTowerRPM = 2400.0;
  // scaling number
  // public static final double atTowerAngle = (-7.5) * (20 / 360);
  public static final double atTowerAngle = -3;
  // this is a comment
}
