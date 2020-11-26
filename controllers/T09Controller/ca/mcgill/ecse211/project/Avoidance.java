package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;

import java.util.ArrayList;
import ca.mcgill.ecse211.playingfield.Point;
import ca.mcgill.ecse211.playingfield.Circle;
import ca.mcgill.ecse211.playingfield.Rect;

public class Avoidance {

	/**
     * Offset between the center of the robot and the front of the usSensor in cm
     */
    private static final double DIST_US_SENSOR_Y = 9;
    private static final double DIST_US_SENSOR_X = 0;

	// These arrays are used to avoid creating new ones at each iteration.
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

	/** Threshold to trigger obstacle avoidance corrections. in cm. */
	private static int THRESHOLD = 33;

	/** Sensor to use. (can be changed depending on search mode). */
	public static int SENSOR = 1;

	//** blacklists based on Search.
	private static ArrayList<Circle> blacklistPoint = new ArrayList<Circle>();
    private static ArrayList<Rect> blacklistEdge = new ArrayList<Rect>();

    public static void initializeAvoidance() {

    	blacklistPoint = Search.getBlacklistPoint();
    	blacklistEdge = Search.getBlacklistEdge();

    	//Initial island edges
    	// bottom wall
        blacklistEdge.add(Rect.creatRectFromEdge(new Point(startZone.ll.x, startZone.ll.y), new Point(startZone.ur.x, startZone.ll.y)));
        // top wall
        blacklistEdge.add(Rect.creatRectFromEdge(new Point(startZone.ll.x, startZone.ur.y), new Point(startZone.ur.x, startZone.ur.y)));
        // left wall
        blacklistEdge.add(Rect.creatRectFromEdge(new Point(startZone.ll.x, startZone.ll.y), new Point(startZone.ll.x, startZone.ur.y)));
        // right wall
        blacklistEdge.add(Rect.creatRectFromEdge(new Point(startZone.ur.x, startZone.ll.y), new Point(startZone.ur.x, startZone.ur.y)));

    }

	/**
	 * Method to decide which direction to go around the obstacle
	 */
	public static void correct() {
		stopMotors();
		System.out.println("First turn");
		setSpeed(ROTATE_SPEED);
		turnBy(90.0);
		int reading = readUsDistance(SENSOR);
		System.out.println(reading);
		// If other obstacle detected at 90 degrees
		int direction = 1;
		boolean wrongDirection = isBlackListed();
		if (reading <= THRESHOLD || wrongDirection == true) {
			// Try in the other side
			System.out.println("Second turn");
			turnBy(-180.0);
			int reading2 = readUsDistance(SENSOR);
			System.out.println(reading2);
			// If both sides are not possible, move back and start again
			direction = -1;
			wrongDirection = isBlackListed();
			if (reading2 <= THRESHOLD || wrongDirection == true) {
				turnBy(90.0);
				moveStraightFor(-1.0);
				System.out.println("Impossible... going back a bit");
				correct();
			}
		}
		setSpeed(FORWARD_SPEED);
		moveStraightFor(1.0);
		setSpeed(ROTATE_SPEED);
		turnBy(direction * -90.0);

		System.out.println("Done correcting");

		stopMotors();
	}


	/**
     * Compute an approximation of an obstacle's coordinate base on the robot's
     * current angle and at a distance of 1 tile. Returns true if the
     * point is near the walls or an already balcklisted point, false otherwise.
     * Circles and rectangles are used instead of points and lines to approximate
     * the point's location.
     * 
     * @return boolean (true: object is blacklisted, false: not blacklisted)
     */
    private static boolean isBlackListed() {

    	System.out.println(blacklistEdge);

        Point crt = getCurrentPosition();
        double angle = getCurrentAngle();
        double hypotenuse = 25;

        double dx = DIST_US_SENSOR_X + Math.sin(Math.toRadians(angle)) * (hypotenuse + DIST_US_SENSOR_Y); // x
                                                                                                          // displacement
        double dy = Math.cos(Math.toRadians(angle)) * (hypotenuse + DIST_US_SENSOR_Y); // y displacement

        Point npt = new Point(crt.x + dx / (TILE_SIZE * 100), crt.y + dy / (TILE_SIZE * 100));

        /*
         * Debug:
        System.out.println("angle = " + angle); System.out.println("hypo = " +
        (hypotenuse + DIST_US_SENSOR_Y)); System.out.println("dx = " + dx + DIST_US_SENSOR_X); System.out.println("dy = " + dy);
        System.out.println("Point curr = " + crt); System.out.println("Point seen = " + npt);
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
	 * Returns the filtered distance between the US sensor and an obstacle in cm.
	 */
	public static int readUsDistance(int usId) {
		if (usId == 1) {
			for (int i = 0; i < 3; i++) {
				usSensor1.fetchSample(usData, 0);
			}
			usSensor1.fetchSample(usData, 0);
			return filter((int) (usData[0] * 100.0));
		}
		if (usId == 2) {
			for (int i = 0; i < 3; i++) {
				usSensor2.fetchSample(usData, 0);
			}
			usSensor2.fetchSample(usData, 0);
			return filter((int) (usData[0] * 100.0));
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
}
