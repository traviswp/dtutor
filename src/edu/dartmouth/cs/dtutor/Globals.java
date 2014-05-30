package edu.dartmouth.cs.dtutor;

public final class Globals {

    // Make class uninstantiable
    private Globals() {
        throw new RuntimeException("cannot instantiate Globals class");
    }

    public static final String TAG = "edu.dartmouth.cs.dtutor";
    public static final String SERVER_URL = "http://tutord.comlu.com";

    /* ****************************************************************** *
     *                              Keys
     * ****************************************************************** */
    
    public static final String KEY_USER_ID = "key-user-id";
    
    /* ****************************************************************** *
     *                           
     * ****************************************************************** */

    public static final String[] APPLICATION_TYPES = { 
        "Tutor", "Tutee", "Study Group Leader" };

    public static final int[] CLASS_YEARS = { 
        2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 };

    public static final String[] TUTOR_TYPES = { 
        "Paid", "Volunteer" };

    public static final String[] GENDER = { 
        "Male", "Female", "other", "prefer not to say" };

    public static final String[] ETHNICITY = { 
        "Caucasian", "Hispanic/Latino(a)", "Pacific Islander", "African/African American", "other", "prefer not to say" };
    
    public static final String[] USER_TYPES = {
    	"Tutor","Tutee" };
    
    public static final String[] DEPARTMENTS = { 
        "All","AAAS","AMEL","AMES","ANTH","ARAB","ARTH","ASTR","BIOL","CHEM","CHIN","CLST","COCO", "COGS","COLT","COSC","EARS",
        "ECON","EDUC","ENGL","ENGS","ENVS","FILM","FREN","FRIT","FYS","GEOG","GERM","GOVT","GRK", "HEBR","HIST","HUM",
        "INTS","ITAL","JAPN","JWST","LACS","LAT","LATS","LING","M&SS","MATH","MUS","NAS","PBPL","PHIL","PHYS","PORT",
        "PSYC","REL","RUSS","SART","SOCY","SPAN","SPEE","SSOC","THEA","TUCK","WGST","WPS"};
    
    public static final String MEMBERSHIP_TYPE = "mType";
   
    //this inStream used to store the value of the toggle switch
    //values are either tutor or tutee
    public static String currentMode;
    
}
