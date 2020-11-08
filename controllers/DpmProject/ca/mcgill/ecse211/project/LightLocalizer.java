package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;


public class LightLocalizer {

  private static float[] colorSensorDataLeft = new float[colorSensorLeft.sampleSize()];
  private static float[] colorSensorDataRight = new float[colorSensorRight.sampleSize()];

  
  /**
   * Method to localize point (1,1) using the light sensor.
   */
  public static void localize() {

    //Detecting the first line and turning by 90 degrees
    detection(90);
    //Detecting the second line and turning by -90 degrees
    detection(-90);

  }

  /**
   * Detects black line.
   * @param angleOfRotation angle by which to turn by
   */
  public static void detection(int angleOfRotation) {
    setSpeed(LOWER_FORWARD_SPEED);
    moveForward();

    boolean colorSensorRightDetected = false;
    boolean colorSensorLeftDetected = false;
    DifferentialMinimalMargin dmmR = new DifferentialMinimalMargin(LightSensorCalibration.dmm);
    DifferentialMinimalMargin dmmL = new DifferentialMinimalMargin(LightSensorCalibration.dmm);

    float currColorRight = getColorRight();
    dmmR.setPrevValue(currColorRight);
	 
    float currColorLeft = getColorLeft();
    dmmL.setPrevValue(currColorLeft);
	
    while (true) {
    	currColorRight = getColorRight();
    	double diffR = dmmR.differential(currColorRight);
    	dmmR.setPrevValue(currColorRight);
    	
    	currColorLeft = getColorLeft();
    	double diffL = dmmL.differential(currColorLeft);
    	dmmL.setPrevValue(currColorLeft);
      
      
      if (diffR < 0) {
        stopMotor(1);
        colorSensorRightDetected = true;
      }
      if (diffL < 0) {
        stopMotor(0);
        colorSensorLeftDetected = true;
      }
      if (colorSensorRightDetected && colorSensorLeftDetected) {
        break;
      }
    }
    stopMotors();
    setSpeed(LOWER_FORWARD_SPEED);
    moveStraightFor((-BASE_WIDTH / 2)/TILE_SIZE);
    turnBy(angleOfRotation);
  }
  
  
  /**
   * Returns the color captured by the right sensor.
   * @return color of the right sensor
   */
  public static float getColorRight() {
    colorSensorRight.fetchSample(colorSensorDataRight, 0);
    return (colorSensorDataRight[0]+colorSensorDataRight[1]+colorSensorDataRight[2])/3;
  }
  
  /**
   * Returns the color captured by the right sensor.
   * @return color of the left sensor
   */
  public static float getColorLeft() {
    colorSensorLeft.fetchSample(colorSensorDataLeft, 0);
    return (colorSensorDataLeft[0]+colorSensorDataLeft[1]+colorSensorDataLeft[2])/3;
  }
}
