package uk.oczadly.karl.csgsi;

import org.junit.Test;
import uk.oczadly.karl.csgsi.state.GameState;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Karl Oczadly
 */
public class ObserverRegistryTest {
    
    @Test
    public void testRegister() {
        ObserverRegistry reg = new ObserverRegistry();
        MockObserver obs1 = new MockObserver(), obs2 = new MockObserver();
        assertEquals(0, reg.size());
        reg.register(obs1);
        reg.register(obs2);
        assertEquals(2, reg.size());
        assertTrue(reg.observers.contains(obs1));
        assertTrue(reg.observers.contains(obs2));
    }
    
    @Test
    public void testRegisterMulti() {
        ObserverRegistry reg = new ObserverRegistry();
        MockObserver obs1 = new MockObserver(), obs2 = new MockObserver();
        assertEquals(0, reg.size());
        reg.register(Set.of(obs1, obs2));
        assertEquals(2, reg.size());
        assertTrue(reg.observers.contains(obs1));
        assertTrue(reg.observers.contains(obs2));
    }
    
    @Test
    public void testRemove() {
        ObserverRegistry reg = new ObserverRegistry();
        MockObserver obs1 = new MockObserver(), obs2 = new MockObserver();
        reg.register(Set.of(obs1, obs2));
        reg.remove(obs2);
        assertEquals(1, reg.size());
    }
    
    @Test
    public void testClear() {
        ObserverRegistry reg = new ObserverRegistry();
        MockObserver obs1 = new MockObserver(), obs2 = new MockObserver();
        reg.register(Set.of(obs1, obs2));
        reg.clear();
        assertEquals(0, reg.size());
    }
    
    @Test //TODO: doesn't test context object is the same
    public void testNotify() throws InterruptedException {
        ObserverRegistry reg = new ObserverRegistry();
        CountDownLatch obsLatch = new CountDownLatch(2);
        MockObserver obs1 = new MockObserver(obsLatch), obs2 = new MockObserver(obsLatch);
        reg.register(Set.of(obs1, obs2));
        GameState mockState = new GameState();
        reg.notify(mockState, null); // Notify
        assertTrue(obsLatch.await(1, TimeUnit.SECONDS)); // Wait for notification
        assertTrue(obs1.called && obs2.called);
        assertSame(mockState, obs1.state);
        assertSame(mockState, obs2.state);
    }
    
}