package org.simpleactors;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * A thread used to dispatch mail.
 *
 * TODO: It might not be the "best" way to do it, but extending Thread seems convenient...
 */
public class WorkerThread implements Runnable {
    private class Mail {
        final Actor actor;
        final Object msg;
        final ActorRef sender;

        public Mail(Actor actor, Object msg, ActorRef sender) {
            this.actor = actor;
            this.msg = msg;
            this.sender = sender;
        }
    }

    /**
     * A DispatchThread will wait for {@code DEFAULT_TIMEOUT} before closing down.
     */
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    /**
     * A queue of mail waiting to be dispatched.
     */
    private BlockingQueue<Mail> mailbox;

    /**
     * The Thread running this Dispatcher.
     */
    private Thread thread;

    /**
     * Create a new dispatcher using the given {@link java.util.concurrent.ThreadFactory}.
     *
     * @param threadFactory This will be used to generate the thread this Dispatcher will run in.
     */
    public WorkerThread(ThreadFactory threadFactory) {
        mailbox = new LinkedBlockingQueue<>();
        thread = threadFactory.newThread(this);
        if (thread == null)
            throw new IllegalArgumentException("A Dispatcher's ThreadFactory must not return null.");
        thread.start();
    }

    /**
     * Queue a message for dispatch.
     *
     * @param actor The concrete actor {@code msg} is being sent to.
     * @param msg The message being sent.
     * @return {@code true} if the mail was successfully added to the queue, {@code false} otherwise.
     */
    public boolean dispatch(Actor actor, Object msg, ActorRef sender) {
        return mailbox.add(new Mail(actor, msg, sender));
    }

    /**
     * @return The thread running this Dispatcher.
     */
    public Thread getThread() {
        return thread;
    }

    /**
     *
     */
    public boolean isAlive() {
        return thread.isAlive();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Mail m = mailbox.poll(DEFAULT_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
                if (m == null) return; // timed out

                try {
                    m.actor.handle(m.msg, m.sender);
                } catch (Exception e) {
                    m.actor.handleException(e);
                }
            }
        } catch (InterruptedException e) {
            // end
        }
    }
}
