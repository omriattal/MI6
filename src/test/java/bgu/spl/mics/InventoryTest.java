package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Inventory;
import junit.framework.TestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

public class InventoryTest extends TestCase {
    Inventory inv;

    @BeforeEach
    public void setUp() {
        inv = Inventory.getInstance();
    }

    @Test
    public void testGetInstance() {
        try {
            Field instance = inv.getClass().getDeclaredField("instance");
            instance.setAccessible(true);

            Assertions.assertNotEquals(null, instance.get(inv));
        } catch (NoSuchFieldException e){
            System.out.println("there's no such field");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
