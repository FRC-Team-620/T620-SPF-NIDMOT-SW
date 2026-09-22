package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.shooter.Hood;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;
import frc.robot.subsystems.vision.Vision;

public class ShootWithVision extends Command {
  private final Shooter shooter;
  private final Vision vision;
  private final Hood hood;

  // private double rpm;
  private double hoodPositionDEG;
  private double velocity;
  private double distFromHub;

  public ShootWithVision(Shooter shooter, Vision vision, Hood hood) {
    this.shooter = shooter;
    this.vision = vision;
    this.hood = hood;
    // addRequirements(shooter);
  }

  @Override
  public void initialize() {
    // do the math here
    // distFromHub = get the x translation somehow??
    hoodPositionDEG = 90 - .5 * Math.atan(distFromHub / (1.83 - ShooterConstants.robotHeightM));
    double minBallVelocity =
        Math.sqrt(
            9.81
                * (Math.sqrt(
                        Math.pow(distFromHub, 2)
                            + Math.pow(1.83 - ShooterConstants.robotHeightM, 2))
                    + 1.83
                    - ShooterConstants.robotHeightM));
    velocity = minBallVelocity / ShooterConstants.shooterWheelRadiusM;

    // TO-DO - HOW DO I ADJUST THE HOOD
    shooter.setTargetVelocity(velocity);
  }

  @Override
  public void execute() {
    // TODO - HOW DO I ADJUST THE HOOD
    shooter.setTargetVelocity(velocity);
  }

  @Override
  public void end(boolean interrupted) {
    shooter.setTargetVelocity(0);
    // hood.doSomething();
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}
