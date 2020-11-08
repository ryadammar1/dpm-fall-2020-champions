package ca.mcgill.ecse211.project;

public class StateMachine {

  // ------------------------
  // MEMBER VARIABLES
  // ------------------------

  // StateMachine Attributes
  private int numTransfers;
  private int numBlocks;
  private boolean blockDetected;

  // StateMachine State Machines
  public enum Status {
    Standard, Avoidance
  }
  public enum StatusStandard {
    Null, Initialization, Operation, Termination, HStar
  }
  public enum StatusStandardInitialization {
    Null, Configuration, Localization, EntryField, HStar
  }
  public enum StatusStandardOperation {
    Null, Search, Transfer, HStar
  }
  public enum StatusStandardTermination {
    Null, ExitField, HStar
  }

  private Status status;
  private StatusStandard statusStandard;
  private StatusStandard statusStandardHStar;
  private StatusStandardInitialization statusStandardInitialization;
  private StatusStandardInitialization statusStandardInitializationHStar;
  private StatusStandardOperation statusStandardOperation;
  private StatusStandardOperation statusStandardOperationHStar;
  private StatusStandardTermination statusStandardTermination;
  private StatusStandardTermination statusStandardTerminationHStar;

  // ------------------------
  // CONSTRUCTOR
  // ------------------------

  public StateMachine() {
    numTransfers = 0;
    numBlocks = 0;
    blockDetected = false;
    setStatusStandard(StatusStandard.Null);
    statusStandardHStar = StatusStandard.Initialization;
    setStatusStandardInitialization(StatusStandardInitialization.Null);
    statusStandardInitializationHStar = StatusStandardInitialization.Configuration;
    setStatusStandardOperation(StatusStandardOperation.Null);
    statusStandardOperationHStar = StatusStandardOperation.Search;
    setStatusStandardTermination(StatusStandardTermination.Null);
    statusStandardTerminationHStar = StatusStandardTermination.ExitField;
    setStatus(Status.Standard);
  }

  // ------------------------
  // INTERFACE
  // ------------------------

  public boolean setNumTransfers(int aNumTransfers) {
    boolean wasSet = false;
    numTransfers = aNumTransfers;
    wasSet = true;
    return wasSet;
  }

  public boolean setNumBlocks(int aNumBlocks) {
    boolean wasSet = false;
    numBlocks = aNumBlocks;
    wasSet = true;
    return wasSet;
  }

  public boolean setBlockDetected(boolean aBlockDetected) {
    boolean wasSet = false;
    blockDetected = aBlockDetected;
    wasSet = true;
    return wasSet;
  }

  public int getNumTransfers() {
    return numTransfers;
  }

  public int getNumBlocks() {
    return numBlocks;
  }

  public boolean getBlockDetected() {
    return blockDetected;
  }

  /* Code from template attribute_IsBoolean */
  public boolean isBlockDetected() {
    return blockDetected;
  }

  public String getStatusFullName() {
    String answer = status.toString();
    if (statusStandard != StatusStandard.Null) {
      answer += "." + statusStandard.toString();
    }
    if (statusStandardInitialization != StatusStandardInitialization.Null) {
      answer += "." + statusStandardInitialization.toString();
    }
    if (statusStandardOperation != StatusStandardOperation.Null) {
      answer += "." + statusStandardOperation.toString();
    }
    if (statusStandardTermination != StatusStandardTermination.Null) {
      answer += "." + statusStandardTermination.toString();
    }
    return answer;
  }

  public Status getStatus() {
    return status;
  }

  public StatusStandard getStatusStandard() {
    return statusStandard;
  }

  public StatusStandardInitialization getStatusStandardInitialization() {
    return statusStandardInitialization;
  }

  public StatusStandardOperation getStatusStandardOperation() {
    return statusStandardOperation;
  }

  public StatusStandardTermination getStatusStandardTermination() {
    return statusStandardTermination;
  }

