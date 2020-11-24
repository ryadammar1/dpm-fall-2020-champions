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


    // 1. travel to tunnel and cross
    if( isTunnelRightIsland()==true){
        goInFrontOfRightTunnelIsland();
        Navigation.turnTo(90);
        FieldEntry.crossRightTunnel();
    }
    else if (isTunnelLeftIsland()==true){
        goInFrontOfLeftTunnelIsland();
        Navigation.turnTo(270);
        FieldEntry.crossLeftTunnel();
    }
    else if (isTunnelTopIsland()==true){
        goInFrontOfTopTunnelIsland();
        Navigation.turnTo(0);
        FieldEntry.crossTopTunnel();
    }
    else if (isTunnelBottomIsland()==true){
        goInFrontOfBottomTunnel();
        Navigation.turnTo(180);
        FieldEntry.crossBottomTunnel();
    }

    //3. go to start corner
    goToCorner();
    //4. 5 beeps 
    backAtCorner();



  }

/**
   * Verifies if tunnel is on the right of the island
   */
  public static boolean isTunnelRightIsland() {
    if (TN_LL.x <= Island_UR.x)
      return true;
    else
      return false;
  }

  /**
   * Verifies if tunnel is on the left of the Island
   */
  public static boolean isTunnelLeftIsland() {
    if (TN_UR.x >= Island_LL.x)
      return true;
    else
      return false;
  }

  /**
   * Verifies if tunnel is on the top of the island
   */
  public static boolean isTunnelTopIsland() {
    if (TN_LL.y <= Island_UR.y)
      return true;
    else
      return false;
  }

  /**
   * Verifies if tunnel is on the bottom of the island
   */
  public static boolean isTunnelBottomIsland() {
    if (TN_UR.y >= Island_LL.y)
      return true;
    else
      return false;
  }

  /**
   * Makes robot go in front of a tunnel that is on the right of the starting zone
   */
  public static void goInFrontOfRightTunnelIsland() {

    Point inFront = new Point((TN_LL.x - 1), (TN_LL.y + TN_UL.y) / 2);
    Navigation.travelToPerpendicularImmReturn(inFront);
    
  }

  /**
   * Makes robot go in front of a tunnel that is on the left of the starting zone
   */
  public static void goInFrontOfLeftTunnelIsland() {

    Point inFront = new Point((TN_UL.x + 1), (TN_LL.y + TN_UL.y) / 2);
    Navigation.travelToPerpendicularImmReturn(inFront);
    
  }

  /**
   * Makes robot go in front of a tunnel that is on the top of the starting zone
   */
  public static void goInFrontOfTopTunnelIsland() {

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

  public static void goToCorner(){
      
    if (Resources.corner == 0){
        double x = TILE_SIZE/2;
        double y = TILE_SIZE/2;
        Point startingCorner = new Point(x, y);
        Navigation.travelToPerpendicularImmReturn(startingCorner);
      }
      else if (Resources.corner == 1){
        double x = ((15*TILE_SIZE)-(TILE_SIZE/2));
        double y = TILE_SIZE/2;
        Point startingCorner = new Point(x,y);
        Navigation.travelToPerpendicularImmReturn(startingCorner);
      }
      else if (Resources.corner == 2){
        double x = ((15*TILE_SIZE)-(TILE_SIZE/2));
        double y = ((9*TILE_SIZE)-(TILE_SIZE/2));
        Point startingCorner = new Point(x,y);
        Navigation.travelToPerpendicularImmReturn(startingCorner);
      }
      else if (Resources.corner == 3){
        double x = TILE_SIZE/2;
        double y = ((9*TILE_SIZE)-(TILE_SIZE/2));
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
    //Main.STATE_MACHINE.
  }


}
