package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.shooter.Hood;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.vision.Vision;

public class ShootWithVision extends Command {
    private final Shooter shooter;
    private final Vision vision;
    private final Hood hood;

    private double rpm;
    private double hoodPositionDEG;
    private double velocity;


    public ShootWithVision(Shooter shooter, Vision vision, Hood hood) {
        this.shooter = shooter;
        this.vision = vision;
        this.hood = hood;
        // addRequirements(shooter);
    }

    @Override
    public void initialize() {
        //do the math here
    }

    @Override
    public void execute() {
        //keep calling here
    }

    @Override
    public void end(boolean interrupted) {
        //call end function
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}