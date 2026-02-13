package YGOscanner.audio;

public class SignalDecoderModule {

    public void processAudioFrame(short[] pcm) {
        // FFT + picco frequenza
    }

    public AudioEvent detectFrequency() {
        // Riconosci frequenza e mappa evento
        return null;
    }

    public AudioEvent mapFrequencyToEvent(double freq) {
        // Converti freq in AudioEvent
        return null;
    }

    public boolean validateEvent(AudioEvent event) {
        // Debounce temporale
        return true;
    }

    public void resetState() {
        // Resetta stato interno
    }
}

