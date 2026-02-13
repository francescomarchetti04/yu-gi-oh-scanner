package YGOscanner.audio;

public class AudioInputModule {

    public void startListening() {
        // Apri microfono
    }

    public void stopListening() {
        // Ferma acquisizione
    }

    public short[] readBuffer() {
        // Ritorna buffer PCM
        return new short[0];
    }

    public boolean isInputAlive() {
        return true;
    }
}

// stest commit fft