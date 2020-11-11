package ca.mcgill.ecse211.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import ca.mcgill.ecse211.playingfield.Vector;


/**
 * 
 * @author nafiz
 * Ensure that sophisticated functions of Vector works properly
 */
public class TestVector {
	/** Tolerate up to this amount of error due to double imprecision. */
	  private static final double ERROR_MARGIN = 0.01;
	
	@Test
	void testCrossProduct() {
	    // test perpendicularity
		Vector a = new Vector(1, 0);
		Vector b = new Vector(0, 1);
		assertEquals(new Vector(0, 0, 1),  Vector.cross(a, b));
		
		a = new Vector(0, 1, 0);
		b = new Vector(0, 0, 1);
		assertEquals(new Vector(1, 0, 0),  Vector.cross(a, b));
		
		// test thumb direction for right hand rule
		a = new Vector(1, 0);
        b = new Vector(0, 1);
        assertEquals(new Vector(0, 0, 1),  Vector.cross(a, b));
        
        a = new Vector(0, 1);
        b = new Vector(1, 0);
        assertEquals(new Vector(0, 0, -1),  Vector.cross(a, b));
	}
}
