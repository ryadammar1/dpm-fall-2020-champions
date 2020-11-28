package ca.mcgill.ecse211.project;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import ca.mcgill.ecse211.playingfield.Point;
import ca.mcgill.ecse211.playingfield.RampEdge;
import ca.mcgill.ecse211.playingfield.RampRect;
import ca.mcgill.ecse211.playingfield.Rect;
import ca.mcgill.ecse211.playingfield.Region;
import ca.mcgill.ecse211.wificlient.WifiConnection;
import simlejos.hardware.motor.Motor;
import simlejos.hardware.port.SensorPort;
import simlejos.hardware.sensor.EV3ColorSensor;
import simlejos.hardware.sensor.EV3UltrasonicSensor;
import simlejos.robotics.RegulatedMotor;
import simlejos.robotics.SampleProvider;

/* (non-Javadoc comment)
 * TODO Integrate this carefully with your existing Resources class (See below for where to add
 * your code from your current Resources file). The order in which things are declared matters!
 */

/**
 * Class for static resources (things that stay the same throughout the entire
 * program execution), like constants and hardware. <br>
 * <br>
 * Use these resources in other files by adding this line at the top (see
 * examples):<br>
 * <br>
 * 
 * {@code import static ca.mcgill.ecse211.project.Resources.*;}
 */
public class Resources {

  // Wi-Fi client parameters
  /** The default server IP used by the profs and TA's. */
  public static final String DEFAULT_SERVER_IP = "127.0.0.1";

  /**
   * The IP address of the server that sends data to the robot. For the beta demo
   * and competition, replace this line with
   * 
   * <p>
   * {@code public static final String SERVER_IP = DEFAULT_SERVER_IP;}
   */
  public static final String SERVER_IP = "127.0.0.1"; // = DEFAULT_SERVER_IP;

  /** Your team number. */
  public static final int TEAM_NUMBER = 9;

  /** Enables printing of debug info from the WiFi class. */
  public static final boolean ENABLE_DEBUG_WIFI_PRINT = true;

  /**
   * Enable this to attempt to receive Wi-Fi parameters at the start of the
   * program.
   */
  public static final boolean RECEIVE_WIFI_PARAMS = true;

  // Simulation-related constants

  /** The time between physics steps in milliseconds. */
  public static final int PHYSICS_STEP_PERIOD = 500; // ms

  /** The relative path of the input vector file. */
  public static final Path VECTORS_FILE = Paths.get("vectors.txt");

  // ----------------------------- DECLARE YOUR CURRENT RESOURCES HERE
  // -----------------------------
  // ----------------------------- eg, constants, motors, sensors, etc
  // -----------------------------

  // Robot constants

  /** Timeout period in milliseconds. */
  public static final int TIMEOUT_PERIOD = 3000;

  /** The maximum distance detected by the ultrasonic sensor, in cm. */
  public static final int MAX_SENSOR_DIST = 255;

  /**
   * The limit of invalid samples that we read from the US sensor before assuming
   * no obstacle.
   */
  public static final int INVALID_SAMPLE_LIMIT = 20;

  /** The wheel radius in meters. */
  public static final double WHEEL_RAD = 0.021;

  /** The robot width in meters. */
  public static final double BASE_WIDTH = 0.12505; // was 0.155

  /** The distance between the color sensors and the wheels in meters. */
  public static final double COLOR_SENSOR_TO_WHEEL_DIST = 0;

  /** The speed at which the robot moves forward in degrees per second. */
  public static final int FORWARD_SPEED = 300;

  /** The speed at which the robot moves forward slowly in degrees per second. */
  public static final int LOWER_FORWARD_SPEED = 150;

  /** The speed at which the robot rotates in degrees per second. */
  public static final int ROTATE_SPEED = 200;

  /** The motor acceleration in degrees per second squared. */
  public static final int ACCELERATION = 3000;

  /** The tile size in meters. Note that 0.3048 m = 1 ft. */
  public static final double TILE_SIZE = 0.3048;

  /**
   * Robot offset from Block when block is on grid point in meters. Used to stop
   * robot after pushing block.
   */
  public static final double BLOCK_OFFSET = 0.1498;

  /** Length of a block in meters. */
  public static final double BLOCK_LENGTH = 0.1;

