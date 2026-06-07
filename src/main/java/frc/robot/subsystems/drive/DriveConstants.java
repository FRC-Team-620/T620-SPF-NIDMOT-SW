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
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;

public class DriveConstants {

  public enum MaxSwerveConfig {
    //                   driveRatio  neoV1   vortex  drivePinion  drivePinionPart      spurTeeth
    // spurPart           azmRatio          steerPinionTeeth  steerPinionPart
    LOW(
        5.50,
        4.12,
        4.92,
        12,
        "REV-21-3005-P09",
        22,
        "REV-21-3005-P11",
        9424.0 / 203.0,
        14,
        "REV-21-3005-P08"),
    MEDIUM(
        5.08,
        4.46,
        5.33,
        13,
        "REV-21-3005-P22",
        22,
        "REV-21-3005-P11",
        9424.0 / 203.0,
        14,
        "REV-21-3005-P08"),
    HIGH(
        4.71,
        4.80,
        5.74,
        14,
        "REV-21-3005-P23",
        22,
        "REV-21-3005-P11",
        9424.0 / 203.0,
        14,
        "REV-21-3005-P08");

    public final double gearRatio;
    public final double freeSpeedNeoV1Mps;
    public final double freeSpeedNeoVortexMps;
    public final int drivePinionTeeth;
    public final String drivePinionPartNumber;
    public final int spurTeeth;
    public final String spurPartNumber;
    public final double azimuthRatio;
    public final int steeringPinionTeeth;
    public final String steeringPinionPartNumber;

    MaxSwerveConfig(
        double gearRatio,
        double freeSpeedNeoV1Mps,
        double freeSpeedNeoVortexMps,
        int drivePinionTeeth,
        String drivePinionPartNumber,
        int spurTeeth,
        String spurPartNumber,
        double azimuthRatio,
        int steeringPinionTeeth,
        String steeringPinionPartNumber) {
      this.gearRatio = gearRatio;
      this.freeSpeedNeoV1Mps = freeSpeedNeoV1Mps;
      this.freeSpeedNeoVortexMps = freeSpeedNeoVortexMps;
      this.drivePinionTeeth = drivePinionTeeth;
      this.drivePinionPartNumber = drivePinionPartNumber;
      this.spurTeeth = spurTeeth;
      this.spurPartNumber = spurPartNumber;
      this.azimuthRatio = azimuthRatio;
      this.steeringPinionTeeth = steeringPinionTeeth;
      this.steeringPinionPartNumber = steeringPinionPartNumber;
    }
  }

  public static final MaxSwerveConfig driveConfig = MaxSwerveConfig.HIGH;

  public static final double maxSpeedMetersPerSec = driveConfig.freeSpeedNeoV1Mps;
  public static final double odometryFrequency = 100.0; // Hz
  public static final double trackWidth = Units.inchesToMeters(26.5);
  public static final double wheelBase = Units.inchesToMeters(26.5);
  public static final double driveBaseRadius = Math.hypot(trackWidth / 2.0, wheelBase / 2.0);
  public static final Translation2d[] moduleTranslations =
      new Translation2d[] {
        new Translation2d(trackWidth / 2.0, wheelBase / 2.0),
        new Translation2d(trackWidth / 2.0, -wheelBase / 2.0),
        new Translation2d(-trackWidth / 2.0, wheelBase / 2.0),
        new Translation2d(-trackWidth / 2.0, -wheelBase / 2.0)
      };

  // Zeroed rotation values for each module, see setup instructions
  public static final Rotation2d frontLeftZeroRotation = new Rotation2d(0.0);
  public static final Rotation2d frontRightZeroRotation = new Rotation2d(0.0);
  public static final Rotation2d backLeftZeroRotation = new Rotation2d(0.0);
  public static final Rotation2d backRightZeroRotation = new Rotation2d(0.0);

  // Device CAN IDs
  public static final int pigeonCanId = 9;

  public static final int frontLeftDriveCanId = 1;
  public static final int backLeftDriveCanId = 3;
  public static final int frontRightDriveCanId = 5;
  public static final int backRightDriveCanId = 7;

  public static final int frontLeftTurnCanId = 2;
  public static final int backLeftTurnCanId = 4;
  public static final int frontRightTurnCanId = 6;
  public static final int backRightTurnCanId = 8;

  // Drive motor configuration
  public static final int driveMotorCurrentLimit = 50;
  public static final double wheelRadiusMeters = Units.inchesToMeters(1.5);
  public static final double driveMotorReduction = driveConfig.gearRatio;
  public static final DCMotor driveGearbox = DCMotor.getNeoVortex(1);

  // Drive encoder configuration
  public static final double driveEncoderPositionFactor =
      2 * Math.PI / driveMotorReduction; // Rotor Rotations ->
  // Wheel Radians
  public static final double driveEncoderVelocityFactor =
      (2 * Math.PI) / 60.0 / driveMotorReduction; // Rotor RPM ->
  // Wheel Rad/Sec

  // Drive PID configuration
  public static final double driveKp = 0.0;
  public static final double driveKd = 0.0;
  public static final double driveKs = 0.0;
  public static final double driveKv = 0.1;
  public static final double driveSimP = 0.05;
  public static final double driveSimD = 0.0;
  public static final double driveSimKs = 0.0;
  public static final double driveSimKv = 0.0789;

  // Turn motor configuration
  public static final boolean turnInverted = false;
  public static final int turnMotorCurrentLimit = 20;
  public static final double turnMotorReduction = driveConfig.azimuthRatio;
  public static final DCMotor turnGearbox = DCMotor.getNeo550(1);

  // Turn encoder configuration
  public static final boolean turnEncoderInverted = true;
  public static final double turnEncoderPositionFactor = 2 * Math.PI; // Rotations -> Radians
  public static final double turnEncoderVelocityFactor = (2 * Math.PI) / 60.0; // RPM -> Rad/Sec

  // Turn PID configuration
  public static final double turnKp = 2.0;
  public static final double turnKd = 0.0;
  public static final double turnSimP = 8.0;
  public static final double turnSimD = 0.0;
  public static final double turnPIDMinInput = 0; // Radians
  public static final double turnPIDMaxInput = 2 * Math.PI; // Radians

  // PathPlanner configuration
  public static final double robotMassKg = 74.088;
  public static final double robotMOI = 6.883;
  public static final double wheelCOF = 1.2;
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
