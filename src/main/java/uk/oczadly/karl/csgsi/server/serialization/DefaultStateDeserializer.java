package uk.oczadly.karl.csgsi.server.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import uk.oczadly.karl.csgsi.internal.Util;
import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.context.AuthTokenMap;

import java.util.Map;

import static uk.oczadly.karl.csgsi.internal.Util.GSON;

public final class DefaultStateDeserializer implements StateDeserializer {

    private final Gson gson;


    public DefaultStateDeserializer() {
        this(Util.GSON);
    }

    public DefaultStateDeserializer(Gson gson) {
        this.gson = gson;
    }


    public Gson getGson() {
        return gson;
    }


    @Override
    public GameState parseState(JsonObject json) throws JsonSyntaxException {
        return gson.fromJson(json, GameState.class);
    }

    @Override
    public AuthTokenMap parseAuthTokens(JsonObject json) throws JsonSyntaxException {
        JsonObject authJson = json.getAsJsonObject("auth");
        if (authJson != null && authJson.size() > 0) {
            return new AuthTokenMap(
                    GSON.fromJson(authJson, new TypeToken<Map<String, String>>(){}.getType()));
        } else {
            return AuthTokenMap.EMPTY;
        }
    }

}
