package ca.mcgill.ecse211.project;

import java.util.ArrayList;
import ca.mcgill.ecse211.playingfield.Point;
import ca.mcgill.ecse211.playingfield.Circle;
import ca.mcgill.ecse211.playingfield.Rect;
import simlejos.hardware.ev3.LocalEV3;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;
import static simlejos.ExecutionController.*;

public class Search {

    /**
     * Different methodologies used by the robot Memorize: The robot memorizes the
     * position of each encountered obstacles found using the color sensor.
     * Recognize: The robot recognizes in real time the obstacles based on their
     * height.
     */
    public enum Mode {
        Memorize, Recognize
    }

    /** Search mode */
    private static final Mode MODE = Mode.Recognize;

    /**
     * Maximum distance difference between sensor usSensor1 and usSensor2
     */
    private static final int MAX_US_SENSOR_DIFFERENCE = 10;

    /** Front light sensor used for block identification */
    private static float[] colorSensorDataFront;

    /** Buffer (array) to store US samples */
    private static float[] usData1 = new float[usSensor1.sampleSize()];
    private static float[] usData2;

    /**
     * The limit of invalid samples that we read from the US sensor before assuming
     * no obstacle.
     */
    public static final int INVALID_SAMPLE_LIMIT = 20;

    /** The distance remembered by the filter() method. */
    private static int prevDistance;

    /** The number of invalid samples seen by filter() so far. */
    private static int invalidSampleCount;

    /** Maximal distance between the robot and an object in meters */
    private static final double DISTANCE_THREESHOLD = TILE_SIZE;

    /** Linked to edge bounding box width in tile size */
    public static final double EDGE_BOUND_WIDTH = 0.5;

    /**
     * Offset between the center of the robot and the front of the usSensor in cm
     */
    private static final double DIST_US_SENSOR_Y = 9;
    private static final double DIST_US_SENSOR_X = 0;

    /**
     * The number of samples bypassed before sampling. The higher the value, the
     * better theperformance will be.
     */
    private static final int SAMPLE_PERIOD = 400;

    /**
     * All blacklisted points identified as obstacles/walls Circle are used instead
     * of points and rectangles are used instead of walls to allow for more error
     * tolerance
     */
    private static ArrayList<Circle> blacklistPoint = new ArrayList<Circle>();
    private static ArrayList<Rect> blacklistEdge = new ArrayList<Rect>();

    /** The previous motor tacho counts (from previous iteration of while loop). */
    private static int[] prevTacho = new int[2];
    /** The current motor tacho counts. */
    private static int[] currTacho = new int[2];

    private static int sampleNumA = 0;
    private static int sampleNumB = 0;

    /**
     * Controls the number of scans performed within a distance during @Code
     * hasDangerWithin(). The higher the value, the more precise the scan.
     */
    private static double SCAN_FREQUENCY = 20;

    /** Array of last 5 light sensor readings to avoid false positives */
    private static int[] lastReadings = new int[5];

    /** Distance at which the the light sensor starts reading. */
    private static float detectionThreshold = 20;

    /** View FOV used by @code hasDangerWithin(distance) */
    public static final double VIEW_FOV = 40;

    /**
     * Initialzes the bounding boxes of known obstacles before starting the search
     */
    public static void initializeSearch() {

        Resources.initSensors();
        if (MODE == Mode.Recognize)
            usData2 = new float[usSensor2.sampleSize()];

        // bottom wall
        blacklistEdge.add(Rect.creatRectFromEdge(new Point(searchZone.ll.x, searchZone.ll.y),
                new Point(searchZone.ur.x, searchZone.ll.y)));
        // top wall
        blacklistEdge.add(Rect.creatRectFromEdge(new Point(searchZone.ll.x, searchZone.ur.y),
                new Point(searchZone.ur.x, searchZone.ur.y)));
        // left wall
        blacklistEdge.add(Rect.creatRectFromEdge(new Point(searchZone.ll.x, searchZone.ll.y),
                new Point(searchZone.ll.x, searchZone.ur.y)));
        // right wall
        blacklistEdge.add(Rect.creatRectFromEdge(new Point(searchZone.ur.x, searchZone.ll.y),
                new Point(searchZone.ur.x, searchZone.ur.y)));

        // red ramp
        blacklistEdge.add(rrbb);
        // green ramp
        blacklistEdge.add(grbb);

        blacklistEdge.add(new Rect(new Point(tunnel.ll.x - 0.25, tunnel.ll.y - 0.25),
                new Point(tunnel.ur.x + 0.25, tunnel.ur.y + 0.25))); // tunnel
    }

