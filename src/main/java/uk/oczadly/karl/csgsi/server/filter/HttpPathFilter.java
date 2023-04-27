package uk.oczadly.karl.csgsi.server.filter;

import uk.oczadly.karl.csgsi.state.context.GameStateContext;

public final class HttpPathFilter implements StateFilter {

    private final String uriPath;

    public HttpPathFilter(String uriPath) {
        this.uriPath = uriPath;
    }


    @Override
    public boolean isPermitted(GameStateContext stateContext) {
        return stateContext.getPath().equalsIgnoreCase(uriPath);
    }


    @Override
    public String toString() {
        return "PathStateFilter{" +
                "uriPath='" + uriPath + '\'' +
                '}';
    }
}
