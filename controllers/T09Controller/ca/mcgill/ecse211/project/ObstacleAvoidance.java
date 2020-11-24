package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;
import static simlejos.ExecutionController.waitUntilNextStep;

public class ObstacleAvoidance implements Runnable {

	/** The singleton odometer instance. */
	private static ObstacleAvoidance avoider;

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
	private static int THRESHOLD = 10;

	/** Sensor to use. (can be changed depending on search mode). */
	public static int SENSOR = 1;

	/** Boolean to enable obstacle avoidance or not. */
	public static boolean ENABLED = false;


	/**
	 * Initializes the obstacle avoidance depending on the search mode.
	 * Selects which sensor to use.
	 */
	public static void initializeObstacleAvoidance(){
		if (Search.getMode() == Search.Mode.Recognize){
			SENSOR = 2;
			usData = new float[usSensor2.sampleSize()];
		}
	}

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
			if (ENABLED == true) {
				int reading = readUsDistance(SENSOR);
				if (reading <= THRESHOLD) {
					Main.STATE_MACHINE.setBlockDetected(false);
					Main.STATE_MACHINE.detectObstacle();
					System.out.println("Obstacle detected...." + reading);
					ENABLED = false;
					//return;
				}
			}
			waitUntilNextStep();
		}

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

	/**
	 * Method to pause the obstacle avoidance system
	 */
	public void pause() {
		ENABLED = false;
	}

	public void resume() {
		ENABLED = true;
	}
}
