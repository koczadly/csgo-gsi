package uk.oczadly.karl.csgsi.state;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import uk.oczadly.karl.csgsi.state.components.DeserializedEnum;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.components.Team;
import uk.oczadly.karl.csgsi.state.components.Weapon;

import java.lang.reflect.Type;
import java.util.*;

public class PlayerState {

    @Expose
    @SerializedName("steamid")
    private String steamId;
    
    @Expose
    @SerializedName("name")
    private String name;
    
    @Expose
    @SerializedName("clan")
    private String groupName;
    
    @Expose
    @SerializedName("observer_slot")
    private Integer observerSlot;
    
    @Expose
    @SerializedName("team")
    private DeserializedEnum<Team> team;
    
    @Expose
    @SerializedName("activity")
    private DeserializedEnum<Activity> activity;
    
    @Expose
    @SerializedName("match_stats")
    private PlayerMatchStats stats;
    
    @Expose
    @SerializedName("state")
    private PlayerStateDetails state;
    
    @Expose
    @SerializedName("weapons")
    @JsonAdapter(WeaponsListDeserializer.class)
    private List<WeaponDetails> weapons;
    
    @Expose
    @SerializedName("spectarget")
    private String specTarget;
    
    @Expose
    @SerializedName("position")
    private Coordinate position;
    
    @Expose
    @SerializedName("forward")
    private Coordinate facing;
    
    private volatile WeaponDetails selectedWeapon;
    
    
    /**
     * @return the Steam ID of the player, or null if not the client
     */
    public String getSteamId() {
        return steamId;
    }
    
    /**
     * @return the display name of the player
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return the name of the player's nominated clan/Steam group
     */
    public String getGroupName() {
        return groupName;
    }
    
    /**
     * @return the slot this player is using (associated numerical key to spectate)
     */
    public Integer getObserverSlot() {
        return observerSlot;
    }
    
    /**
     * @return which team this player is on (terrorist or counter-terrorist)
     */
    public DeserializedEnum<Team> getTeam() {
        return team;
    }
    
    /**
     * @return this player's activity, or null if not the client
     */
    public DeserializedEnum<Activity> getActivity() {
        return activity;
    }
    
    /**
     * @return the current statistics for this player for this map
     */
    public PlayerMatchStats getStatistics() {
        return stats;
    }
    
    /**
     * @return state information relating to this player
     */
    public PlayerStateDetails getState() {
        return state;
    }
    
    /**
     * @return the current weapons inventory of this player
     */
    public List<WeaponDetails> getWeaponsInventory() {
        return weapons;
    }
    
    /**
     * @return the active weapon that the player currently has selected
     */
    public WeaponDetails getSelectedWeapon() {
        if (selectedWeapon == null && getWeaponsInventory() != null) {
            synchronized (this) { // Double-checked locking
                if (selectedWeapon == null) {
                    for (WeaponDetails w : getWeaponsInventory()) {
                        if (w.getState().getEnum() != WeaponState.HOLSTERED) {
                            selectedWeapon = w;
                            break;
                        }
                    }
                }
            }
        }
        return selectedWeapon;
    }
    
    /**
     * @return the ID of the player being spectated, or null if not spectating
     */
    public String getSpectatorTarget() {
        return specTarget;
    }
    
    /**
     * @return the position of this player on the map
     */
    public Coordinate getPosition() {
        return position;
    }
    
    /**
     * @return the direction this player is facing
     */
    public Coordinate getDirection() {
        return facing;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    
    private static class WeaponsListDeserializer implements JsonDeserializer<List<WeaponDetails>> {
        @Override
        public List<WeaponDetails> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            // Fetch map of weapons
            Map<String, WeaponDetails> map = context.deserialize(json,
                    new TypeToken<HashMap<String, WeaponDetails>>() {}.getType());
            
            // Add to list (in order)
            List<WeaponDetails> list = new ArrayList<>(map.size());
            for (int i=0; i<map.size(); i++) {
                list.add(map.get("weapon_" + i));
            }
            return Collections.unmodifiableList(list);
        }
    }
    
    public enum Activity {
        /**
         * Currently playing/watching the game.
         */
        @SerializedName("playing")
        PLAYING,
        
        /**
         * Currently typing in the chat.
         */
        @SerializedName("textinput")
        TEXT_INPUT,
        
        /**
         * Currently navigating a game menu.
         */
        @SerializedName("menu")
        MENU
    }
    
    public static class PlayerStateDetails {
        
        @Expose
        @SerializedName("health")
        private int health;
        
        @Expose
        @SerializedName("armor")
        private int armor;
        
        @Expose
        @SerializedName("helmet")
        private boolean helmet;
        