    /** Main method of search */
    public static void doSearch() {

        odometer.printPosition();

        stopMotors();

        // Sleep for 1 seconds
        try {
            Thread.sleep(TIMEOUT_PERIOD / 2);
        } catch (InterruptedException e) {
        }

        prevTacho[0] = leftMotor.getTachoCount();
        prevTacho[1] = rightMotor.getTachoCount();

        while (true) {
            rotateClockwise();

            System.out.println(readUsDistance(1)); // Helps synchronize thread? Don't remove
            if (MODE == Mode.Recognize)
                System.out.println(readUsDistance(2)); // Helps synchronize thread? Don't remove

            if (MODE == Mode.Memorize && hasSpottedNewOject()) {
                System.out.println("Object detected");
                stopMotors();
                int result = identify(); // 0 = unidentified, 1 = Block, 2 = Obstacle
                switch (result) {
                    case (0): {
                        System.out.println("Could not identify object");
                        break;
                    }
                    case (1): {
                        stopMotors();

                        Main.STATE_MACHINE.setBlockDetected(true);
                        Main.STATE_MACHINE.detectObstacle();

                        LocalEV3.getAudio().beep();
                        try {
                            LocalEV3.getAudio().beep();
                            Thread.sleep(TIMEOUT_PERIOD / 2);
                            LocalEV3.getAudio().beep();
                            Thread.sleep(TIMEOUT_PERIOD / 2);
                            LocalEV3.getAudio().beep();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return;
                    }
                    case (2): {
                        addToBlackList(readUsDistance(1), getCurrentAngle());
                        setSpeed(FORWARD_SPEED);
                        moveStraightFor(-0.5); // backoff a bit to avoid touching obstacle later.
                    }
                }
                break;
            }

            if (MODE == Mode.Recognize && hasSpottedNewOject()) {
                stopMotors();

                Main.STATE_MACHINE.setBlockDetected(true);
                Main.STATE_MACHINE.detectObstacle();

                LocalEV3.getAudio().beep();
                try {
                    LocalEV3.getAudio().beep();
                    Thread.sleep(TIMEOUT_PERIOD / 2);
                    LocalEV3.getAudio().beep();
                    Thread.sleep(TIMEOUT_PERIOD / 2);
                    LocalEV3.getAudio().beep();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;
            }

            if (hasFullyRotated()) {
                // If no near object is detected, find a secure place to navigate to
                boolean hasSeenDanger = false;

                System.out.println("Could not find near object");
                while (hasDangerWithin((int) (1.2 * DISTANCE_THREESHOLD * 100))) {
                    rotateClockwise();
                    hasSeenDanger = true;
                }
                // If an object was avoided, the robot is prone to collision due to tight us
                // sensor FOV.
                // Turn by 20 degrees to assure no collision.
                // Otherwise, check right side of the robot to assure no obstacle and correct
                // orientation accordingly.
                if (MODE == Mode.Recognize && hasSeenDanger)
                    turnBy(20);
                else {
                    turnBy(20);
                    if (hasDangerWithin((int) (1.2 * DISTANCE_THREESHOLD * 100)))
                        turnBy(-40);
                    else
                        turnBy(-20);
                }
                moveStraightFor(DISTANCE_THREESHOLD / TILE_SIZE);
                return;
            }
        }
        stopMotors();
    }

    /**
     * Returns true if the robot has rotated by at least 2 PI rad, false otherwise
     * Calculates current change in heading using motor's tacho count
     * 
     * @return boolean
     */
    private static boolean hasFullyRotated() {
        currTacho[0] = leftMotor.getTachoCount();
        currTacho[1] = rightMotor.getTachoCount();

        // compute L and R wheel displacements
        double distL = Math.PI * WHEEL_RAD * (currTacho[0] - prevTacho[0]) / 180;
        double distR = Math.PI * WHEEL_RAD * (currTacho[1] - prevTacho[1]) / 180;

        double dtheta = (distL - distR) / BASE_WIDTH; // compute change in heading

        if (dtheta >= 2 * Math.PI) {
            return true;
        }
        return false;
    }

    private static void addToBlackList(double hypotenuse, double angle) {
        Point crt = getCurrentPosition();

        double xc = 0.5 + (int) (crt.x + DIST_US_SENSOR_X
                + Math.sin(Math.toRadians(angle)) * (hypotenuse + DIST_US_SENSOR_Y) / (TILE_SIZE * 100));
        double yc = 0.5
                + (int) (crt.y + Math.cos(Math.toRadians(angle)) * (hypotenuse + DIST_US_SENSOR_Y) / (TILE_SIZE * 100));

        Rect box = new Rect(new Point(xc, yc), 1.25, 1.25);

        System.out.println(xc);
        System.out.println(yc);

        blacklistEdge.add(box);

    }

    /**
     * Compute an approximation of an obstacle's coordinate base on the robot's
     * current angle and the distance read by the distance. Returns true if the
     * point is near the walls or an already balcklisted point, false otherwise.
     * Circles and rectangles are used instead of points and lines to approximate
     * the point's location.
     * 
     * @param hypotenus in cm
     * @param angle
     */
    private static boolean isBlackListed(double hypotenuse, double angle) {

        Point crt = getCurrentPosition();

        double dx = DIST_US_SENSOR_X + Math.sin(Math.toRadians(angle)) * (hypotenuse + DIST_US_SENSOR_Y); // x
                                                                                                          // displacement
        double dy = Math.cos(Math.toRadians(angle)) * (hypotenuse + DIST_US_SENSOR_Y); // y displacement

        Point npt = new Point(crt.x + dx / (TILE_SIZE * 100), crt.y + dy / (TILE_SIZE * 100));

        /*
         * Debug:
         *
         * System.out.println("angle = " + angle); System.out.println("hypo = " +
         * (hypotenuse + DIST_US_SENSOR_Y)); System.out.println("dx = " + dx +
         * DIST_US_SENSOR_X); System.out.println("dy = " + dy);
         * System.out.println("Point curr = " + crt); System.out.println("Point seen = "
         * + npt);
         */

        for (Circle point : blacklistPoint) {
            if (point.contains(npt))
                return true;
        }
        for (Rect edge : blacklistEdge) {
            if (edge.contains(npt))
                return true;
        }
        return false;
    }

    /**
     * Memorize: Returns true if usSensor1 has spotted an non-blacklisted object,
     * false otherwise.
     * 
     * Recognize: Returns true if usSensor1 has spotted an non-blacklisted object
     * and usSensor2 did not, false otherwise.
     * 
     * @return boolean
     */
    private static boolean hasSpottedNewOject() {
        sampleNumA++;

        if (sampleNumA != SAMPLE_PERIOD) {
            return false;
        }
        sampleNumA = 0;

        int hypotenuse = readUsDistance(1);
        if (((MODE == Mode.Memorize)
                && (hypotenuse < DISTANCE_THREESHOLD * 100 && !isBlackListed(hypotenuse, getCurrentAngle())))
                || ((MODE == Mode.Recognize) && !(Math.abs(hypotenuse - readUsDistance(2)) < MAX_US_SENSOR_DIFFERENCE))
                        && (hypotenuse < DISTANCE_THREESHOLD * 100 && !isBlackListed(hypotenuse, getCurrentAngle())))
            return true;
        return false;
    }

    /**
     * Returns true if no danger is within a certain distance, false otherwise.
     * 
     * @return boolean
     */
    private static boolean hasDangerWithin(double hypotenuse) {
        sampleNumB++;

        if (sampleNumB != SAMPLE_PERIOD) {
            return true;
        }
        sampleNumB = 0;

        double hyp = hypotenuse;

        while (hyp > 0) {
            if (isBlackListed(hyp, getCurrentAngle()) || isBlackListed(hyp, getCurrentAngle() + VIEW_FOV / 2)
                    || isBlackListed(hyp, getCurrentAngle() - VIEW_FOV / 2)) {
                return true;
            }
            hyp -= hypotenuse * (1 / SCAN_FREQUENCY);
        }
        if (MODE == Mode.Recognize && (Math.abs(readUsDistance(1) - readUsDistance(2)) < MAX_US_SENSOR_DIFFERENCE)) // Necessary
                                                                                                                    // second
                                                                                                                    // check
                                                                                                                    // for
                                                                                                                    // recognize
            return true;

        return false;

    }

    /**
     * Method to perform the identification step by driving towards obstacle end
     * performing readings with bothe the US and light sensor
     * 
     * @return the type of object identified (0=Unidentified, 1=Block, 2=Obstacle)
     */
    public static int identify() {

        // Speed could be modified but FORWARD_SPEED is too fast for that.
        setSpeed(LOWER_FORWARD_SPEED);
        moveForward();

        int result = 0;
        while (result == 0) {
            sleepFor(PHYSICS_STEP_PERIOD * 5);
            float usReading = readUsDistance(1);
            if (usReading >= detectionThreshold) {
                // Need to be close enought to object for light sensor readings to detect object
                continue;
            }
            int newReading = identifyObject(); // getting result from light sensor
            int counter = 1;
            for (int j = 4; j > 0; j--) { // loop to add new result to array and switch old ones.
                lastReadings[j] = lastReadings[j - 1];
                if (lastReadings[j] == newReading) {
                    counter++;
                }

            }
            lastReadings[0] = newReading;

            // Needs at least 3 similar readings to ensure that the identification is
            // somewhat accurate.
            // Attempt to avoid false positives and sensor just seeing noise/background
            // light.
            if (counter >= 3) {
                result = newReading;
            }

        }

        stopMotors();

        return result;

    }

    /**
     * Method using the front light sensors to perform measurements and attempt to
     * identify if ht eobject is a block (white) or a wall/obstacle (brown)
     */
    private static int identifyObject() {
        Resources.colorSensorFront.fetchSample(colorSensorDataFront, 0);
        System.out.println(colorSensorDataFront[0] + "   " + colorSensorDataFront[1] + "   " + colorSensorDataFront[2]);

        int red = (int) (colorSensorDataFront[0]);
        int green = (int) (colorSensorDataFront[1]);
        int blue = (int) (colorSensorDataFront[2]);

        /*
         * Value differences between each color channel to identify colors White: all 3
         * channels should be within a +-5 margin. Brown: Red needs to be 10 over Green
         * which itself needs to be 10 over blue.
         */
        int BROWN_MARGIN = 10;

        if (red - green <= -5 || red - green <= 5) {
            if (blue - green <= -5 || blue - green <= 5) {
                System.out.println("white detected");
                return 1;
            }
        }

        if (red >= green + BROWN_MARGIN && blue <= green - BROWN_MARGIN) {
            System.out.println("brown detected");
            return 2;

        }
        // return: 0 is nothing, 1 is block, 2 is obstacle
        return 0;
    }

    // ULTRASONIC SENSOR RELATED //

    /**
     * Returns the filtered distance between the US sensor and an obstacle in cm.
     */
    public static int readUsDistance(int usId) {
        if (usId == 1) {
            usSensor1.fetchSample(usData1, 0);
            return filter((int) (usData1[0] * 100.0));
        }
        if (usId == 2) {
            usSensor2.fetchSample(usData2, 0);
            return filter((int) (usData2[0] * 100.0));
        }
        return -1;
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

    // GETTERS AND SETTERS

    public static Mode getMode() {
        return Search.MODE;
    }

    public static void setUsData2(float[] usData2) {
        Search.usData2 = usData2;
    }

    public static void setColorSensorDataFront(float[] colorSensorDataFront) {
        Search.colorSensorDataFront = colorSensorDataFront;
    }

    public static ArrayList<Circle> getBlacklistPoint() {
        return Search.blacklistPoint;
    }

    public static ArrayList<Rect> getBlacklistEdge() {
        return Search.blacklistEdge;
    }

}
