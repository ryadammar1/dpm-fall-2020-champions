package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;
import java.util.ArrayList;
import ca.mcgill.ecse211.playingfield.Point;

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
    
    private static boolean isCageClosed = false;

    // TODO: Handle the case when the block is stuck inside the cage
    // TODO: Obstacle avoidance around other bin and ramp
    public static void doTransfer() {
      /**
       * Secure block
       */
    
      obstacleavoidance.pause();
       setSpeed(FORWARD_SPEED/2);
       while(readUsDistance()>=0.1)
           moveForward();
       
       Utils.stopMotors();
       
       if (!isCageClosed) {
         cageMotor.setSpeed(60);
         cageMotor.rotate(180, false);  
       }
       
     obstacleavoidance.resume();
      Point midPoint = new Point((ramp.left.x + ramp.right.x) / 2, (ramp.left.y + ramp.right.y) / 2);
      ArrayList<Point> path;
      
      if (Resources.ramp == Resources.rr) {
          Point tail = new Point(midPoint.x + Resources.rFacingX, midPoint.y + Resources.rFacingY);
          path = PathPlanning.plan(Utils.getCurrentPosition(), tail, midPoint);
      } else {
          Point tail = new Point(midPoint.x + Resources.gFacingX, midPoint.y + Resources.gFacingY);
          path = PathPlanning.plan(Utils.getCurrentPosition(), tail, midPoint);
      }

      for (Point p : path) {
          System.out.println(p);
          Navigation.travelToImmReturn(p);
      }
      
      obstacleavoidance.pause();
      
      Point destination;
      if (Resources.ramp == Resources.rr) {
          destination = new Point(midPoint.x + Resources.rFacingX, midPoint.y + Resources.rFacingY);
      } else {
          destination = new Point(midPoint.x + Resources.gFacingX, midPoint.y + Resources.gFacingY);
      }

      Navigation.travelToImmReturn(destination);

      Navigation.moveStraightFor(-1.5);

      cageMotor.setSpeed(60);
      cageMotor.rotate(-180, false);
      isCageClosed = false;
      
      obstacleavoidance.resume();
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
