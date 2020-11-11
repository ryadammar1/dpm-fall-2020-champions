package ca.mcgill.ecse211.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.mcgill.ecse211.project.StateMachine;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests the StateMachine class.
 * 
 * @author Ryad Ammar
 */
@TestMethodOrder(OrderAnnotation.class)
public class TestStateMachine {

  @Test
  void testStateMachine1() {

    StateMachine SM = new StateMachine();

    // Simulation starts, the robot performs initialization
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Configuration");

    SM.doneConfiguring();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Localization");

    SM.doneLocalizing();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.EntryField");

    // The robot has entered the field, it is now searching for blocks
    SM.enteredField();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Search");

    // Robot detects obstacle
    // Obstacle is a block
    // Robot transfers the block
    SM.setBlockDetected(true);
    SM.detectObstacle();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Transfer");

    // While transferring robot detects an obstacle
    // Robot avoids obstacle whether it is a block or not
    SM.detectObstacle();
    assertEquals(SM.getStatusFullName(), "Avoidance");

    // Block is avoided, robot continues transferring the block
    SM.obstacleAvoided();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Transfer");

  }

  @Test
  void testStateMachine2() {
    StateMachine SM = new StateMachine();

    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Configuration");

    SM.doneConfiguring();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Localization");

    SM.doneLocalizing();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.EntryField");

    SM.enteredField();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Search");

    SM.setBlockDetected(false);
    SM.detectObstacle();
    assertEquals(SM.getStatusFullName(), "Avoidance");

    SM.obstacleAvoided();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Search");

  }

  @Test
  void testStateMachine3() {
    StateMachine SM = new StateMachine();

    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Configuration");

    SM.doneConfiguring();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Localization");

    SM.doneLocalizing();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.EntryField");

    SM.setBlockDetected(false);
    SM.detectObstacle();
    assertEquals(SM.getStatusFullName(), "Avoidance");

    SM.obstacleAvoided();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.EntryField");

  }

  @Test
  void testStateMachine4() {
    StateMachine SM = new StateMachine();

    SM.setNumTransfers(4);
    SM.setNumBlocks(5);

    assertEquals(SM.getNumTransfers(), 4);

    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Configuration");

    SM.doneConfiguring();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Localization");

    SM.doneLocalizing();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.EntryField");

    SM.enteredField();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Search");

    SM.setBlockDetected(true);
    SM.detectObstacle();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Transfer");

    SM.blockTransfered();
    assertEquals(SM.getNumTransfers(), 5);
    assertEquals(SM.getStatusFullName(), "Standard.Termination.ExitField");

  }

  @Test
  void testStateMachine5() {
    StateMachine SM = new StateMachine();

    SM.setNumTransfers(3);
    SM.setNumBlocks(5);

    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Configuration");

    SM.doneConfiguring();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.Localization");

    SM.doneLocalizing();
    assertEquals(SM.getStatusFullName(), "Standard.Initialization.EntryField");

    SM.enteredField();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Search");

    SM.setBlockDetected(true);
    SM.detectObstacle();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Transfer");

    SM.blockTransfered();
    assertEquals(SM.getStatusFullName(), "Standard.Operation.Search");

  }

}
