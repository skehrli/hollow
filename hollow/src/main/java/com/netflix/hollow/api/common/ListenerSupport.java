package com.netflix.hollow.api.common;

import org.checkerframework.dataflow.qual.Impure;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListenerSupport {

    protected final CopyOnWriteArrayList<EventListener> eventListeners;

    @Impure
    public ListenerSupport() {
        eventListeners = new CopyOnWriteArrayList<>();
    }

    @Impure
    public ListenerSupport(List<? extends EventListener> listeners) {
        eventListeners = new CopyOnWriteArrayList<>(listeners);
    }

    @Impure
    public ListenerSupport(ListenerSupport that) {
        eventListeners = new CopyOnWriteArrayList<>(that.eventListeners);
    }

    @Impure
    public void addListener(EventListener listener) {
        eventListeners.addIfAbsent(listener);
    }

    @Impure
    public void removeListener(EventListener listener) {
        eventListeners.remove(listener);
    }

}
