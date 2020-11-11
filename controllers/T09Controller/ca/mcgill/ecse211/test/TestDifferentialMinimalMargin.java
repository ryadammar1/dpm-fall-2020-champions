package ca.mcgill.ecse211.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import ca.mcgill.ecse211.project.DifferentialMinimalMargin;

public class TestDifferentialMinimalMargin {
	double[] samples = {0.5, 1, 2, 1.9, 0.5, 2, 2, 1, 0.5, 1};
	
	@Test
	void testBlackToGrey() {
		DifferentialMinimalMargin d = new DifferentialMinimalMargin();
		for (double v : samples) {
			d.calibrate(v);
			d.setPrevValue(v);
		}
		
		d.setPrevValue(0.5);
		assertTrue(d.differential(0.75) == 0);
		d.setPrevValue(0.75);
		assertTrue(d.differential(2) > 0);
	}
	
	@Test
	void testGreyToBlack() {
		DifferentialMinimalMargin d = new DifferentialMinimalMargin();
		for (double v : samples) {
			d.calibrate(v);
			d.setPrevValue(v);
		}
		
		d.setPrevValue(2);
		assertTrue(d.differential(1.8) == 0);
		d.setPrevValue(1.8);
		assertTrue(d.differential(0.5) < 0);
	}
}
