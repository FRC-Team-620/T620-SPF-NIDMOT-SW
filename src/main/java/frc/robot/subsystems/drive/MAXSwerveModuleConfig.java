// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.drive;

import edu.wpi.first.math.system.plant.DCMotor;

/**
 * Hardware configuration enums for the REV MAXSwerve module (REV-21-3005).
 *
 * <p>READONLY — do not instantiate or mutate these enums at runtime. They exist solely to provide
 * named hardware profiles that are selected once in DriveConstants and used to populate its fields.
 * All active selections live in DriveConstants; edit only there.
 *
 * <p>For other swerve module types, create a parallel config file (e.g. SwerveXModuleConfig.java)
 * following the same pattern and update the imports in DriveConstants accordingly.
 *
 * <p>Pinion/gear data: https://www.revrobotics.com/rev-21-3005/
 * <p>AK template ref: https://docs.advantagekit.org/getting-started/template-projects/spark-swerve-template
 */
public class MAXSwerveModuleConfig {

  // -------------------------------------------------------------------------
  // Physical module geometry
  //
  // Top-down view — one module in a corner:
  //
  //   ┌──────────────────────────┐  ← outer frame edge
  //   │◄─ inset ─►●              │  ● = wheel center
  //   │           │              │
  //   └───────────┼──────────────┘
  //
  // Full robot top-down:
  //
  //   ├─────────── frameWidth ───────────┤
  //   ┌──────────────────────────────────┐ ─┐
  //   │  ●────────────────────────────●  │  │
  //   │  FL         trackWidth        FR │  │ frameLength
  //   │  ●────────────────────────────●  │  │
  //   │  BL                           BR │  │
  //   └──────────────────────────────────┘ ─┘
  //      ├──────── trackWidth ────────┤
  //         └── wheelBase (front-back)
  //
  //   trackWidth = frameWidth  − 2 × WHEEL_INSET_INCHES
  //   wheelBase  = frameLength − 2 × WHEEL_INSET_INCHES
  // -------------------------------------------------------------------------

  /** Distance from the outer edge of the module housing to the wheel center, in inches. */
  public static final double WHEEL_INSET_INCHES = 1.75;

  /** Distance from the outer edge of the module housing to the wheel center, in meters. */
  public static double wheelInsetMeters() {
    return edu.wpi.first.math.util.Units.inchesToMeters(WHEEL_INSET_INCHES);
  }

  /** Track width (m) from outer frame width (in). */
  public static double getTrackWidth(double frameWidthInches) {
    return edu.wpi.first.math.util.Units.inchesToMeters(frameWidthInches - 2 * WHEEL_INSET_INCHES);
  }

  /** Wheel base (m) from outer frame length (in). */
  public static double getWheelBase(double frameLengthInches) {
    return edu.wpi.first.math.util.Units.inchesToMeters(frameLengthInches - 2 * WHEEL_INSET_INCHES);
  }

  // -------------------------------------------------------------------------
  // MAXSwerve module gear configuration (Base Kit, REV-21-3005)
  // Selects the drive pinion installed in the module.
  // -------------------------------------------------------------------------

  public enum GearConfig {
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

    /** Overall drive gear reduction (motor rotations per wheel rotation). */
    public final double gearRatio;
    /** Theoretical free speed with a NEO V1.1 drive motor (m/s). */
    public final double freeSpeedNeoV1Mps;
    /** Theoretical free speed with a NEO Vortex drive motor (m/s). */
    public final double freeSpeedNeoVortexMps;
    /** Drive pinion tooth count. */
    public final int drivePinionTeeth;
    /** REV part number for the drive pinion. */
    public final String drivePinionPartNumber;
    /** Drive spur gear tooth count (constant across configs). */
    public final int spurTeeth;
    /** REV part number for the drive spur gear. */
    public final String spurPartNumber;
    /** Azimuth (turn) gear reduction — 9424:203 (~46.42:1) for all MAXSwerve configs. */
    public final double azimuthRatio;
    /** Steering pinion tooth count (constant across configs). */
    public final int steeringPinionTeeth;
    /** REV part number for the steering pinion. */
    public final String steeringPinionPartNumber;

    GearConfig(
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

  // -------------------------------------------------------------------------
  // Drive motor selection
  // Determines the DCMotor model used for simulation and PathPlanner config.
  // Free speed is resolved against the active GearConfig.
  // -------------------------------------------------------------------------

  public enum DriveMotor {
    NEO_V1(DCMotor.getNEO(1)),
    NEO_VORTEX(DCMotor.getNeoVortex(1));

    /** WPILib DCMotor model for simulation and feedforward calculations. */
    public final DCMotor motor;

    DriveMotor(DCMotor motor) {
      this.motor = motor;
    }

    /** Returns the free speed (m/s) for this motor in the given gear config. */
    public double freeSpeedMps(GearConfig gearConfig) {
      return this == NEO_VORTEX
          ? gearConfig.freeSpeedNeoVortexMps
          : gearConfig.freeSpeedNeoV1Mps;
    }
  }

  // -------------------------------------------------------------------------
  // Turn motor selection
  // MAXSwerve azimuth is driven by a NEO 550 via UltraPlanetary. Only one
  // option exists for this module; enum is here for consistency and future-proofing.
  // -------------------------------------------------------------------------

  public enum TurnMotor {
    NEO_550(DCMotor.getNeo550(1));

    /** WPILib DCMotor model for simulation. */
    public final DCMotor motor;

    TurnMotor(DCMotor motor) {
      this.motor = motor;
    }
  }

  // -------------------------------------------------------------------------
  // Per-module zero rotations after running the REV MAXSwerve Zeroing Tool
  //
  // The REV zeroing tool sets the through-bore encoder to 0 when the bevel
  // gear faces the robot center. Because each module housing is rotated
  // differently on the chassis, "encoder = 0" maps to a different wheel
  // direction per corner. These constants are the encoder readings when the
  // wheel points straight forward, i.e. the values to use for
  // DriveConstants.*ZeroRotation immediately after running the zeroing tool.
  //
  // Physical zero position (bevel gear facing robot center):
  //   FL → wheel points right  (+90° from fwd) → forward is at  -π/2
  //   FR → wheel points forward (0° from fwd)  → forward is at   0
  //   BL → wheel points backward (180°)        → forward is at   π
  //   BR → wheel points left   (-90° from fwd) → forward is at  +π/2
  //
  // Ref: REV MAXSwerve Java template Constants.java kFront/BackLeft/RightChassisAngularOffset
  // -------------------------------------------------------------------------

  public static final class ZeroRotations {
    public static final edu.wpi.first.math.geometry.Rotation2d FRONT_LEFT =
        edu.wpi.first.math.geometry.Rotation2d.fromRadians(-Math.PI / 2);
    public static final edu.wpi.first.math.geometry.Rotation2d FRONT_RIGHT =
        edu.wpi.first.math.geometry.Rotation2d.fromRadians(0);
    public static final edu.wpi.first.math.geometry.Rotation2d BACK_LEFT =
        edu.wpi.first.math.geometry.Rotation2d.fromRadians(Math.PI);
    public static final edu.wpi.first.math.geometry.Rotation2d BACK_RIGHT =
        edu.wpi.first.math.geometry.Rotation2d.fromRadians(Math.PI / 2);
  }
}
