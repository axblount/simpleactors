package org.axblount.simpleactors;

/**
 * An abstract actor.
 *
 * All concrete actor classes must inherit from {@link org.axblount.simpleactors.Actor}.
 */
public abstract class Actor<REF> {
    /**
     * The {@link ActorSystem} that this {@link Actor} runs inside of.
     */
    private ActorSystem context = null;

    /**
     * A reference to this actor.
     */
    private REF self = null;

    /**
     * The {@link ActorSystem} this {@link Actor} is running inside of.
     * @return The {@link ActorSystem} this {@link Actor} is running inside of.
     */
    protected final ActorSystem getContext() { return context; }

    /**
     * A reference to this {@link Actor}. Can be used to send messages to itself or for debug output.
     * @return A reference to this {@link Actor}.
     */
    protected final REF getSelf() { return self; }

    /**
     * This method is used by an {@link ActorSystem} to bind this actor to it.
     *
     * @param context
     * @param self
     */
    /*package*/ final void bind(ActorSystem context, REF self) {
        if (context == null || self == null)
            throw new IllegalArgumentException("arguments to Actor#bind cannot be null");
        if (this.context != null)
            throw new IllegalStateException("An actor cannot be bound more than once.");
        this.context = context;
        this.self = self;
    }

    /**
     * TODO
     * @param e
     */
    protected abstract void exceptionHandler(Throwable e);
}
