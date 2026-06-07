// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.drive;

import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.RobotConfig;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import frc.robot.subsystems.drive.MAXSwerveModuleConfig.DriveMotor;
import frc.robot.subsystems.drive.MAXSwerveModuleConfig.GearConfig;
import frc.robot.subsystems.drive.MAXSwerveModuleConfig.TurnMotor;

public class DriveConstants {

  // -------------------------------------------------------------------------
  // Active hardware selection — edit only these three lines to reconfigure.
  // Enum definitions and their data live in DriveModuleConfig.java (READONLY).
  // -------------------------------------------------------------------------
  public static final GearConfig  driveConfig      = GearConfig.HIGH;
  public static final DriveMotor  driveMotorConfig = DriveMotor.NEO_V1;
  public static final TurnMotor   turnMotorConfig  = TurnMotor.NEO_550;

  // -------------------------------------------------------------------------
  // Kinematics
  // -------------------------------------------------------------------------
  public static final double maxSpeedMetersPerSec = driveMotorConfig.freeSpeedMps(driveConfig); // m/s
  public static final double odometryFrequency = 100.0; // Hz

  // Outer frame dimensions (to edge of swerve module housing), in inches.
  // Wheel inset is sourced from MAXSwerveModuleConfig — see ASCII diagram there.
  public static final double frameWidthInches  = 26.5;
  public static final double frameLengthInches = 26.5;

  public static final double trackWidth = MAXSwerveModuleConfig.getTrackWidth(frameWidthInches);
  public static final double wheelBase  = MAXSwerveModuleConfig.getWheelBase(frameLengthInches);
  public static final double driveBaseRadius = Math.hypot(trackWidth / 2.0, wheelBase / 2.0);
  public static final Translation2d[] moduleTranslations =
      new Translation2d[] {
        new Translation2d(trackWidth / 2.0, wheelBase / 2.0),
        new Translation2d(trackWidth / 2.0, -wheelBase / 2.0),
        new Translation2d(-trackWidth / 2.0, wheelBase / 2.0),
        new Translation2d(-trackWidth / 2.0, -wheelBase / 2.0)
      };

  // Zeroed rotation values for each module, see setup instructions
  public static final Rotation2d frontLeftZeroRotation  = new Rotation2d(0.0);
  public static final Rotation2d frontRightZeroRotation = new Rotation2d(0.0);
  public static final Rotation2d backLeftZeroRotation   = new Rotation2d(0.0);
  public static final Rotation2d backRightZeroRotation  = new Rotation2d(0.0);

  // -------------------------------------------------------------------------
  // Device CAN IDs
  // Ref: https://docs.google.com/spreadsheets/d/1E2qQl7P2I0ImZtpcdnKNVKiHB-iJdpjz9EQkrZBmuj4/edit?usp=sharing
  // -------------------------------------------------------------------------
  public static final int pigeonCanId = 62;

  public static final int frontLeftDriveCanId  = 10;
  public static final int backLeftDriveCanId   = 12;
  public static final int frontRightDriveCanId = 11;
  public static final int backRightDriveCanId  = 13;

  public static final int frontLeftTurnCanId  = 20;
  public static final int backLeftTurnCanId   = 22;
  public static final int frontRightTurnCanId = 21;
  public static final int backRightTurnCanId  = 23;

  // -------------------------------------------------------------------------
  // Drive motor configuration
  // -------------------------------------------------------------------------
  public static final int driveMotorCurrentLimit = 50;
  public static final double wheelRadiusMeters   = Units.inchesToMeters(1.5);
  public static final double driveMotorReduction = driveConfig.gearRatio;
  public static final edu.wpi.first.math.system.plant.DCMotor driveGearbox = driveMotorConfig.motor;

  // Drive encoder configuration
  public static final double driveEncoderPositionFactor =
      2 * Math.PI / driveMotorReduction; // Rotor Rotations -> Wheel Radians
  public static final double driveEncoderVelocityFactor =
      (2 * Math.PI) / 60.0 / driveMotorReduction; // Rotor RPM -> Wheel Rad/Sec

  // Drive PID configuration
  public static final double driveKp    = 0.0;
  public static final double driveKd    = 0.0;
  public static final double driveKs    = 0.0;
  public static final double driveKv    = 0.1;
  public static final double driveSimP  = 0.05;
  public static final double driveSimD  = 0.0;
  public static final double driveSimKs = 0.0;
  public static final double driveSimKv = 0.0789;

  // -------------------------------------------------------------------------
  // Turn motor configuration
  // -------------------------------------------------------------------------
  public static final boolean turnInverted        = false;
  public static final int turnMotorCurrentLimit   = 20;
  public static final double turnMotorReduction   = driveConfig.azimuthRatio;
  public static final edu.wpi.first.math.system.plant.DCMotor turnGearbox = turnMotorConfig.motor;

  // Turn encoder configuration
  public static final boolean turnEncoderInverted        = true;
  public static final double turnEncoderPositionFactor   = 2 * Math.PI; // Rotations -> Radians
  public static final double turnEncoderVelocityFactor   = (2 * Math.PI) / 60.0; // RPM -> Rad/Sec

  // Turn PID configuration
  public static final double turnKp           = 2.0;
  public static final double turnKd           = 0.0;
  public static final double turnSimP         = 8.0;
  public static final double turnSimD         = 0.0;
  public static final double turnPIDMinInput  = 0;          // Radians
  public static final double turnPIDMaxInput  = 2 * Math.PI; // Radians

  // -------------------------------------------------------------------------
  // PathPlanner configuration
  // -------------------------------------------------------------------------
  public static final double robotMassKg = 74.088;
  public static final double robotMOI    = 6.883;
  public static final double wheelCOF    = 1.2;
  public static final RobotConfig ppConfig =
      new RobotConfig(
          robotMassKg,
          robotMOI,
          new ModuleConfig(
              wheelRadiusMeters,
              maxSpeedMetersPerSec,
              wheelCOF,
              driveGearbox.withReduction(driveMotorReduction),
              driveMotorCurrentLimit,
              1),
          moduleTranslations);
}
