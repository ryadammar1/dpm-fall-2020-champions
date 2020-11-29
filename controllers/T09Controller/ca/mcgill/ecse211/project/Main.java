package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static simlejos.ExecutionController.*;

import java.lang.Thread;
import simlejos.hardware.ev3.LocalEV3;

/**
 * Main class of the program.
 * 
 * TODO Describe your project overview in detail here (in this Javadoc comment).
 */
public class Main {

  public static final int NUM_BLOCKS = 1; // Number of blocks to transfer before retiring
  public static final StateMachine STATE_MACHINE = new StateMachine();

  /**
   * The number of threads used in the program (main, odometer), other than the
   * one used to perform physics steps.
   */
  public static final int NUMBER_OF_THREADS = 3;

  public static long startTime = 0;

  /** Main entry point. */
  public static void main(String[] args) {
    // Poll states and call corresponding functions
    while (true) { // main loop
      if (System.currentTimeMillis() - startTime >= 180000)
        STATE_MACHINE.timeUp();
      switch (STATE_MACHINE.getStatusFullName()) {
        case ("Standard.Initialization.Configuration"): {
          System.out.println("Configuring");

          STATE_MACHINE.setNumBlocks(NUM_BLOCKS);

          startTime = System.currentTimeMillis();

          initializeAll();

          // Start the odometer and obstacle avoidance thread
          new Thread(odometer).start();

          new Thread(obstacleavoidance).start();
          obstacleavoidance.pause();

          LocalEV3.getAudio().beep(); // beeps once

          STATE_MACHINE.doneConfiguring();
          break;
        }
        case ("Standard.Initialization.Localization"): {
          System.out.println("Localizing");
          UltrasonicLocalizer.localize();
          LightSensorCalibration.calibrate();
          LightLocalizer.localize();
          switch (Resources.corner) {
            case 0:
              odometer.setX(1 * TILE_SIZE);
              odometer.setY(1 * TILE_SIZE);
              odometer.setTheta(0);
              break;
            case 1:
              odometer.setX(14 * TILE_SIZE);
              odometer.setY(1 * TILE_SIZE);
              odometer.setTheta(270);
              break;
            case 2:
              odometer.setX(14 * TILE_SIZE);
              odometer.setY(8 * TILE_SIZE);
              odometer.setTheta(180);
              break;
            case 3:
              odometer.setX(1 * TILE_SIZE);
              odometer.setY(8 * TILE_SIZE);
              odometer.setTheta(90);
              break;
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
          STATE_MACHINE.doneLocalizing();
          break;
        }
        case ("Standard.Initialization.EntryField"): {
          System.out.println(STATE_MACHINE.getStatusFullName());
          obstacleavoidance.resume(15);
          Avoidance.setThreshold(33);
          odometer.printPosition();
          System.out.println("Entering field");
          FieldEntry.enterField();
          obstacleavoidance.pause();
          break;
        }
        case ("Standard.Operation.Search"): {
          System.out.println("Searching");
          Search.doSearch();
          break;
        }
        case ("Standard.Operation.Transfer"): {
          System.out.println("Transfering");
          Avoidance.setThreshold(40);
          Transfer.doTransfer();
          obstacleavoidance.pause();
          break;
        }
        case ("Standard.Termination.ExitField"):
          obstacleavoidance.resume(15);
          Avoidance.setThreshold(33);
          System.out.println("Exiting field");
          FieldExit.exitField();
          obstacleavoidance.pause();
          break;
        case ("Avoidance"): {
          System.out.println("Avoiding");
          Avoidance.correct();
          STATE_MACHINE.obstacleAvoided();
          break;
        }
        default:
          break;
      }
    }
  }

  /**
   * Example using WifiConnection to communicate with a server and receive data
   * concerning the competition such as the starting corner the robot is placed
   * in.<br>
   * 
   * <p>
   * Keep in mind that this class is an <b>example</b> of how to use the Wi-Fi
   * code; you must use the WifiConnection class yourself in your own code as
   * appropriate. In this example, we simply show how to get and process different
   * types of data.<br>
   * 
   * <p>
   * There are two variables you MUST set manually (in Resources.java) before
   * using this code:
   * 
   * <ol>
   * <li>SERVER_IP: The IP address of the computer running the server application.
   * This will be your own laptop, until the beta beta demo or competition where
   * this is the TA or professor's laptop. In that case, set the IP to the default
   * (indicated in Resources).</li>
   * <li>TEAM_NUMBER: your project team number.</li>
   * </ol>
   * 
   * <p>
   * Note: You can disable printing from the Wi-Fi code via
   * ENABLE_DEBUG_WIFI_PRINT.
   * 
   * @author Michael Smith, Tharsan Ponnampalam, Younes Boubekeur, Olivier
   *         St-Martin Cormier
   */
  public static void wifiExample() {
    System.out.println("Running...");

    // Example 1: Print out all received data
    System.out.println("Map:\n" + wifiParameters);

    // Example 2: Print out specific values
    System.out.println("Red Team: " + redTeam);
    System.out.println("Green Zone: " + green);
    System.out.println("Island Zone, upper right: " + island.ur);
    System.out.println("Red tunnel footprint, lower left y value: " + tnr.ll.y);

    // Example 3: Compare value
    if (szg.ll.x >= island.ll.x && szg.ll.y >= island.ll.y) {
      System.out.println("The green search zone is on the island.");
    } else {
      System.err.println("The green search zone is in the water!");
    }

    // Example 4: Calculate the area of a region
    System.out.println("The island area is " + island.getWidth() * island.getHeight() + ".");
  }

  private static void initializeAll() {
    Main.initialize();
    Resources.initializeResources();
    Search.initializeSearch();
    ObstacleAvoidance.initializeObstacleAvoidance();
    Avoidance.initializeAvoidance();
    FieldEntry.setTunnelAndSearchZone();
    FieldExit.setTunnelAndSearchZone();
  }

  /**
   * Initializes the robot logic. It starts a new thread to perform physics steps
   * regularly.
   */
  private static void initialize() {
    // Run a few physics steps to make sure everything is initialized and has
    // settled properly
    for (int i = 0; i < 50; i++) {
      performPhysicsStep();
    }

    // We are going to start two threads, so the total number of parties is 2
    setNumberOfParties(NUMBER_OF_THREADS);

    // Does not count as a thread because it is only for physics steps
    new Thread(() -> {
      while (performPhysicsStep()) {
        sleepFor(PHYSICS_STEP_PERIOD);
      }
    }).start();
  }

}
