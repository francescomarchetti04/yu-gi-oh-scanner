package YGOscanner.core;
package YGOscanner;

public class  DecisionControlModule{

    public void onRecognitionResult(CardResult result) {
        // Approva, retry o segnala errore
    }

    public void handleError() {
        // gestione errori
    }

    public void requestRetry() {
        // chiedi nuovo scatto
    }

    public void approveCard() {
        // segna carta come OK
    }
}
