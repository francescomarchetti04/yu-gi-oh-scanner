package YGOscanner.core;

public class TriggerSyncModule implements EventListener {

    @Override
    public void onEvent(AudioEvent event) {
        // Gestisci CARD_IN, CARD_STABLE, CARD_OUT
    }

    public void requestCapture() {
        // Segnala CameraModule di scattare
    }

    public void reset() {
        // Reset stato sincronizzazione
    }
}

