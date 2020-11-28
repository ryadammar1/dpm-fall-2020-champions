package ca.mcgill.ecse211.project;

import static simlejos.ExecutionController.*;
import static ca.mcgill.ecse211.project.Resources.*;

public class CageController {

    private static final int torqueThreshold = 12;

    private static int prevTacho = 0;
    private static int currTacho = 0;

    /**
     * Closes the cage.
     * 
     * @return true if the cage successfully closed
     */
    public static boolean closeCage() {
        prevTacho = cageMotor.getTachoCount();
        cageMotor.setSpeed(60);
        cageMotor.rotate(180, true);
        currTacho = cageMotor.getTachoCount();

        return waitForCage();
    }

    /**
     * Opens the cage.
     */
    public static void openCage() {
        cageMotor.setSpeed(30);
        cageMotor.rotate(180, false);
    }

    /**
     * Resets the cage to initial position.
     */
    public static void resetCage() {
        cageMotor.setSpeed(60);
        cageMotor.rotate((prevTacho - currTacho), false);
    }

    /**
     * Waits until the cage is down.
     * 
     * @return boolean true if the cage successfully closed
     */
    public static boolean waitForCage() {
        // Wait while motor is moving
        currTacho = cageMotor.getTachoCount();
        double previousTacho = Double.POSITIVE_INFINITY;
        while ((Math.abs(currTacho - previousTacho) > 0.000000001)) {
            previousTacho = currTacho;
            // Sleep for 400 physics steps
            for (int i = 0; i < 400; i++)
                waitUntilNextStep();
            currTacho = cageMotor.getTachoCount();

            if (cageMotor.getTorque() > torqueThreshold)
                return false;
        }
        return true;
    }

}
