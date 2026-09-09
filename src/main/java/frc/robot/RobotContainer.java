// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import frc.robot.commands.Autos;
import frc.robot.commands.DriveCommands;
import frc.robot.commands.HoodCommands;
import frc.robot.commands.IndexerCommands;
import frc.robot.commands.IntakePivotCommands;
import frc.robot.commands.ShooterCommands;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.GyroIO;
import frc.robot.subsystems.drive.GyroIOPigeon2;
import frc.robot.subsystems.drive.ModuleIO;
import frc.robot.subsystems.drive.ModuleIOSim;
import frc.robot.subsystems.drive.ModuleIOSpark;
import frc.robot.subsystems.indexer.Indexer;
import frc.robot.subsystems.indexer.IndexerIO;
import frc.robot.subsystems.indexer.IndexerIOSpark;
import frc.robot.subsystems.intake.IntakePivot;
import frc.robot.subsystems.intake.IntakePivotIO;
import frc.robot.subsystems.intake.IntakePivotIOSpark;
import frc.robot.subsystems.intake.IntakeRoller;
import frc.robot.subsystems.intake.IntakeRollerIO;
import frc.robot.subsystems.intake.IntakeRollerIOSpark;
import frc.robot.subsystems.shooter.Hood;
import frc.robot.subsystems.shooter.HoodIO;
import frc.robot.subsystems.shooter.HoodIOSpark;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;
import frc.robot.subsystems.shooter.ShooterIO;
import frc.robot.subsystems.shooter.ShooterIOSpark;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionConstants;
import frc.robot.subsystems.vision.VisionIO;
import frc.robot.subsystems.vision.VisionIOLimelight;
import frc.robot.subsystems.vision.VisionIOPhotonVisionSim;
import java.util.function.DoubleSupplier;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // Subsystems
  private final Drive drive;
  private final Indexer indexer;
  private final IntakePivot intakePivot;
  private final IntakeRoller intakeRoller;
  private final Hood hood;
  private final Shooter shooter;
  private final Vision vision;

  // Controller, controller = driver, op = operator
  private final CommandXboxController driver = new CommandXboxController(0);
  private final CommandXboxController op = new CommandXboxController(1);

  // Dashboard inputs
  private final LoggedDashboardChooser<Command> autoChooser;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    switch (Constants.currentMode) {
      case REAL:
        // Real robot, instantiate hardware IO implementations
        SmartDashboard.putData(CommandScheduler.getInstance());
        drive =
            new Drive(
                new GyroIOPigeon2(),
                new ModuleIOSpark(0),
                new ModuleIOSpark(1),
                new ModuleIOSpark(2),
                new ModuleIOSpark(3));
        indexer = new Indexer(new IndexerIOSpark());
        intakePivot = new IntakePivot(new IntakePivotIOSpark());
        intakeRoller = new IntakeRoller(new IntakeRollerIOSpark());
        hood = new Hood(new HoodIOSpark());
        shooter = new Shooter(new ShooterIOSpark());
        vision =
            new Vision(
                drive::addVisionMeasurement,
                new VisionIOLimelight(VisionConstants.camera0Name, VisionConstants.robotToCamera0),
                new VisionIOLimelight(VisionConstants.camera1Name, VisionConstants.robotToCamera1));
        break;

      case SIM:
        // Sim robot, instantiate physics sim IO implementations
        drive =
            new Drive(
                new GyroIO() {},
                new ModuleIOSim(),
                new ModuleIOSim(),
                new ModuleIOSim(),
                new ModuleIOSim());
        indexer = new Indexer(new IndexerIO() {});
        intakePivot = new IntakePivot(new IntakePivotIO() {});
        intakeRoller = new IntakeRoller(new IntakeRollerIO() {});
        hood = new Hood(new HoodIO() {});
        shooter = new Shooter(new ShooterIO() {});
        vision =
            new Vision(
                drive::addVisionMeasurement,
                new VisionIOPhotonVisionSim(
                    VisionConstants.camera0Name, VisionConstants.robotToCamera0, drive::getPose),
                new VisionIOPhotonVisionSim(
                    VisionConstants.camera1Name, VisionConstants.robotToCamera1, drive::getPose));
        break;

      default:
        // Replayed robot, disable IO implementations
        drive =
            new Drive(
                new GyroIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new ModuleIO() {},
                new ModuleIO() {});
        indexer = new Indexer(new IndexerIO() {});
        intakePivot = new IntakePivot(new IntakePivotIO() {});
        intakeRoller = new IntakeRoller(new IntakeRollerIO() {});
        hood = new Hood(new HoodIO() {});
        shooter = new Shooter(new ShooterIO() {});
        vision = new Vision(drive::addVisionMeasurement, new VisionIO() {}, new VisionIO() {});
        break;
    }

    // Set up auto routines
    autoChooser = new LoggedDashboardChooser<>("Auto Choices", AutoBuilder.buildAutoChooser());

    // Set up SysId routines
    // autoChooser.addOption(
    //     "Drive Wheel Radius Characterization", DriveCommands.wheelRadiusCharacterization(drive));
    // autoChooser.addOption(
    //     "Drive Simple FF Characterization", DriveCommands.feedforwardCharacterization(drive));
    // autoChooser.addOption(
    //     "Drive SysId (Quasistatic Forward)",
    //     drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    // autoChooser.addOption(
    //     "Drive SysId (Quasistatic Reverse)",
    //     drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    // autoChooser.addOption(
    //     "Drive SysId (Dynamic Forward)", drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
    // autoChooser.addOption(
    //     "Drive SysId (Dynamic Reverse)", drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));

    autoChooser.addDefaultOption(
        "Center Front Shoot", Autos.centerFrontShoot(shooter, hood, intakePivot, indexer));

    // Configure the button bindings
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    // Coast on disable, brake on enable
    RobotModeTriggers.disabled()
        .onTrue(
            Commands.runOnce(
                    () -> {
                      intakePivot.setBrakeMode(false);
                      hood.setBrakeMode(false);
                    })
                .ignoringDisable(true))
        .onFalse(
            Commands.runOnce(
                () -> {
                  intakePivot.setBrakeMode(true);
                  hood.setBrakeMode(true);
                }));

    // Zero-encoder buttons (usable while disabled)
    SmartDashboard.putData(
        "IntakePivot/ZeroEncoder",
        Commands.runOnce(intakePivot::resetEncoder, intakePivot).ignoringDisable(true));
    SmartDashboard.putData(
        "Hood/ZeroEncoder", Commands.runOnce(hood::resetEncoder, hood).ignoringDisable(true));

    // Idle shooter at 750 RPM by default; auto sequence controls spinup during autonomous
    shooter.setDefaultCommand(
        ShooterCommands.runAtVelocity(shooter, ShooterConstants.shooterIdleRPM));

    // Spin shooter at preset RPM while Y is held
    driver.y().whileTrue(ShooterCommands.runAtVelocity(shooter, ShooterConstants.shooterPresetRPM));

    op.x().whileTrue(ShooterCommands.runAtVelocity(shooter, ShooterConstants.shooterPresetRPM));
    // Op A toggles idle: off = stopped, on = 750 RPM default resumes
    op.a().toggleOnTrue(ShooterCommands.stopShooter(shooter));

    // Indexer tuning via SmartDashboard ("Indexer/Enable", "Indexer/DutyCycle")
    indexer.setDefaultCommand(IndexerCommands.indexerTuning(indexer));

    // Run indexer at 50% while RB is held
    driver.rightBumper().whileTrue(IndexerCommands.runAtDutyCycle(indexer, 0.85));
    op.rightBumper().whileTrue(IndexerCommands.runAtDutyCycle(indexer, 0.85));
    // Intake pivot position control: stow on D-pad down, extend on D-pad up
    driver.povDown().onTrue(IntakePivotCommands.stow(intakePivot));
    driver.povUp().onTrue(IntakePivotCommands.extend(intakePivot));

    // op.rightBumper().onTrue(IntakePivotCommands.stow(intakePivot));
    // op.leftBumper().onTrue(IntakePivotCommands.extend(intakePivot));

    // Hood position control: stow on L3, extend on R3
    driver.leftStick().onTrue(HoodCommands.stow(hood));
    driver.rightStick().onTrue(HoodCommands.extend(hood));

    // Hood position trim: op POV left raises, op POV right lowers
    op.povLeft().onTrue(HoodCommands.adjustPosition(hood, ShooterConstants.hoodAdjustDelta));
    op.povRight().onTrue(HoodCommands.adjustPosition(hood, -ShooterConstants.hoodAdjustDelta));

    // Intake roller speed mapped 1:1 to left trigger
    double intakeSpeedModifier = 0.9;
    intakeRoller.setDefaultCommand(
        Commands.run(
            () ->
                intakeRoller.setSpeed(
                    (driver.getRightTriggerAxis() - driver.getLeftTriggerAxis())
                        * intakeSpeedModifier),
            intakeRoller));

    // Default command, normal field-relative drive
    double speedModifier = 1;
    drive.setDefaultCommand(
        DriveCommands.joystickDrive(
            drive,
            () -> driver.getLeftY() * speedModifier,
            () -> driver.getLeftX() * speedModifier,
            () -> -driver.getRightX()));

    // Auto-aim at boiler while left bumper is held: rotate drive toward target and adjust hood
    // angle
    DoubleSupplier distanceToBoiler = DriveCommands.distanceToBoiler(drive);
    driver
        .leftBumper()
        .whileTrue(
            Commands.parallel(
                DriveCommands.autoAim(drive, () -> -driver.getLeftY(), () -> -driver.getLeftX()),
                HoodCommands.autoAim(hood, distanceToBoiler)));

    // Lock to 0° when A button is held
    driver
        .a()
        .whileTrue(
            DriveCommands.joystickDriveAtAngle(
                drive, () -> -driver.getLeftY(), () -> -driver.getLeftX(), () -> Rotation2d.kZero));

    // Switch to X pattern when X button is pressed
    driver.x().onTrue(Commands.runOnce(drive::stopWithX, drive));

    // Reset gyro to 0° when B button is pressed
    driver
        .b()
        .onTrue(
            Commands.runOnce(
                    () ->
                        drive.setPose(
                            new Pose2d(drive.getPose().getTranslation(), Rotation2d.kZero)),
                    drive)
                .ignoringDisable(true));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoChooser.get();
  }
}
