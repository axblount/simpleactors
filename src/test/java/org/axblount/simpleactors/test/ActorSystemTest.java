package org.axblount.simpleactors.test;

import org.axblount.simpleactors.ActorSystem;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by alex on 10/4/14.
 */
public class ActorSystemTest  {
    @Test
    public void testName() {
        ActorSystem sys = new ActorSystem("hello");
        assertThat(sys.getName(), is("hello"));
    }

    @Test(timeout = 500)
    public void testShutdown() {
        ActorSystem sys = new ActorSystem("test");
        sys.shutdown();
    }
}
