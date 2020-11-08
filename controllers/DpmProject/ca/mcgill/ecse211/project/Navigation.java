package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static java.lang.Math.*;
import static ca.mcgill.ecse211.project.Utils.*;
import static simlejos.ExecutionController.*;

import ca.mcgill.ecse211.playingfield.Point;

public class Navigation {

  /** Do not instantiate this class. */
  private Navigation() {
  }

  // added
  public static double destinationValueX;
  public static double destinationValueY;

  // Light sensors at the back of the robot
  private static float[] colorSensorDataLeft = new float[colorSensorLeft.sampleSize()];
  private static float[] colorSensorDataRight = new float[colorSensorRight.sampleSize()];

  /** Travels to the given destination. */
  public static void travelTo(Point destination) {
    var xyt = odometer.getXyt();
    var currentLocation = new Point(xyt[0] / TILE_SIZE, xyt[1] / TILE_SIZE);
    var currentTheta = xyt[2];
    var destinationTheta = getDestinationAngle(currentLocation, destination);
    turnBy(minimalAngle(currentTheta, destinationTheta));
    moveStraightFor(distanceBetween(currentLocation, destination));
  }

  /**
   * Returns the angle that the robot should point towards to face the destination
   * in degrees.
   */
  public static double getDestinationAngle(Point current, Point destination) {
    return (toDegrees(atan2(destination.x - current.x, destination.y - current.y)) + 360) % 360;
  }

  /**
   * Returns the signed minimal angle from the initial angle to the destination
   * angle.
   */
  public static double minimalAngle(double initialAngle, double destAngle) {
    var dtheta = destAngle - initialAngle;
    if (dtheta < -180) {
      dtheta += 360;
    } else if (dtheta > 180) {
      dtheta -= 360;
    }
    return dtheta;
  }

  /** Returns the distance between the two points in tile lengths. */
  public static double distanceBetween(Point p1, Point p2) {
    var dx = p2.x - p1.x;
    var dy = p2.y - p1.y;
    return sqrt(dx * dx + dy * dy);
  }

  // TODO Bring Navigation-related helper methods from Labs 2 and 3 here

  /**
   * Moves the robot straight for the given distance.
   * 
   * @param distance in feet (tile sizes), may be negative
   */
  public static void moveStraightFor(double distance) {
    setSpeed(FORWARD_SPEED);
    leftMotor.rotate(convertDistance(distance * TILE_SIZE), true);
    rightMotor.rotate(convertDistance(distance * TILE_SIZE), false);
  }

  /** Moves the robot forward for an indeterminate distance. */
  public static void forward() {
    setSpeed(FORWARD_SPEED);
    leftMotor.forward();
    rightMotor.forward();
  }

  /** Moves the robot backward for an indeterminate distance. */
  public static void backward() {
    setSpeed(FORWARD_SPEED);
    leftMotor.backward();
    rightMotor.backward();
  }

  /**
   * Turns the robot by a specified angle. Note that this method is different from
   * {@code turnTo()}. For example, if the robot is facing 90 degrees, calling
   * {@code turnBy(90)} will make the robot turn to 180 degrees, but calling
   * {@code turnTo(90)} should do nothing (since the robot is already at 90
   * degrees).
   * 
   * @param angle the angle by which to turn, in degrees
   */
  public static void turnBy(double angle) {
    setSpeed(ROTATE_SPEED);
    leftMotor.rotate(convertAngle(angle), true);
    rightMotor.rotate(-convertAngle(angle), false);
  }

  /** Rotates motors clockwise. */
  public static void clockwise() {
    setSpeed(ROTATE_SPEED);
    leftMotor.forward();
    rightMotor.backward();
  }

  /** Rotates motors counterclockwise. */
  public static void counterclockwise() {
    setSpeed(ROTATE_SPEED);
    leftMotor.backward();
    rightMotor.forward();
  }

  /** Stops both motors. This also resets the motor speeds to zero. */
  public static void stopMotors() {
    leftMotor.stop();
    rightMotor.stop();
  }

  /**
   * Converts input distance to the total rotation of each wheel needed to cover
   * that distance.
   * 
   * @param distance the input distance in meters
   * @return the wheel rotations necessary to cover the distance in degrees
   */
  public static int convertDistance(double distance) {
    return (int) toDegrees(distance / WHEEL_RAD);
  }

  /**
   * Converts input angle to total rotation of each wheel needed to rotate robot
   * by that angle.
   * 
   * @param angle the input angle in degrees
   * @return the wheel rotations (in degrees) necessary to rotate the robot by the
   *         angle
   */
  public static int convertAngle(double angle) {
    return convertDistance(toRadians((BASE_WIDTH / 2) * angle));
  }

  /**
   * Sets the speed of both motors to the same values.
   * 
   * @param speed the speed in degrees per second
   */
  public static void setSpeed(int speed) {
    setSpeeds(speed, speed);
  }

  /**
   * Sets the speed of both motors to different values.
   * 
   * @param leftSpeed  the speed of the left motor in degrees per second
   * @param rightSpeed the speed of the right motor in degrees per second
   */
  public static void setSpeeds(int leftSpeed, int rightSpeed) {
    leftMotor.setSpeed(leftSpeed);
    rightMotor.setSpeed(rightSpeed);
  }

  /**
   * Sets the acceleration of both motors.
   * 
   * @param acceleration the acceleration in degrees per second squared
   */
  public static void setAcceleration(int acceleration) {
    leftMotor.setAcceleration(acceleration);
    rightMotor.setAcceleration(acceleration);
  }

  public static void travelCorrected(Point destination) {
    // Getting current robot position and create a Point object in feets from it.
    double[] initialPos = odometer.getXyt();
    Point initialPoint = new Point(initialPos[0], initialPos[1]);
    destination.x *= TILE_SIZE;
    destination.y *= TILE_SIZE;

    // Computing the distance the robot needs to move to push the block to final
    // position.
    double distance = distanceBetween(initialPoint, destination);

    // Setting up sensors
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
        sleepFor(PHYSICS_STEP_PERIOD * 10);
      }
    }

    stopMotors();

  }

  /**
   * Calculates the distance the robot moved from the InitialPoint (class
   * attribute).
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
