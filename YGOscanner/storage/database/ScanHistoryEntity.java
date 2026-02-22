package YGOscanner.storage.database;

@Entity(tableName = "scan_history")
public class ScanHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String cardId;

    public long timestamp;

    public boolean pendingSync; // servirà in Fase 2
}
