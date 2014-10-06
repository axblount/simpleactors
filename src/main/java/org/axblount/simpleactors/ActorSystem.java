package org.axblount.simpleactors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * An {@link org.axblount.simpleactors.ActorSystem} is the context in which actors run.
 * Each actor belongs to exactly one actor system.
 */
public class ActorSystem {
    /**
     * The name of this {@link ActorSystem}.
     * This will be used by other {@link ActorSystem}s.
     */
    private final String name;

    /**
     * The port used to listen for messages from other {@link ActorSystem}s.
     */
    private final int port;

    /**
     * The default value for {@link #port}.
     */
    private static final int DEFAULT_PORT = 12321;

    /**
     * Each time a new actor is spawned it is given an actor unique to this {@link ActorSystem}.
     */
    private AtomicInteger nextId;

    /**
     * A map of all running {@link Actor}s indexed by actor.
     */
    private ConcurrentMap<Integer, Actor> actors;

    /**
     * A map of all {@link Actor}s to the threads they run in.
     */
    private ConcurrentMap<Actor, Dispatcher> dispatchThreads;

    /**
     * Create a new {@link ActorSystem}.
     *
     * @param name The name of this {@link ActorSystem}.
     * @param port The port number to listen on.
     */
    public ActorSystem(String name, int port) {
        this.name = name;
        this.port = port;
        nextId = new AtomicInteger(1000);
        actors = new ConcurrentHashMap<>();
        dispatchThreads = new ConcurrentHashMap<>();
    }

    public ActorSystem(String name) {
        this(name, DEFAULT_PORT);
    }

    /**
     * Gets the name of this {@link ActorSystem}.
     *
     * @return The name of this {@link ActorSystem}.
     */
    public String getName() { return name; }

    /**
     * This is the {@link java.lang.reflect.InvocationHandler} used for local references to actors.
     * An instance of this class is supplied to newly constructed proxy references.
     */
    private class LocalActor implements ActorRef {
        private final Actor actor;
        private Dispatcher disp;
        public LocalActor(Actor actor) {
            this.actor = actor;
            this.disp = getDispatcher(actor);
        }
        @Override public void send(Object msg) {
            // We cache the dispatcher. But we need to get a new one is this one is dead.
            if (!disp.isAlive())
                disp = getDispatcher(actor);
            disp.dispatch(actor, msg);
        }
    }

    /**
     * Spawn a new actor inside of this system.
     *
     * @param type The class of actor to be spawned.
     * @return A reference to the newly spawned actor.
     */
    public ActorRef spawn(Class<? extends Actor> type) {
        // construct our actor
        try {
            Actor actor = type.newInstance();
            return new LocalActor(actor);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException("Couldn't spawn actor of type " + type.getName(), e);
        }
    }

    /**
     * Shutdown the ActorSystem.
     */
    public void shutdown() {
        //TODO
    }

    private Thread threadFactory(Runnable r) {
        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler(this::uncaughtException);
        return t;
    }

    private void uncaughtException(Thread t, Throwable e) {
        System.out.println("***The actor system <" + name + "> has caught an error***");
        e.printStackTrace();
    }


    private Dispatcher getDispatcher(Actor actor) {
        Dispatcher dispatcher = dispatchThreads.get(actor);
        if (dispatcher == null || !dispatcher.isAlive()) {
            // thread per actor, easy peasy
            dispatcher = new Dispatcher(this::threadFactory);
            dispatchThreads.put(actor, dispatcher);
        }
        return dispatcher;
    }
}
