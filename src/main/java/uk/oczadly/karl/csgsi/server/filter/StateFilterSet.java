package uk.oczadly.karl.csgsi.server.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class StateFilterSet {

    private static final Logger log = LoggerFactory.getLogger(StateFilterSet.class);

    public static final StateFilterSet UNFILTERED = new StateFilterSet();

    private final Set<StateFilter> filters;


    public StateFilterSet() {
        this.filters = Collections.emptySet();
    }

    public StateFilterSet(Set<StateFilter> filters) {
        this.filters = new HashSet<>(filters);
        for (StateFilter filter : this.filters) {
            if (filter == null)
                throw new NullPointerException("StateFilter object cannot be null.");
        }
    }


    public Set<StateFilter> getFilters() {
        return Collections.unmodifiableSet(filters);
    }

    public boolean isPermitted(GameStateContext stateContext) {
        if (filters.isEmpty()) {
            log.debug("Assuming proposed state is permitted as no filters are configured.");
        } else {
            for (StateFilter filter : getFilters()) {
                if (filter.isPermitted(stateContext)) {
                    log.debug("Proposed game state permitted by filter {}.", filter);
                } else {
                    log.debug("Proposed game state rejected by filter {}.", filter);
                    return false;
                }
            }
            log.debug("Proposed game state is permitted by all {} filters.", getFilters().size());
        }
        return true;
    }

}
