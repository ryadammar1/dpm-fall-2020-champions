package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.TIMEOUT_PERIOD;
import ca.mcgill.ecse211.playingfield.Point;
import simlejos.hardware.ev3.LocalEV3;

public class FieldEntry {

  /** Values retrieved from server */
  static Point Island_LL = Resources.island.ll;
  static Point Island_UR = Resources.island.ur;

  static Point Z_LL;
  static Point Z_UR;
  static Point TN_LL;
  static Point TN_UR;
  static Point TN_UL;
  static Point TN_LR;
  static Point SZ_LL;
  static Point SZ_UR;



  /**
   * Main method that performs the enter field
   */
  public static void enterField() {

    if (isTunnelRight() == true) {
      goInFrontOfRightTunnel();
      Navigation.turnTo(90);
      crossRightTunnel();
    } else if (isTunnelLeft() == true) {
      goInFrontOfLeftTunnel();
      Navigation.turnTo(270);
      crossLeftTunnel();
    } else if (isTunnelTop() == true) {
      goInFrontOfTopTunnel();
      Navigation.turnTo(0);
      crossTopTunnel();
    } else if (isTunnelBottom() == true) {
      goInFrontOfBottomTunnel();
      Navigation.turnTo(180);
      crossBottomTunnel();
    }


    if (checkIfInSearchZone() == true) {
      enteredSearchZone();
    } else {
      goToSearchZone();
      enteredSearchZone();

    }
  }



//  public static boolean isRedTeam() {
//    if (Resources.redTeam == Resources.TEAM_NUMBER) {
//      return true;
//    } else
//      return false;
//  }

  public static void setTunnelAndSearchZone() {
    Z_LL = Resources.startZone.ll;
    Z_UR = Resources.startZone.ur;
    TN_LL = Resources.tunnel.ll;
    TN_UR = Resources.tunnel.ur;
    TN_UL = new Point(TN_LL.x, TN_UR.y);
    TN_LR = new Point(TN_UR.x, TN_LL.y);
    SZ_LL = Resources.searchZone.ll;
    SZ_UR = Resources.searchZone.ur;
  }

  /**
   * Verifies if tunnel is on the right of the starting zone
   */
  public static boolean isTunnelRight() {
    if (TN_UR.x >= Island_LL.x)
      return true;
    else
      return false;
  }

  /**
   * Verifies if tunnel is on the left of the starting zone
   */
  public static boolean isTunnelLeft() {
    if (TN_LL.x <= Island_UR.x)
      return true;
    else
      return false;
  }

  /**
   * Verifies if tunnel is on the top of the starting zone
   */
  public static boolean isTunnelTop() {
    if (TN_UR.y >= Island_LL.y)
      return true;
    else
      return false;
  }

  /**
   * Verifies if tunnel is on the bottom of the starting zone
   */
  public static boolean isTunnelBottom() {
    if (TN_LL.y <= Island_UR.y)
      return true;
    else
      return false;
  }

  /**
   * Makes robot go in front of a tunnel that is on the right of the starting zone
   */
  public static void goInFrontOfRightTunnel() {

    Point inFront = new Point((TN_LL.x - 1), (TN_LL.y + TN_UL.y) / 2);
    Navigation.travelToPerpendicularImmReturn(inFront);

  }

  /**
   * Makes robot go in front of a tunnel that is on the left of the starting zone
   */
  public static void goInFrontOfLeftTunnel() {

    Point inFront = new Point((TN_UR.x + 1), (TN_LL.y + TN_UL.y) / 2);
    Navigation.travelToPerpendicularImmReturn(inFront);

  }

  /**
   * Makes robot go in front of a tunnel that is on the top of the starting zone
   */
  public static void goInFrontOfTopTunnel() {

    Point inFront = new Point((TN_UL.x + TN_UR.x) / 2, (TN_LL.y - 1));
    Navigation.travelToPerpendicularImmReturn(inFront);

  }

  /**
   * Makes robot go in front of a tunnel that is on the bottom of the starting zone
   */
  public static void goInFrontOfBottomTunnel() {

    Point inFront = new Point((TN_UL.x + TN_UR.x) / 2, (TN_UL.y + 1));
    Navigation.travelToPerpendicularImmReturn(inFront);

  }

  /**
   * Robot crosses tunnel on the right of start zone
   */
  public static void crossRightTunnel() {
    Point destination = new Point((TN_LR.x + 1), (Odometer.getOdometer().getXyt()[1]) / 0.3048);
    // Navigation.travelCorrected(destination);
    Navigation.travelToImmReturn(destination);
  }

  /**
   * Robot crosses tunnel on the left of start zone
   */
  public static void crossLeftTunnel() {
    Point destination = new Point((TN_LL.x + 1), (Odometer.getOdometer().getXyt()[1]) / 0.3048);
    // Navigation.travelCorrected(destination);
    Navigation.travelToImmReturn(destination);
  }

  /**
   * Robot crosses tunnel on the top of start zone
   */
  public static void crossTopTunnel() {
    Point destination = new Point((Odometer.getOdometer().getXyt()[0]) / 0.3048, TN_UR.y + 1);
    // Navigation.travelCorrected(destination);
    Navigation.travelToImmReturn(destination);
  }

  /**
   * Robot crosses tunnel on the bottom of start zone
   */
  public static void crossBottomTunnel() {
    Point destination = new Point((Odometer.getOdometer().getXyt()[0]) / 0.3048, TN_LL.y - 1);
    // Navigation.travelCorrected(destination);
    Navigation.travelToImmReturn(destination);
  }



  /**
   * Checks if the robot is located inside the search zone
   */
  public static boolean checkIfInSearchZone() {

    double currentX = (Odometer.getOdometer().getXyt()[0]) / 0.3048;
    double currentY = (Odometer.getOdometer().getXyt()[1]) / 0.3048;

    if (currentX > SZ_LL.x && currentX < SZ_UR.x) {
      if (currentY > SZ_LL.y && currentY < SZ_UR.y) {
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
      double xInSZ = SZ_LL.x + 1;
      double yInSZ = SZ_LL.y + 1;
      Point inSZ = new Point(xInSZ, yInSZ);
      Navigation.travelToPerpendicularImmReturn(inSZ);

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
