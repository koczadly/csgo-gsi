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
    //Enum                "weapon_xxx"             Type                 Display name
    CZ75_A               ("cz75a",                 Type.PISTOL,         "CZ75-Auto"),
    DESERT_EAGLE         ("deagle",                Type.PISTOL,         "Desert Eagle"),
    DUAL_BERETTAS        ("elite",                 Type.PISTOL,         "Dual Berettas"),
    FIVE_SEVEN           ("fiveseven",             Type.PISTOL,         "Five-SeveN"),
    GLOCK                ("glock",                 Type.PISTOL,         "Glock"),
    P2000                ("hkp2000",               Type.PISTOL,         "P2000"),
    P250                 ("p250",                  Type.PISTOL,         "P250"),
    REVOLVER             ("revolver",              Type.PISTOL,         "R8 Revolver"),
    TEC_9                ("tec9",                  Type.PISTOL,         "Tec-9"),
    USP_SILENCER         ("usp_silencer",          Type.PISTOL,         "USP-S"),
    AK_47                ("ak47",                  Type.RIFLE,          "AK47"),
    AUG                  ("aug",                   Type.RIFLE,          "AUG"),
    FAMAS                ("famas",                 Type.RIFLE,          "FAMAS"),
    GALIL_AR             ("galilar",               Type.RIFLE,          "Galil AR"),
    M4A4                 ("m4a1",                  Type.RIFLE,          "M4A4"),
    M4A1_S               ("m4a1_silencer",         Type.RIFLE,          "M4A1-S"),
    SG_553               ("sg556",                 Type.RIFLE,          "SG 553"),
    AWP                  ("awp",                   Type.SNIPER_RIFLE,   "AWP"),
    G3SG1                ("g3sg1",                 Type.SNIPER_RIFLE,   "G3SG1"),
    SCAR_20              ("scar20",                Type.SNIPER_RIFLE,   "SCAR-20"),
    SSG_08               ("ssg08",                 Type.SNIPER_RIFLE,   "SSG 08"),
    BIZON                ("bizon",                 Type.SUBMACHINE_GUN, "PP-Bizon"),
    MAC_10               ("mac10",                 Type.SUBMACHINE_GUN, "MAC-10"),
    MP7                  ("mp7",                   Type.SUBMACHINE_GUN, "MP7"),
    MP9                  ("mp9",                   Type.SUBMACHINE_GUN, "MP9"),
    P90                  ("p90",                   Type.SUBMACHINE_GUN, "P90"),
    UMP_45               ("ump45",                 Type.SUBMACHINE_GUN, "UMP-45"),
    MP5_SD               ("mp5sd",                 Type.SUBMACHINE_GUN, "MP5-SD"),
    M249                 ("m249",                  Type.MACHINE_GUN,    "M249"),
    NEGEV                ("negev",                 Type.MACHINE_GUN,    "Negev"),
    MAG_7                ("mag7",                  Type.SHOTGUN,        "MAG-7"),
    NOVA                 ("nova",                  Type.SHOTGUN,        "Nova"),
    SAWED_OFF            ("sawedoff",              Type.SHOTGUN,        "Sawed-Off"),
    XM1014               ("xm1014",                Type.SHOTGUN,        "XM1014"),
    DECOY                ("decoy",                 Type.GRENADE,        "Decoy Grenade"),
    FLASH_BANG           ("flashbang",             Type.GRENADE,        "Flashbang"),
    HE_GRENADE           ("hegrenade",             Type.GRENADE,        "HE Grenade"),
    INC_GRENADE          ("incgrenade",            Type.GRENADE,        "Incendiary Grenade"),
    TA_GRENADE           ("tagrenade",             Type.GRENADE,        "Tactical Awareness Grenade"),
    SMOKE_GRENADE        ("smokegrenade",          Type.GRENADE,        "Smoke Grenade"),
    SNOWBALL             ("snowball",              Type.GRENADE,        "Snowball"),
    MOLOTOV              ("molotov",               Type.GRENADE,        "Molotov"),
    DIVERSION            ("diversion",             Type.GRENADE,        "Diversion Grenade"),
    HEALTH_SHOT          ("healthshot",            Type.STACKABLE_ITEM, "Health Shot"),
    C4                   ("c4",                    Type.BOMB,           "C4 Explosive"),
    TASER                ("taser",                 null,                "Zeus x27"),
    AXE                  ("axe",                   Type.MELEE,          "Axe"),
    HAMMER               ("hammer",                Type.MELEE,          "Hammer"),
    SPANNER              ("spanner",               Type.MELEE,          "Spanner"),
    TABLET               ("tablet",                Type.TABLET,         "Tablet"),
    BREACH_CHARGE        ("breachcharge",          Type.BREACH_CHARGE,  "Breach Charge"),
    BUMP_MINE            ("bumpmine",              Type.BUMP_MINE,      "Bump Mine"),
    SHIELD               ("shield",                null,                "Shield"),
    ZONE_REPULSOR        ("zone_repulsor",         null,                "Zone Repulsor"),
    FISTS                ("fists",                 Type.FISTS,          "Fists"),
    KNIFE_T              ("knife_t",               Type.KNIFE,          "Knife"),
    KNIFE_CT             ("knife",                 Type.KNIFE,          "Knife"),
    KNIFE_GHOST          ("knife_ghost",           Type.KNIFE,          "Spectral Shiv"),
    KNIFE_BAYONET        ("bayonet",               Type.KNIFE,          "Bayonet"),
    KNIFE_BOWIE          ("knife_survival_bowie",  Type.KNIFE,          "Bowie Knife"),
    KNIFE_BUTTERFLY      ("knife_butterfly",       Type.KNIFE,          "Butterfly Knife"),
    KNIFE_FALCHION       ("knife_falchion",        Type.KNIFE,          "Falchion Knife"),
    KNIFE_FLIP           ("knife_flip",            Type.KNIFE,          "Flip Knife"),
    KNIFE_GUT            ("knife_gut",             Type.KNIFE,          "Gut Knife"),
    KNIFE_HUNTSMAN       ("knife_tactical",        Type.KNIFE,          "Huntsman Knife"),
    KNIFE_KARAMBIT       ("knife_karambit",        Type.KNIFE,          "Karambit"),
    KNIFE_M9_BAYONET     ("knife_m9_bayonet",      Type.KNIFE,          "M9 Bayonet"),
    KNIFE_SHADOW_DAGGERS ("knife_push",            Type.KNIFE,          "Shadow Daggers"),
    KNIFE_NAVAJA         ("knife_gypsy_jackknife", Type.KNIFE,          "Navaja Knife"),
    KNIFE_STILETTO       ("knife_stiletto",        Type.KNIFE,          "Stiletto Knife"),
    KNIFE_TALON          ("knife_widowmaker",      Type.KNIFE,          "Talon Knife"),
    KNIFE_URSUS          ("knife_ursus",           Type.KNIFE,          "Ursus Knife"),
    KNIFE_CLASSIC        ("knife_css",             Type.KNIFE,          "Classic Knife"),
    KNIFE_PARACORD       ("knife_cord",            Type.KNIFE,          "Paracord Knife"),
    KNIFE_SURVIVAL       ("knife_canis",           Type.KNIFE,          "Survival Knife"),
    KNIFE_NOMAD          ("knife_outdoor",         Type.KNIFE,          "Nomad Knife"),
    KNIFE_SKELETON       ("knife_skeleton",        Type.KNIFE,          "Skeleton Knife");
    
    
    
    private static final Map<String, Weapon> LOOKUP_MAP = new HashMap<>();
    
    static { // Initialize lookup map
        for (Weapon weapon : Weapon.values())
            LOOKUP_MAP.put(weapon.getName().toLowerCase(), weapon);
    }
    
    
    final String entityName, displayName;
    final Type type;
    
    Weapon(String name, Type type, String displayName) {
        this.entityName = "weapon_" + name;
        this.type = type;
        this.displayName = displayName;
    }
    
    
    /**
     * @return the internal name of the weapon
     */
    public String getName() {
        return entityName;
    }
    
    /**
     * @return the display name of the weapon
     */
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    public Type getType() {
        return type;
    }
    
    
    public static Weapon valueOfFromName(String name) {
        return LOOKUP_MAP.get(name.toLowerCase());
    }
    
    
    static class WeaponDeserializer implements JsonDeserializer<Weapon> {
        @Override
        public Weapon deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return valueOfFromName(json.getAsString());
        }
    }
    
    
    
    public enum Type {
        @SerializedName("Pistol")         PISTOL          (false, false, true, false, true),
        @SerializedName("Rifle")          RIFLE           (false, false, true, true, false),
        @SerializedName("Knife")          KNIFE           (true, false, false, false, false),
        @SerializedName("Fists")          FISTS           (true, false, false, false, false),
        @SerializedName("Tablet")         TABLET          (false, false, false, false, false),
        @SerializedName("StackableItem")  STACKABLE_ITEM  (false, true, false, false, false),
        @SerializedName("Submachine Gun") SUBMACHINE_GUN  (false, false, true, true, false),
        @SerializedName("C4")             BOMB            (false, false, false, false, false),
        @SerializedName("Melee")          MELEE           (true, false, false, false, false),
        @SerializedName("Breach Charge")  BREACH_CHARGE   (false, false, false, false, false),
        @SerializedName("Grenade")        GRENADE         (false, true, false, false, false),
        @SerializedName("Machine Gun")    MACHINE_GUN     (false, false, true, true, false),
        @SerializedName("Shotgun")        SHOTGUN         (false, false, true, true, false),
        @SerializedName("SniperRifle")    SNIPER_RIFLE    (false, false, true, true, false),
        @SerializedName("Bump Mine")      BUMP_MINE       (false, false, false, false, false);
        
        
        final boolean isMelee, isUtility, isGun, isPrimary, isSecondary;
        
        Type(boolean isMelee, boolean isUtility, boolean isGun, boolean isPrimary, boolean isSecondary) {
            this.isMelee = isMelee;
            this.isUtility = isUtility;
            this.isGun = isGun;
            this.isPrimary = isPrimary;
            this.isSecondary = isSecondary;
        }
    
    
        /**
         * @return true if the type is a melee (close-range) weapon
         */
        public boolean isMelee() {
            return isMelee;
        }
    
        /**
         * @return true if the type is a utility item (grenade or stackable item)
         */
        public boolean isUtility() {
            return isUtility;
        }
    
        /**
         * @return true if the type is a gun
         */
        public boolean isFirearm() {
            return isGun;
        }
    
        /**
         * @return true if the type is a primary weapon
         */
        public boolean isPrimaryWeapon() {
            return isPrimary;
        }
    
        /**
         * @return true if the type is a secondary weapon
         */
        public boolean isSecondaryWeapon() {
            return isSecondary;
        }
    }
    
}


