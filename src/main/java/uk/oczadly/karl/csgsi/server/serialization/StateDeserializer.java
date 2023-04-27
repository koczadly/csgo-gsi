package uk.oczadly.karl.csgsi.server.serialization;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import uk.oczadly.karl.csgsi.state.GameState;
import uk.oczadly.karl.csgsi.state.context.AuthTokenMap;

public interface StateDeserializer {

    GameState parseState(JsonObject json) throws JsonParseException;

    AuthTokenMap parseAuthTokens(JsonObject json) throws JsonParseException;

}
