package uk.oczadly.karl.csgsi.server.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;

public class LocalClientFilter implements StateFilter {

    private static final Logger log = LoggerFactory.getLogger(LocalClientFilter.class);


    @Override
    public boolean isPermitted(GameStateContext stateContext) {
        boolean allowed = stateContext.getClientAddress().isLoopbackAddress();
        if (!allowed)
            log.info("Received state update from non-local client host {}!", stateContext.getClientAddress());
        return allowed;
    }


    @Override
    public String toString() {
        return "LocalClientStateFilter{}";
    }

}
