package uk.oczadly.karl.csgsi.server.filter;

import uk.oczadly.karl.csgsi.state.context.GameStateContext;

public interface StateFilter {

    boolean isPermitted(GameStateContext stateContext);

}
