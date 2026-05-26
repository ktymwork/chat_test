package app.message;

public class WatchdogMessage extends Message {
    private final int participantCount;

    public WatchdogMessage(int participantCount) {
        this.participantCount = participantCount;
    }

    public int getParticipantCount() {
        return participantCount;
    }
}
