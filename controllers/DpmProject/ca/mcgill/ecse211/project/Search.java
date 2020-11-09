package ca.mcgill.ecse211.project;

import java.util.ArrayList;
import ca.mcgill.ecse211.playingfield.Point;
import ca.mcgill.ecse211.playingfield.Circle;
import ca.mcgill.ecse211.playingfield.Rect;
import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;

public class Search {

    /** Buffer (array) to store US samples. */
    private static float[] usData = new float[usSensor.sampleSize()];

    //Buffer size used to receive US sensor readings and perform filtering.
    private static int BUFFER_SIZE = 3;

    //Buffer array to store sensor readings and perform filtering.
    private static int[] buffer = new int[BUFFER_SIZE];

    /**
     * The limit of invalid samples that we read from the US sensor before assuming
     * no obstacle.
     */
    public static final int INVALID_SAMPLE_LIMIT = 20;
    /** The distance remembered by the filter() method. */
    private static int prevDistance;
    /** The number of invalid samples seen by filter() so far. */
    private static int invalidSampleCount;

    /** Maximal distance between the robot and an object */
    private static final double DISTANCE_THREESHOLD = TILE_SIZE;

    /** Linked to edge bounding box width in tile size */
    private static final double EDGE_BOUND_WIDTH = 1;

    /**
     * Distance between the center of the robot and the front of the usSensor in cm
     */
    private static final double DIST_US_SENSOR = 10;

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

    /** Controls the number of scans performed within a distance
     * during @Code hasDangerWithin().
     * The higher the value, the more precise the scan.
     */
    private static double SCAN_FREQUENCY = 3;

    public static void initializeSearch() {
        // TODO : Transform edges to rectangles and add to blacklist
        blacklistEdge.add(creatRectFromEdge(new Point(6, 5), new Point(15, 5)));
        blacklistEdge.add(creatRectFromEdge(new Point(6, 9), new Point(15, 9)));
        blacklistEdge.add(creatRectFromEdge(new Point(6, 5), new Point(6, 9)));

    }

    public static void doSearch() {

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

            if (hasSpotedNewOject()) {
                System.out.println("Object detected");
                break;
            }

            if (hasFullyRotated()) {
                /** If no near object is detected, find a secure place to navigate to */
                System.out.println("Could not find near object");
                while (hasDangerWithin((int) (DISTANCE_THREESHOLD * 100)))
                    rotateClockwise();
                moveStraightFor(DISTANCE_THREESHOLD / TILE_SIZE);
                doSearch();
            }
        }

