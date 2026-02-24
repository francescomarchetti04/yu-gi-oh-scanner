package YGOscanner.network;

package network;

public class ScanRequest {

    private String cardId;
    private long timestamp;
    private String deviceId;

    public ScanRequest(String cardId, long timestamp, String deviceId) {
        this.cardId = cardId;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
    }

    public String getCardId() {
        return cardId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }
}