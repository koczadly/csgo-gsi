package uk.oczadly.karl.csgsi.state.components;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameStateBaseTest;

import static org.junit.Assert.*;

public class WeaponEnumTest extends GameStateBaseTest {
    
    @Test
    public void testWeaponTypeEnum() {
        testEnums(Weapon.Type.class,
                "Pistol", "Rifle", "Knife", "Tablet", "Fists", "StackableItem", "Submachine Gun", "C4", "Melee",
                "Breach Charge", "Grenade");
    }
    
    @Test
    public void testWeaponEnum() { //TODO not all are listed
        testEnums(Weapon.class,
                "weapon_ak47", "weapon_aug", "weapon_awp", "weapon_bizon", "weapon_c4", "weapon_cz75a",
                "weapon_deagle", "weapon_decoy", "weapon_elite", "weapon_famas", "weapon_fiveseven", "weapon_flashbang",
                "weapon_g3sg1", "weapon_galilar", "weapon_glock", "weapon_healthshot", "weapon_hegrenade",
                "weapon_hkp2000", "weapon_incgrenade", "weapon_knife", "weapon_m4a1", "weapon_m4a1_silencer",
                "weapon_m249", "weapon_mac10", "weapon_mag7", "weapon_molotov", "weapon_mp7", "weapon_mp9",
                "weapon_negev", "weapon_nova", "weapon_p90", "weapon_p250", "weapon_revolver", "weapon_sawedoff",
                "weapon_scar20", "weapon_sg556", "weapon_tagrenade", "weapon_smokegrenade", "weapon_ssg08",
                "weapon_tec9", "weapon_ump45", "weapon_usp_silencer", "weapon_xm1014", "weapon_taser");
    }
    
    @Test
    public void testWeaponLookup() {
        for (Weapon w : Weapon.values()) {
            assertEquals(w, Weapon.valueOfFromName(w.getName()));
        }
    }

}