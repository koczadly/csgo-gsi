package uk.oczadly.karl.csgsi.server.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.oczadly.karl.csgsi.state.context.GameStateContext;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClientAddressFilter implements StateFilter {

    private static final Logger log = LoggerFactory.getLogger(ClientAddressFilter.class);

    private final Set<InetAddress> allowableAddresses;


    public ClientAddressFilter(Set<InetAddress> allowableAddresses) {
        this.allowableAddresses = allowableAddresses;
    }

    public ClientAddressFilter(InetAddress... allowableAddresses) {
        this(Arrays.stream(allowableAddresses).collect(Collectors.toSet()));
    }


    public Set<InetAddress> getAllowableAddresses() {
        return Collections.unmodifiableSet(allowableAddresses);
    }


    @Override
    public boolean isPermitted(GameStateContext stateContext) {
        boolean allowed = allowableAddresses.contains(stateContext.getClientAddress());
        if (!allowed)
            log.info("Received state update from unauthorized client host {}!", stateContext.getClientAddress());
        return allowed;
    }


    @Override
    public String toString() {
        return "ClientIPStateFilter{" +
                "allowableAddresses=" + allowableAddresses +
                '}';
    }

}
