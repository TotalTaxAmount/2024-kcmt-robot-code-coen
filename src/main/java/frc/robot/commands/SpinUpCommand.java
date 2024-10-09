package frc.robot.commands;

import com.ctre.phoenix6.hardware.Pigeon2;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.constants.Constants.Target;
import frc.robot.constants.ShooterConstants;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.utils.ConfigManager;

public class SpinUpCommand extends Command {
    private final ShooterSubsystem shooterSubsystem;

    private final Target target;

    /**
     * Command to spin up the shooter
     * @param target Amp, Amp-Shoot, Speaker, High Pass, or Low Pass from {@link Target}
     * @param shooterSubsystem Instance of {@link ShooterSubsystem}
     */
    public SpinUpCommand(ShooterSubsystem shooterSubsystem, Target target) {
        this.target = target;
        this.shooterSubsystem = shooterSubsystem;
    }

//    @Override
//    public void initialize() {
//        shooterSubsystem.resetPID();
//    }

    @Override
    public void execute() {
        switch (this.target) {
            case AMP, AMP_SHOOT ->
                shooterSubsystem.setShooterSpeed(ConfigManager.getInstance().get("amp_spinup_rpm", Double.class, ShooterConstants.AMP_RPM));
            case SPEAKER, LOW_PASS ->
                shooterSubsystem.setShooterSpeed(ConfigManager.getInstance().get("speaker_spinup_rpm", Double.class, ShooterConstants.SPEAKER_RPM));
            case HIGH_PASS ->
                shooterSubsystem.setShooterSpeed(ConfigManager.getInstance().get("pass_spinup_rpm", Double.class, ShooterConstants.PASS_RPM));
        }
    }

    @Override
    public void end(boolean interrupted) {
        shooterSubsystem.runVolts(0.0);
    }

    @Override
    public void initialize() {
        shooterSubsystem.resetPID();
    }
}
