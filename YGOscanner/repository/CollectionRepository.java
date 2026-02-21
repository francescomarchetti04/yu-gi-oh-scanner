package YGOscanner.repository;


public class CollectionRepository {

    private CardDao cardDao;
    private ScanHistoryDao scanHistoryDao;

    public CollectionRepository(Context context) {
        AppDatabase db = Room.databaseBuilder(
                context,
                AppDatabase.class,
                "ygo_database"
        ).build();

        cardDao = db.cardDao();
        scanHistoryDao = db.scanHistoryDao();
    }

    public void addCard(String cardId) {

        Executors.newSingleThreadExecutor().execute(() -> {

            CardEntity card = cardDao.getCardById(cardId);

            if (card == null) {
                card = new CardEntity();
                card.cardId = cardId;
                card.quantity = 1;
            } else {
                card.quantity++;
            }

            cardDao.insert(card);

            ScanHistoryEntity scan = new ScanHistoryEntity();
            scan.cardId = cardId;
            scan.timestamp = System.currentTimeMillis();
            scan.pendingSync = true;

            scanHistoryDao.insert(scan);
        });
    }
}