package YGOscanner.recognition;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class RecognitionModule {

    private Interpreter tflite;
    private List<String> labels;
    private static final int INPUT_SIZE = 224; // Dimensione tipica per MobileNet/EfficientNet
    private static final float MIN_CONFIDENCE_THRESHOLD = 0.75f;

    public RecognitionModule(Context context, String modelPath) {
        try {
            tflite = new Interpreter(loadModelFile(context, modelPath));
            // Qui dovresti caricare anche le labels da labels.txt
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Esegue l'inferenza REALE usando il modello TFLite.
     */
    public CardResult runInference(Bitmap input) {
        if (input == null || tflite == null) return null;

        // 1. Pre-elaborazione: Resize e normalizzazione per il modello
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(input, INPUT_SIZE, INPUT_SIZE, true);
        float[][][][] inputBuffer = bitmapToByteBuffer(resizedBitmap);

        // 2. Buffer di output (es. un array di float con le probabilità per ogni carta)
        float[][] output = new float[1][labels.size()];

        // 3. ESECUZIONE AI
        tflite.run(inputBuffer, output);

        // 4. Trova il risultato migliore
        return processOutput(output, input);
    }

    /**
     * ALGORITMO dHash (Eseguito in parallelo all'AI)
     */
    public String generateDHash(Bitmap input) {
        Bitmap small = Bitmap.createScaledBitmap(input, 9, 8, true);
        long hash = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (getLuminance(small.getPixel(x, y)) > getLuminance(small.getPixel(x + 1, y))) {
                    hash |= (1L << (y * 8 + x));
                }
            }
        }
        return Long.toHexString(hash);
    }

    private float[][][][] bitmapToByteBuffer(Bitmap bitmap) {
        float[][][][] buffer = new float[1][INPUT_SIZE][INPUT_SIZE][3];
        for (int x = 0; x < INPUT_SIZE; x++) {
            for (int y = 0; y < INPUT_SIZE; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalizzazione tra 0 e 1 o tra -1 e 1 a seconda del modello
                buffer[0][x][y][0] = Color.red(pixel) / 255.0f;
                buffer[0][x][y][1] = Color.green(pixel) / 255.0f;
                buffer[0][x][y][2] = Color.blue(pixel) / 255.0f;
            }
        }
        return buffer;
    }

    private CardResult processOutput(float[][] output, Bitmap original) {
        int maxIndex = 0;
        float maxProb = 0.0f;
        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIndex = i;
            }
        }

        CardResult result = new CardResult();
        result.cardName = labels.get(maxIndex);
        result.confidence = maxProb;
        result.imageHash = generateDHash(original); // Aggiungiamo l'hash
        result.quantity = 1;
        return result;
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws Exception {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    private int getLuminance(int pixel) {
        return (int) (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel));
    }

    public void resetModel() {
        if (tflite != null) tflite.resetVariableTensors();
    }
}