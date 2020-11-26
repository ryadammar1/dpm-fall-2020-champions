package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;


public class UltrasonicLocalizer {

  // These arrays are used to avoid creating new ones at each iteration.
  /** Buffer (array) to store US samples. */
  private static float[] usData = new float[usSensor1.sampleSize()];
  /** The limit of invalid samples that we read from the US sensor before assuming no obstacle. */
  public static final int INVALID_SAMPLE_LIMIT = 20;
  /** The distance remembered by the filter() method. */
  private static int prevDistance;
  /** The number of invalid samples seen by filter() so far. */
  private static int invalidSampleCount;
  /** Angle A */
  private static double angleA;
  /** Angle B */
  private static double angleB;

  /**
   * WHAT IT DOES:
   * This method uses the odometer and the ultrasonic sensor to align the robot towards the 'north'.
   * HOW IT WORKS:
   */
  public static void localize() {
      
      // reset the motors
      stopMotors();
      setAcceleration(ACCELERATION);
      setSpeed(ROTATE_SPEED);

      // Sleep for 2 seconds
      try {
        Thread.sleep(TIMEOUT_PERIOD);
      } catch (InterruptedException e) {
      }

      // Rotate the robot until it detects no wall and saves the current angle computed by the odometer
      while (readUsDistance() < 30) {
        rotateClockwise();
      }
      
      while (readUsDistance() > 20) {
        rotateCounterClockwise();
      }

      // Save the angle
      angleA = odometer.getXyt()[2];

      // Rotate until the wall is out of reach
      while (readUsDistance() < 60) {
        rotateClockwise();
      }
      
      // Rotate the robot clockwise until it detects a wall and saves the current angle
      while (readUsDistance() > 30) {
        rotateClockwise();
      }
      
      // Save the angle
      angleB = odometer.getXyt()[2];

      if (angleA > angleB)
        angleA = angleA - 360;
      
      // Calculate the angle required to rotate the robot towards the north and perform the rotation
      // The robot "undershoots", so subtract an additional 2.5 to compensate
      double angleC = (angleA + angleB)/2 - angleB - 45;
     
      turnBy(angleC);

      stopMotors();

  }

  /** Returns the filtered distance between the US sensor and an obstacle in cm. */
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
      // bad value, increment the filter value and return the distance remembered from before
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
