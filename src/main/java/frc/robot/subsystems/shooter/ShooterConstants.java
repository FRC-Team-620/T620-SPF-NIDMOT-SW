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

  // -------------------------------------------------------------------------
  // Motor configuration
  // -------------------------------------------------------------------------
  public static final IdleMode idleMode = IdleMode.kCoast;
  public static final int currentLimitAmps = 40;

  // -------------------------------------------------------------------------
  // Bank inversion — configure once on the real robot
  // -------------------------------------------------------------------------
  public static final boolean invertLeftBank = false;
  public static final boolean invertRightBank = false;
}