        stopMotors();

    }

    private static Rect creatRectFromEdge(Point pt1, Point pt2) {
        double centerY;
        double centerX;
        double height;
        double width;

        /** If the line is vertical */
        if (pt1.x == pt2.x) {
            centerY = pt1.y > pt2.y ? (pt1.y - pt2.y) / 2 + pt2.y : (pt2.y - pt1.y) / 2 + pt1.y;
            centerX = pt1.x;
            height = pt1.y > pt2.y ? (pt1.y - pt2.y) : (pt2.y - pt1.y);
            width = EDGE_BOUND_WIDTH;
        } else {
            centerX = pt1.x > pt2.x ? (pt1.x - pt2.x) / 2 + pt2.x : (pt2.x - pt1.x) / 2 + pt1.x;
            centerY = pt1.y;
            height = EDGE_BOUND_WIDTH;
            width = pt1.x > pt2.x ? (pt1.x - pt2.x) : (pt2.x - pt1.x);
        }

        return new Rect(new Point(centerX, centerY), width, height);
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

    private static void addToBlackList(Point point) {
        blacklistPoint.add(new Circle(point));
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

        System.out.println("crt.x = " + crt.x);
        System.out.println("crt.y = " + crt.y);
        System.out.println("angle = " + angle);
        System.out.println("hypo = " + (hypotenuse));

        double dx = Math.sin(Math.toRadians(angle)) * (hypotenuse + DIST_US_SENSOR); // x displacement
        double dy = Math.cos(Math.toRadians(angle)) * (hypotenuse + DIST_US_SENSOR); // y displacement

        System.out.println("dx = " + dx);
        System.out.println("dy = " + dy);

        Point npt = new Point(crt.x + dx / (TILE_SIZE * 100), crt.y + dy / (TILE_SIZE * 100));

        System.out.println("Point seen = " + npt);

        for (Circle point : blacklistPoint) {
            if (point.contains(npt))
                return true;
        }
        for (Rect edge : blacklistEdge) {
            System.out.println(edge);
            if (edge.contains(npt))
                return true;
        }
        return false;
    }

    /**
     * Returns true if the robot has spotted an non-blacklisted object, false
     * otherwise.
     * 
     * @return boolean
     */
    private static boolean hasSpotedNewOject() {
        int hypotenuse = tapeReader();
        if (hypotenuse < DISTANCE_THREESHOLD && !isBlackListed(hypotenuse, getCurrentAngle()))
            return true;
        return false;
    }

    /**
     * Returns true if no danger is within a certain distance, false otherwise.
     * 
     * @return boolean
     */
    private static boolean hasDangerWithin(double hypotenuse) {
        double hyp = hypotenuse;
        while (hyp > 0) {
        if (isBlackListed(hyp, getCurrentAngle()))
            return true;
        hyp -= hypotenuse * (1 / SCAN_FREQUENCY);
        }
        return false;
    }

        /**
    * Method which implements a basic tape technique to fill and update the buffer.
    * This method is called by the ...Edge() methods to get a new filtered sensor reading.
    *
    * @return the median of the filtered buffer.
    */
    private static int tapeReader() {

        //if the array is empty, fill it up to be able to compute filtering.
        if (buffer[0] == 0) {
            for (int i = 0; i < BUFFER_SIZE; i++) {
                buffer[i] = getSensorValue(); //get new sensor reading.
            }
        }
        //shift elements tto right to then add a new sensor reading (tape mechanism).
        else {
            for (int j = BUFFER_SIZE-1; j > 0; j--) {
                buffer[j] = buffer[j-1];
            }
            buffer[0] = getSensorValue(); //get new sensor reading.
        }

        int value = medianFilter(buffer); //get the median of the current buffer.

        //System.out.println("median: "+value);
        return value;

    }


    /**
    * This methods calls the SampleProvider methods to receive a new reading from the US sensor
    * and perform some "filtering" to ensure a correct reading is being returned.
    *
    * @return a corrected value of the sensor reading.
    */
    private static int getSensorValue() {
        
        usSensor.fetchSample(usData, 0);
        int distance = (int)(usData[0]*100); 
        /*Ccale sensor reading to cm. and typecast to int.
        //We do not necessarily need double values for the distance here as it's the difference between
        that value and the noise margin threshold which is of interest.*/

        if (distance >= MAX_SENSOR_DIST && invalidSampleCount < INVALID_SAMPLE_LIMIT) {
            // bad value, increment the filter value and return the distance remembered from before
            invalidSampleCount++;
            return prevValue;
        } else {
            if (distance < MAX_SENSOR_DIST) {
                invalidSampleCount = 0; // reset filter and remember the input distance.
            }
            prevValue = distance;
            return distance;
        }
    }


    /**
    * This method implements a basic median filter as seen in the lectures.
    *
    * @param arr buffer array to compute median on.
    * @return the median of the array.
    */
    private static int medianFilter(int[] arr) {
        int[] temp = new int[BUFFER_SIZE];

        for (int i = 0; i < BUFFER_SIZE; i++) {
            temp[i] = arr[i];
        }
        Arrays.sort(temp);

        int median = temp[BUFFER_SIZE/2];

        return median;
    }


    /*
    * This method is used to reset the buffer when needed.
    */
    private static void clearBuffer() {

        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer[i] = 0;
        }
    }

}
