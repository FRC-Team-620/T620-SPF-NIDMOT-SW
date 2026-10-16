package frc.robot.subsystems.intake;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import edu.wpi.first.math.geometry.Translation3d;

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

  // Motor rotations per pivot rotation. Estimate, not measured — shared by sim and 3D poses.
  public static final double pivotGearing = 75.0;

  // -------------------------------------------------------------------------
  // 3D visualization — AdvantageScope Robot_Spitfire component 0 (model_0)
  // -------------------------------------------------------------------------
  // Pivot axis in robot coordinates (meters), measured from CAD (27.5" MAXSpline pivot shaft)
  public static final Translation3d pivotOrigin = new Translation3d(0.291, 0.0, 0.207);
  // Encoder position the CAD model was exported at (intake deployed)
  public static final double pivotCadPosition = pivotExtendPosition;
  // Flip to -1.0 if the model swings the wrong way in AdvantageScope
  public static final double pivotVisualizationDirection = 1.0;
}
