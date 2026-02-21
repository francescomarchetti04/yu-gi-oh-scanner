package YGOscanner.storage.database;


@Entity(tableName = "cards")
public class CardEntity {

    @PrimaryKey
    @NonNull
    public String cardId;

    public int quantity;
}