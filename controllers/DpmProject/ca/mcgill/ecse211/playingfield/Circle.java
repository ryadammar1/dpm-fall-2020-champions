package ca.mcgill.ecse211.playingfield;
import static ca.mcgill.ecse211.project.Resources.*;

public class Circle{

    private Point centerPoint;
    /** Error threshold */
    private final double RADIUS = 0.5; // in tile size

    public Circle(Point centerPoint) {
        this.centerPoint = centerPoint;
    }

    public boolean contains(Point point){
        return (Math.sqrt(point.x - centerPoint.x) + Math.sqrt(point.y - centerPoint.y)) <= Math.sqrt(RADIUS);
    }
    
}
