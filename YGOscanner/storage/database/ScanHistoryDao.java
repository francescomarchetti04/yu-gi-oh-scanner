package YGOscanner.storage.database;

@Dao
public interface ScanHistoryDao {

    @Insert
    void insert(ScanHistoryEntity scan);

    @Query("SELECT * FROM scan_history")
    List<ScanHistoryEntity> getAllScans();
}