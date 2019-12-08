package bgu.spl.mics.application.passiveObjects;

import java.util.List;

/**
 * That's where Q holds his gadget (e.g. an explosive pen was used in GoldenEye, a geiger counter in Dr. No, etc).
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Inventory {
    private static Inventory instance;
    private List<String> gadgets;

    /**
     * Retrieves the single instance of this class.
     */
    public static Inventory getInstance() {
        if (instance == null) {
            instance = new Inventory();
        }

        return null;
    }

    /**
     * The constructor of Inventory. It is private as it is a singleton.
     */
    private Inventory(){
    }

    /**
     * Initializes the inventory. This method adds all the items given to the gadget
     * inventory.
     * <p>
     *
     * @param inventory Data structure containing all data necessary for initialization
     *                  of the inventory.
     */
    public void load(String[] inventory) {
        //TODO: Implement this
    }

    /**
     * acquires a gadget and returns 'true' if it exists.
     * <p>
     *
     * @param gadget Name of the gadget to check if available
     * @return ‘false’ if the gadget is missing, and ‘true’ otherwise
     * <p>
     * @pre: @param gadget != null && @param gadget != ""
     * @post:
     */
    public boolean getItem(String gadget) {
        //TODO: Implement this
        return true;
    }

    /**
     * <p>
     * Prints to a file name @filename a serialized object List<Gadget> which is a
     * List of all the reports in the diary.
     * This method is called by the main method in order to generate the output.
     * <p>
     */
    public void printToFile(String filename) {
        //TODO: Implement this
    }
}
