package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;

import ca.mcgill.ecse211.playingfield.Point;
import static simlejos.ExecutionController.*;

/**
 * The Navigation class is used to make the robot navigate around the playing field.
 */
public class Navigation {

  // added
  public static double destinationValueX;
  public static double destinationValueY;

  //Light sensors at the back of the robot
  private static float[] colorSensorDataLeft = new float[colorSensorLeft.sampleSize()];
  private static float[] colorSensorDataRight = new float[colorSensorRight.sampleSize()];

  // Initial position of the robot when start() is called.
  //public static Point initialPoint;

  /** Do not instantiate this class. */
  private Navigation() {}

  /**
   * Travels to the given destination.
   * 
   * @param destination Point to which the robot needs to travel to.
   */
  public static void travelTo(Point destination) {
    // Perpendicular traveling
    System.out.println("Traveling to " + destination.toString());

    Point currentX = new Point(odometer.getXyt()[0] / TILE_SIZE, 0);
    Point currentY = new Point(0, odometer.getXyt()[1] / TILE_SIZE);

    Point destinationX = new Point(destination.x, 0);
    Point destinationY = new Point(0, destination.y);

    double angleX = getDestinationAngle(currentX, destinationX);
    double angleY = getDestinationAngle(currentY, destinationY);

    double distanceX = distanceBetween(currentX, destinationX);
    double distanceY = distanceBetween(currentY, destinationY);

    if (distanceX >= 0.2) {
      // Move along the X axis
      setSpeed(ROTATE_SPEED);
      turnTo(angleX);
      setSpeed(FORWARD_SPEED);
      moveStraightFor(distanceX);
    }

    if (distanceY >= 0.2) {
      // Move along the Y axis
      setSpeed(ROTATE_SPEED);
      turnTo(angleY);
      setSpeed(FORWARD_SPEED);
      moveStraightFor(distanceY);
    }

  }

  /**
   * Turns the robot with a minimal angle towards the given input angle in degrees, no matter what its current
   * orientation is. This method is different from {@code turnBy()}.
   * 
   * @param angle the angle to turn to (in degrees)
   */
  public static void turnTo(double angle) {
    System.out.println("turn to: " + angle);
    turnBy(minimalAngle(odometer.getXyt()[2], angle));
  }

  /**
   * Returns the angle that the robot should point towards to face the destination in degrees.
   * 
   * @param current the current point.
   * @param destination the destination point to compute the angle from.
   * @return angle to point towards.
   */
  public static double getDestinationAngle(Point current, Point destination) {
    double x = destination.x - Math.round(current.x);
    double y = destination.y - Math.round(current.y);

    if (x == 0)
      return 90 - Math.signum(y) * 90;

    if (y == 0)
      return Math.signum(x) * 90;

    return 90 - Math.atan(y / x) * 57.3;
  }

  /**
   * Returns the signed minimal angle in degrees from initial angle to destination angle (deg).
   * 
   * @param initialAngle
   * @param destAngle
   * @return the mininal angle needed to turn to the destAngle.
   */
  public static double minimalAngle(double initialAngle, double destAngle) {
    destAngle = destAngle < 0 ? destAngle + 360 : destAngle;
    initialAngle = initialAngle < 0 ? initialAngle + 360 : initialAngle;

    double nAngle = destAngle - initialAngle;

    if (Math.abs(nAngle) > 180)
      nAngle = -Math.signum(nAngle) * 360 + nAngle;

    // System.out.println("Robot must turn by: "+nAngle);

    return nAngle;
  }

  /**
   * Returns the distance between the two points in tile lengths (feet).
   * 
   * @param pi the first point.
   * @param p2 the second point.
   * @return the Euclidean distance bewteen the two points.
   */
  public static double distanceBetween(Point p1, Point p2) {
    return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
  }

   public static void travelCorrected(Point destination) {
    // Getting current robot position and create a Point object in feets from it.
    double[] initialPos = odometer.getXyt();
    Point initialPoint = new Point(initialPos[0], initialPos[1]);
    destination.x *= TILE_SIZE;
    destination.y *= TILE_SIZE;

    // Computing the distance the robot needs to move to push the block to final position.
    double distance = distanceBetween(initialPoint, destination);

    //Setting up sensors
    DifferentialMinimalMargin dmmR = new DifferentialMinimalMargin(LightSensorCalibration.dmm);
    DifferentialMinimalMargin dmmL = new DifferentialMinimalMargin(LightSensorCalibration.dmm);

    float currColorRight = getColorRight();
    dmmR.setPrevValue(currColorRight);

    float currColorLeft = getColorLeft();
    dmmL.setPrevValue(currColorLeft);

    boolean colorSensorRightDetected = false;
    boolean colorSensorLeftDetected = false;

    // Setting motor speeds and move forward
    setSpeed(FORWARD_SPEED);
    moveForward();

    // Loop to keep robot moving forward until distance is reached.
    while (distanceMoved(initialPoint) < distance) {
      currColorRight = getColorRight();
      double diffR = dmmR.differential(currColorRight);
      dmmR.setPrevValue(currColorRight);

      currColorLeft = getColorLeft();
      double diffL = dmmL.differential(currColorLeft);
      dmmL.setPrevValue(currColorLeft);


      if (diffR != 0) {
        stopMotor(1);
        System.out.println("stopping Right");
        colorSensorRightDetected = true;
      }
      if (diffL != 0) {
        stopMotor(0);
        System.out.println("stopping Left");
        colorSensorLeftDetected = true;
      }
      if (colorSensorRightDetected && colorSensorLeftDetected) {
        colorSensorRightDetected = false;
        colorSensorLeftDetected = false;
        System.out.println("Starting Motors");
        setSpeed(FORWARD_SPEED);
        moveForward();
        sleepFor(PHYSICS_STEP_PERIOD*10);
      }
    }

    stopMotors();

  }

  /**
   * Calculates the distance the robot moved from the InitialPoint (class attribute).
   * 
   * @return the distance moved
   */
  public static double distanceMoved(Point initialPoint) {
    // Getting current robot position (in meters) and converting it to Point object
    double[] currentPos = odometer.getXyt();
    Point currentPoint = new Point(currentPos[0], currentPos[1]);
    // getting distance bewteen initialPoint and currentPoint
    double distance = distanceBetween(initialPoint, currentPoint);

    return distance;
  }

  /**
   * Returns the color captured by the right sensor.
   * 
   * @return color of the right sensor
   */
  private static float getColorRight() {
    colorSensorRight.fetchSample(colorSensorDataRight, 0);
    float average = (colorSensorDataRight[0] + colorSensorDataRight[1] + colorSensorDataRight[2]) / 3;
    return average;
  }

  /**
   * Returns the color captured by the right sensor.
   * 
   * @return color of the left sensor
   */
  private static float getColorLeft() {
    colorSensorLeft.fetchSample(colorSensorDataLeft, 0);
    float average = (colorSensorDataLeft[0] + colorSensorDataLeft[1] + colorSensorDataLeft[2]) / 3;
    return colorSensorDataLeft[0];
  }

}