  // Hardware resources

  /** The left motor. */
  public static final RegulatedMotor leftMotor = Motor.A;

  /** The right motor. */
  public static final RegulatedMotor rightMotor = Motor.D;

  /** The medium motor. */
  public static final RegulatedMotor cageMotor = Motor.B;

  /** The ultrasonic sensor. */
  public static final EV3UltrasonicSensor usSensor1 = new EV3UltrasonicSensor(SensorPort.S1);

  public static EV3UltrasonicSensor usSensor2;

  /** The color sensor sample provider. */
  public static final SampleProvider colorSensorLeft = new EV3ColorSensor(SensorPort.S2).getRGBMode();

  public static final SampleProvider colorSensorRight = new EV3ColorSensor(SensorPort.S3).getRGBMode();

  public static SampleProvider colorSensorFront;

  /** Initializes the used sensors */
  public static void initSensors() {
    switch (Search.getMode()) {
      case Recognize: {
        usSensor2 = new EV3UltrasonicSensor(SensorPort.S4);
        Search.setUsData2(new float[usSensor2.sampleSize()]);
        break;
      }
      case Memorize: {
        colorSensorFront = new EV3ColorSensor(SensorPort.S4).getRGBMode();
        Search.setColorSensorDataFront(new float[colorSensorFront.sampleSize()]);
        break;
      }
    }
  }

  /** The differential minimal margin object. */
  public static DifferentialMinimalMargin dmm = new DifferentialMinimalMargin();

  /** The starting corner. */
  public static int corner;

  /** The edge when facing the ramp. */
  public static RampEdge ramp;

  /** The start Zone. */
  public static Region startZone;

  /** The tunnel footprint. */
  public static Region tunnel;

  /** The red search zone. */
  public static Region searchZone;

  // Software singletons

  /** The odometer. */
  public static Odometer odometer = Odometer.getOdometer();

  /** The obstacle Avoidance. */
  public static ObstacleAvoidance obstacleavoidance = ObstacleAvoidance.getAvoider();

  public static void initializeResources() {
    makeGreenRamp();
    makeRedRamp();
  }

  /** The Red ramp bounding box and path plan. */
  public static RampRect rrpp;
  public static RampRect rrbb;
  public static double rFacingX;
  public static double rFacingY;

  private static void makeRedRamp() {
    rFacingX = -Math.signum(Resources.rr.right.y - Resources.rr.left.y);
    rFacingY = Math.signum(Resources.rr.right.x - Resources.rr.left.x);

    final double OFFSETPP = 1;
    final double OFFSETBB = 0.25;

    if (rFacingY != 0) {
      if (rFacingY > 0) {
        Resources.rrpp = new RampRect(new Point(Resources.rr.left.x - OFFSETPP, Resources.rr.left.y - OFFSETPP),
            new Point(Resources.rr.right.x + OFFSETPP, Resources.rr.right.y + OFFSETPP + 2));

        Resources.rrbb = new RampRect(new Point(Resources.rr.left.x - OFFSETBB, Resources.rr.left.y - OFFSETBB),
            new Point(Resources.rr.right.x + OFFSETPP, Resources.rr.right.y + OFFSETBB + 2));

        Resources.rrpp.setFrontLeft(rrpp.ll);
        Resources.rrpp.setFrontRight(new Point(rrpp.ur.x, rrpp.ll.y));
      }
      if (rFacingY < 0) {
        Resources.rrpp = new RampRect(new Point(Resources.rr.right.x - OFFSETPP, Resources.rr.right.y - OFFSETPP - 2),
            new Point(Resources.rr.left.x + OFFSETPP, Resources.rr.left.y + OFFSETPP)); // Ramp

        Resources.rrbb = new RampRect(new Point(Resources.rr.right.x - OFFSETBB, Resources.rr.right.y - OFFSETBB - 2),
            new Point(Resources.rr.left.x + OFFSETBB, Resources.rr.left.y + OFFSETBB)); // Ramp

        Resources.rrpp.setFrontLeft(rrpp.ur);
        Resources.rrpp.setFrontRight(new Point(rrpp.ll.x, rrpp.ur.y));
      }
    }

    if (rFacingX != 0) {
      if (rFacingX > 0) {
        Resources.rrpp = new RampRect(new Point(Resources.rr.right.x - OFFSETPP, Resources.rr.right.y - OFFSETPP),
            new Point(Resources.rr.left.x + OFFSETPP + 2, Resources.rr.left.y + OFFSETPP)); // Ramp

        Resources.rrbb = new RampRect(new Point(Resources.rr.right.x - OFFSETBB, Resources.rr.right.y - OFFSETBB),
            new Point(Resources.rr.left.x + OFFSETBB + 2, Resources.rr.left.y + OFFSETBB)); // Ramp

        Resources.rrpp.setFrontLeft(new Point(rrpp.ll.x, rrpp.ur.y));
        Resources.rrpp.setFrontRight(rrpp.ll);
      }
      if (rFacingX < 0) {
        Resources.rrpp = new RampRect(new Point(Resources.rr.left.x - OFFSETPP - 2, Resources.rr.left.y - OFFSETPP),
            new Point(Resources.rr.right.x + OFFSETPP, Resources.rr.right.y + OFFSETPP)); // Ramp

        Resources.rrbb = new RampRect(new Point(Resources.rr.left.x - OFFSETBB - 2, Resources.rr.left.y - OFFSETBB),
            new Point(Resources.rr.right.x + OFFSETBB, Resources.rr.right.y + OFFSETBB)); // Ramp

        Resources.rrpp.setFrontLeft(new Point(rrpp.ur.x, rrpp.ll.y));
        Resources.rrpp.setFrontRight(rrpp.ur);
      }
    }
  }

