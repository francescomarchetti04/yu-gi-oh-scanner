package YGOscanner.camera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Matrix;

public class ImagePreprocessingModule {

    // 1. RITAGLIO (CROP)
    // Utile per isolare solo il nome della carta o l'area del testo
    public Bitmap cropImage(Bitmap input, int x, int y, int width, int height) {
        if (input == null) return null;
        // Crea una sottoporzione della bitmap originale
        return Bitmap.createBitmap(input, x, y, width, height);
    }

    // 2. RIDIMENSIONAMENTO (RESIZE)
    // Molti modelli AI richiedono dimensioni fisse (es. 224x224 o 300x300)
    public Bitmap resizeImage(Bitmap input, int newWidth, int newHeight) {
        if (input == null) return null;
        return Bitmap.createScaledBitmap(input, newWidth, newHeight, true);
    }

    // 3. NORMALIZZAZIONE LUCE E CONTRASTO
    // Fondamentale per leggere le scritte piccole delle carte
    public Bitmap normalizeLighting(Bitmap input) {
        if (input == null) return null;

        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), input.getConfig());
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();

        // Aumentiamo il contrasto e leggermente la luminosità
        ColorMatrix cm = new ColorMatrix(new float[] {
                1.2f, 0, 0, 0, 10, // Rosso
                0, 1.2f, 0, 0, 10, // Verde
                0, 0, 1.2f, 0, 10, // Blu
                0, 0, 0, 1, 0      // Alpha
        });

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(input, 0, 0, paint);
        return output;
    }

    // 4. PREPARAZIONE FINALE (SCALA DI GRIGI + CONTRASTO)
    // L'OCR legge molto meglio se l'immagine è in bianco e nero ad alto contrasto
    public Bitmap prepareForRecognition(Bitmap input) {
        if (input == null) return null;

        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();

        // Matrice per scala di grigi
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0); // Rimuove il colore
        
        // Aggiungiamo un filtro per scurire i neri e schiarire i bianchi
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(input, 0, 0, paint);

        return output;
    }

    /**
     * Ruota l'immagine se il telefono è tenuto in orizzontale
     */
    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}