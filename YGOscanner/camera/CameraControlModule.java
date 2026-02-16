package YGOscanner.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import androidx.annotation.NonNull;

import java.util.Collections;

public class CameraControlModule {

    private final CameraManager cameraManager;
    private final String cameraId;
    private final CameraCharacteristics characteristics;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;

    // Background Thread per non bloccare la UI
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    public CameraControlModule(Context context) throws CameraAccessException {
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        
        String[] ids = cameraManager.getCameraIdList();
        if (ids.length == 0) {
            throw new CameraAccessException(CameraAccessException.CAMERA_DISCONNECTED, "Nessuna camera!");
        }

        // Selezioniamo la camera posteriore (LENS_FACING_BACK)
        String selectedId = ids[0];
        for (String id : ids) {
            Integer facing = cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                selectedId = id;
                break;
            }
        }
        
        this.cameraId = selectedId;
        this.characteristics = cameraManager.getCameraCharacteristics(cameraId);
        startBackgroundThread();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    @SuppressLint("MissingPermission")
    public void startCamera() throws CameraAccessException {
        cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                cameraDevice = camera;
                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                camera.close();
                cameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                camera.close();
                cameraDevice = null;
            }
        }, backgroundHandler);
    }

    private void createCameraPreviewSession() {
        try {
            // Per lo scanner serve una Surface dove mandare le immagini
            // Qui usiamo una SurfaceTexture dummy se non hai una View, 
            // ma solitamente si usa una TextureView o ImageReader
            SurfaceTexture texture = new SurfaceTexture(1); 
            texture.setDefaultBufferSize(1920, 1080);
            Surface surface = new Surface(texture);

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        captureSession = session;
                        setFixedParameters(); // Applichiamo i parametri qui
                        try {
                            captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, backgroundHandler);
                        } catch (CameraAccessException e) { e.printStackTrace(); }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
                }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setFixedParameters() {
        if (previewRequestBuilder == null) return;

        // 1. FOCUS FISSO (Fondamentale per lo scanner Yu-Gi-Oh)
        // Usiamo CONTINUOUS_PICTURE per far sì che la camera cerchi sempre il fuoco sulla carta
        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

        // 2. ESPOSIZIONE (Evitiamo riflessi sulle bustine lucide)
        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        
        // 3. STABILIZZAZIONE VIDEO (Se supportata)
        previewRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
    }

    public void stopCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        stopBackgroundThread();
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
}