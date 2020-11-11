package ca.mcgill.ecse211.playingfield;


/**
 * Represents a 3-D vector
 * @author nafiz
 *
 */
public class Vector extends Point {
	/** The z component. */
	public double z;

	/** Constructs a Vector. */
	public Vector(double x, double y, double z) {
		super(x, y);
		this.z = z;
	}
	
	/** Constructs a Vector. z is initialized to 0 */
	public Vector(double x, double y) {
		this(x, y, 0);
	}
	
	/** Constructs a Vector from Point p. z is initialized to 0 */
	public Vector(Point p) {
		this(p.x, p.y, 0);
	}
	
	/** Returns a new vector that is this scaled by scalar */
	public Vector scaled(double scalar) {
		return new Vector(this.x * scalar, this.y * scalar, this.z * scalar);
	}
	
	/** Returns a new vector that is equal to a + b */
	public static Vector add(Vector a, Vector b) {
		return new Vector(a.x + b.x, a.y + b.y, a.z + b.z);
	}
	
	/** Returns a new vector that is equal to a - b */
	public static Vector sub(Vector a, Vector b) {
		return new Vector(a.x - b.x, a.y - b.y, a.z - b.z);
	}
	
	/** Returns a new vector that is equal to a - b */
	public static Vector sub(Point a, Point b) {
		return new Vector(a.x - b.x, a.y - b.y, 0);
	}
	
	/** Returns a dot product between a and b */
	public static double dot(Vector a, Vector b) {
		return (a.x * b.x) + (a.y * b.y) + (a.z * b.z);
	}
	
	/** Returns a cross product between a and b */
	public static Vector cross(Vector a, Vector b) {
		return new Vector(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
	}
	
	/** Returns the length of this vector */
	public double length() {
		return Math.sqrt(dot(this, this));
	}
	
	/** Returns the normalized vector of this vector */
	public Vector normalized() {
		return this.scaled(1/this.length());
	}
	
	@Override
	public String toString() {
	  return String.format("<%f, %f, %f>", x, y, z);
	}
	
	@Override
	  public boolean equals(Object obj) {
	    if (this == obj)
	      return true;
	    if (!super.equals(obj))
	      return false;
	    if (getClass() != obj.getClass())
	      return false;
	    Vector other = (Vector) obj;
	    if (z != other.z)
	      return false;
	    return true;
	  }
}
