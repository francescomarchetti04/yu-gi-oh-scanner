package YGOscanner.stub;

public class AudioRecord {
    public AudioRecord(int source, int sampleRate, int channelConfig, int audioFormat, int bufferSize) {}

    public void startRecording() {}
    public void stop() {}
    public int read(short[] buffer, int offset, int size) { return 0; }
}
