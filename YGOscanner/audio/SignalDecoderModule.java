package YGOscanner.audio;

public class SignalDecoderModule {

    private Complex[] fftResult;
    private double samplingRate = 44100.0;
    private double lastMaxMagnitude = 0.0;
    private long lastEventTime = 0;
    private AudioEvent lastEvent = null;
    
    // Parametri Stabilità e Debounce
    private static final long DEBOUNCE_MS = 200;
    private long stableStartTime = 0;
    private static final long STABLE_TIME_MS = 300;
    private boolean inStableCandidate = false;

    /**
     * Calcola il livello RMS partendo dai BYTE.
     * Usato per decidere se il segnale è abbastanza forte (lastMaxMagnitude).
     */
    public double getLevel(byte[] buffer, int bytesRead) {
        long sum = 0;
        int samples = bytesRead / 2;

        for (int i = 0; i < samples; i++) {
            // Conversione Bitwise Little Endian
            short sample = (short) ((buffer[2 * i] & 0xFF) | (buffer[2 * i + 1] << 8));
            sum += (long) sample * sample;
        }

        if (samples == 0) return 0;
        return Math.sqrt((double) sum / samples);
    }

    /**
     * VERSIONE AGGIORNATA: Riceve i byte, li converte internamente in campioni 
     * normalizzati e calcola la FFT.
     */
    public void processAudioFrame(byte[] byteBuffer, int bytesRead) {
        int originalN = bytesRead / 2; // Numero di campioni short
        
        // 1. Calcolo potenza di 2 per FFT (Zero-Padding)
        int n = 1;
        while (n < originalN) n <<= 1; 

        Complex[] x = new Complex[n];
        for (int i = 0; i < n; i++) {
            if (i < originalN) {
                // Ricostruiamo lo short dai byte per l'analisi
                short s = (short) ((byteBuffer[2 * i] & 0xFF) | (byteBuffer[2 * i + 1] << 8));
                double normalized = s / 32768.0;
                
                // Finestra di Hamming per ridurre il rumore spettrale
                double window = 0.54 - 0.46 * Math.cos(2 * Math.PI * i / (originalN - 1));
                x[i] = new Complex(normalized * window, 0);
            } else {
                x[i] = new Complex(0, 0);
            }
        }

        // 2. Calcolo FFT
        fftResult = fft(x);
    }

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

    public double detectFrequency() {
        if (fftResult == null) return 0;

        int n = fftResult.length;
        double maxMagnitude = -1;
        int peakIndex = -1;

        // Analizziamo solo la prima metà (Nyquist)
        for (int i = 0; i < n / 2; i++) {
            double magnitude = fftResult[i].abs();
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude;
                peakIndex = i;
            }
        }
        
        this.lastMaxMagnitude = maxMagnitude;
        return peakIndex * (samplingRate / n);
    }

    public AudioEvent mapFrequencyToEvent(double freq) {
        double tolerance = 50.0;

        // Soglia di ampiezza basata sulla FFT (regolabile)
        if (lastMaxMagnitude < 0.01) { 
            inStableCandidate = false;
            stableStartTime = 0;
            return AudioEvent.CARD_OUT;
        }

        if (Math.abs(freq - 1000.0) <= tolerance) {
            inStableCandidate = false;
            return AudioEvent.CARD_IN;
        }

        if (Math.abs(freq - 2000.0) <= tolerance) {
            long now = System.currentTimeMillis();
            if (!inStableCandidate) {
                inStableCandidate = true;
                stableStartTime = now;
                return AudioEvent.CARD_IN; 
            }
            if (now - stableStartTime >= STABLE_TIME_MS) {
                return AudioEvent.CARD_STABLE;
            }
            return AudioEvent.CARD_IN;
        }

        inStableCandidate = false;
        return AudioEvent.ERROR;
    }

    public boolean validateEvent(AudioEvent event) {
        long now = System.currentTimeMillis();
        if (event == lastEvent) return false;
        if (now - lastEventTime < DEBOUNCE_MS) return false;

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

    // --- INNER CLASS COMPLEX ---
    public static class Complex {
        public final double re; 
        public final double im; 
        public Complex(double re, double im) { this.re = re; this.im = im; }
        public double abs() { return Math.sqrt(re * re + im * im); }
        public Complex plus(Complex b) { return new Complex(this.re + b.re, this.im + b.im); }
        public Complex minus(Complex b) { return new Complex(this.re - b.re, this.im - b.im); }
        public Complex times(Complex b) {
            return new Complex(this.re * b.re - this.im * b.im, this.re * b.im + this.im * b.re);
        }
    }
}