package org.axblount.simpleactors;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ActorTest {
    private ActorSystem sys;

    public interface I {
        public default void doNothing() {}
    }

    public class A extends Actor<I> implements I {
        @Override
        protected void exceptionHandler(Throwable e) {

        }
    }

    @Before
    public void setupSystem() {
        sys = new ActorSystem("test");
    }

    @After
    public void teardownSystem() {
        sys.shutdown();
    }

    @Test
    public void testMessage() {
        I ref = sys.spawn(A::new, I.class);
        ref.doNothing();
    }
}
