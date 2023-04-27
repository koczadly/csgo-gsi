package uk.oczadly.karl.csgsi.server.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;

import java.util.Objects;

public final class AuthTokenFilter implements StateFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final String key, expectedValue;


    public AuthTokenFilter(String key, String expectedValue) {
        Objects.requireNonNull(key, "Key cannot be null.");
        Objects.requireNonNull(expectedValue, "Expected value cannot be null.");
        this.key = key.toLowerCase();
        this.expectedValue = expectedValue;
    }


    public String getKey() {
        return key;
    }

    public String getExpectedValue() {
        return expectedValue;
    }


    @Override
    public boolean isPermitted(GameStateContext stateContext) {
        boolean allowed = stateContext.getAuthTokens()
                .getToken(key)
                .map(expectedValue::equals)
                .orElse(false);
        if (!allowed)
            log.info("Received state update from unauthorized client! Missing or invalid auth token '{}'.", key);
        return allowed;
    }


    @Override
    public String toString() {
        return "AuthTokenStateFilter{" +
                "key='" + key + '\'' +
                ", expectedValue='" + expectedValue + '\'' +
                '}';
    }

}
