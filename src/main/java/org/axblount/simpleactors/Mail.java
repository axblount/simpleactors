package org.axblount.simpleactors;

/**
 *
 */
public class Mail {
    private final ActorImpl actor;
    private final Object msg;

    public Mail(ActorImpl actor, Object msg) {
        this.actor = actor;
        this.msg = msg;
    }

    public void deliver() {
        actor.handle(msg);
    }
}
