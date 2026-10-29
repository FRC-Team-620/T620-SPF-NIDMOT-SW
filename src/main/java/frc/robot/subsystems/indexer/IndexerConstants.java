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
  public static final int currentLimitAmps = 35;

  // -------------------------------------------------------------------------
  // Bank inversion — configure once on the real robot.
  // The two banks must always be opposite; set invertLeftBank and the right
  // bank is automatically derived as its negation.
  // -------------------------------------------------------------------------
  public static final boolean invertLeftBank = false;
  public static final boolean invertRightBank = true;

  public static final double kJamMinCommandDutyCycle = 0.1;
  public static final double kJamVelocityThresholdRPM = 200.0; // TODO tune (IMPORTANT)
  public static final double kJamDetectionSeconds = 0.15; // sustained stall time before reacting
  public static final double kJamReverseDutyCycle = 0.6;
  public static final double kJamReverseSeconds = 0.2; // how long to run in reverse to clear
}
