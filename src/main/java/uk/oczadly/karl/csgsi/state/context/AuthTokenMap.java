package uk.oczadly.karl.csgsi.state.context;

import java.util.*;

public class AuthTokenMap {

    public static final AuthTokenMap EMPTY = new AuthTokenMap(Collections.emptyMap());

    private final Map<String, String> tokens = new HashMap<>();


    public AuthTokenMap(Map<String, String> tokens) {
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "Auth key cannot be null.");
            Objects.requireNonNull(entry.getValue(), "Auth token cannot be null.");
            String prev = this.tokens.put(entry.getKey().toLowerCase(), entry.getValue());
            if (prev != null)
                throw new IllegalArgumentException("Conflicting tokens are specified!");
        }
    }


    public Optional<String> getToken(String key) {
        return Optional.ofNullable(this.tokens.get(key.toLowerCase()));
    }

    public boolean isAuthenticated(String key, String expectedToken) {
        return getToken(key).map(expectedToken::equals).orElse(false);
    }

    public Set<Map.Entry<String, String>> getTokens() {
        return Collections.unmodifiableSet(tokens.entrySet());
    }

}
