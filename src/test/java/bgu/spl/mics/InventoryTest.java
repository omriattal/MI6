package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Inventory;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class InventoryTest extends TestCase {
    private Inventory inv;

    @BeforeEach
    public void setUp(){
        inv = Inventory.getInstance();
    }

    @Test
    public void testGetInstance(){

    }
}
