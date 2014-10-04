package org.axblount.simpleactors;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * An {@link org.axblount.simpleactors.ActorSystem} is the context in which actors run.
 * Each actor belongs to exactly one actor system.
 * <p>
 * TODO: research for remote actors
 * <ul>
 *     <li>{@link java.net.URLClassLoader}</li>
 *     <li>How to serialize {@link java.lang.reflect.Method}</li>
 * </ul>
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
     * Each time a new actor is spawned it is given an id unique to this {@link ActorSystem}.
     */
    private AtomicInteger nextId;

    /**
     * A map of all running {@link Actor}s indexed by id.
     */
    private ConcurrentMap<Integer, Actor> actors;

    /**
     * This queue stores all messages sent to {@link Actor}s running on this {@link ActorSystem}.
     */
    private BlockingQueue<Mail> mailbox;

    /**
     * The {@link java.util.concurrent.ExecutorService} responsible for running the {@link Actor}s.
     */
    private ExecutorService executorService;

    /**
     * The {@link Thread} responsible for dispatching incoming mail to {@link Actor}s.
     * The thread runs the private method {@link #mailDispatcher()}.
     */
    private Thread mailDispatcherThread;

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
        mailbox = new LinkedBlockingQueue<>();

        executorService = Executors.newCachedThreadPool(this::threadFactory);

        mailDispatcherThread = new Thread(this::mailDispatcher);
        mailDispatcherThread.start();
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
        private final int id;
        private LocalRefProxyHandler(int id) { this.id = id; }
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // if we are trying to print out the reference, do it now.
            if (method.getName() == "toString" && method.getParameterCount() == 0) {
                return String.format("<%s@%s>", id, getName());
            }
            // otherwise we are trying to send an message. place it in the mailbox
            Mail m = new Mail(id, method, args);
            System.out.println(m);
            return mailbox.offer(m);
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
            int id = nextId.getAndIncrement();

            // create a new instance of the proxy class with a new InvocationHandler for this actor's id
            @SuppressWarnings("unchecked")
            REF ref = (REF)proxyCls.getConstructor(InvocationHandler.class)
                               .newInstance(new LocalRefProxyHandler(id));
            actors.put(id, actor);
            actor.bind(this, ref);
            return ref;
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            // something went wrong
            throw new RuntimeException("Couldn't create proxy instance.", e);
        }
    }

    /**
     * Shutdown this {@link ActorSystem}. Pending mail will be discarded.
     */
    public void shutdown() {
        try {
            mailDispatcherThread.interrupt();
            executorService.shutdown();

            mailDispatcherThread.join();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This is used a reference to run the {@link #mailDispatcherThread}.
     */
    private void mailDispatcher() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Mail m = mailbox.take();
                Actor<?> actor = actors.get(m.id);
                // TODO:
                // this seems very inefficient
                // I need to figure out how to sending throwing lambdas to the executor service.
                executorService.execute(() -> {
                    try {
                        m.method.invoke(actor, m.args);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        actor.exceptionHandler(e);
                    }
                });
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private Thread threadFactory(Runnable r) {
        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler(this::uncaughtException);
        return t;
    }

    private void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }
}
