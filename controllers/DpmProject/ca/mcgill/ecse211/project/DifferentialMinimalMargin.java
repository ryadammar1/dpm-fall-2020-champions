package ca.mcgill.ecse211.project;

/**
 * 
 * @author nafiz
 * A general purpose class for obtaining discrete differential 
 * where it matters based on a  minimal margin of absolute difference 
 * from a noisy signal
 */
public class DifferentialMinimalMargin {
    // max value samples
    private double max;
    // sum of values sampled (used for calculating average)
    private double sum;
    // size of sample (used for calculating average)
    private int size;

    // previous value
    private double prevValue;

    // minimal margin for absolute differential
    private double minMargin;
    
    /**
     * copy constructor
     * @param dmm an instance of DifferentialMinimalMargin
     */
    public DifferentialMinimalMargin(DifferentialMinimalMargin dmm) {
        this.max = dmm.max;
        this.sum = dmm.sum;
        this.size = dmm.size;
        this.prevValue = dmm.prevValue;
        this.minMargin = dmm.minMargin;
    }
    
    public DifferentialMinimalMargin() {
    }
    
    /**
     * Calculates new minMargin if at least 1 sample got collected.
     * Must set prevValue before calling this method.
     * @param newValue new value
     * @return returns 0 if absolute difference is below minimal margin, the actual difference otherwise
     */
    public double calibrate(double newValue) {              
        final double diff = newValue - prevValue;
        max = Math.max(max, Math.abs(diff));
        sum += Math.abs(diff);
        ++size;
        
        if (size > 0) {
            double avg = sum / size;
            minMargin = (max - avg) / 2 + avg;
        }
        
        return differential(newValue);
    }
    
    /**
     * Calculates useful difference between prevValue and newValue based on minimal margin.
     * Must set prevValue before calling this method.
     * @param newValue
     * @return returns 0 if absolute difference is below minimal margin, the actual difference otherwise
     */
    public double differential(double newValue) {
        final double diff = newValue - prevValue;
        return Math.abs(diff) < minMargin ? 0 : diff;
    }

    /**
     * @return the minimal margin
     */
    public double getMinMargin() {
        return minMargin;
    }
    
    /**
     * @param minMargin
     */
    public void setMinMargin(double minMargin) {
        this.minMargin = minMargin;
    }
    
    /**
     * 
     * @return the max difference found
     */
    public double getMax() {
        return max;
    }
    
    /**
     * @return the average difference found
     */
    public double getAverage() {
        return sum / size;
    }
    
    /**
     * must be called before calibration and differential
     * @param new prevValue
     */
    public void setPrevValue(double prevValue) {
        this.prevValue = prevValue;
    }
    
    /**
     * resets minimal margin and other variables to 0 to restart calibration
     */
    public void clearCalibration() {
        max = 0;
        sum = 0;
        size = 0;
        minMargin = 0;
        prevValue = 0;
    }
}
