// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.RobotBase;

/**
 * This class defines the runtime mode used by AdvantageKit. The mode is always "real" when running
 * on a roboRIO. Change the value of "simMode" to switch between "sim" (physics sim) and "replay"
 * (log replay from a file).
 */
public final class Constants {
  public static final Mode simMode = Mode.SIM;
  public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;
  public static final int stallCurrentBuffer = 10;

  public static final class Intake {
    public static final double kJamMinCommandDutyCycle = 0.1;
    public static final double kJamCurrentThresholdAmps = 30.0; // TODO tune (IMPORTANT)
    public static final double kJamVelocityThresholdRPM = 200.0; // TODO tune (IMPORTANT)
    public static final double kJamDetectionSeconds = 0.15; // sustained stall time before reacting
    public static final double kJamReverseDutyCycle = 0.6;
    public static final double kJamReverseSeconds = 0.2; // how long to run in reverse to clear
  }

  public static enum Mode {
    /** Running on a real robot. */
    REAL,

    /** Running a physics simulator. */
    SIM,

    /** Replaying from a log file. */
    REPLAY
  }

  public static class Poses {
    public static final Pose2d hubBLUE = new Pose2d(3.569, 4.051, Rotation2d.fromDegrees(180));
    public static final Pose2d hubRED = new Pose2d(13.00, 4.051, Rotation2d.fromDegrees(0));
  }
}
