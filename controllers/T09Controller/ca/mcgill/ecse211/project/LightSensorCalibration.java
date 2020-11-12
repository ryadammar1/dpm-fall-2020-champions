package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Utils.*;
import static ca.mcgill.ecse211.project.LightLocalizer.*;
import static ca.mcgill.ecse211.project.Navigation.distanceBetween;

import ca.mcgill.ecse211.playingfield.Point;
import java.util.Arrays;


/**
 * A class that represents the process of calibrating light sensor assuming it's on the
 * starting tile
 * @author nafiz
 *
 */
public class LightSensorCalibration {

	public static DifferentialMinimalMargin dmm = new DifferentialMinimalMargin();

	public static float floorAverageIntensity = 0;
	
	/**
	 * Moves forward for TILE_SIZE meter to collect samples.
	 * After TILE_SIZE meter, it prints the average and minMargin calculated.
	 * It then moves back by TILE_SIZE meter
	 */
	public static void calibrate() {
		double[] odoData = odometer.getXyt();
		Point start = new Point(odoData[0], odoData[1]);
		
		float currentColorRight = 0;
	    float currentColorLeft = 0;
	    
	    currentColorRight = getColorRight();
	    currentColorLeft = getColorLeft();
	    
	    // useful for when you want to test if the light sensor is on a
	    // line without first reading outside lines
	    floorAverageIntensity = (currentColorRight + currentColorLeft) / 2;

	    // initialize prevValue
	    dmm.setPrevValue(floorAverageIntensity);
		
	    // move forward
		setSpeed(LOWER_FORWARD_SPEED);
		moveForward();
	    
	    odoData = odometer.getXyt();
		Point current = new Point(odoData[0], odoData[1]);
		while (distanceBetween(start, current) < TILE_SIZE) {
			currentColorRight = getColorRight();
		    currentColorLeft = getColorLeft();
		    
		    // calibration
		    float averageIntensity = (currentColorRight + currentColorLeft) / 2; 
		    dmm.calibrate(averageIntensity);
		    // set prevValue for next calibration
			dmm.setPrevValue(averageIntensity);
			
			odoData = odometer.getXyt();
			current = new Point(odoData[0], odoData[1]);
		}
		
		System.out.println("Average intensity differences: " + dmm.getAverage());
		System.out.println("Maximum intensity difference: " + dmm.getMax());
		System.out.println("The minimum margin for intensity difference: " + dmm.getMinMargin());
		
		// move back to starting point
		stopMotors();
	    setSpeed(FORWARD_SPEED);
	    moveStraightFor(-1);
	}
}
