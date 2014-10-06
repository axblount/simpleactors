package org.axblount.simpleactors;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

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
