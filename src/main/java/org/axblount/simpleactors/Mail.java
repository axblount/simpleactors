package org.axblount.simpleactors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 */
public class Mail {
    public final Actor<?> actor;
    public final MethodCall call;

    public Mail(Actor<?> actor, MethodCall call) {
        this.actor = actor;
        this.call = call;
    }

    public Mail(Actor<?> actor, Method m, Object[] args) {
        this(actor, new MethodCall(m, args));
    }

    public Object deliver() {
        try {
            return call.invoke(actor);
        } catch (InvocationTargetException e) {
            actor.exceptionHandler(e.getCause());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toString() {
        return call + " -> " + actor;
    }
}
