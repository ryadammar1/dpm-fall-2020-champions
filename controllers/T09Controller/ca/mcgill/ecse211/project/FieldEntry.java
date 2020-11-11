package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.playingfield.Point;

public class FieldEntry {
  //first demo map  
  //parameters of these two will be given
  static Point TNR_LL = new Point(4,7);
  static Point TNR_UR = new Point(6,8);
  static Point Island_LL = new Point(6,5);
  static Point Island_UR = new Point(15,9);
  
  static Point TNR_UL = new Point(TNR_LL.x,TNR_UR.y);
  static Point TNR_LR = new Point(TNR_UR.x,TNR_LL.y );
  
  // localization, goes to 1,1, beep 3 times then enterField is called
  public static void enterField() {
    // first demo map 
    
    if(isTunnelHorizontal()==true) {
      goInFrontOfHTunnel();
      Navigation.turnTo(90); 
      crossHTunnel();
    }
    else {
      goInFrontOfVTunnel();
      Navigation.turnTo(180); 
      crossVTunnel();
      
    }
 
    Main.STATE_MACHINE.enteredField();
    
  }
  
  public static void goInFrontOfHTunnel() {
    
    //first find middle of line in front of tunnel
    
    Point inFront = new Point((TNR_LL.x-2),(TNR_LL.y+TNR_UL.y)/2);
    Navigation.travelToPerpendicular(inFront);
    
  }
 
 // vertical downwards
public static void goInFrontOfVTunnel() {
    
    //first find middle of line in front of tunnel
    
    Point inFront = new Point((TNR_UL.x+TNR_UR.x)/2,(TNR_UL.y+1));
    Navigation.travelToPerpendicular(inFront);
    
  }
  
  public static void crossHTunnel() {
   Point destination = new Point((TNR_LR.x+1),(Odometer.getOdometer().getXyt()[1])/0.3048 );
     //Navigation.travelCorrected(destination);
    Navigation.travelTo(destination);
  }
  
  public static void crossVTunnel() {
    Point destination = new Point((Odometer.getOdometer().getXyt()[0])/0.3048,TNR_LL.y-1 );
      Navigation.travelCorrected(destination);
     //Navigation.travelTo(destination);
   }
  
  public static boolean isTunnelHorizontal() {
    if (TNR_UR.x == Island_LL.x) return true;
    else return false;
  }
}
