package pt.cmg.sweranker.ranking;

import java.util.Collection;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * This class represents a single score for a complete Degree program or part of it (a Degree Class
 * or an Annual Combination of classes).
 * <p>
 * It is not overly complicated. It basically has a counter for the number of time a given Degree
 * Class Topic has appeared, a counter for the number of times any topic for a given KA has appeared
 * (meaning that if Topic 1 and Topic 3 are both related to KA 1 then the KA will have the number of times
 * the Topic 1 has appeared plus the number of time Topic 3 has appeared) and an average percentage
 * calculation for the KAs in the whole of the calculation.
 * <p>
 * More info on the class comments.
 * <p>
 * Created by Carlos on 07/03/2017.
 */

public class SweScore extends RealmObject {

    public static final String TYPE_CLASS_SCORE = "degreeClassScore";
    public static final String TYPE_ANNUAL_SCORE = "annualCombinationScore";
    public static final String TYPE_DEGREE_SCORE = "degreeCombinationScore";

    private static final int PERCENT = 100;


    /**
     * Very important -> this id is:
     * the Degree Class Id if this is a Degree Class Score,
     * the Annual Combination Id if this is an Annual Combination Score or
     * the Degree Combination Id if this is a Degree Combination Score.
     */
    @PrimaryKey
    private String id;

    /**
     * Either TYPE_CLASS_SCORE, TYPE_ANNUAL_SCORE or TYPE_DEGREE_SCORE.
     * It should be an Enum if Realm supported it.
     */
    private String scoreType;

    private int degreeId;

    // These are for storing the values in Realm
    private RealmList<KATopicCount> topicCounters;
    private RealmList<KACount> kaCounters;
    public RealmList<KAPercent> kaPercents;

    // Used to calculate the percents
    private int totalTopicCount;

    public SweScore() {
        topicCounters = new RealmList<>();
        kaCounters = new RealmList<>();
        kaPercents = new RealmList<>();
    }

    public SweScore(String scoreType) {
        this.scoreType = scoreType;
        degreeId = 0;
        topicCounters = new RealmList<>();
        kaCounters = new RealmList<>();
        kaPercents = new RealmList<>();
    }

    public SweScore(String id, int degreeId, String scoreType) {
        this.id = id;
        this.scoreType = scoreType;
        this.degreeId = degreeId;
        topicCounters = new RealmList<>();
        kaCounters = new RealmList<>();
        kaPercents = new RealmList<>();
    }

    public SweScore(SweScore anotherScore) {
        id = anotherScore.getId();
        scoreType = anotherScore.getScoreType();
        degreeId = anotherScore.getDegreeId();

        topicCounters = new RealmList<>();
        for (KATopicCount topicCounter : anotherScore.getTopicCounters()) {
            topicCounters.add(new KATopicCount(topicCounter.getTopicId(), topicCounter.getTopicCount()));
        }


        kaCounters = new RealmList<>();
        for (KACount kaCounter : anotherScore.getKaCounters()) {
            kaCounters.add(new KACount(kaCounter.getKaId(), kaCounter.getKaCount()));
        }

        kaPercents = new RealmList<>();
        for (KAPercent kaPercent : anotherScore.getKaPercents()) {
            kaPercents.add(new KAPercent(kaPercent.getKaId(), kaPercent.getKaPercent()));
        }
        totalTopicCount = anotherScore.getTotalTopicCount();
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public int getTotalTopicCount() {
        return totalTopicCount;
    }

    public void setTotalTopicCount(int topicCount) {
        totalTopicCount = topicCount;
    }

    public RealmList<KATopicCount> getTopicCounters() {
        return topicCounters;
    }

    public void setTopicCounters(Collection<KATopicCount> topicCounters) {
        for (KATopicCount topicCounter : topicCounters) {
            this.topicCounters.add(topicCounter);
            totalTopicCount += topicCounter.getTopicCount();
        }
    }


    public void addKATopicCounter(KATopicCount topicCounter) {
        topicCounters.add(topicCounter);
        totalTopicCount++;
    }

    public int getDegreeId() {
        return degreeId;
    }

    public void setDegreeId(int degreeId) {
        this.degreeId = degreeId;
    }

    public RealmList<KACount> getKaCounters() {
        return this.kaCounters;
    }


    public void setKaCounters(List<KACount> kaCounters) {
        this.kaCounters.addAll(kaCounters);
    }

    public void setKaCounters(Collection<KACount> kaCounters) {
        this.kaCounters.addAll(kaCounters);
    }

    public void addKaCounter(KACount kaCounter) {
        kaCounters.add(kaCounter);
    }

    public RealmList<KAPercent> getKaPercents() {
        return this.kaPercents;
    }

    public void addKAPercent(KAPercent kaPercent) {
        kaPercents.add(kaPercent);
    }

    public void setKaPercents(RealmList<KAPercent> kaPercents) {
        this.kaPercents = kaPercents;
    }


    /**
     * This resets the percent calculations. Only really useful after every counter has been put into place and
     * no further data will be added.
     */
    public void calculateScores2() {
        for (KACount kaCount : kaCounters) {
            kaPercents.add(new KAPercent(kaCount.getKaId(), (double) kaCount.getKaCount() / totalTopicCount * PERCENT));
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Total topics: " + totalTopicCount);
        s.append("; KA Counts: " + kaCounters.size());
        s.append("; Topics " + topicCounters.size());
        return s.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
