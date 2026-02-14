package YGOscanner.audio;

public class  SignalDecoderModule{

    private Complex[] fftResult;
    private double samplingRate = 44100.0; // Valore tipico per il jack
    private double lastMaxMagnitude = 0.0; // Variabile di classe per salvare la forza del segnale
    private long lastEventTime = 0;
    private AudioEvent lastEvent = null;
    private static final long DEBOUNCE_MS = 200;
    private long stableStartTime = 0;
    private static final long STABLE_TIME_MS = 300; // tempo richiesto per stabilità
    private boolean inStableCandidate = false;

    
    public void processAudioFrame(short[] pcm) {
        // FFT + picco frequenza
        /**
     * Metodo principale: riceve l'audio dal jack, lo stabilizza e calcola la FFT.
     */
            // 1. STABILIZZAZIONE LUNGHEZZA (Zero-Padding per potenza di 2)
        int originalN = pcm.length;
        int n = 1;
        while (n < originalN) n <<= 1; 

        // 2. PREPARAZIONE DATI (Normalizzazione + Hamming Window)
        Complex[] x = new Complex[n];
        for (int i = 0; i < n; i++) {
            if (i < originalN) {
                double normalized = pcm[i] / 32768.0;
                // Finestra di Hamming per pulire il segnale
                double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (originalN - 1));
                x[i] = new Complex(normalized * window, 0);
            } else {
                x[i] = new Complex(0, 0); // Padding con zeri
            }
        }

        // 3. CALCOLO FFT
        fftResult = fft(x);

        // Ora puoi usare fftResult per l'analisi (es. cercare il picco a 1kHz)
    }

    /**
     * Algoritmo FFT Cooley-Tukey (Ricorsivo)
     */
    private Complex[] fft(Complex[] x) {
        int n = x.length;
        if (n == 1) return new Complex[] { x[0] };

        Complex[] even = new Complex[n / 2];
        Complex[] odd = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
            odd[k] = x[2 * k + 1];
        }

        Complex[] q = fft(even);
        Complex[] r = fft(odd);

        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + n / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

    /**
     * INNER CLASS COMPLEX: Gestisce la parte reale e immaginaria.
     * Indispensabile per la FFT e coerente con la formula S = P + jQ.
     */
    public static class Complex {
        public final double re; // Parte reale (es. Potenza Attiva P)
        public final double im; // Parte immaginaria (es. Potenza Reattiva Q)

        public Complex(double re, double im) {
            this.re = re;
            this.im = im;
        }

        public double abs() { return Math.sqrt(re * re + im * im); }
        
        public Complex plus(Complex b) { 
            return new Complex(this.re + b.re, this.im + b.im); 
        }
        
        public Complex minus(Complex b) { 
            return new Complex(this.re - b.re, this.im - b.im); 
        }
        
        public Complex times(Complex b) {
            double real = this.re * b.re - this.im * b.im;
            double imag = this.re * b.im + this.im * b.re;
            return new Complex(real, imag);
        }
    }

    public double detectFrequency() {
    if (fftResult == null) return 0;

    int n = fftResult.length;
    double maxMagnitude = -1;
    int peakIndex = -1;

    for (int i = 0; i < n / 2; i++) {
        double magnitude = fftResult[i].abs();
        if (magnitude > maxMagnitude) {
            maxMagnitude = magnitude;
            peakIndex = i;
        }
    }
    
    // Salviamo la magnitudo per il controllo successivo
    this.lastMaxMagnitude = maxMagnitude;
    double  detectedFreq = peakIndex * (samplingRate / n);
    // Calcolo della frequenza reale
    return  detectedFreq ;

}
public AudioEvent mapFrequencyToEvent(double freq) {

    double tolerance = 50.0;

    //  Segnale debole → carta fuori
    if (lastMaxMagnitude < 0.1) {
        inStableCandidate = false;
        stableStartTime = 0;
        return AudioEvent.CARD_OUT;
    }

    //  Frequenza CARD_IN (1000 Hz)
    if (Math.abs(freq - 1000.0) <= tolerance) {
        inStableCandidate = false;
        stableStartTime = 0;
        return AudioEvent.CARD_IN;
    }

    //  Frequenza candidata a STABLE (2000 Hz)
    if (Math.abs(freq - 2000.0) <= tolerance) {

        long now = System.currentTimeMillis();

        if (!inStableCandidate) {
            // Inizia timer stabilità
            inStableCandidate = true;
            stableStartTime = now;
            return AudioEvent.CARD_IN; // ancora non stabile
        }

        // Se è stabile da abbastanza tempo
        if (now - stableStartTime >= STABLE_TIME_MS) {
            return AudioEvent.CARD_STABLE;
        }

        // Ancora in fase di stabilizzazione
        return AudioEvent.CARD_IN;
    }

    //  Segnale presente ma non valido
    inStableCandidate = false;
    stableStartTime = 0;
    return AudioEvent.ERROR;
}

    public boolean validateEvent(AudioEvent event) {

    long now = System.currentTimeMillis();

    // 1️ Se è lo stesso evento dell'ultimo → ignoralo
    if (event == lastEvent) {
        return false;
    }

    // 2️ Se è passato troppo poco tempo → ignoralo
    if (now - lastEventTime < DEBOUNCE_MS) {
        return false;
    }

    // 3️ Evento valido → aggiorna stato
    lastEvent = event;
    lastEventTime = now;

    return true;
}

public void resetState() {
    lastEvent = null;
    lastEventTime = 0;
    lastMaxMagnitude = 0.0;
    stableStartTime = 0;
    inStableCandidate = false;
}

}
// fix ore 14.22 errato 