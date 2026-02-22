package YGOscanner.storage.database;

@Dao
public interface CardDao {

    @Query("SELECT * FROM cards WHERE cardId = :id")
    CardEntity getCardById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CardEntity card);
}
