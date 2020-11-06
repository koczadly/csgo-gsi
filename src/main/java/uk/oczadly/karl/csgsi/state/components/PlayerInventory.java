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
    private ItemDetails activeWeapon, primarySlot, secondarySlot, knifeSlot;
    private Collection<ItemDetails> utilities;
    
    
    private PlayerInventory(List<ItemDetails> items) {
        this.items = Collections.unmodifiableList(items);
        
        // Determine helper values
        List<ItemDetails> utilities = new ArrayList<>();
        for (ItemDetails item : items) {
            if (activeWeapon == null && (item.getState().get() == WeaponState.ACTIVE
                    || item.getState().get() == WeaponState.RELOADING))
                activeWeapon = item;
            if (knifeSlot == null && item.getType() != null && item.getType().get() == Weapon.Type.KNIFE)
                knifeSlot = item;
            if (item.getType() != null && item.getType().isResolved()) {
                if (primarySlot == null && item.getType().get().isPrimaryWeapon())
                    primarySlot = item;
                if (secondarySlot == null && item.getType().get().isSecondaryWeapon())
                    secondarySlot = item;
                if (item.getType().get().isUtility())
                    utilities.add(item);
            }
        }
        this.utilities = Collections.unmodifiableList(utilities);
    }
    
    
    /**
     * Gets a list of all the items, weapons and utilities currently in the players inventory.
     *
     * @return a list of all the items that the player currently has
     */
    public List<ItemDetails> getItems() {
        return items;
    }
    
    /**
     * Gets the current actively selected weapon or item.
     *
     * @return the current active item
     */
    public ItemDetails getActiveItem() {
        return activeWeapon;
    }
    
    /**
     * Gets the item in the primary weapon (rifle) slot.
     *
     * @return the primary weapon (rifle), or null if they don't have one
     */
    public ItemDetails getPrimarySlot() {
        return primarySlot;
    }
    
    /**
     * Gets the item in the secondary weapon (pistol) slot.
     *
     * @return the secondary weapon (pistol), or null if they don't have one
     */
    public ItemDetails getSecondarySlot() {
        return secondarySlot;
    }
    
    /**
     * Gets the item in the knife slot.
     * <p>Note that this will only return the knife item, and will not return any other melee weapons or fists.</p>
     *
     * @return the knife item, or null if they don't have one
     */
    public ItemDetails getKnifeSlot() {
        return knifeSlot;
    }
    
    /**
     * Gets the main (best) weapon which the player has in their inventory.
     *
     * <p>This is either the primary weapon (rifle), or the secondary weapon (pistol) if no primary is specified. If
     * neither are present, this method will return null.</p>
     *
     * @return the best weapon the player has, or null if they don't have one
     */
    public ItemDetails getMainWeapon() {
        return primarySlot != null ? primarySlot : secondarySlot;
    }
    
    /**
     * Gets a collection of utility items held by the player.
     *
     * <p><i>Utility items include grenades and miscellaneous stackable items like health shots.</i></p>
     *
     * @return a collection of utility items the player has
     */
    public Collection<ItemDetails> getUtilityItems() {
        return utilities;
    }
    
    /**
     * Gets an {@link ItemDetails} instance for the requested item or weapon.
     *
     * @param weapon the item to retrieve
     * @return the details of the requested item, or null if the player does not have the item
     */
    public ItemDetails getItem(Weapon weapon) {
        for (ItemDetails item : getItems()) {
            if (item.getWeapon().get() == weapon)
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
        private Integer maxAmmoClip;
        
        @Expose @SerializedName("ammo_reserve")
        private Integer ammoReserve;
        
        @Expose @SerializedName("state")
        private EnumValue<WeaponState> state;
    
    
        /**
         * @return the weapon value
         */
        public EnumValue<Weapon> getWeapon() {
            return weapon;
        }
    
        /**
         * @return the name of the skin, or null if not applicable
         */
        public String getSkin() {
            return skin;
        }
    
        /**
         * @return true if the player is using the default weapon skin, or if the item cannot be skinned
         */
        public boolean isDefaultSkin() {
            return skin == null || skin.equalsIgnoreCase("default");
        }
    
        /**
         * @return the weapon type/category
         */
        public EnumValue<Weapon.Type> getType() {
            return weaponType;
        }
    
        /**
         * @return the amount of ammo in the clip (currently loaded), or the number of items stacked
         */
        public int getAmmoClip() {
            return ammoClip;
        }
    
        /**
         * @return the maximum amount of ammo which can be held in the clip
         */
        public int getMaxAmmoClip() {
            return maxAmmoClip != null ? maxAmmoClip : 0;
        }
    
        /**
         * @return the amount of ammo in reserve (not loaded)
         */
        public int getAmmoReserve() {
            return ammoReserve != null ? ammoReserve : 0;
        }
    
        /**
         * @return the total amount of ammo remaining (clip + reserve)
         */
        public int getAmmoRemaining() {
            return getAmmoReserve() + getAmmoClip();
        }
    
        /**
         * @return true if the weapon can take ammo and has completely ran out
         */
        public boolean isAmmoEmpty() {
            return ammoReserve != null && getAmmoRemaining() <= 0;
        }
    
        /**
         * @return true if the weapon takes ammunition
         */
        public boolean isAmmoApplicable() {
            return maxAmmoClip != null;
        }
    
        /**
         * @return true if the item is stackable
         */
        public boolean isStackable() {
            return maxAmmoClip == null && ammoReserve != null;
        }
    
        /**
         * @return the current holding state of the weapon
         */
        public EnumValue<WeaponState> getState() {
            return state;
        }
        
        @Override
        public String toString() {
            return "ItemDetails{" +
                    "weapon=" + getWeapon() +
                    ", skin='" + getSkin() + '\'' +
                    (ammoReserve != null ? (", ammo=" + getAmmoRemaining()) : "") +
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
