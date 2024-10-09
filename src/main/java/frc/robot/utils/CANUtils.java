package frc.robot.utils;

import com.revrobotics.CANSparkFlex;
import com.revrobotics.CANSparkLowLevel;
import com.revrobotics.CANSparkMax;

public class CANUtils {


    /**
     * Configure a {@link CANSparkMax} to free up CAN bus bandwidth
     * @param motor The motor to configure
     * @return The configured motor
     */
    public static CANSparkMax configure(CANSparkMax motor) {
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus0, 1000);
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus3, 1000);
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus4, 1000);
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus5, 1000);
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus6, 1000);
        return motor;
    }

    /**
     * Configure a {@link CANSparkFlex} to free up CAN bus bandwidth
     * @param motor The motor to configure
     * @return The configured motor
     */
    public static CANSparkFlex configure(CANSparkFlex motor) {
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus0, 1000);
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus3, 1000);
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus4, 1000);
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus5, 1000);
        motor.setPeriodicFramePeriod(CANSparkLowLevel.PeriodicFrame.kStatus6, 1000);
        return motor;
    }
}
