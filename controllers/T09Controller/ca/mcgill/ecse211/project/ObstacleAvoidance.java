package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;
import static simlejos.ExecutionController.waitUntilNextStep;

public class ObstacleAvoidance implements Runnable {

	/** The singleton odometer instance. */
	private static ObstacleAvoidance avoider;

	// These arrays are used to avoid creating new ones at each iteration.
	/** Buffer (array) to store US samples. */
	private static float[] usData2 = new float[usSensor2.sampleSize()];
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
	private static int THRESHOLD = 20;

	/** Sensor to use. (can be changed depending on state). */
	public static int SENSOR = 2;

	/** Boolean to enable obstacle avoidance or not. */
	public static boolean PAUSE = true;

	/**
	 * Returns the Odometer Object. Use this method to obtain an instance of
	 * Odometer.
	 */
	public static synchronized ObstacleAvoidance getAvoider() {
		if (avoider == null) {
			avoider = new ObstacleAvoidance();
		}
		return avoider;
	}

	/**
	 * Main method run in a separate thread. Monitors second ultrasonic sensor and
	 * triggers state machine's "detect obstacle" event when the reading is under a
	 * set threshold.
	 */
	@Override
	public void run() {
		System.out.println("Running obstacle avoidance..");
		while (true) {
			if (PAUSE == false) {
				int reading = readUsDistance(SENSOR);
				if (reading <= THRESHOLD) {
					Main.STATE_MACHINE.setBlockDetected(false);
					Main.STATE_MACHINE.detectObstacle();
					System.out.println("Obstacle detected...." + reading);
				}

			}
			waitUntilNextStep();
		}

	}

	/**
	 * Method to decide which direction to go around the obstacle
	 */
	public static void correct() {
		setSpeed(ROTATE_SPEED);
		turnBy(90.0);
		int reading = readUsDistance(SENSOR);
		// If other obstacle detected at 90 degrees
		if (reading <= THRESHOLD) {
			// Try in the other side
			turnBy(-180.0);
			int reading2 = readUsDistance(SENSOR);
			// If both sides are not possible, move back and start again
			if (reading2 <= THRESHOLD) {
				turnBy(90.0);
				moveStraightFor(-1.0);
				System.out.println("Impossible... going back a bit");
				correct();
			}
			System.out.println("goAround(2)");
			goAround(2);
		}

		System.out.println("goAround(1)");
		goAround(1);

		Main.STATE_MACHINE.obstacleAvoided();
	}

	/**
	 * Method to move the robot around the obstacle
	 * 
	 * @param direction direction in which the correction is done. (1:clockwise,
	 *                  2:counter-clockwise)
	 */
	private static void goAround(int direction) {
		int coefficient = 0;
		if (direction == 1) {
			coefficient = -1;
		} else {
			coefficient = 1;
		}
		setSpeed(FORWARD_SPEED);
		moveStraightFor(1.0);
		turnBy(coefficient * 90.0);
		moveStraightFor(2.0);
		turnBy(coefficient * 90.0);
		moveStraightFor(1.0);
		turnBy(coefficient * (-90.0));

	}

	/**
	 * Returns the filtered distance between the US sensor and an obstacle in cm.
	 */
	public static int readUsDistance(int usId) {
		if (usId == 1) {
			for (int i = 0; i < 3; i++) {
				usSensor1.fetchSample(usData2, 0);
			}
			usSensor1.fetchSample(usData2, 0);
			return filter((int) (usData2[0] * 100.0));
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

	/**
	 * Method to pause the obstacle avoidance system
	 */
	public void pause() {
		PAUSE = true;
	}

	public void resume() {
		PAUSE = false;
	}
}
