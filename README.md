Simple Actors
=============
[![license](http://img.shields.io/badge/license-AGPL3-red.svg?style=flat)](https://github.com/axblount/simpleactors/blob/master/LICENSE)

A simple actor framework for java.

```java
import org.axblount.simpleactors;

public class Main {
    public static class MyActor extends Actor {
        @Override public void handle(Object msg) {
            if (msg instanceof String)
                System.out.println("Someone said: " + msg);
            else if (msg instanceof Integer)
                System.out.println("I like the number " + msg);
            else
                throw new RuntimeException("I don't get it...");
        }
        @Override public void exceptionHandler(Throwable e) {
            System.out.println("Uh oh: " + e.getMessage());
        }
    }

    public static void Main(String[] args) {
        ActorSystem sys = new ActorSystem("test");

        ActorRef ref = sys.spawn(MyActor.class);
        ref.send("Hi!");
        ref.send(17);
        ref.send(new Object());
    }
}
```

Motivation
----------

I was curious about the implementation of an actor framework. Much of the API has been inspired by Akka, but any similarity to its internals in coincidental; I haven't peeked yet.

I can't hope to create something as robust or full featured as Akka. Instead I'll aim for something small and **easy to understand**.
