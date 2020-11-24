package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.playingfield.Vector.*;
import ca.mcgill.ecse211.playingfield.Point;
import ca.mcgill.ecse211.playingfield.Rect;
import ca.mcgill.ecse211.playingfield.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class PathPlanning {

  /**
   * @param Robot's current position
   * @param Vector's tail position
   * @param Vector's head position
   * @return List of points constructing the path
   */

  private static final double OFFSET = 0.5;

  public static ArrayList<Point> plan(Point current1, Point tail, Point head) {
    ArrayList<Point> path = new ArrayList<Point>();
    Point current = new Point(Math.round(current1.x), Math.round(current1.y));

    // Is the robot's position contained in the director vector and exterior to tail or between head and tail?
    // If so, move the robot one tile perpendicularly to the vector
    if ((current.x == head.x && current.x == tail.x)
        && ((Math.abs(current.y - head.y) > Math.abs(current.y - tail.y)) || isBetweenX(current, tail, head))) {
      path.add(new Point(current.x + OFFSET, current.y));
      current = path.get(0);
    }
    if ((current.y == head.y && current.y == tail.y)
        && ((Math.abs(current.x - head.x) > Math.abs(current.x - tail.x)) || isBetweenY(current, tail, head))) {
      path.add(new Point(current.x, current.y + OFFSET));
      current = path.get(0);
    }

    // Is the robot at least one position away from the tail relative to the axis
    // defined by the director vector & closer to the tail than to the head?
    if (current.y >= head.y && tail.y > head.y) {
      path.add(new Point(current.x, head.y - OFFSET));
      try {
        current = path.get(1);
      } catch (Exception e) {
        current = path.get(0);
      }
    }
    if (current.y <= head.y && tail.y < head.y) {
      path.add(new Point(current.x, head.y + OFFSET));
      try {
        current = path.get(1);
      } catch (Exception e) {
        current = path.get(0);
      }
    }

    if (current.x >= head.x && tail.x > head.x) {
      path.add(new Point(head.x - OFFSET, current.y));
      try {
        current = path.get(1);
      } catch (Exception e) {
        current = path.get(0);
      }
    }
    if (current.x <= head.x && tail.x < head.x) {
      path.add(new Point(head.x + OFFSET, current.y));
      try {
        current = path.get(1);
      } catch (Exception e) {
        current = path.get(0);
      }
    }

    // Decide the final destination of the robot to get ready to push the block
    // according to the vector head and tail position
    if (tail.y > head.y)
      path.add(new Point(head.x, head.y - OFFSET));
    if (tail.y < head.y)
      path.add(new Point(head.x, head.y + OFFSET));
    if (tail.x > head.x)
      path.add(new Point(head.x - OFFSET, head.y));
    if (tail.x < head.x)
      path.add(new Point(head.x + OFFSET, head.y));

    // System.out.println("Path to take: " + path.toString());
    return path;
  }

  /**
   * 
   * @param current
   * @param tail
   * @param head
   * @return true if the 1st argument is between 2nd and 3rd argument
   */
  public static boolean isBetweenX(Point current, Point tail, Point head) {
    return ((current.y >= tail.y && current.y <= head.y) || (current.y <= tail.y && current.y >= head.y));

  }

  public static boolean isBetweenY(Point current, Point tail, Point head) {
    return ((current.x >= tail.x && current.x <= head.x) || (current.x <= tail.x && current.x >= head.x));

  }

//  /**
//   * Creates a new path that takes into account blocks between current and last point in path
//   * 
//   * @param path
//   * @param c
//   * @return the new path
//   */
//  public static ArrayList<Point> checkPath(ArrayList<Point> path, Point c) {
//    ArrayList<Point> newPath = new ArrayList<>();
//
//    Point current = c;
//    for (Point destination : path) {
//      Point theHeadInDestination = null;
//      for (Point head : Main.heads) {
//
//        if (head.equals(destination))
//          theHeadInDestination = head;
//
//        if ((head.x == current.x && head.x == destination.x) && isBetweenX(head, destination, current)) {
//          // TODO write what to do when a head is between the robot and the destination on the x axis
//          /**
//           * Example of what it has to do: _____ r ____| h |_____ d
//           * 
//           */
//          // Don't forget to modify the path. To do so, keep track of the index of the current destination
//          // (let's say index = i) and add points surrounding the block between path.get(i) and path.get(i+1).
//          System.out.println(current + " " + head);
//          newPath.addAll(PathPlanning.plan(current, current, head));
//        }
//
//        if ((head.y == current.y && head.y == destination.y) && isBetweenY(head, destination, current)
//            && theHeadInDestination == null) {
//          newPath.addAll(PathPlanning.plan(current, current, head));
//        }
//      }
//
//      if (theHeadInDestination != null) {
//        destination = newPath.get(newPath.size() - 1);
//      } else {
//        newPath.add(destination);
//      }
//
//      current = destination;
//    }
//
//    return newPath;
//  }

  public static double planDirection(Point current, Point tail) {
    if (Math.round(current.x) > tail.x)
      return -90;
    if (Math.round(current.x) < tail.x)
      return 90;
    if (Math.round(current.y) > tail.y)
      return 180;
    if (Math.round(current.y) < tail.y)
      return 0;
    return 0;
  }

}
