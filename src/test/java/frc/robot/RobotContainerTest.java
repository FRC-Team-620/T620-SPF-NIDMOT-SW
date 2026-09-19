// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.littletonrobotics.junction.Logger;

/**
 * All tests in this class share a single {@link RobotContainer} instance, constructed once in
 * {@link #setup()}. PathPlannerLib's {@code AutoBuilder} is configured as a process-wide singleton
 * inside {@code Drive}'s constructor and refuses to be reconfigured, so constructing more than one
 * {@link RobotContainer} in the same JVM breaks {@link RobotContainer#getAutonomousCommand()} for
 * every instance after the first.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RobotContainerTest {
  private RobotContainer robotContainer;
  private Exception constructionException;

  @BeforeAll
  void setup() {
    assert HAL.initialize(500, 0);

    // Drive the AdvantageKit logging loop manually (no LoggedRobot in a unit test) so that
    // LoggedDashboardChooser picks up its selected/default value from the underlying
    // SendableChooser; otherwise autoChooser.get() stays null forever, since it's only ever
    // refreshed from Logger's periodic dashboard-input dispatch.
    Logger.AdvancedHooks.disableRobotBaseCheck();
    Logger.start();

    try {
      robotContainer = new RobotContainer();
      Logger.AdvancedHooks.invokePeriodicBeforeUser();
    } catch (Exception e) {
      constructionException = e;
    }
  }

  @AfterAll
  void shutdown() {
    Logger.end();
    HAL.shutdown();
  }

  @Test
  void createRobotContainer() {
    if (constructionException != null) {
      constructionException.printStackTrace();
      fail("Failed to instantiate RobotContainer, see stack trace above.");
    }
  }

  @Test
  void getAutonomousCommandResolves() {
    assertNotNull(
        robotContainer, "RobotContainer failed to construct; see createRobotContainer().");
    assertNotNull(
        robotContainer.getAutonomousCommand(),
        "getAutonomousCommand() should never return null after construction.");
  }

  @Test
  void enableDisableTransitionsDoNotThrow() {
    assertNotNull(
        robotContainer, "RobotContainer failed to construct; see createRobotContainer().");

    assertDoesNotThrow(
        () -> {
          DriverStationSim.setEnabled(false);
          DriverStationSim.notifyNewData();
          for (int i = 0; i < 5; i++) {
            CommandScheduler.getInstance().run();
          }

          DriverStationSim.setEnabled(true);
          DriverStationSim.notifyNewData();
          for (int i = 0; i < 5; i++) {
            CommandScheduler.getInstance().run();
          }

          DriverStationSim.setEnabled(false);
          DriverStationSim.notifyNewData();
          for (int i = 0; i < 5; i++) {
            CommandScheduler.getInstance().run();
          }
        },
        "Disable/enable/disable transition threw an exception, see stack trace above.");
  }
}