        @Expose
        @SerializedName("defusekit")
        private boolean defuseKit;
        
        @Expose
        @SerializedName("flashed")
        private int flashed;
        
        @Expose
        @SerializedName("smoked")
        private int smoked;
        
        @Expose
        @SerializedName("burning")
        private int burning;
        
        @Expose
        @SerializedName("money")
        private int money;
        
        @Expose
        @SerializedName("round_kills")
        private int roundKills;
        
        @Expose
        @SerializedName("round_killhs")
        private int roundKillsHeadshot;
        
        @Expose
        @SerializedName("round_totaldmg")
        private int roundTotalDamage;
        
        @Expose
        @SerializedName("equip_value")
        private int equipmentValue;
        
        
        public int getHealth() {
            return health;
        }
        
        public int getArmor() {
            return armor;
        }
        
        public boolean hasHelmet() {
            return helmet;
        }
    
        public boolean hasDefuseKit() {
            return defuseKit;
        }
    
        public int getFlashed() {
            return flashed;
        }
        
        public int getSmoked() {
            return smoked;
        }
        
        public int getBurning() {
            return burning;
        }
        
        public int getMoney() {
            return money;
        }
        
        public int getRoundKills() {
            return roundKills;
        }
        
        public int getRoundKillsHeadshot() {
            return roundKillsHeadshot;
        }
        
        public int getRoundTotalDamage() {
            return roundTotalDamage;
        }
        
        public int getEquipmentValue() {
            return equipmentValue;
        }
        
    }
    
    public static class PlayerMatchStats {
        
        @Expose
        @SerializedName("kills")
        private int kills;
        
        @Expose
        @SerializedName("assists")
        private int assists;
        
        @Expose
        @SerializedName("deaths")
        private int deaths;
        
        @Expose
        @SerializedName("mvps")
        private int mvps;
        
        @Expose
        @SerializedName("score")
        private int score;
        
        
        public int getKillCount() {
            return kills;
        }
        
        public int getAssistCount() {
            return assists;
        }
        
        public int getDeathCount() {
            return deaths;
        }
        
        public int getMvpCount() {
            return mvps;
        }
        
        public int getScore() {
            return score;
        }
    
    
        @Override
        public String toString() {
            return "PlayerMatchStats{" +
                    "kills=" + kills +
                    ", assists=" + assists +
                    ", deaths=" + deaths +
                    ", mvps=" + mvps +
                    ", score=" + score +
                    '}';
        }
    }
    
    public static class WeaponDetails {
        
        @Expose
        @SerializedName("name")
        private DeserializedEnum<Weapon> weapon;
        
        @Expose
        @SerializedName("paintkit")
        private String skin;
        
        @Expose
        @SerializedName("type")
        private DeserializedEnum<Weapon.Type> weaponType;
        
        @Expose
        @SerializedName("ammo_clip")
        private int ammoClip;
        
        @Expose
        @SerializedName("ammo_clip_max")
        private int maxAmmoClip;
        
        @Expose
        @SerializedName("ammo_reserve")
        private int ammoReserve;
        
        @Expose
        @SerializedName("state")
        private DeserializedEnum<WeaponState> state;
        
        
        public DeserializedEnum<Weapon> getWeapon() {
            return weapon;
        }
        
        /**
         * @return the internal name of the weapon
         * @deprecated use {@link #getWeapon()} to retrieve the raw name or enum value
         */
        @Deprecated(forRemoval = true)
        public String getName() {
            return weapon.getRawString();
        }
        
        public String getSkin() {
            return skin;
        }
        
        public boolean isDefaultSkin() {
            return skin == null || skin.equalsIgnoreCase("default");
        }
        
        public DeserializedEnum<Weapon.Type> getWeaponType() {
            return weaponType;
        }
        
        public int getAmmoClip() {
            return ammoClip;
        }
        
        public int getMaxAmmoClip() {
            return maxAmmoClip;
        }
        
        public int getAmmoReserve() {
            return ammoReserve;
        }
        
        public DeserializedEnum<WeaponState> getState() {
            return state;
        }
    
        @Override
        public String toString() {
            return "WeaponDetails{" +
                    "weapon=" + getName() +
                    ", skin='" + getSkin() + '\'' +
                    ", ammoClip=" + getAmmoClip() +
                    ", state=" + getState().getRawString() +
                    '}';
        }
    }
    
    public enum WeaponState {
        /**
         * Weapon is currently being held by the player.
         */
        @SerializedName("active")
        ACTIVE,
        
        /**
         * Weapon is currently holstered (not selected).
         */
        @SerializedName("holstered")
        HOLSTERED,
        
        /**
         * Weapon is currently active and being reloaded.
         */
        @SerializedName("reloading")
        RELOADING
    }

}
