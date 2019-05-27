package uk.oczadly.karl.csgsi;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameState;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class GSIServerTest {

    @Test
    public void testObservers() throws Exception {
        GSIServer server = new GSIServer(1337);
    
        TestObserver observer = new TestObserver();
        server.registerObserver(observer);
        
        //Create mock objects
        GameState state = new GameState(), previous = new GameState();
        InetAddress address = InetAddress.getLoopbackAddress();
        
        //Notify observing object
        server.notifyObservers(state, previous, address);
        
        server.getObserverExecutorService().shutdown();
        server.getObserverExecutorService().awaitTermination(2, TimeUnit.SECONDS);
        
        //Verify objects match
        assertSame(state, observer.state);
        assertSame(previous, observer.previous);
        assertSame(address, observer.address);
    }
    
    
    private class TestObserver implements GSIObserver {
        
        GameState state;
        GameState previous;
        InetAddress address;
        
        @Override
        public void update(GameState state, GameState previous, InetAddress address) {
            this.state = state;
            this.previous = previous;
            this.address = address;
        }
        
    }

}