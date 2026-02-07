package YGOscanner.storage;
import YGOscanner.recognition.CardResult;
import android.graphics.Bitmap;

public class StorageModule {

    public void saveImage(Bitmap image, String filename) {
        // salva JPG
    }

    public void saveResult(CardResult result, String filename) {
        // salva JSON / CSV
    }

    public void saveLog(String log) {
        // salva log
    }

    public long checkFreeSpace() {
        return 0;
    }

    public void rotateStorage() {
        // gestione spazio SD
    }
}
