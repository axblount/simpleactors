package org.simpleactors;

import org.junit.*;

public class Main {
    public static class SayTo {
        public final String say;
        public final ActorRef to;
        public SayTo(String say, ActorRef to) {
            this.say = say;
            this.to = to;
        }
    }

    public static class MyActor extends Actor {
        @Override public void handle(Object msg, ActorRef sender) {
            if (msg instanceof String)
                System.out.println(sender + " said to me: " + msg);
            else
                System.out.println("I don't get it...");
        }
    }

    public static class Repeater extends Actor {
        @Override public void handle(Object msg, ActorRef sender) {
            if (msg instanceof SayTo) {
                System.out.println(sender + " told me to say something...");
                SayTo st = (SayTo) msg;
                st.to.send(st.say, getSelf());
            }
        }
    }

    @Test
    public void main() {
        ActorSystem sys = new ActorSystem("test");

        ActorRef a = sys.spawn(MyActor.class);
        ActorRef b = sys.spawn(Repeater.class);
        a.send("Hi!");
        a.send(17);
        b.send(new SayTo("Boo!", a));
    }
}
