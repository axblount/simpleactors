package org.axblount.simpleactors;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * TODO make this serializable
 */
public class MethodCall implements Serializable {
    public final Method method;
    public final Object[] args;

    public MethodCall(Method method, Object[] args) {
        this.method = method;
        if (args != null)
            this.args = args;
        else
            this.args = new Object[0];
    }

    public Object invoke(Object that)
            throws InvocationTargetException, IllegalAccessException {
        return method.invoke(that, args);
    }

    public String toString() {
        String[] sargs = Stream.of(args).map(Object::toString).toArray(String[]::new);
        return method.getName() + "(" + String.join(",", sargs) + ")";
    }
}
