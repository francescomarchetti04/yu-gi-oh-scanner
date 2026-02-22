package YGOscanner.recognition;

import java.io.Serializable;

/**
 * Questa classe è compatibile con Android e pronta per l'invio dati verso l'ESP32.
 * Implementa Serializable per permettere il passaggio tra Activity o l'invio via socket.
 */
public class CardResult implements Serializable {
    
    // Dati identificativi della carta
    public String cardName;
    public String cardCode;    // Es: "LOB-001"
    public String edition;     // Es: "1st Edition"
    public String rarity;      // Es: "Ultra Rare"
    
    // Metadati per l'app Android
    public float confidence;   // Affidabilità (0.0 - 1.0)
    public String imageHash;   // Impronta digitale per evitare duplicati
    public int quantity;       // Quantità posseduta
    
    // Timestamp per l'ESP32 (utile se vuoi mostrare l'ora dell'ultima scansione)
    public long timestamp;

    public CardResult() {
        this.timestamp = System.currentTimeMillis();
        this.quantity = 1;
    }

    /**
     * Metodo fondamentale: Trasforma l'oggetto in JSON.
     * L'ESP32 può leggere questo formato molto facilmente con la libreria ArduinoJson.
     */
    public String toJson() {
        return "{" +
                "\"name\":\"" + cardName + "\"," +
                "\"code\":\"" + cardCode + "\"," +
                "\"qty\":" + quantity + "," +
                "\"conf\":" + confidence +
                "}";
    }

    @Override
    public String toString() {
        return cardName + " [" + cardCode + "] x" + quantity;
    }
}