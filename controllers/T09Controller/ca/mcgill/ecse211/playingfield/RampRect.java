package ca.mcgill.ecse211.playingfield;

public class RampRect extends Rect {
  private Point frontLeft;
  private Point frontRight;

  public RampRect(Point lowerLeft, Point upperRight) {
    super(lowerLeft, upperRight);
  }

  public Point getFrontLeft() {
    return frontLeft;
  }

  public void setFrontLeft(Point frontLeft) {
    this.frontLeft = frontLeft;
  }

  public Point getFrontRight() {
    return frontRight;
  }

  public void setFrontRight(Point frontRight) {
    this.frontRight = frontRight;
  }
}
