package ca.mcgill.ecse211.playingfield;

import static ca.mcgill.ecse211.playingfield.Vector.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet; 

/**
 * Rect class represents a rectangular region
 * @author nafiz
 *
 */
public class Rect extends Region {

  /**
   * Constructs a Rect given a lower left and upper right point
   * @param lowerLeft
   * @param upperRight
   */
  public Rect(Point lowerLeft, Point upperRight) {
    super(lowerLeft, upperRight);
  }
  
  /**
   * Constructs a Rect based on the fields on Region
   * @param region
   */
  public Rect(Region region) {
    this(region.ll, region.ur);
  }
  
  /**
   * Constructs a Rect based on center point, width and height
   * @param center
   * @param width
   * @param height
   */
  public Rect(Point center, double width, double height) {
    this(
        new Point(center.x - width / 2, center.y - height / 2),
        new Point(center.x + width / 2, center.y + height / 2)
    );
  }
  
  /**
   * returns the center of this rectangle
   * @return center of this rectangle
   */
  public Point getCenter() {
    return new Point((ll.x + ur.x) / 2, (ll.y + ur.y) / 2);
  }
  
  /**
   * returns the points of four corners of this rectangle
   * @return points of four corners of this rectangle
   */
  public Point[] corners() {
    Point[] corners = new Point[4];
    corners[0] = ll;
    corners[1] = new Point(ll.x, ur.y);
    corners[2] = ur;
    corners[3] = new Point(ur.x, ll.y);
    
    return corners;
  }

  /**
   * Returns the 0 to 2 points of intersection of a direction vector originating from start with this rectangle.
   * It is sorted based on distance from the start point in ascending order. 
   * @param start
   * @param direction
   * @return 0 to 2 points of intersection of a line/vector starting from start to destination with this rectangle
   */
  public ArrayList<Point> intersections2(Point start, Vector direction) {
    // abondoned work (in case of emergency)
//    final Vector startToRect = sub(getCenter(), start);
//    // all the normal component of a rectangle
//    final Vector[] normals = {
//        new Vector(-1, 0),
//        new Vector(0, 1),
//        new Vector(1, 0),
//        new Vector(0, -1)
//    };
//    // sort based on dot product
//    Arrays.sort(normals, (n1, n2) -> (int)Math.signum(dot(n1, direction) - dot(n2, direction)));
//    
//    final Vector sideA = sub(startToRect, normals[3].scaled(this.getWidth() / 2));
//    final Vector sideB = sub(startToRect, normals[2].scaled(this.getWidth() / 2));
    
    // positions local to the starting point
    final double localLeft = ll.x - start.x;
    final double localTop = ur.y - start.y;
    final double localRight = ur.x - start.x;
    final double localBottom = ll.y - start.y;
    
    HashSet<Point> intersections = new HashSet<>();
    
    if (direction.x != 0) {
      final Point leftIntersection = new Point(ll.x, start.y + direction.y * (localLeft / direction.x));
      
      if (leftIntersection.y >= ll.y && leftIntersection.y <= ur.y)
        intersections.add(leftIntersection);
    
      final Point rightIntersection = new Point(ur.x, start.y + direction.y * (localRight / direction.x));
  
      if (rightIntersection.y >= ll.y && rightIntersection.y <= ur.y)
        intersections.add(rightIntersection);
    }
    
    if (direction.y != 0) {
      final Point topIntersection = new Point(start.x + direction.x * (localTop / direction.y), ur.y);
  
      if (topIntersection.x >= ll.x && topIntersection.x <= ur.x)
        intersections.add(topIntersection);
      
      final Point bottomIntersection = new Point(start.x + direction.x * (localBottom / direction.y), ll.y);
  
      if (bottomIntersection.x >= ll.x && bottomIntersection.x <= ur.x)
        intersections.add(bottomIntersection);
    }
    
    ArrayList<Point> asList = new ArrayList<>(intersections);
    asList.sort((a, b) -> (int)Math.signum(sub(a, start).length() - sub(b, start).length()));
    
    return asList; 
  }
  
  /**
   * returns the 0 to 2 points of intersection  of a line/vector from start to destination with this rectangle
   * @param start
   * @param destination
   * @return 0 to 2 points of intersection of a line/vector starting from start to destination with this rectangle
   */
  public ArrayList<Point> intersections(Point start, Point destination) {
    return this.intersections2(start, sub(destination, start));
  }
  
  public boolean contains(Point p) {
    return p.x >= ll.x && p.x <= ur.x
        && p.y >= ll.y && p.y <= ur.y;
  }
}
