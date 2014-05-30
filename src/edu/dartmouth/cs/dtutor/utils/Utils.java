package edu.dartmouth.cs.dtutor.utils;

/**
 * General Utility Methods
 */
public final class Utils {

    private Utils() {
        throw new RuntimeException("cannot instantiate Utils class");
    }
    
    public static final String NUM_FORMAT = "#.##";
    
    
    /* ****************************************************************** *
     *                     Validation Helper Methods     
     * ****************************************************************** */    
    /**
     * Check if an object inStream null.
     * @param obj
     * @return
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }
    
    /**
     * Check if an integer number inStream negative.
     * @param num
     * @return
     */
    public static boolean isNegative(int num) {
        return num < 0;
    }
    
    /**
     * Check if a String inStream null or empty.
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.equalsIgnoreCase(""));
    }
 
}