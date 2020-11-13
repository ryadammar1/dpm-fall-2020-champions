package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.TIMEOUT_PERIOD;
import ca.mcgill.ecse211.playingfield.Point;
import simlejos.hardware.ev3.LocalEV3;

public class FieldEntry {

  /** Values retrieved from server */
  static Point Island_LL = Resources.island.ll;
  static Point Island_UR = Resources.island.ur;
  static Point TNR_LL = Resources.tnr.ll;
  static Point TNR_UR = Resources.tnr.ur;
  static Point TNR_UL = new Point(TNR_LL.x, TNR_UR.y);
  static Point TNR_LR = new Point(TNR_UR.x, TNR_LL.y);
  static Point SZR_LL = Resources.szr.ll;
  static Point SZR_UR = Resources.szr.ur;

  /**
   * Main method that performs the enter field
   */
  public static void enterField() {

    if (isTunnelHorizontal() == true) {
      goInFrontOfHTunnel();
      Navigation.turnTo(90);
      crossHTunnel();
    } else {
      goInFrontOfVTunnel();
      Navigation.turnTo(180);
      crossVTunnel();

    }
    boolean inSearchZone = checkIfInSearchZone();
    if (checkIfInSearchZone() == true) {
      enteredSearchZone();
    } else {
      goToSearchZone();
      enteredSearchZone();

    }
  }

  /**
   * Makes robot go in front of a horizontal tunnel
   */
  public static void goInFrontOfHTunnel() {

    Point inFront = new Point((TNR_LL.x - 1), (TNR_LL.y + TNR_UL.y) / 2);
    Navigation.travelToPerpendicular(inFront);

  }

  /**
   * Makes robot go in front of a vertical tunnel
   */
  public static void goInFrontOfVTunnel() {

    Point inFront = new Point((TNR_UL.x + TNR_UR.x) / 2, (TNR_UL.y + 1));
    Navigation.travelToPerpendicular(inFront);

  }

  /**
   * Robot crosses horizontal tunnel
   */
  public static void crossHTunnel() {
    Point destination = new Point((TNR_LR.x + 1), (Odometer.getOdometer().getXyt()[1]) / 0.3048);
    // Navigation.travelCorrected(destination);
    Navigation.travelTo(destination);
  }

  /**
   * Robot crosses vertical tunnel
   */
  public static void crossVTunnel() {
    Point destination = new Point((Odometer.getOdometer().getXyt()[0]) / 0.3048, TNR_LL.y - 1);
    // Navigation.travelCorrected(destination);
    Navigation.travelTo(destination);
  }

  /**
   * Verifies the orientation of the tunnel
   */
  public static boolean isTunnelHorizontal() {
    if (TNR_UR.x == Island_LL.x)
      return true;
    else
      return false;
  }

  /**
   * Checks if the robot is located inside the search zone
   */
  public static boolean checkIfInSearchZone() {

    double currentX = (Odometer.getOdometer().getXyt()[0]) / 0.3048;
    double currentY = (Odometer.getOdometer().getXyt()[1]) / 0.3048;

    if (currentX > SZR_LL.x && currentX < SZR_UR.x) {
      if (currentY > SZR_LL.y && currentY < SZR_UR.y) {
        return true;
      }
      return false;

    }

    return false;

  }

  /**
   * Robot travels to search zone
   */
  public static void goToSearchZone() {
    if (checkIfInSearchZone() == false) {
      double xInSZ = SZR_LL.x + 1;
      double yInSZ = SZR_LL.y + 1;
      Point inSZ = new Point(xInSZ, yInSZ);
      Navigation.travelToPerpendicular(inSZ);

    }

  }

  /**
   * Steps to be performed once robot is inside the search zone
   */
  public static void enteredSearchZone() {
    System.out.println("In Search Zone");
    LocalEV3.getAudio().beep();
    try {
      Thread.sleep(TIMEOUT_PERIOD / 2);
    } catch (InterruptedException e) {
    }
    LocalEV3.getAudio().beep();
    try {
      Thread.sleep(TIMEOUT_PERIOD / 2);
    } catch (InterruptedException e) {
    }
    LocalEV3.getAudio().beep();
    Main.STATE_MACHINE.enteredField();
  }

}