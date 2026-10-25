package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants;
import frc.robot.subsystems.drive.Drive;

public class SnakeMode extends Command {
  private final Drive drive;
  private final CommandXboxController controller;

  private final PIDController thetaController = new PIDController(3, 0.0, 0.0); // Adjust

  private boolean isRed;
  private Rotation2d targetHeading;
  private final double deadband = Constants.controllerDeadband; // Deadband for joystick input

  public SnakeMode(Drive drive, CommandXboxController controller) {
    this.drive = drive;
    this.controller = controller;
    addRequirements(drive);
  }

  @Override
  public void initialize() {
    thetaController.reset();
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    targetHeading = drive.getPose().getRotation();
  }

  @Override
  public void execute() {

    double xSpeed =
        MathUtil.applyDeadband(
            getSquareInput(-controller.getLeftY()) * drive.getMaxLinearSpeedMetersPerSec(),
            deadband);
    double ySpeed =
        MathUtil.applyDeadband(
            getSquareInput(-controller.getLeftX()) * drive.getMaxLinearSpeedMetersPerSec(),
            deadband);
    isRed =
        DriverStation.getAlliance().isPresent()
            && DriverStation.getAlliance().get() == Alliance.Red;

    if (Math.hypot(xSpeed, ySpeed) > 0.1) {
      targetHeading = new Rotation2d(xSpeed, ySpeed);
    }
    double thetaOut =
        thetaController.calculate(
            (isRed ? drive.getRotation().plus(new Rotation2d(Math.PI)) : drive.getRotation())
                .getRadians(),
            targetHeading.getRadians());

    ChassisSpeeds speeds = new ChassisSpeeds(xSpeed, ySpeed, thetaOut);
    speeds =
        ChassisSpeeds.fromFieldRelativeSpeeds(
            speeds,
            isRed ? drive.getRotation().plus(new Rotation2d(Math.PI)) : drive.getRotation());
    this.drive.runVelocity(speeds);
  }

  private double getSquareInput(double input) {
    return Math.pow(input, 2) * Math.signum(input);
  }
}
