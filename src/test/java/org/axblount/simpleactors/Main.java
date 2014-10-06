package org.axblount.simpleactors;

import java.util.Random;
import org.junit.*;

public class Main {
    public static interface I {
        public default void say(String s) {
            if (s == "error")
                throw new RuntimeException("test error.");
            System.out.println(s);
        }
        public default void sayTo(I ref, String s) {
            ref.say(s);
        }
    }

    public static class A extends Actor<I> implements I {
        @Override
        public void exceptionHandler(Throwable e) {
            System.out.println(getSelf() + " had an issue: " + e.getMessage());
        }
    }

    @Test
    public void main() {
        ActorSystem sys = new ActorSystem("test");
        Random r = new Random();

        int N = 3;
        I[] refs = new I[N];

        for (int i = 0; i < N; i++)
            refs[i] = sys.spawn(A::new, I.class);

        for (int i = 0; i < N; i++)
            refs[i].sayTo(refs[r.nextInt(N)], "This is " + i);

        refs[0].say("error");
    }
}
