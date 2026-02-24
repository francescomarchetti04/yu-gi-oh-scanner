package YGOscanner.network;

import android.util.Log;

import java.util.List;

import repository.CollectionRepository;
import storage.database.ScanHistoryEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncManager {

    private final CollectionRepository repository;
    private final ApiService apiService;

    public SyncManager(CollectionRepository repository) {
        this.repository = repository;
        this.apiService = ApiClient.getApiService();
    }

    public void syncPendingScans() {

        List<ScanHistoryEntity> pendingScans = repository.getPendingScans();

        for (ScanHistoryEntity scan : pendingScans) {

            ScanRequest request = new ScanRequest(
                    scan.cardId,
                    scan.timestamp,
                    "ANDROID_DEVICE"
            );

            Call<Void> call = apiService.sendScan(
                    "Bearer " + NetworkConfig.API_KEY,
                    request
            );

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    if (response.isSuccessful()) {
                        repository.markScanAsSynced(scan.id);
                        Log.d("SyncManager", "Scan sincronizzata: " + scan.cardId);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("SyncManager", "Errore sync: " + t.getMessage());
                }
            });
        }
    }
}