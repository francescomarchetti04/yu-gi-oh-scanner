package YGOscanner.storage.database;

@Database(entities = {CardEntity.class, ScanHistoryEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CardDao cardDao();
    public abstract ScanHistoryDao scanHistoryDao();
}
