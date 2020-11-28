package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import ca.mcgill.ecse211.playingfield.Point;
import simlejos.hardware.ev3.LocalEV3;


public class FieldExit {
    
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

private static boolean backAtCorner = false;

/** Initialize values */
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
   * Main method that performs the exit of field
   */
  public static void exitField() {
    if (!backAtCorner) {
    if (FieldEntry.isOnIsland()) {
      // 1. travel to tunnel and cross
      if( isTunnelRightIsland()==true){
          goInFrontOfRightTunnelIsland();
          if (Main.STATE_MACHINE.getStatusFullName() == "Avoidance")
            return;
          Navigation.turnTo(90);
          FieldEntry.crossRightTunnel();
      }
      else if (isTunnelLeftIsland()==true){
          goInFrontOfLeftTunnelIsland();
          if (Main.STATE_MACHINE.getStatusFullName() == "Avoidance")
            return;
          Navigation.turnTo(270);
          FieldEntry.crossLeftTunnel();
      }
      else if (isTunnelTopIsland()==true){
          goInFrontOfTopTunnelIsland();
          if (Main.STATE_MACHINE.getStatusFullName() == "Avoidance")
            return;
          Navigation.turnTo(0);
          FieldEntry.crossTopTunnel();
      }
      else if (isTunnelBottomIsland()==true){
          goInFrontOfBottomTunnel();
          if (Main.STATE_MACHINE.getStatusFullName() == "Avoidance")
            return;
          Navigation.turnTo(180);
          FieldEntry.crossBottomTunnel();
      }
    }
    FieldEntry.setOnIsland(false);

    //3. go to start corner
    goToCorner();
    
    if (Main.STATE_MACHINE.getStatusFullName() == "Avoidance")
      return;
    
    //4. 5 beeps 
    backAtCorner();
    }
  }

/**
   * Verifies if tunnel is on the right of the island
   */
  public static boolean isTunnelRightIsland() {
    return FieldEntry.isTunnelLeft();
  }

  /**
   * Verifies if tunnel is on the left of the Island
   */
  public static boolean isTunnelLeftIsland() {
    return FieldEntry.isTunnelRight();
  }

  /**
   * Verifies if tunnel is on the top of the island
   */
  public static boolean isTunnelTopIsland() {
    return FieldEntry.isTunnelBottom();
  }

  /**
   * Verifies if tunnel is on the bottom of the island
   */
  public static boolean isTunnelBottomIsland() {
    return FieldEntry.isTunnelTop();
  }

  /**
   * Makes robot go in front of a tunnel that is on the right of the starting zone
   */
  public static void goInFrontOfRightTunnelIsland() {
    Point inFront = new Point((TN_LL.x - 0.5), (TN_LL.y + TN_UL.y) / 2);
    Navigation.travelToPerpendicularImmReturn(inFront);
    
  }

  /**
   * Makes robot go in front of a tunnel that is on the left of the starting zone
   */
  public static void goInFrontOfLeftTunnelIsland() {
    Point inFront = new Point((TN_UR.x + 0.5), (TN_LL.y + TN_UL.y) / 2);
    Navigation.travelToPerpendicularImmReturn(inFront);
    
  }

  /**
   * Makes robot go in front of a tunnel that is on the top of the starting zone
   */
  public static void goInFrontOfTopTunnelIsland() {

    Point inFront = new Point((TN_UL.x + TN_UR.x) / 2, (TN_LL.y - 0.5));
    Navigation.travelToPerpendicularImmReturn(inFront);
    
  }

  /**
   * Makes robot go in front of a tunnel that is on the bottom of the starting zone
   */
  public static void goInFrontOfBottomTunnel() {

    Point inFront = new Point((TN_UL.x + TN_UR.x) / 2, (TN_UL.y + 0.5));
    Navigation.travelToPerpendicularImmReturn(inFront);
    
  }

  public static void goToCorner(){
    if (Resources.corner == 0){
        double x = 1;
        double y = 1;
        Point startingCorner = new Point(x, y);
        Navigation.travelToPerpendicularImmReturn(startingCorner);
      }
      else if (Resources.corner == 1){
        double x = 14;
        double y = 1;
        Point startingCorner = new Point(x,y);
        Navigation.travelToPerpendicularImmReturn(startingCorner);
      }
      else if (Resources.corner == 2){
        double x = 14;
        double y = 8;
        Point startingCorner = new Point(x,y);
        Navigation.travelToPerpendicularImmReturn(startingCorner);
      }
      else if (Resources.corner == 3){
        double x = 1;
        double y = 8;
        Point startingCorner = new Point(x,y);
        Navigation.travelToPerpendicularImmReturn(startingCorner);
      }
  }

/**
   * Steps to be performed once robot is back at starting corner
   */
  public static void backAtCorner() {
    System.out.println("In Starting Corner");
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
      
      backAtCorner = true;
  }


}
