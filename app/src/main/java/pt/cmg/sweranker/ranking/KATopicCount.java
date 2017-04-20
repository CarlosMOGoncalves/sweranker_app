package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;

/**
 * Created by Carlos on 11/04/2017.
 */

public class KATopicCount extends RealmObject {

    private int topicId;
    private int topicCount;

    public KATopicCount() {
        this.topicCount = 0;
        this.topicId = 0;
    }

    public KATopicCount(int topicId) {
        this.topicId = topicId;
        this.topicCount = 1;
    }

    public KATopicCount(int topicId, int topicCount) {
        this.topicId = topicId;
        this.topicCount = topicCount;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(int topicCount) {
        this.topicCount = topicCount;
    }

    public void incrementCounter(int increment) {
        topicCount += increment;
    }
}
