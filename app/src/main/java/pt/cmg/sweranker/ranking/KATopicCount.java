package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;

/**
 * Created by Carlos on 11/04/2017.
 */

public class KATopicCount extends RealmObject {

    /**
     * Note that both these two numbers used to be ints.
     * However since these values are the main bulk of data in calculations this was being
     * really heavy on the database. So I used lower precision to solve it.
     */

    // Value will NEVER be higher than 127, which is the max positive range for a byte
    private byte topicId;

    // Sadly this could not also be a byte, but a short will do just fine. In fact is way more than needed.
    private short topicCount;

    public KATopicCount() {
        this.topicCount = 0;
        this.topicId = 0;
    }

    public KATopicCount(byte topicId) {
        this.topicId = topicId;
        this.topicCount = 1;
    }

    public KATopicCount(byte topicId, short topicCount) {
        this.topicId = topicId;
        this.topicCount = topicCount;
    }

    public byte getTopicId() {
        return topicId;
    }

    public void setTopicId(byte topicId) {
        this.topicId = topicId;
    }

    public short getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(short topicCount) {
        this.topicCount = topicCount;
    }

    public void incrementCounter(short increment) {
        topicCount += increment;
    }
}