  /** The Green ramp bounding box and path plan. */
public static RampRect grpp;
public static RampRect grbb;
public static double gFacingX;
public static double gFacingY;

private static void makeGreenRamp() {
  rFacingX = -Math.signum(Resources.gr.right.y - Resources.gr.left.y);
  rFacingY = Math.signum(Resources.gr.right.x - Resources.gr.left.x);

  final double OFFSETPP = 0.5;
  final double OFFSETBB = 0.25;

  if (rFacingY != 0) {
    if (rFacingY > 0) {
      Resources.grpp = new RampRect(new Point(Resources.gr.left.x - OFFSETPP, Resources.gr.left.y - OFFSETPP),
          new Point(Resources.gr.right.x + OFFSETPP, Resources.gr.right.y + OFFSETPP + 2));

      Resources.grbb = new RampRect(new Point(Resources.gr.left.x - OFFSETBB, Resources.gr.left.y - OFFSETBB),
          new Point(Resources.gr.right.x + OFFSETPP, Resources.gr.right.y + OFFSETBB + 2));

      Resources.grpp.setFrontLeft(grpp.ll);
      Resources.grpp.setFrontRight(new Point(grpp.ur.x, grpp.ll.y));
    }
    if (rFacingY < 0) {
      Resources.grpp = new RampRect(new Point(Resources.gr.right.x - OFFSETPP, Resources.gr.right.y - OFFSETPP - 2),
          new Point(Resources.gr.left.x + OFFSETPP, Resources.gr.left.y + OFFSETPP)); // Ramp

      Resources.grbb = new RampRect(new Point(Resources.gr.right.x - OFFSETBB, Resources.gr.right.y - OFFSETBB - 2),
          new Point(Resources.gr.left.x + OFFSETBB, Resources.gr.left.y + OFFSETBB)); // Ramp

      Resources.grpp.setFrontLeft(grpp.ur);
      Resources.grpp.setFrontRight(new Point(grpp.ll.x, grpp.ur.y));
    }
  }

  if (rFacingX != 0) {
    if (rFacingX > 0) {
      Resources.grpp = new RampRect(new Point(Resources.gr.right.x - OFFSETPP, Resources.gr.right.y - OFFSETPP),
          new Point(Resources.gr.left.x + OFFSETPP + 2, Resources.gr.left.y + OFFSETPP)); // Ramp

      Resources.grbb = new RampRect(new Point(Resources.gr.right.x - OFFSETBB, Resources.gr.right.y - OFFSETBB),
          new Point(Resources.gr.left.x + OFFSETBB + 2, Resources.gr.left.y + OFFSETBB)); // Ramp

      Resources.grpp.setFrontLeft(new Point(grpp.ll.x, grpp.ur.y));
      Resources.grpp.setFrontRight(grpp.ll);
    }
    if (rFacingX < 0) {
      Resources.grpp = new RampRect(new Point(Resources.gr.left.x - OFFSETPP - 2, Resources.gr.left.y - OFFSETPP),
          new Point(Resources.gr.right.x + OFFSETPP, Resources.gr.right.y + OFFSETPP)); // Ramp

      Resources.grbb = new RampRect(new Point(Resources.gr.left.x - OFFSETBB - 2, Resources.gr.left.y - OFFSETBB),
          new Point(Resources.gr.right.x + OFFSETBB, Resources.gr.right.y + OFFSETBB)); // Ramp

      Resources.grpp.setFrontLeft(new Point(grpp.ur.x, grpp.ll.y));
      Resources.grpp.setFrontRight(grpp.ur);
    }
  }
}

