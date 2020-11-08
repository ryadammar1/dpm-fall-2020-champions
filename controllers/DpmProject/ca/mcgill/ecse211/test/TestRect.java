package ca.mcgill.ecse211.test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

import ca.mcgill.ecse211.playingfield.Point;
import ca.mcgill.ecse211.playingfield.Rect;
import ca.mcgill.ecse211.playingfield.Region;

public class TestRect {
  @Test
  void testRectConstructors() {
    Rect rect = new Rect(new Point(1, 1), new Point(2, 2));
    assertEquals(new Point(1.5, 1.5), rect.getCenter());
    assertEquals(rect.getWidth(), 1);
    assertEquals(rect.getHeight(), 1);
    
    rect = new Rect(new Region(new Point(1, 1), new Point(2, 2)));
    assertEquals(new Point(1.5, 1.5), rect.getCenter());
    assertEquals(rect.getWidth(), 1);
    assertEquals(rect.getHeight(), 1);
    
    rect = new Rect(new Point(1.5, 1.5), 1, 1);
    assertEquals(new Point(1.5, 1.5), rect.getCenter());
    assertEquals(rect.getWidth(), 1);
    assertEquals(rect.getHeight(), 1);
  }
  
  @Test
  void testRectCorners() {
    Rect rect = new Rect(new Point(1, 1), new Point(2, 2));
    Point[] corners = rect.corners();
    assertEquals(corners[0], rect.ll);
    assertEquals(corners[1], new Point(1, 2));
    assertEquals(corners[2], rect.ur);
    assertEquals(corners[2], new Point(2, 2));
  }
  
  @Test
  void testIntersections() {
    Point start = new Point(0, 0);
    Point destination = new Point(3, 3);
    
    // test intersection with rectangle corners
    Rect rect = new Rect(new Point(1, 1), new Point(2, 2));
    ArrayList<Point> intersections = rect.intersections(start, destination);
    assertEquals(2, intersections.size());
    assertEquals(new Point(1, 1), intersections.get(0));
    assertEquals(new Point(2, 2), intersections.get(1));
    
    // test intersection from bottom left
    rect = new Rect(new Point(0.5, 0), new Point(1.5, 1));
    intersections = rect.intersections(start, destination);
    assertEquals(2, intersections.size());
    assertEquals(new Point(0.5, 0.5), intersections.get(0));
    assertEquals(new Point(1, 1), intersections.get(1));
    
    // test intersection from top right
    start = new Point(1, 2);
    destination = new Point(0, 0);
    rect = new Rect(new Point(0, 0), new Point(1, 1));
    intersections = rect.intersections(start, destination);
    assertEquals(2, intersections.size());
    assertEquals(new Point(0.5, 1), intersections.get(0));
    assertEquals(new Point(0, 0), intersections.get(1));
    
    // test no intersection
    destination = new Point(10, 10);
    intersections = rect.intersections(start, destination);
    assertEquals(0, intersections.size());
  }
}
