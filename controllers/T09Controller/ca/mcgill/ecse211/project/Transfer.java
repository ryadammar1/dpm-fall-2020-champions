package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;

public class Transfer {

    /** Buffer (array) to store US samples. */
    private static float[] usData = new float[usSensor1.sampleSize()];
    /**
     * The limit of invalid samples that we read from the US sensor before assuming
     * no obstacle.
     */
    public static final int INVALID_SAMPLE_LIMIT = 20;
    /** The distance remembered by the filter() method. */
    private static int prevDistance;
    /** The number of invalid samples seen by filter() so far. */
    private static int invalidSampleCount;

    public static void doTransfer() {
        // Get into position
        setSpeed(FORWARD_SPEED/2);
        while(readUsDistance()>0)
            moveForward();

        stopMotors();

        // Secure the block
        cageMotor.rotate(180, false);

        
        
    }

    // ULTRASONIC SENSOR RELATED //

    /**
     * Returns the filtered distance between the US sensor and an obstacle in cm.
     */
    public static int readUsDistance() {
        usSensor1.fetchSample(usData, 0);
        // extract from buffer, cast to int, and filter
        return filter((int) (usData[0] * 100.0));
    }

    /**
     * Rudimentary filter - toss out invalid samples corresponding to null signal.
     * 
     * @param distance raw distance measured by the sensor in cm
     * @return the filtered distance in cm
     */
    public static int filter(int distance) {
        if (distance >= MAX_SENSOR_DIST && invalidSampleCount < INVALID_SAMPLE_LIMIT) {
            // bad value, increment the filter value and return the distance remembered from
            // before
            invalidSampleCount++;
            return prevDistance;
        } else {
            if (distance < MAX_SENSOR_DIST) {
                invalidSampleCount = 0; // reset filter and remember the input distance.
            }
            prevDistance = distance;
            return distance;
        }
    }
}
