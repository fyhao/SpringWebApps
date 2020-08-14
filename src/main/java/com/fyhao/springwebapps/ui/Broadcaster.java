package com.fyhao.springwebapps.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.vaadin.flow.shared.Registration;

public class Broadcaster {
    static Executor executor = Executors.newSingleThreadExecutor();

    static Map<String, LinkedList<Consumer<String>>> listeners = new HashMap<String, LinkedList<Consumer<String>>>();
    
    public static synchronized Registration register(
    		String eventName,
            Consumer<String> listener) {
    	listeners.get(eventName).add(listener);
        return () -> {
            synchronized (Broadcaster.class) {
                listeners.get(eventName).remove(listener);
            }
        };
    }

    public static synchronized void broadcast(String eventName, String message) {
        for (Consumer<String> listener : listeners.get(eventName)) {
            executor.execute(() -> listener.accept(message));
        }
    }
}