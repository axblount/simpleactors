package org.simpleactors;

import java.util.Random;
import org.junit.*;

public class Main {
    public static class A extends Actor {
        @Override public void handle(Object msg) {
            if (msg instanceof String)
                System.out.println("I got: " + msg);
            else
                System.out.println("I got a message i don't understand.");
        }
        @Override public void exceptionHandler(Throwable e) {
            System.out.println(getSelf() + " had an issue: " + e.getMessage());
        }
    }

    @Test
    public void main() {
        ActorSystem sys = new ActorSystem("test");
        Random r = new Random();

        int N = 3;
        ActorRef[] refs = new ActorRef[N];

        for (int i = 0; i < N; i++)
            refs[i] = sys.spawn(A.class);

        for (int i = 0; i < N; i++)
            refs[i].send("This is " + i);
    }
}
