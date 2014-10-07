package org.simpleactors;

/**
 * An abstract actor.
 *
 * All concrete actor classes must inherit from {@link org.axblount.simpleactors.Actor}.
 */
public abstract class Actor {
    /**
     * NOBODY is the default message sender when none is specified.
     * Messages sent to NOBODY are not sent or acknowledged in any way.
     * NOBODY eats messages.
     */
    public static final ActorRef NOBODY = new ActorRef() {
        @Override public void send(Object msg, ActorRef sender) { }
        @Override public int getId() { return 0; }
        @Override public String toString() { return "<NOBODY>"; }
    };

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

    public abstract void handle(Object msg, ActorRef sender);

    public void handleException(Exception e) { }
}