  public boolean obstacleAvoided() {
    boolean wasEventProcessed = false;

    Status aStatus = status;
    switch (aStatus) {
      case Avoidance:
        setStatusStandard(StatusStandard.HStar);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean timeUp() {
    boolean wasEventProcessed = false;

    StatusStandard aStatusStandard = statusStandard;
    if (statusStandard != StatusStandard.Null) {
      statusStandardHStar = statusStandard;
    }
    switch (aStatusStandard) {
      case Operation:
        exitStatusStandard();
        setStatusStandard(StatusStandard.Termination);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean doneConfiguring() {
    boolean wasEventProcessed = false;

    StatusStandardInitialization aStatusStandardInitialization = statusStandardInitialization;
    if (statusStandardInitialization != StatusStandardInitialization.Null) {
      statusStandardInitializationHStar = statusStandardInitialization;
    }
    switch (aStatusStandardInitialization) {
      case Configuration:
        exitStatusStandardInitialization();
        setStatusStandardInitialization(StatusStandardInitialization.Localization);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean doneLocalizing() {
    boolean wasEventProcessed = false;

    StatusStandardInitialization aStatusStandardInitialization = statusStandardInitialization;
    if (statusStandardInitialization != StatusStandardInitialization.Null) {
      statusStandardInitializationHStar = statusStandardInitialization;
    }
    switch (aStatusStandardInitialization) {
      case Localization:
        exitStatusStandardInitialization();
        setStatusStandardInitialization(StatusStandardInitialization.EntryField);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean detectObstacle() {
    boolean wasEventProcessed = false;

    StatusStandardInitialization aStatusStandardInitialization = statusStandardInitialization;
    if (statusStandardInitialization != StatusStandardInitialization.Null) {
      statusStandardInitializationHStar = statusStandardInitialization;
    }
    StatusStandardOperation aStatusStandardOperation = statusStandardOperation;
    if (statusStandardOperation != StatusStandardOperation.Null) {
      statusStandardOperationHStar = statusStandardOperation;
    }
    StatusStandardTermination aStatusStandardTermination = statusStandardTermination;
    if (statusStandardTermination != StatusStandardTermination.Null) {
      statusStandardTerminationHStar = statusStandardTermination;
    }
    switch (aStatusStandardInitialization) {
      case EntryField:
        exitStatus();
        setStatus(Status.Avoidance);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    switch (aStatusStandardOperation) {
      case Search:
        if (!blockDetected) {
          exitStatus();
          setStatus(Status.Avoidance);
          wasEventProcessed = true;
          break;
        }
        if (blockDetected) {
          exitStatusStandardOperation();
          setStatusStandardOperation(StatusStandardOperation.Transfer);
          wasEventProcessed = true;
          break;
        }
        break;
      case Transfer:
        exitStatus();
        setStatus(Status.Avoidance);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    switch (aStatusStandardTermination) {
      case ExitField:
        exitStatus();
        setStatus(Status.Avoidance);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean enteredField() {
    boolean wasEventProcessed = false;

    StatusStandardInitialization aStatusStandardInitialization = statusStandardInitialization;
    if (statusStandardInitialization != StatusStandardInitialization.Null) {
      statusStandardInitializationHStar = statusStandardInitialization;
    }
    switch (aStatusStandardInitialization) {
      case EntryField:
        exitStatusStandard();
        setStatusStandard(StatusStandard.Operation);
        wasEventProcessed = true;
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  public boolean blockTransfered() {
    boolean wasEventProcessed = false;

    StatusStandardOperation aStatusStandardOperation = statusStandardOperation;
    if (statusStandardOperation != StatusStandardOperation.Null) {
      statusStandardOperationHStar = statusStandardOperation;
    }
    switch (aStatusStandardOperation) {
      case Transfer:
        if (numTransfers < numBlocks) {
          exitStatusStandardOperation();
          setStatusStandardOperation(StatusStandardOperation.Search);
          wasEventProcessed = true;
          break;
        }
        if (numTransfers >= numBlocks) {
          exitStatusStandard();
          setStatusStandard(StatusStandard.Termination);
          wasEventProcessed = true;
          break;
        }
        break;
      default:
        // Other states do respond to this event
    }

    return wasEventProcessed;
  }

  private void exitStatus() {
    switch (status) {
      case Standard:
        exitStatusStandard();
        break;
    }
  }

  private void setStatus(Status aStatus) {
    status = aStatus;

    // entry actions and do activities
    switch (status) {
      case Standard:
        if (statusStandard == StatusStandard.Null) {
          setStatusStandard(StatusStandard.Initialization);
        }
        break;
    }
  }

  private void exitStatusStandard() {
    switch (statusStandard) {
      case Initialization:
        exitStatusStandardInitialization();
        setStatusStandard(StatusStandard.Null);
        break;
      case Operation:
        exitStatusStandardOperation();
        setStatusStandard(StatusStandard.Null);
        break;
      case Termination:
        exitStatusStandardTermination();
        setStatusStandard(StatusStandard.Null);
        break;
      case HStar:
        setStatusStandard(StatusStandard.Null);
        break;
    }
  }

  private void setStatusStandard(StatusStandard aStatusStandard) {
    statusStandard = aStatusStandard;
    if (status != Status.Standard && aStatusStandard != StatusStandard.Null) {
      setStatus(Status.Standard);
    }

    // entry actions and do activities
    switch (statusStandard) {
      case Initialization:
        // line 7 "model.ump"
        statusStandardHStar = StatusStandard.Initialization;
        if (statusStandardInitialization == StatusStandardInitialization.Null) {
          setStatusStandardInitialization(StatusStandardInitialization.Configuration);
        }
        break;
      case Operation:
        // line 22 "model.ump"
        statusStandardHStar = StatusStandard.Operation;
        if (statusStandardOperation == StatusStandardOperation.Null) {
          setStatusStandardOperation(StatusStandardOperation.Search);
        }
        break;
      case Termination:
        // line 40 "model.ump"
        statusStandardHStar = StatusStandard.Termination;
        if (statusStandardTermination == StatusStandardTermination.Null) {
          setStatusStandardTermination(StatusStandardTermination.ExitField);
        }
        break;
      case HStar:
        if (statusStandardHStar == StatusStandard.Initialization) {
          if (statusStandardInitialization == StatusStandardInitialization.Null) {
            setStatusStandardInitialization(StatusStandardInitialization.HStar);
          }
        }
        if (statusStandardHStar == StatusStandard.Operation) {
          if (statusStandardOperation == StatusStandardOperation.Null) {
            setStatusStandardOperation(StatusStandardOperation.HStar);
          }
        }
        if (statusStandardHStar == StatusStandard.Termination) {
          if (statusStandardTermination == StatusStandardTermination.Null) {
            setStatusStandardTermination(StatusStandardTermination.HStar);
          }
        }
        break;
    }
    if (aStatusStandard == StatusStandard.HStar) {
      statusStandard = statusStandardHStar;
    }
  }

  private void exitStatusStandardInitialization() {
    switch (statusStandardInitialization) {
      case Configuration:
        setStatusStandardInitialization(StatusStandardInitialization.Null);
        break;
      case Localization:
        setStatusStandardInitialization(StatusStandardInitialization.Null);
        break;
      case EntryField:
        setStatusStandardInitialization(StatusStandardInitialization.Null);
        break;
      case HStar:
        setStatusStandardInitialization(StatusStandardInitialization.Null);
        break;
    }
  }

  private void setStatusStandardInitialization(StatusStandardInitialization aStatusStandardInitialization) {
    statusStandardInitialization = aStatusStandardInitialization;
    if (statusStandard != StatusStandard.Initialization
        && aStatusStandardInitialization != StatusStandardInitialization.Null) {
      setStatusStandard(StatusStandard.Initialization);
    }
    if (aStatusStandardInitialization == StatusStandardInitialization.HStar) {
      statusStandardInitialization = statusStandardInitializationHStar;
    }
  }

  private void exitStatusStandardOperation() {
    switch (statusStandardOperation) {
      case Search:
        setStatusStandardOperation(StatusStandardOperation.Null);
        break;
      case Transfer:
        setStatusStandardOperation(StatusStandardOperation.Null);
        break;
      case HStar:
        setStatusStandardOperation(StatusStandardOperation.Null);
        break;
    }
  }

  private void setStatusStandardOperation(StatusStandardOperation aStatusStandardOperation) {
    statusStandardOperation = aStatusStandardOperation;
    if (statusStandard != StatusStandard.Operation && aStatusStandardOperation != StatusStandardOperation.Null) {
      setStatusStandard(StatusStandard.Operation);
    }

    // entry actions and do activities
    switch (statusStandardOperation) {
      case Transfer:
        // line 29 "model.ump"
        numTransfers++;
        break;
    }
    if (aStatusStandardOperation == StatusStandardOperation.HStar) {
      statusStandardOperation = statusStandardOperationHStar;
    }
  }

  private void exitStatusStandardTermination() {
    switch (statusStandardTermination) {
      case ExitField:
        setStatusStandardTermination(StatusStandardTermination.Null);
        break;
      case HStar:
        setStatusStandardTermination(StatusStandardTermination.Null);
        break;
    }
  }

  private void setStatusStandardTermination(StatusStandardTermination aStatusStandardTermination) {
    statusStandardTermination = aStatusStandardTermination;
    if (statusStandard != StatusStandard.Termination && aStatusStandardTermination != StatusStandardTermination.Null) {
      setStatusStandard(StatusStandard.Termination);
    }
    if (aStatusStandardTermination == StatusStandardTermination.HStar) {
      statusStandardTermination = statusStandardTerminationHStar;
    }
  }

  public void delete() {}


  public String toString() {
    return super.toString() + "[" + "numTransfers" + ":" + getNumTransfers() + "," + "numBlocks" + ":" + getNumBlocks()
        + "," + "blockDetected" + ":" + getBlockDetected() + "]";
  }
}
