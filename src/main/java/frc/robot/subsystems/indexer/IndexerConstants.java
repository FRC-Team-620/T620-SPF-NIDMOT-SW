package frc.robot.subsystems.indexer;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

public class IndexerConstants {

  // -------------------------------------------------------------------------
  // Device CAN IDs
  // you can set the leader and follower motor here. just make sure it's the right CAN ID
  // -------------------------------------------------------------------------
  public static final int leftLeaderCanId = 51;
  public static final int leftFollowerCanId = 53;
  public static final int rightMotorCanId = 52;

  // -------------------------------------------------------------------------
  // Motor configuration
  // -------------------------------------------------------------------------
  public static final IdleMode idleMode = IdleMode.kCoast;
  public static final int currentLimitAmps = 40;

  // -------------------------------------------------------------------------
  // Bank inversion — configure once on the real robot.
  // The two banks must always be opposite; set invertLeftBank and the right
  // bank is automatically derived as its negation.
  // -------------------------------------------------------------------------
  public static final boolean invertLeftBank = false;
  public static final boolean invertRightBank = true;
}
