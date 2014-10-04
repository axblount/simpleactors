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
    private ConcurrentMap<Integer, Actor<?>> actors;

    /**
     * A map of all {@link Actor}s to the threads they run in.
     */
    private ConcurrentMap<Actor<?>, Dispatcher> dispatchThreads;

    /**
     * Create a new {@link ActorSystem}.
     *
     * @param name The name of this {@link ActorSystem}.
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
    private class LocalRefProxyHandler implements InvocationHandler {
        private final Actor<?> actor;
        private LocalRefProxyHandler(Actor<?> actor) { this.actor = actor; }
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // if we are trying to print out the reference, do it now.
            if (method.getName() == "toString" && method.getParameterCount() == 0) {
                return String.format("<%s@%s>", actor, getName());
            }
            // otherwise we are trying to send an message. place it in the mailbox
            Mail m = new Mail(actor, method, args);
            Dispatcher dispatcher = getDispatcher(actor);
            return dispatcher.addMail(m);
        }
    }

    /**
     * Create a new actor and return a reference that implements the given interface.
     *
     * @param constructor This function should return a new instance of actor when called.
     *             The easiest way is {@code MyActor::new}.
     * @param refType This interface you this actor's reference will implement.
     * @param <ACTOR> The type of the actor.
     * @param <REF> The type of the actor's reference interface.
     * @return A reference to the newly spawned actor.
     */
    public <ACTOR extends Actor<REF>, REF> REF spawn(Supplier<ACTOR> constructor, Class<REF> refType) {
        // construct our actor
        ACTOR actor = constructor.get();

        if (!refType.isAssignableFrom(actor.getClass()))
            throw new IllegalArgumentException("The actor must implement the reference type interface.");

        // load the proxy class for the reference interface
        // classes created this way should be cached by the Proxy class
        Class<?> proxyCls = Proxy.getProxyClass(refType.getClassLoader(), refType);

        try {
            // create a new instance of the proxy class with a new InvocationHandler for this actor's actor
            @SuppressWarnings("unchecked")
            REF ref = (REF)proxyCls.getConstructor(InvocationHandler.class)
                               .newInstance(new LocalRefProxyHandler(actor));

            int id = nextId.getAndIncrement();
            actors.put(id, actor);
            actor.bind(this, ref);
            return ref;
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            // something went wrong
            throw new RuntimeException("Couldn't create proxy instance.", e);
        }
    }

    /**
     * Shutdown the ActorSystem.
     */
    public void shutdown() {
        //TODO
    }

    /*package*/ Actor<?> getActorById(int id) {
        return actors.get(id);
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


    private Dispatcher getDispatcher(Actor<?> actor) {
        Dispatcher dispatcher = dispatchThreads.get(actor);
        if (dispatcher == null || !dispatcher.getThread().isAlive()) {
            // thread per actor, easy peasy
            dispatcher = new Dispatcher(this::threadFactory);
            dispatchThreads.put(actor, dispatcher);
        }
        return dispatcher;
    }
}
