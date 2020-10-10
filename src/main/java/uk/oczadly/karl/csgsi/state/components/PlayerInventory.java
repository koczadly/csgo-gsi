package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

/**
 * This class represents a player's in-game inventory slots. This includes weapons, utilities and other miscellaneous
 * items that the player can purchase or collect and carry around with them.
 *
 * @author Karl Oczadly
 */
@com.google.gson.annotations.JsonAdapter(PlayerInventory.JsonAdapter.class)
public class PlayerInventory {
    
    private final List<ItemDetails> items;
    private ItemDetails activeWeapon, primaryWeapon, secondaryWeapon;
    private Collection<ItemDetails> utilities;
    
    
    private PlayerInventory(List<ItemDetails> items) {
        this.items = Collections.unmodifiableList(items);
        
        // Determine helper values
        List<ItemDetails> utilities = new ArrayList<>();
        for (ItemDetails item : items) {
            if (activeWeapon == null && (item.getState().val() == WeaponState.ACTIVE
                    || item.getState().val() == WeaponState.RELOADING)) {
                activeWeapon = item;
            }
            if (item.getType().isResolved()) {
                if (primaryWeapon == null && item.getType().val().isPrimaryWeapon())
                    primaryWeapon = item;
                if (secondaryWeapon == null && item.getType().val().isSecondaryWeapon())
                    secondaryWeapon = item;
                if (item.getType().val().isUtility())
                    utilities.add(item);
            }
        }
        this.utilities = Collections.unmodifiableList(utilities);
    }
    
    
    /**
     * Returns a list of all the items, weapons and utilities currently in the players inventory.
     * @return a list of all the items that the player currently has
     */
    public List<ItemDetails> getItems() {
        return items;
    }
    
    /**
     * Returns the current actively selected weapon or item.
     * @return the current active item
     */
    public ItemDetails getActiveItem() {
        return activeWeapon;
    }
    
    /**
     * Returns the item in the primary weapon (rifle) slot.
     * @return the primary weapon which the player currently has (rifle), or null if they don't have one
     */
    public ItemDetails getPrimarySlot() {
        return primaryWeapon;
    }
    
    /**
     * Returns the item in the secondary weapon (pistol) slot.
     * @return the secondary weapon which the player currently has (pistol), or null if they don't have one
     */
    public ItemDetails getSecondarySlot() {
        return secondaryWeapon;
    }
    
    /**
     * Returns the main (best) weapon which the player has in their inventory.
     *
     * <p>This is either the primary weapon (rifle), or the secondary weapon (pistol) if no primary is specified. If
     * neither are present, this method will return null.</p>
     *
     * @return the best weapon the player currently has, or null if they don't have one
     */
    public ItemDetails getMainWeapon() {
        return primaryWeapon != null ? primaryWeapon : secondaryWeapon;
    }
    
    /**
     * Returns a collection of utility items held by the player.
     *
     * <p><i>Utility items include grenades and miscellaneous stackable items like health shots.</i></p>
     *
     * @return a collection of utility items the player has
     */
    public Collection<ItemDetails> getUtilityItems() {
        return utilities;
    }
    
    /**
     * Returns an {@link ItemDetails} instance for the requested item or weapon.
     *
     * @param weapon the item to retrieve
     * @return the details of the requested item, or null if the player does not have the item
     */
    public ItemDetails getItem(Weapon weapon) {
        for (ItemDetails item : getItems()) {
            if (item.getWeapon().val() == weapon)
                return item;
        }
        return null;
    }
    
    /**
     * Checks whether the player has the specified item or weapon, and that it has some remaining ammunition or uses (if
     * applicable).
     *
     * @param weapon the item to check
     * @return true if the player has the item
     */
    public boolean hasItem(Weapon weapon) {
        ItemDetails details = getItem(weapon);
        return details != null && !details.isAmmoEmpty();
    }
    
    
    
    public static class ItemDetails {
        @Expose @SerializedName("name")
        private EnumValue<Weapon> weapon;
        
        @Expose @SerializedName("paintkit")
        private String skin;
        
        @Expose @SerializedName("type")
        private EnumValue<Weapon.Type> weaponType;
        
        @Expose @SerializedName("ammo_clip")
        private int ammoClip;
        
        @Expose @SerializedName("ammo_clip_max")
        private int maxAmmoClip;
        
        @Expose @SerializedName("ammo_reserve")
        private Integer ammoReserve;
        
        @Expose @SerializedName("state")
        private EnumValue<WeaponState> state;
        
        
        public EnumValue<Weapon> getWeapon() {
            return weapon;
        }
        
        public String getSkin() {
            return skin;
        }
        
        public boolean isDefaultSkin() {
            return skin == null || skin.equalsIgnoreCase("default");
        }
        
        public EnumValue<Weapon.Type> getType() {
            return weaponType;
        }
        
        public int getAmmoClip() {
            return ammoClip;
        }
        
        public int getMaxAmmoClip() {
            return maxAmmoClip;
        }
        
        public int getAmmoReserve() {
            return ammoReserve != null ? ammoReserve : 0;
        }
        
        public int getAmmoRemaining() {
            return getAmmoReserve() + getAmmoClip();
        }
        
        public boolean isAmmoEmpty() {
            return ammoReserve != null && getAmmoRemaining() <= 0;
        }
        
        public EnumValue<WeaponState> getState() {
            return state;
        }
        
        @Override
        public String toString() {
            return "ItemDetails{" +
                    "weapon=" + getWeapon() +
                    ", skin='" + getSkin() + '\'' +
                    ", ammoReserve=" + getAmmoReserve() +
                    ", state=" + getState() +
                    '}';
        }
    }
    
    public enum WeaponState {
        /** Weapon is currently being held by the player. */
        @SerializedName("active") ACTIVE,
        
        /** Weapon is currently holstered (not selected). */
        @SerializedName("holstered") HOLSTERED,
        
        /** Weapon is currently active and being reloaded. */
        @SerializedName("reloading") RELOADING
    }
    
    
    
    static class JsonAdapter implements JsonDeserializer<PlayerInventory> {
        @Override
        public PlayerInventory deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            // Fetch map of weapons
            Map<String, ItemDetails> map = context.deserialize(json,
                    new TypeToken<HashMap<String, ItemDetails>>() {}.getType());
            
            // Add to list (in order)
            List<ItemDetails> list = new ArrayList<>(map.size());
            for (int i=0; i<map.size(); i++) {
                list.add(map.get("weapon_" + i));
            }
            return new PlayerInventory(list);
        }
    }
    
}
