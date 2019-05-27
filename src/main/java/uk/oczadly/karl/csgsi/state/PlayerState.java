package uk.oczadly.karl.csgsi.state;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import uk.oczadly.karl.csgsi.state.components.Coordinate;
import uk.oczadly.karl.csgsi.state.components.Team;

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
    private Team team;
    
    @Expose
    @SerializedName("activity")
    private Activity activity;
    
    @Expose
    @SerializedName("match_stats")
    private PlayerMatchStats stats;
    
    @Expose
    @SerializedName("state")
    private PlayerStateDetails state;
    
    @Expose
    @SerializedName("weapons")
    @JsonAdapter(WeaponDeserializer.class)
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
    public Team getTeam() {
        return team;
    }
    
    /**
     * @return this player's activity, or null if not the client
     */
    public Activity getActivity() {
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
    
    
    
    private class WeaponDeserializer implements JsonDeserializer<List<WeaponDetails>> {
        @Override
        public List<WeaponDetails> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Map<String, WeaponDetails> map = context.deserialize(json,
                    new TypeToken<TreeMap<String, WeaponDetails>>(){}.getType());
            
            List<WeaponDetails> list = new ArrayList<>(map.size());
            
            if (map != null)
                list.addAll(map.values());
            
            return Collections.unmodifiableList(list);
        }
    }
    
    public enum Activity {
        /** Currently playing/watching the game. */
        @SerializedName("playing")
        PLAYING,
    
        /** Currently typing in the chat. */
        @SerializedName("textinput")
        TEXT_INPUT,
        
        /** Currently navigating a game menu. */
        @SerializedName("menu")
        MENU;
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
        
    }
    
    public static class WeaponDetails {
        
        @Expose
        @SerializedName("name")
        private String name;
    
        @Expose
        @SerializedName("paintkit")
        private String skin;
    
        @Expose
        @SerializedName("type")
        private WeaponType weaponType;
    
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
        private WeaponState state;
    
    
        public String getName() {
            return name;
        }
    
        public String getSkin() {
            return skin;
        }
    
        public WeaponType getWeaponType() {
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
    
        public WeaponState getState() {
            return state;
        }
        
    }
    
    public enum WeaponType {
        @SerializedName("Pistol")
        PISTOL,
        @SerializedName("Rifle")
        RIFLE,
        @SerializedName("Knife")
        KNIFE,
        @SerializedName("Tablet")
        TABLET,
        @SerializedName("Fists")
        FISTS,
        @SerializedName("StackableItem")
        STACKABLE_ITEM,
        @SerializedName("Submachine Gun")
        SUBMACHINE_GUN,
        @SerializedName("C4")
        BOMB,
        @SerializedName("Melee")
        MELEE,
        @SerializedName("Breach Charge")
        BREACH_CHARGE,
        @SerializedName("Grenade")
        GRENADE;
    }
    
    public enum WeaponState {
        /** Weapon is currently being held by the player. */
        @SerializedName("active")
        ACTIVE,
    
        /** Weapon is currently holstered (not selected). */
        @SerializedName("holstered")
        HOLSTERED,
        
        /** Weapon is currently active and being reloaded. */
        @SerializedName("reloading")
        RELOADING;
    }
    
}
