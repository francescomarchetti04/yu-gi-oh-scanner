package YGOscanner.recognition;
import android.graphics.Bitmap;

public class RecognitionModule {

    public CardResult runInference(Bitmap input) {
        return null;
    }

    public float getConfidence(CardResult result) {
        return 0.0f;
    }

    public boolean isValidResult(CardResult result) {
        return true;
    }

    public void resetModel() {
        // reset AI model
    }
}

