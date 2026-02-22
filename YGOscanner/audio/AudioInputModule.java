package YGOscanner.audio;

import javax.sound.sampled.*; 
public class AudioInputModule {

// Parametri del formato
AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED; // <--- Ecco la codifica che cercavi
float sampleRate = 44100.0f;     // Frequenza di campionamento
int sampleSizeInBits = 16;       // 16 bit per campione
int channels = 1;                // Mono (consigliato per ESP32 per risparmiare banda)
int frameSize = 2;               // (sampleSizeInBits / 8) * channels
float frameRate = 44100.0f;
boolean bigEndian = false;       // L'ESP32 (architettura Little Endian) preferisce 'false'

AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
private TargetDataLine line;

public void startListening() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new RuntimeException("Formato audio non supportato!");
        }
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start(); // Inizia a riempire il buffer interno della scheda audio
        System.out.println("Microfono aperto e in ascolto...");
        // Apri microfono
    }

    public void stopListening() {
        // Ferma acquisizione
        if (line != null) {
            line.stop();
            line.close();
            System.out.println("Microfono chiuso.");
        }
    }

 // 2. LEGGERE IL BUFFER (Restituisce il numero di byte letti)
    // Passiamo il buffer dall'esterno per evitare di creare nuovi oggetti ogni volta
    public int readBuffer(byte[] buffer) {
        if (line != null && line.isOpen()) {
            // Legge i dati direttamente dalla scheda audio nel buffer
            return line.read(buffer, 0, buffer.length);
        }
        return -1; // Ritorna -1 se la linea è chiusa
    }

    /**
     * IL METODO RICHIESTO: Controlla se il microfono è operativo.
     */
    public boolean isInputAlive() {
        // Controlla se la linea esiste, se è aperta e se sta effettivamente catturando
        return line != null && line.isOpen() && line.isActive();
    }

    /**
     * Ritorna il livello di riempimento del buffer interno (utile per debug)
     */
    public int getAvailableBytes() {
        return (line != null) ? line.available() : 0;
    }
}

// stest commit fft git 