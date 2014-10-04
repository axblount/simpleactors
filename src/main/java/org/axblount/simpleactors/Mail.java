package org.axblount.simpleactors;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * TODO: make this serializable
 */
public class Mail {
    public final int id;
    public final Method method;
    public final Object[] args;

    public Mail(int id, Method method, Object[] args) {
        this.id = id;
        this.method = method;
        if (args != null)
            this.args = args;
        else
            this.args = new Object[0];
    }

    public String toString() {
        String[] sargs = Stream.of(args).map(Object::toString).toArray(String[]::new);
        return method.getName() + "(" + String.join(",", sargs) + ") -> " + id;
    }
}
