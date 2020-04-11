package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

@JsonAdapter(Weapon.WeaponDeserializer.class)
public enum Weapon {
    AK_47           ("ak47",          Type.RIFLE),
    AUG             ("aug",           Type.RIFLE),
    AWP             ("awp",           Type.SNIPER_RIFLE),
    BIZON           ("bizon",         Type.SUBMACHINE_GUN),
    C4              ("c4",            Type.BOMB),
    CZ75_A          ("cz75a",         Type.PISTOL),
    DESERT_EAGLE    ("deagle",        Type.PISTOL),
    DECOY           ("decoy",         Type.GRENADE),
    DUAL_BERETTAS   ("elite",         Type.PISTOL),
    FAMAS           ("famas",         Type.RIFLE),
    FIVE_SEVEN      ("fiveseven",     Type.PISTOL),
    FLASH_BANG      ("flashbang",     Type.GRENADE),
    G3SG1           ("g3sg1",         Type.SNIPER_RIFLE),
    GALIL_AR        ("galilar",       Type.RIFLE),
    GLOCK           ("glock",         Type.PISTOL),
    HEALTH_SHOT     ("healthshot",    Type.STACKABLE_ITEM),
    HE_GRENADE      ("hegrenade",     Type.GRENADE),
    P2000           ("hkp2000",       Type.PISTOL),
    INC_GRENADE     ("incgrenade",    Type.GRENADE),
    KNIFE_T         ("knife_t",       Type.KNIFE),
    KNIFE_CT        ("knife",         Type.KNIFE),
    M4A4            ("m4a1",          Type.RIFLE),
    M4A1_S          ("m4a1_silencer", Type.RIFLE),
    M249            ("m249",          Type.MACHINE_GUN),
    MAC_10          ("mac10",         Type.SUBMACHINE_GUN),
    MAG_7           ("mag7",          Type.SHOTGUN),
    MOLOTOV         ("molotov",       Type.GRENADE),
    MP7             ("mp7",           Type.SUBMACHINE_GUN),
    MP9             ("mp9",           Type.SUBMACHINE_GUN),
    NEGEV           ("negev",         Type.MACHINE_GUN),
    NOVA            ("nova",          Type.SHOTGUN),
    P90             ("p90",           Type.SUBMACHINE_GUN),
    P250            ("p250",          Type.PISTOL),
    REVOLVER        ("revolver",      Type.PISTOL),
    SAWED_OFF       ("sawedoff",      Type.SHOTGUN),
    SCAR_20         ("scar20",        Type.SNIPER_RIFLE),
    SG_553          ("sg556",         Type.RIFLE),
    TA_GRENADE      ("tagrenade",     Type.GRENADE),
    SMOKE_GRENADE   ("smokegrenade",  Type.GRENADE),
    SSG_08          ("ssg08",         Type.SNIPER_RIFLE),
    TEC_9           ("tec9",          Type.PISTOL),
    UMP_45          ("ump45",         Type.SUBMACHINE_GUN),
    USP_SILENCER    ("usp_silencer",  Type.PISTOL),
    XM1014          ("xm1014",        Type.SHOTGUN),
    TASER           ("taser",         null),
    FISTS           ("fists",         Type.FISTS),
    AXE             ("axe",           Type.MELEE),
    BREACH_CHARGE   ("breachcharge",  Type.BREACH_CHARGE),
    BUMP_MINE       ("bumpmine",      Type.BUMP_MINE),
    DIVERSION       ("diversion",     Type.GRENADE),
    MP5_SD          ("mp5sd",         Type.SUBMACHINE_GUN),
    HAMMER          ("hammer",        Type.MELEE),
    SHIELD          ("shield",        null),
    SNOWBALL        ("snowball",      Type.GRENADE),
    SPANNER         ("spanner",       Type.MELEE),
    TABLET          ("tablet",        Type.TABLET),
    KNIFE_GHOST     ("knife_ghost",   Type.KNIFE),
    ZONE_REPULSOR   ("zone_repulsor", null);
    
    
    
    private static final Map<String, Weapon> LOOKUP_MAP = new HashMap<>();
    
    static { // Initialize lookup map
        for (Weapon weapon : Weapon.values())
            LOOKUP_MAP.put(weapon.getName().toLowerCase(), weapon);
    }
    
    
    String name;
    Type type;
    
    Weapon(String name, Type type) {
        this.name = "weapon_" + name;
        this.type = type;
    }
    
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    public Type getType() {
        return type;
    }
    
    
    public static Weapon valueOfFromName(String name) {
        return LOOKUP_MAP.get(name.toLowerCase());
    }
    
    
    static class WeaponDeserializer implements JsonDeserializer<Weapon> {
        @Override
        public Weapon deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return valueOfFromName(json.getAsString());
        }
    }
    
    
    
    public enum Type {
        @SerializedName("Pistol")
        PISTOL,
        @SerializedName("Rifle")
        RIFLE,
        @SerializedName("Knife")
        KNIFE,
        @SerializedName("Fists")
        FISTS,
        @SerializedName("Tablet")
        TABLET,
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
        GRENADE,
        @SerializedName("Machine Gun")
        MACHINE_GUN,
        @SerializedName("Shotgun")
        SHOTGUN,
        @SerializedName("SniperRifle")
        SNIPER_RIFLE,
        @SerializedName("Bump Mine")
        BUMP_MINE;
    }
    
}


