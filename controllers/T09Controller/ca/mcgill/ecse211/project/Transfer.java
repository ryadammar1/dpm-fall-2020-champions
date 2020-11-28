package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ca.mcgill.ecse211.playingfield.Point;
import ca.mcgill.ecse211.playingfield.RampRect;
import ca.mcgill.ecse211.playingfield.Rect;

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
       while(readUsDistance()>0)
           moveForward();
       Utils.stopMotors();
       
       if (!isCageClosed) {
         cageMotor.setSpeed(60);
         cageMotor.rotate(180, false);
         isCageClosed = true;
       }
     obstacleavoidance.pause();
     
      Point midPoint = new Point((ramp.left.x + ramp.right.x) / 2, (ramp.left.y + ramp.right.y) / 2);
      
      Point pushTo;
      if (Resources.ramp == Resources.rr) {
        pushTo = new Point(midPoint.x + Resources.rFacingX, midPoint.y + Resources.rFacingY);
      } else {
        pushTo = new Point(midPoint.x + Resources.gFacingX, midPoint.y + Resources.gFacingY);
      }
      
      Point pushFrom;
      if (Resources.ramp == Resources.rr) {
        pushFrom = new Point(midPoint.x - Resources.rFacingX * 0.5, midPoint.y - Resources.rFacingY * 0.5);
      } else {
        pushFrom = new Point(midPoint.x - Resources.gFacingX * 0.5, midPoint.y - Resources.gFacingY * 0.5);
      }
      
     RampRect bb;
     if (Resources.ramp == Resources.rr) {
       bb = Resources.rrbb;
     } else {
       bb = Resources.grbb;
     }
     
     List<Point> cornersInIsland = Arrays.asList(bb.corners());
     cornersInIsland.sort((Point p1, Point p2) -> (int)Math.signum(Navigation.distanceBetween(Utils.getCurrentPosition(), p1) - Navigation.distanceBetween(Utils.getCurrentPosition(), p2)));
     Navigation.travelToImmReturn(cornersInIsland.get(0));
     
     if (Main.STATE_MACHINE.getStatusFullName() == "Avoidance")
       return;
     
     if ((new Rect(Resources.searchZone).contains(bb.getFrontLeft()))) {
       Navigation.travelToPerpendicular(bb.getFrontLeft());
     } else {
       Navigation.travelToPerpendicular(bb.getFrontRight());
     }
     
     Navigation.travelTo(pushFrom);
      
      if (Main.STATE_MACHINE.getStatusFullName() == "Avoidance")
        return;
      
      obstacleavoidance.pause();

      Navigation.travelTo(pushTo); 

    Point pushFrom;
    if (Resources.ramp == Resources.rr) {
      pushFrom = new Point(midPoint.x - Resources.rFacingX * 0.5, midPoint.y - Resources.rFacingY * 0.5);
    } else {
      pushFrom = new Point(midPoint.x - Resources.gFacingX * 0.5, midPoint.y - Resources.gFacingY * 0.5);
      
      Navigation.travelTo(pushFrom);
      
      obstacleavoidance.pause();
      
      Main.STATE_MACHINE.blockTransfered();
    }
    Navigation.travelToImmReturn(pushFrom);

    if (Main.STATE_MACHINE.getStatusFullName() == "Avoidance")
      return;

    obstacleavoidance.pause();

    Point pushTo;
    if (Resources.ramp == Resources.rr) {
      pushTo = new Point(midPoint.x + Resources.rFacingX, midPoint.y + Resources.rFacingY);
    } else {
      pushTo = new Point(midPoint.x + Resources.gFacingX, midPoint.y + Resources.gFacingY);
    }

    Navigation.travelToImmReturn(pushTo);

    CageController.openCage();
    isCageClosed = false;

    //Navigation.moveStraightFor(-1.5);
    Navigation.moveStraightFor(-0.5);
    Navigation.turnBy(180);
    Navigation.travelToPerpendicular(pushFrom);
    
    obstacleavoidance.resume();
    
    Main.STATE_MACHINE.blockTransfered();
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
