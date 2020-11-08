import com.cyberbotics.webots.controller.Robot;
import com.cyberbotics.webots.controller.Supervisor;
import com.cyberbotics.webots.controller.Node;
import com.cyberbotics.webots.controller.Field;
import com.cyberbotics.webots.controller.Emitter;

public class mapBuilder {
  final static Supervisor supervisor = new Supervisor();
  final static Node arena = supervisor.getFromDef("ARENA");

  public static void setTileColor(int x,int y,double r,double g,double b){
    Field tileColor = arena.getField("tileColor_"+x+"_"+y);
    if(tileColor==null)System.out.println("tileColor not found ["+x+"]["+y+"]");
    double[] color = new double[]{r,g,b}; 
    tileColor.setSFColor(color);
  }
  
  public static void setTileHeight(int x,int y,double h){
    Field tileHeight = arena.getField("tileHeight_"+x+"_"+y);
    if(tileHeight==null)System.out.println("tileHeight not found");
    tileHeight.setSFFloat(h);
  }

  public static void setBridge(int index,double x,double y,double r){
    Node bridge = supervisor.getFromDef("BRIDGE"+index);
    if(bridge==null)System.out.println("bridge not found");
    //Set Position
    Field position = bridge.getField("position");
    if(position==null)System.out.println("position not found");
    double[] positionVector = new double[]{x,y}; 
    position.setSFVec2f(positionVector);
    //Set rotation
    Field rotation = bridge.getField("rotation");
    if(rotation==null)System.out.println("rotation not found");
    rotation.setSFFloat(r);
  }
  
  public static void setObstacle(int index,double x,double y,double r){
    Node obstacle = supervisor.getFromDef("OBSTACLE"+index);
    if(obstacle==null)System.out.println("obstacle not found");
    //Set Position
    Field position = obstacle.getField("position");
    if(position==null)System.out.println("position not found");
    double[] positionVector = new double[]{x,y}; 
    position.setSFVec2f(positionVector);
    //Set Rotation
    Field rotation = obstacle.getField("rotation");
    if(rotation==null)System.out.println("rotation not found");
    rotation.setSFFloat(r);    
  }
  
  public static void setChute(int index,double x,double y,double r){
    Node obstacle = supervisor.getFromDef("CHUTE"+index);
    if(obstacle==null)System.out.println("chute not found");
    //Set Position
    Field position = obstacle.getField("position");
    if(position==null)System.out.println("position not found");
    double[] positionVector = new double[]{x,y}; 
    position.setSFVec2f(positionVector);
    //Set Rotation
    Field rotation = obstacle.getField("rotation");
    if(rotation==null)System.out.println("rotation not found");
    double[] rotationVector = new double[]{0,1,0,r}; 
    rotation.setSFRotation(rotationVector);
  }

  public static void setMap(){
    //Reset floor to all blue
    for(int x=0;x<15;x++)for(int y=0;y<9;y++)setTileColor(x,y, 0.5,0.5,1);
    for(int x=0;x<15;x++)for(int y=0;y<9;y++)setTileHeight(x,y,-0.1);
    
    //Manually set tile colors for now
    //Red island
    for(int x=0;x<4;x++)for(int y=0;y<4;y++)setTileColor(x,y, 1,0.5,0.5);
    for(int x=0;x<4;x++)for(int y=0;y<4;y++)setTileHeight(x,y,0);
    //Yellow Island
    for(int x=6;x<15;x++)for(int y=0;y<4;y++)setTileColor(x,y, 0.82,0.82,0.4);
    for(int x=6;x<15;x++)for(int y=0;y<4;y++)setTileHeight(x,y,0);
    //Green Island
    for(int x=10;x<15;x++)for(int y=5;y<9;y++)setTileColor(x,y, 0.5,1,0.5);
    for(int x=10;x<15;x++)for(int y=5;y<9;y++)setTileHeight(x,y,0);
    //Bridge tiles
    setTileColor(5,1, 0.7,0.7,0.7);
    setTileHeight(5,1,0);
    setTileColor(4,1, 0.7,0.7,0.7);
    setTileHeight(4,1,0);
    setTileColor(10,4, 0.7,0.7,0.7);
    setTileHeight(10,4,0);
    setTileColor(10,5, 0.7,0.7,0.7);
    setTileHeight(10,5,0);
        
    //Place bridges
    setBridge(1, 5,1.5, 0);
    setBridge(2, 10.5,5, 1.57);
    
    //Place Obstacles
    setObstacle(1, 8.5,3.5, 0);
    setObstacle(2, 14.5,3.5, 2.1);
    setObstacle(3, 3,3, 0);
    setObstacle(4, 13.5,5.5, 0);
    
    //Place Chutes
    setChute(1, 0,9,1.57);
    setChute(2, 0,12,1.57);
    
    //This is completely optional and simply for style
    //Make the water nicer by lowering sections and making the deeper parts darker
    for(int x=1;x<9;x++)for(int y=5;y<8;y++)setTileHeight(x,y,-0.2);
    for(int x=1;x<9;x++)for(int y=5;y<8;y++)setTileColor(x,y, 0.25,0.25,0.75);
    //even deeper and darker
    for(int x=2;x<8;x++)for(int y=6;y<7;y++)setTileHeight(x,y,-0.3);
    for(int x=2;x<8;x++)for(int y=6;y<7;y++)setTileColor(x,y, 0.1,0.1,0.5);
  }

  public static void main(String[] args) {
    setMap();
    while(supervisor.step((int)supervisor.getBasicTimeStep()) != -1) {
      //Display text
      supervisor.setLabel(0,"Supervisor: running",0,0,0.1,0xff0000,0,"Arial");
    }
  }

}

