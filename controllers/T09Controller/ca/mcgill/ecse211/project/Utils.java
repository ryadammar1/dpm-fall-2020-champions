package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import ca.mcgill.ecse211.playingfield.Point;

public class Utils {

  /**
   * Moves the robot straight for the given distance.
   * 
   * @param distance in feet (tile sizes), may be negative
   */
  public static void moveStraightFor(double distance) {
    int convertedDistance = convertDistance(distance * TILE_SIZE);

    leftMotor.rotate(convertedDistance, true);
    rightMotor.rotate(convertedDistance, false);
  }

  /**
   * Gets the current position of the robot from the odometer.
   * 
   * @return current coordinate of the robot
   */
  public static Point getCurrentPosition() {
    return new Point(odometer.getXyt()[0] / TILE_SIZE, odometer.getXyt()[1] / TILE_SIZE);
  }

  /**
   * Gets the current angle of the robot from the odometer.
   * 
   * @return current angle of the robot
   */
  public static double getCurrentAngle() {
    return odometer.getXyt()[2];
  }

  /**
   * Turns the robot by a specified angle. Note that this method is different from
   * {@code Navigation.turnTo()}. For example, if the robot is facing 90 degrees,
   * calling {@code turnBy(90)} will make the robot turn to 180 degrees, but
   * calling {@code Navigation.turnTo(90)} should do nothing (since the robot is
   * already at 90 degrees).
   * 
   * @param angle the angle by which to turn, in degrees
   */
  public static void turnBy(double angle) {
    int convertedAngle = convertAngle(angle);

    leftMotor.rotate(convertedAngle, true);
    rightMotor.rotate(-convertedAngle, false);
  }

  /**
   * Converts input distance to the total rotation of each wheel needed to cover
   * that distance.
   * 
   * @param distance the input distance in meters
   * @return the wheel rotations necessary to cover the distance
   */
  public static int convertDistance(double distance) {
    distance = distance * 1000;
    double wheel_rad = WHEEL_RAD * 1000;

    double converted_distance = (((180 * distance) / (Math.PI * wheel_rad)) * 1000);
    int convertedDistance = (int) converted_distance;
    return convertedDistance / 1000;
  }

  /**
   * Converts input angle to the total rotation of each wheel needed to rotate the
   * robot by that angle.
   * 
   * @param angle the input angle in degrees
   * @return the wheel rotations necessary to rotate the robot by the angle
   */
  public static int convertAngle(double angle) {
    return convertDistance(Math.PI * BASE_WIDTH * angle / 360.0);
  }

  /**
   * Stops both motors.
   */
  public static void stopMotors() {
    leftMotor.stop(true);
    rightMotor.stop(false);
  }

  /**
   * Stops one motor.
   * 
   * @param motor int value to choose motor (0:left, 1:right)
   */
  public static void stopMotor(int motor) {
    if (motor == 0) {
      leftMotor.stop(true);
    } else {
      rightMotor.stop(true);
    }
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

  /**
   * Moves the robot forwards.
   */
  public static void moveForward() {
    leftMotor.forward();
    rightMotor.forward();
  }

  /**
   * Moves the robot backwards.
   */
  public static void moveBackward() {
    leftMotor.backward();
    rightMotor.backward();
  }

  /**
   * Rotates the robot Clockwise.
   */
  public static void rotateClockwise() {
    setSpeed(ROTATE_SPEED);
    leftMotor.forward();
    rightMotor.backward();
  }

  /**
   * Rotates the robot CounterClockwise.
   */
  public static void rotateCounterClockwise() {
    setSpeed(ROTATE_SPEED);
    leftMotor.backward();
    rightMotor.forward();
  }

  /**
   * Reads torque from both motors.
   * 
   * @return average torque from both L and R motors.
   */
  public static double getTorque() {
    double leftTorque = leftMotor.getTorque();
    double rightTorque = rightMotor.getTorque();

    return (leftTorque + rightTorque) / 2;
  }

}
