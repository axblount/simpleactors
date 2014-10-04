TODO
====
HOT
---
* Ensure actor methods are not run concurrently.

WARM
----
* research java.net.URLClassLoader for remote actors
* How to serialize java.lang.reflect.Method

Notes
=====
I want to implement this with one mailbox per actorsystem, but I don't see a way to guarentee that actors process one
message at a time. I plan to make the following changes.
* When the ActorSystem gets a new piece of mail, it will place it into the Actor's mailbox.
* If the Actor's thread is null or dead, it will start a new thread for that actor.
* Inside of its own thread an Actor will process messages from the mailbox until it's empty. After a certain timeout,
  the actor will shutdown and set it's thread reference to null so it can be garbage collected.
This will allow me to scrap the executor service. It seemed like a nice abstraction but at least I'll be less dependent
on the jdk.