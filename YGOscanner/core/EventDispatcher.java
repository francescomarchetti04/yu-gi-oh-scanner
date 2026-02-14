package YGOscanner.core;

import YGOscanner.audio.AudioEvent;

import java.util.List;
import java.util.ArrayList;

public class EventDispatcher {

    private List<EventListener> listeners = new ArrayList<>();

    public void dispatch(AudioEvent event) {
        for (EventListener l : listeners) {
            l.onEvent(event);
        }
    }

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }

    public void flushQueue() {
        // Pulisce eventuale coda eventi
    }
}

