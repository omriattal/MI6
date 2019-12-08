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
    /**
     * @inv: instance != null, gadgets != null
     */
    private List<String> gadgets;
    /**
     * the singleton instance of the object
     */
    private static Inventory instance;

    /**
     * Constructs a new Inventory
     * <p>
     * @pre:
     */
    private Inventory() {
        //TODO: Implement this
    }

    /**
     * Retrieves the single instance of this class.
     * <p>
     * @pre: none
     * @post: this.instance != null
     */
    public static Inventory getInstance() {
        if (instance == null) {
            instance = new Inventory();
        }
        return instance;
    }

    /**
     * Initializes the inventory. This method adds all the items given to the gadget
     * inventory.
     * <p>
     *
     * @param inventory Data structure containing all data necessary for initialization
     *                  of the inventory.
     * <p>
     * @pre: @param inventory != null, forall 0<=i<inventory() inventory[i] != null && inventory[i]!=""
     * @post: this.gadgets.size() == @param inventory.length &&
     * forall 0<=i<this.gadgets.size() this.gadgets.get(i) == inventory[i]
     * </p>
     */
    public void load(String[] inventory) {
        //TODO: Implement this
    }

    /**
     * acquires a gadget and returns 'true' if it exists.
     * <p>
     * @param gadget Name of the gadget to check if available
     * @return ‘false’ if the gadget is missing, and ‘true’ otherwise
     * <p>
     * @pre: @param gadget != null
     * @post: @ret == this.gadget.contains(gadget), this.gadgets.removeFromGadgets(gadget) == false
     */
    boolean getItem(String gadget) {
        //TODO: Implement this
        return true;
    }

    /**
     * removes a gadget from the gadgets list
     *
     * @param gadget name of the gadget to remove
     * @return 'false' if the gadget was removed, and 'true' otherwise
     * <p>
     * @pre: @param gadget != null
     * @post: @ret == this.gadgets.contains(gadget)
     */
    private boolean removeFromGadgets(String gadget) {
        return true;
    }

    /**
     * <p>
     * Prints to a file name @filename a serialized object List<Gadget> which is a
     * List of all the gadgets in the inventory.
     * This method is called by the main method in order to generate the output.
     *
     * @param filename the name of the file to write to
     *                 <p>
     * @pre: @param filename != null
     * @post: the content of gadget was printed
     */
    public void printToFile(String filename) {
        //TODO: Implement this
    }
}
