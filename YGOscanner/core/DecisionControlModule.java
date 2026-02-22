package YGOscanner.core;

import YGOscanner.recognition.CardResult;

package core;

import java.util.concurrent.atomic.AtomicBoolean;

import recognition.CardRecognitionResult;
import repository.CollectionRepository;

/**
 * DecisionControlModule
 *
 * Responsabilità:
 * - Coordinare il flusso dopo il riconoscimento
 * - Applicare regole di business
 * - Delegare al Repository
 * - Notificare eventuali listener (UI)
 *
 * NON deve:
 * - Accedere direttamente al database
 * - Fare networking
 * - Usare Android Context
 */
public class DecisionControlModule {

    // ===============================
    // Configurazioni
    // ===============================

    private static final long MIN_SCAN_INTERVAL_MS = 1000; // anti doppia scansione
    private static final boolean ENABLE_DEBOUNCE = true;

    // ===============================
    // Dipendenze
    // ===============================

    private final CollectionRepository collectionRepository;

    // ===============================
    // Stato interno
    // ===============================

    private long lastScanTimestamp = 0;
    private String lastCardId = null;

    private final AtomicBoolean processing = new AtomicBoolean(false);

    private DecisionListener decisionListener;

    // ===============================
    // Costruttore
    // ===============================

    public DecisionControlModule(CollectionRepository repository) {
        this.collectionRepository = repository;
    }

    // ===============================
    // Listener per UI
    // ===============================

    public interface DecisionListener {
        void onCardAccepted(String cardId);
        void onCardRejected(String reason);
    }

    public void setDecisionListener(DecisionListener listener) {
        this.decisionListener = listener;
    }

    // ===============================
    // Metodo principale
    // ===============================

    public void onCardRecognized(CardRecognitionResult result) {

        if (processing.get()) {
            log("Sistema occupato, ignorato evento.");
            return;
        }

        if (result == null) {
            notifyRejected("Risultato nullo");
            return;
        }

        if (!result.isValid()) {
            notifyRejected("Riconoscimento non valido");
            return;
        }

        String cardId = result.getCardId();

        if (cardId == null || cardId.trim().isEmpty()) {
            notifyRejected("Card ID vuoto");
            return;
        }

        if (ENABLE_DEBOUNCE && isDuplicate(cardId)) {
            notifyRejected("Scansione duplicata troppo ravvicinata");
            return;
        }

        processCard(cardId);
    }

    // ===============================
    // Logica interna
    // ===============================

    private void processCard(String cardId) {

        processing.set(true);

        try {

            log("Carta valida rilevata: " + cardId);

            // Aggiornamento collezione
            collectionRepository.addCard(cardId);

            // Aggiorna stato interno
            lastScanTimestamp = System.currentTimeMillis();
            lastCardId = cardId;

            notifyAccepted(cardId);

        } catch (Exception e) {

            log("Errore durante elaborazione: " + e.getMessage());
            notifyRejected("Errore interno");

        } finally {
            processing.set(false);
        }
    }

    // ===============================
    // Anti duplicazione intelligente
    // ===============================

    private boolean isDuplicate(String cardId) {

        long now = System.currentTimeMillis();

        if (lastCardId == null) {
            return false;
        }

        boolean sameCard = lastCardId.equals(cardId);
        boolean tooSoon = (now - lastScanTimestamp) < MIN_SCAN_INTERVAL_MS;

        return sameCard && tooSoon;
    }

    // ===============================
    // Notifiche
    // ===============================

    private void notifyAccepted(String cardId) {
        if (decisionListener != null) {
            decisionListener.onCardAccepted(cardId);
        }
    }

    private void notifyRejected(String reason) {
        if (decisionListener != null) {
            decisionListener.onCardRejected(reason);
        }
    }

    // ===============================
    // Logging interno
    // ===============================

    private void log(String message) {
        System.out.println("[DecisionControlModule] " + message);
    }

    // ===============================
    // Reset stato (utile per debug o restart)
    // ===============================

    public void reset() {
        lastCardId = null;
        lastScanTimestamp = 0;
        processing.set(false);
    }
}