// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import frc.robot.subsystems.drive.GyroIOPigeon2;
import frc.robot.subsystems.drive.ModuleIOSpark;
import frc.robot.subsystems.indexer.IndexerIOSpark;
import frc.robot.subsystems.intake.IntakePivotIOSpark;
import frc.robot.subsystems.intake.IntakeRollerIOSpark;
import frc.robot.subsystems.shooter.HoodIOSpark;
import frc.robot.subsystems.shooter.ShooterIOSpark;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Smoke tests for the hardware IO implementations that only ever run on the real robot ({@link
 * RobotContainer}'s {@code REAL} branch). Under {@code ./gradlew test} or the desktop simulator,
 * {@link Constants#currentMode} never resolves to {@code REAL}, so these constructors are otherwise
 * completely untested. Catches bad CAN/Spark config calls before they reach the field.
 */
public class HardwareIOConstructionTest {
  @BeforeEach
  void setup() {
    assert HAL.initialize(500, 0);
  }

  @AfterEach
  void shutdown() {
    HAL.shutdown();
  }

  @Test
  void constructShooterIOSpark() {
    try {
      new ShooterIOSpark();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to construct ShooterIOSpark, see stack trace above.");
    }
  }

  @Test
  void constructHoodIOSpark() {
    try {
      new HoodIOSpark();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to construct HoodIOSpark, see stack trace above.");
    }
  }

  @Test
  void constructIntakePivotIOSpark() {
    try {
      new IntakePivotIOSpark();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to construct IntakePivotIOSpark, see stack trace above.");
    }
  }

  @Test
  void constructIntakeRollerIOSpark() {
    try {
      new IntakeRollerIOSpark();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to construct IntakeRollerIOSpark, see stack trace above.");
    }
  }

  @Test
  void constructIndexerIOSpark() {
    try {
      new IndexerIOSpark();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to construct IndexerIOSpark, see stack trace above.");
    }
  }

  @Test
  void constructModuleIOSpark() {
    try {
      new ModuleIOSpark(0);
      new ModuleIOSpark(1);
      new ModuleIOSpark(2);
      new ModuleIOSpark(3);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to construct ModuleIOSpark, see stack trace above.");
    }
  }

  @Test
  void constructGyroIOPigeon2() {
    try {
      new GyroIOPigeon2();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failed to construct GyroIOPigeon2, see stack trace above.");
    }
  }
}
