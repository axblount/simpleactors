package org.simpleactors;

public interface ActorRef {
    public default void send(Object msg) {
        send(msg, Actor.NOBODY);
    }
    public void send(Object msg, ActorRef sender);
    public int getId();
}
