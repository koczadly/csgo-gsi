package uk.oczadly.karl.csgsi.state;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import uk.oczadly.karl.csgsi.state.components.grenade.EffectGrenade;
import uk.oczadly.karl.csgsi.state.components.grenade.Grenade;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;
import uk.oczadly.karl.csgsi.state.components.grenade.IncendiaryGrenade;
import uk.oczadly.karl.csgsi.state.components.grenade.ProjectileGrenade;

import java.lang.reflect.Type;
import java.util.*;

/**
 * This state object represents a set of grenades which currently exist on the map.
 *
 * <p>Grenade classes can exist in the following types, which can be casted to access the specific attributes.</p>
 * <ul>
 *     <li>{@link Grenade} (base)</li>
 *     <li>{@link ProjectileGrenade}</li>
 *     <li>{@link EffectGrenade}</li>
 *     <li>{@link IncendiaryGrenade}</li>
 * </ul>
 */
@JsonAdapter(GrenadeState.Adapter.class)
public class GrenadeState {
    
    private final Map<Integer, Grenade> grenades;
    
    private GrenadeState(Map<Integer, Grenade> grenades) {
        this.grenades = grenades;
    }
    
    
    public Map<Integer, Grenade> getAll() {
        return grenades;
    }
    
    public Grenade getById(int id) {
        return grenades.get(id);
    }
    
    public Map<Integer, Grenade> getByType(Grenade.Type type) {
        Map<Integer, Grenade> matches = new HashMap<>();
        for (Map.Entry<Integer, Grenade> g : grenades.entrySet()) {
            if (g.getValue().getType().enumVal() == type)
                matches.put(g.getKey(), g.getValue());
        }
        return Collections.unmodifiableMap(matches);
    }
    
    public Map<Integer, Grenade> getByOwner(PlayerSteamID playerId) {
        Map<Integer, Grenade> matches = new HashMap<>();
        for (Map.Entry<Integer, Grenade> g : grenades.entrySet()) {
            if (g.getValue().getOwner().equals(playerId))
                matches.put(g.getKey(), g.getValue());
        }
        return Collections.unmodifiableMap(matches);
    }
    
    
    static class Adapter implements JsonDeserializer<GrenadeState> {
        @Override
        public GrenadeState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new GrenadeState(context.deserialize(json, new TypeToken<Map<Integer, Grenade>>() {}.getType()));
        }
    }
    
}