  // Wi-Fi parameters

  /** Container for the Wi-Fi parameters. */
  public static Map<String, Object> wifiParameters;

  // This static initializer MUST be declared before any Wi-Fi parameters.
  static {
    receiveWifiParameters();
  }

  /** Red team number. */
  public static int redTeam = getWP("RedTeam");

  /** Red team's starting corner. */
  public static int redCorner = getWP("RedCorner");

  /** Green team number. */
  public static int greenTeam = getWP("GreenTeam");

  /** Green team's starting corner. */
  public static int greenCorner = getWP("GreenCorner");

  /** The edge when facing the Red ramp. */
  public static RampEdge rr = makeRampEdge("RR");

  /** The edge when facing the Green ramp. */
  public static RampEdge gr = makeRampEdge("GR");

  /** The Red Zone. */
  public static Region red = makeRegion("Red");

  /** The Green Zone. */
  public static Region green = makeRegion("Green");

  /** The Island. */
  public static Region island = makeRegion("Island");

  /** The red tunnel footprint. */
  public static Region tnr = makeRegion("TNR");

  /** The green tunnel footprint. */
  public static Region tng = makeRegion("TNG");

  /** The red search zone. */
  public static Region szr = makeRegion("SZR");

  /** The green search zone. */
  public static Region szg = makeRegion("SZG");

  static {
    if (Resources.redTeam == Resources.TEAM_NUMBER) {
      corner = redCorner;
      ramp = rr;
      startZone = red;
      tunnel = tnr;
      searchZone = szr;
    } else {
      corner = greenCorner;
      ramp = gr;
      startZone = green;
      tunnel = tng;
      searchZone = szg;
    }
  }

  /**
   * Receives Wi-Fi parameters from the server program.
   */
  public static void receiveWifiParameters() {
    // Only initialize the parameters if needed
    if (!RECEIVE_WIFI_PARAMS || wifiParameters != null) {
      return;
    }
    System.out.println("Waiting to receive Wi-Fi parameters.");

    // Connect to server and get the data, catching any errors that might occur
    try (var conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT)) {
      /*
       * Connect to the server and wait until the user/TA presses the "Start" button
       * in the GUI on their laptop with the data filled in.
       */
      wifiParameters = conn.getData();
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }

  /**
   * Returns the Wi-Fi parameter int value associated with the given key.
   * 
   * @param key the Wi-Fi parameter key
   * @return the Wi-Fi parameter int value associated with the given key
   */
  public static int getWP(String key) {
    if (wifiParameters != null) {
      return ((BigDecimal) wifiParameters.get(key)).intValue();
    } else {
      return 0;
    }
  }

  /** Makes a point given a Wi-Fi parameter prefix. */
  public static Point makePoint(String paramPrefix) {
    return new Point(getWP(paramPrefix + "_x"), getWP(paramPrefix + "_y"));
  }

  /** Makes a ramp edge given a Wi-Fi parameter prefix. */
  public static RampEdge makeRampEdge(String paramPrefix) {
    return new RampEdge(makePoint(paramPrefix + "L"), makePoint(paramPrefix + "R"));
  }

  /** Makes a region given a Wi-Fi parameter prefix. */
  public static Region makeRegion(String paramPrefix) {
    return new Region(makePoint(paramPrefix + "_LL"), makePoint(paramPrefix + "_UR"));
  }

}
