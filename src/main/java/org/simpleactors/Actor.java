package org.simpleactors;

/**
 * An abstract actor.
 *
 * All concrete actor classes must inherit from {@link org.axblount.simpleactors.Actor}.
 */
public abstract class Actor {
    /**
     * The {@link ActorSystem} that this {@link Actor} runs inside of.
     */
    private ActorSystem system = null;

    /**
     * A reference to this actor.
     */
    private ActorRef self = null;

    /**
     * The {@link ActorSystem} this {@link Actor} is running inside of.
     * @return The {@link ActorSystem} this {@link Actor} is running inside of.
     */
    protected final ActorSystem getSystem() { return system; }

    /**
     * A reference to this {@link Actor}. Can be used to send messages to itself or for debug output.
     * @return A reference to this {@link Actor}.
     */
    protected final ActorRef getSelf() { return self; }

    /**
     * This method is used by an {@link ActorSystem} to bind this actor to it.
     *
     * @param system The {@link ActorSystem} this Actor is running inside of.
     * @param self A reference to this actor.
     */
    /*package*/ final void bind(ActorSystem system, ActorRef self) {
        if (this.system != null)
            throw new IllegalStateException("An actor cannot be bound more than once.");
        if (system == null || self == null)
            throw new IllegalArgumentException("arguments to Actor#bind cannot be null");
        this.system = system;
        this.self = self;
    }

    public abstract void handle(Object msg);

    /**
     * TODO
     * @param e
     */
    protected abstract void exceptionHandler(Throwable e);
}
