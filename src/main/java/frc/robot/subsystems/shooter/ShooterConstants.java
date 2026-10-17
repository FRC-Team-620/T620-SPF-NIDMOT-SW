package frc.robot.subsystems.shooter;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.geometry.Translation3d;

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
  public static final int currentLimitAmps =
      40; // was 40, then 60; limits spin-up/recovery acceleration
  public static final int hoodCurrentLimitAmps = 15; // was 40

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
  public static final double hoodStowPosition = -0.1; // to-do : verfiy correct value
  public static final double hoodExtendPosition = -1.25; // to-do: verfiy correct value
  public static final double hoodMaxPosition = -8.0;
  public static final double hoodMaxOutput = 0.75;

  public static final double hoodAdjustDelta = 0.5;

  // Motor rotations per hood rotation. Estimate, not measured — shared by sim and 3D poses.
  public static final double hoodGearing = 50.0; // TODO: We need to fix

  // -------------------------------------------------------------------------
  // 3D visualization — AdvantageScope Robot_Spitfire component 1 (model_1)
  // -------------------------------------------------------------------------
  // Hood pivot axis in robot coordinates (meters), measured from CAD (shooter drum axis)
  public static final Translation3d hoodOrigin = new Translation3d(-0.26, 0.0, 0.482);
  // Encoder position the CAD model was exported at. Unverified — assumes hood at stow stop.
  public static final double hoodCadPosition = 0.0;
  // Flip to -1.0 if the model swings the wrong way in AdvantageScope
  public static final double hoodVisualizationDirection = 1.0;

  // -------------------------------------------------------------------------
  // Shooter velocity control
  // -------------------------------------------------------------------------
  public static final double shooterIdleRPM = 750.0;
  // public static final double shooterPresetRPM = 2200.0; // tower
  public static final double shooterPresetRPM = 2000.0; // tower
  public static final double shooterKI = 0.0;
  public static final double shooterKD = 0.0;
  public static final double shooterKS = 0.190783; // volts, static friction offset
  public static final double shooterKV = 0.00182048; // volts per RPM
  public static final double shooterVelocityToleranceRPM = 150.0;

  // -------------------------------------------------------------------------
  // Known tested shot points (not wired to anything yet)
  // -------------------------------------------------------------------------
  public static final class TestedPoints {
    public static final class Tower { // bumper againnst line
      public static final double hoodPosition = -4.0;
      public static final double rpm = 2200.0;
    }

    public static final class Hub { // ~1ft from hub, directly against is not a realistic shot angle
      public static final double hoodPosition = -1.25;
      public static final double rpm = 2000.0;
    }
  }
}
