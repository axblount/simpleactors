package org.simpleactors;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ConcurrentMap<Actor, WorkerThread> workerThreads;

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
        workerThreads = new ConcurrentHashMap<>();
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
    private class LocalActorRef implements ActorRef {
        private final Actor actor;
        private final int id;
        private WorkerThread worker;

        public LocalActorRef(Actor actor, int id) {
            this.actor = actor;
            this.id = id;
            this.worker = getWorkerThread(actor);
        }

        @Override public void send(Object msg, ActorRef sender) {
            // We cache the dispatcher. But we need to get a new one is this one is dead.
            if (!worker.isAlive())
                worker = getWorkerThread(actor);
            worker.dispatch(actor, msg, sender);
        }

        @Override public int getId() {
            return this.id;
        }

        @Override public String toString() {
            return String.format("<%s@%s>", id, getName());
        }
    }

    /**
     * Spawn a new actor inside of this system.
     *
     * @param type The class of actor to be spawned.
     * @return A reference to the newly spawned actor.
     */
    public ActorRef spawn(Class<? extends Actor> type) {
        Actor actor;
        try {
            actor = type.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException("Couldn't spawn actor of type " + type.getName(), e);
        }

        int id = nextId.getAndIncrement();
        actors.put(id, actor);
        ActorRef self = new LocalActorRef(actor, id);
        actor.bind(this, self);

        return self;
    }

    /**
     * Shutdown the ActorSystem.
     */
    public void shutdown() {
        //TODO
    }

    private Thread threadFactory(Runnable r) {
        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler(this::uncaughtThreadException);
        return t;
    }

    private void uncaughtThreadException(Thread t, Throwable e) {
        System.out.println("***The actor system <" + name + "> has caught an error***");
        e.printStackTrace();
    }

    private WorkerThread getWorkerThread(Actor actor) {
        WorkerThread worker = workerThreads.get(actor);
        if (worker == null || !worker.isAlive()) {
            // thread per actor, easy peasy
            worker = new WorkerThread(this::threadFactory);
            workerThreads.put(actor, worker);
        }
        return worker;
    }
}
