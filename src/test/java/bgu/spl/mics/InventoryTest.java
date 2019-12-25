package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {
    private Inventory inv;

    @BeforeEach
    public void setUp() {
        inv = Inventory.getInstance();
    }

    @Test
    public void testGetItem(){
        String[] items = {"pen", "glove"};
        inv.load(items);
        assertTrue(inv.getItem("pen"));
        assertFalse(inv.getItem("pen"));
        assertFalse(inv.getItem("knife"));
        assertFalse(inv.getItem(""));
        assertFalse(inv.getItem(null));
    }

    @Test
    public void testLoad() {
        String[] items1 = {"BrainFreeze Gun", "BallPen of Doom", "The Federrer Slam", "Chris Singleton"};
        inv.load(items1);
        for (String item : items1) {
            assertTrue(inv.getItem(item));
        }

        String[] items2 = {"Rami Malek", "Bruce Willis", "Angelina July"};
        for (String item : items2) {
            assertFalse(inv.getItem(item));
        }

        String[] items3 = {"Shane Larkin", "Scottie Wilbekin", "", "Sergio Rodriguez"};
        inv.load(items3);
        for (String item : items3) {
            assertFalse(inv.getItem(item));
        }

        String[] items4 = {"Shane Larkin", "Scottie Wilbekin", null, "Sergio Rodriguez"};
        inv.load(items4);
        for (String item : items4) {
            assertFalse(inv.getItem(item));
        }
    }
}
