package YGOscanner;

import YGOscanner.audio.AudioInputModule;
import YGOscanner.audio.SignalDecoderModule;
import YGOscanner.audio.SignalDecoderModule;
import YGOscanner.audio.AudioEvent;
import YGOscanner.camera.CameraControlModule;
import YGOscanner.camera.ImagePreprocessingModule;
import YGOscanner.core.DecisionControlModule;
import YGOscanner.core.EventDispatcher;
import YGOscanner.core.SystemBootManager;
import YGOscanner.core.TriggerSyncModule;
import YGOscanner.recognition.CardResult;
import YGOscanner.recognition.RecognitionModule;
import YGOscanner.storage.StorageModule;
import YGOscanner.system.SystemHealthMonitor;
import YGOscanner.stub.AudioRecord;
public class Main {

    public static void main(String[] args) {

        // 1️⃣ System boot
        SystemBootManager bootManager = new SystemBootManager();
        bootManager.onBoot();
        bootManager.startCoreServices();

        // 2️⃣ Core modules
        AudioInputModule audioInput = new AudioInputModule();
        SignalDecoderModule decoder = new SignalDecoderModule();
        EventDispatcher dispatcher = new EventDispatcher();
        TriggerSyncModule trigger = new TriggerSyncModule();
        CameraControlModule camera = new CameraControlModule();
        ImagePreprocessingModule preprocess = new ImagePreprocessingModule();
        RecognitionModule recognition = new RecognitionModule();
        DecisionControlModule decision = new DecisionControlModule();
        StorageModule storage = new StorageModule();
        SystemHealthMonitor health = new SystemHealthMonitor();

        // 3️⃣ Subscriptions
        dispatcher.subscribe(trigger);  // trigger ascolta eventi audio

        // 4️⃣ Simulazione flusso
        System.out.println("=== Inizio simulazione flusso eventi ===");

        // Simuliamo un AudioEvent CARD_IN
        AudioEvent event = AudioEvent.CARD_IN;
        System.out.println("Evento audio ricevuto: " + event);

        // dispatcher invia evento
        dispatcher.dispatch(event);

        // trigger decide di scattare
        trigger.requestCapture();

        // camera cattura frame (dummy)
        Object frame = camera.captureFrame();
        System.out.println("Camera: frame catturato");

        // preprocess
        Object preprocessed = preprocess.prepareForRecognition(null);
        System.out.println("Preprocessing completato");

        // recognition
        CardResult result = recognition.runInference(null);
        System.out.println("Recognition completata: " + result);

        // decisione
        decision.onRecognitionResult(result);
        System.out.println("Decisione presa");

        // storage
        storage.saveImage(null, "image1.jpg");
        storage.saveResult(result, "result1.json");
        System.out.println("Storage completato");

        System.out.println("=== Fine simulazione ===");

        // 5️⃣ Health check
        if (!health.checkAudio() || !health.checkCamera() || !health.checkStorage()) {
            health.triggerRecovery();
        }

        System.out.println("System check completato");
    }
}
