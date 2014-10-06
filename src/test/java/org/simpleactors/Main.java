package org.simpleactors;

import org.junit.*;

public class Main {
    public static class MyActor extends Actor {
        @Override public void handle(Object msg) {
            if (msg instanceof String)
                System.out.println("Someone said: " + msg);
            else
                System.out.println("I don't get it...");
        }
    }

    @Test
    public void main() {
        ActorSystem sys = new ActorSystem("test");

        ActorRef ref = sys.spawn(MyActor.class);
        ref.send("Hi!");
        ref.send(17);
    }
}
