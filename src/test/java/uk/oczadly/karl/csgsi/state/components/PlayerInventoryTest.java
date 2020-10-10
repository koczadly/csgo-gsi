package uk.oczadly.karl.csgsi.state.components;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.oczadly.karl.csgsi.internal.Util;

import static org.junit.Assert.*;

/**
 * @author Karl Oczadly
 */
public class PlayerInventoryTest {
    
    static PlayerInventory inventory;
    
    @BeforeClass
    public static void setUp() {
        inventory = Util.createGsonObject().fromJson("{\n" +
                "      \"weapon_0\": {\n" +
                "        \"name\": \"weapon_knife_t\",\n" +
                "        \"paintkit\": \"default\",\n" +
                "        \"type\": \"Knife\",\n" +
                "        \"state\": \"holstered\"\n" +
                "      },\n" +
                "      \"weapon_1\": {\n" +
                "        \"name\": \"weapon_glock\",\n" +
                "        \"paintkit\": \"default\",\n" +
                "        \"type\": \"Pistol\",\n" +
                "        \"ammo_clip\": 1,\n" +
                "        \"ammo_clip_max\": 20,\n" +
                "        \"ammo_reserve\": 0,\n" +
                "        \"state\": \"holstered\"\n" +
                "      },\n" +
                "      \"weapon_2\": {\n" +
                "        \"name\": \"weapon_c4\",\n" +
                "        \"paintkit\": \"default\",\n" +
                "        \"type\": \"C4\",\n" +
                "        \"state\": \"active\"\n" +
                "      },\n" +
                "      \"weapon_3\": {\n" +
                "        \"name\": \"weapon_hegrenade\",\n" +
                "        \"type\": \"Grenade\",\n" +
                "        \"state\": \"holstered\"\n" +
                "      },\n" +
                "      \"weapon_4\": {\n" +
                "        \"name\": \"weapon_ak47\",\n" +
                "        \"paintkit\": \"default\",\n" +
                "        \"type\": \"Rifle\",\n" +
                "        \"ammo_clip\": 0,\n" +
                "        \"ammo_clip_max\": 30,\n" +
                "        \"ammo_reserve\": 0,\n" +
                "        \"state\": \"holstered\"\n" +
                "      }\n" +
                "    }", PlayerInventory.class);
    }
    
    
    @Test
    public void testGetItems() {
        assertEquals(Weapon.KNIFE_T, inventory.getItems().get(0).getWeapon().val());
        assertEquals(Weapon.GLOCK, inventory.getItems().get(1).getWeapon().val());
        assertEquals(Weapon.C4, inventory.getItems().get(2).getWeapon().val());
        assertEquals(Weapon.HE_GRENADE, inventory.getItems().get(3).getWeapon().val());
        assertEquals(Weapon.AK_47, inventory.getItems().get(4).getWeapon().val());
    }
    
    @Test
    public void testGetActiveItem() {
        assertEquals(Weapon.C4, inventory.getActiveItem().getWeapon().val());
    }
    
    @Test
    public void testGetPrimarySlot() {
        assertEquals(Weapon.AK_47, inventory.getPrimarySlot().getWeapon().val());
    }
    
    @Test
    public void testGetSecondarySlot() {
        assertEquals(Weapon.GLOCK, inventory.getSecondarySlot().getWeapon().val());
    }
    
    @Test
    public void testGetKnifeSlot() {
        assertEquals(Weapon.KNIFE_T, inventory.getKnifeSlot().getWeapon().val());
    }
    
    @Test
    public void testGetMainWeapon() {
        assertEquals(Weapon.AK_47, inventory.getMainWeapon().getWeapon().val());
    }
    
    @Test
    public void testGetUtilities() {
        assertEquals(1, inventory.getUtilityItems().size());
        assertSame(inventory.getItems().get(3), inventory.getUtilityItems().iterator().next());
    }
    
    @Test
    public void testGetItem() {
        assertSame(inventory.getItems().get(2), inventory.getItem(Weapon.C4));
    }
    
    @Test
    public void testHasItem() {
        assertTrue(inventory.hasItem(Weapon.GLOCK));
        assertTrue(inventory.hasItem(Weapon.HE_GRENADE));
        assertTrue(inventory.hasItem(Weapon.C4));
        assertFalse(inventory.hasItem(Weapon.AK_47)); // No ammo
        assertFalse(inventory.hasItem(Weapon.NEGEV));
    }
    
}