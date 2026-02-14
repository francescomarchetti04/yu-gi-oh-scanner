package YGOscanner.core;

import YGOscanner.audio.AudioEvent;


public interface EventListener {
    void onEvent(AudioEvent event);
}
