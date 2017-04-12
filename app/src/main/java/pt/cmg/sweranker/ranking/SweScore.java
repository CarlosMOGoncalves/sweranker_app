package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 07/03/2017.
 */

public class SweScore extends RealmObject {

    public static final String TYPE_CLASS_SCORE = "degreeClassScore";
    public static final String TYPE_ANNUAL_SCORE = "annualCombinationScore";
    public static final String TYPE_DEGREE_SCORE = "degreeCombinationScore";

    private static final int INCREMENT = 1;
    private static final int PERCENT = 100;


    @PrimaryKey
    private String id;

    private String scoreType;

    // Keys -> KA Topic ids, Values -> number of times this ka topic was matched in this Degree Class
    @Ignore
    private Map<Integer, Integer> _kaTopicCounts;

    // Keys -> KA ids , Values -> number of times this same KA has been matched in this Degree Class;
    @Ignore
    private Map<Integer, Integer> _kaCounts;

    // Keys -> KA ids , Values -> the percent that KA appears in matches on the overall Degree Class
    @Ignore
    private Map<Integer, Double> _kaPercents;


    // These are for storing the values in Realm
    private RealmList<KATopicCount> topicCounters;
    private RealmList<KACount> kaCounters;
    private RealmList<KAPercent> kaPercents;

    // Used to calculate the percents
    private int totalTopicCount;

    public SweScore() {
        _kaTopicCounts = new HashMap<>();
        _kaPercents = new HashMap<>();
        _kaCounts = new HashMap<>();
        topicCounters = new RealmList<>();
        kaCounters = new RealmList<>();
        kaPercents = new RealmList<>();
    }

    public SweScore(String scoreType) {
        this.scoreType = scoreType;
        _kaTopicCounts = new HashMap<>();
        _kaPercents = new HashMap<>();
        _kaCounts = new HashMap<>();
        topicCounters = new RealmList<>();
        kaCounters = new RealmList<>();
        kaPercents = new RealmList<>();
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

    public void setTopicCounters(RealmList<KATopicCount> topicCounters) {
        this.topicCounters = topicCounters;
    }

    public RealmList<KACount> getKaCounters() {
        return this.kaCounters;
    }

    public void setKaCounters(RealmList<KACount> kaCounters) {
        this.kaCounters = kaCounters;
    }

    public RealmList<KAPercent> getKaPercents() {
        return this.kaPercents;
    }

    public void setKaPercents(RealmList<KAPercent> kaPercents) {
        this.kaPercents = kaPercents;
    }

    public Map<Integer, Integer> getKaTopicCountersByTopicId() {
        return _kaTopicCounts;
    }

    public Map<Integer, Integer> getKaCountersByKaId() {
        return _kaCounts;
    }


    public Map<Integer, Double> getKaPercentsByKaId() {
        return _kaPercents;
    }

    public void setKaPercent(Map<Integer, Double> kaPercent) {
        _kaPercents = kaPercent;
    }

    /**
     * Adds a whole collection of KA and KA Topic counts to this object.
     * This is used mostly when one wants to aggregate multiple Calculations together.
     * <p>
     * <p>
     * NOTE: this will leave the percents in an inconsistent state for performance reasons
     * this is used inside a cycle, adding multiple calculations together).
     * You have been warned.
     * </p>
     *
     * @param newTopicCounts
     * @param newKaCounts
     */
    public void addCounters(Map<Integer, Integer> newTopicCounts, Map<Integer, Integer> newKaCounts) {

        for (Map.Entry<Integer, Integer> entry : newTopicCounts.entrySet()) {

            Integer topicId = entry.getKey();
            Integer topicCount = entry.getValue();

            if (_kaTopicCounts.containsKey(topicId)) {
                // If it contains already a value for this topic, then sum it with the new
                _kaTopicCounts.put(topicId, _kaTopicCounts.get(topicId) + topicCount);
            } else {
                // Otherwise put it there
                _kaTopicCounts.put(topicId, topicCount);
            }

            // In any case add to the total topic count
            totalTopicCount += topicCount;
        }

        for (Map.Entry<Integer, Integer> entry : newKaCounts.entrySet()) {

            Integer kaId = entry.getKey();
            Integer kaCount = entry.getValue();

            if (_kaCounts.containsKey(kaId)) {
                _kaCounts.put(kaId, _kaCounts.get(kaId) + kaCount);
            } else {
                _kaCounts.put(kaId, kaCount);
            }
        }

        // For performance reasons this will be left out of here and must be called separately so that it
        // does not hurt even more the performance of this
        // resetPercentCalculations();

    }


    /**
     * Adds a topic, or increments its count if it already exists.
     * Increments the overall topic count and recalculates all the percents.
     * <p>
     * The way this calculates is massively important. If you notice, every time a new KA Topic
     * is added, its parent KA counter is also incremented. This is crucial for the calculation
     * of the percents as it relies on the division of "the total number of times a given KA has
     * appeared DIVIDED BY the total number of topic of all the available KAs in this object".
     * </p>
     *
     * @param knowledgeAreaTopicId The KA Topic id that was added.
     * @param knowledgeAreaId      The matching KA id for this topic
     */
    public void addTopic(int knowledgeAreaTopicId, int knowledgeAreaId) {

        // first update the topic counter
        if (_kaTopicCounts.containsKey(knowledgeAreaTopicId)) {
            _kaTopicCounts.put(knowledgeAreaTopicId, _kaTopicCounts.get(knowledgeAreaTopicId) + INCREMENT);
        } else {
            _kaTopicCounts.put(knowledgeAreaTopicId, 1);
        }


        // then update the KA counter
        if (_kaCounts.containsKey(knowledgeAreaId)) {
            _kaCounts.put(knowledgeAreaId, _kaCounts.get(knowledgeAreaId) + INCREMENT);
        } else {
            _kaCounts.put(knowledgeAreaId, 1);
        }

        // then global topic counter
        totalTopicCount++;

        // I can't calculate them here, it was being used in a cycle, by God. I will call resetPercentCalculations instead
        //calculateNewPercents(knowledgeAreaId);
    }

    /**
     * Recalculates all the percents again.
     * This is obviously needed every time a new degree topic is matched with any ka topic.
     *
     * @param knowledgeAreaId
     */
    private void calculateNewPercents(int knowledgeAreaId) {

        if (!_kaPercents.containsKey(knowledgeAreaId)) {
            _kaPercents.put(knowledgeAreaId, null);
        }

        for (Map.Entry<Integer, Double> entry : _kaPercents.entrySet()) {
            entry.setValue((double) _kaCounts.get(entry.getKey()) / totalTopicCount * PERCENT);
        }
    }

    /**
     * This resets the percent calculations. Only really useful after every counter has been put into place and
     * no further data will be added.
     */
    public void resetPercentCalculations() {

        _kaPercents.clear();
        for (Map.Entry<Integer, Integer> entry : _kaCounts.entrySet()) {
            _kaPercents.put(entry.getKey(), (double) _kaCounts.get(entry.getKey()) / totalTopicCount * PERCENT);
        }

        // Now update the realm object view...
        kaPercents.clear();
        for (Map.Entry<Integer, Double> kaPercent : _kaPercents.entrySet()) {
            kaPercents.add(new KAPercent(kaPercent.getKey(), kaPercent.getValue()));
        }

        // This is pathetic...
        topicCounters.clear();
        for (Map.Entry<Integer, Integer> kaTopicCounter : _kaTopicCounts.entrySet()) {
            topicCounters.add(new KATopicCount(kaTopicCounter.getKey(), kaTopicCounter.getValue()));
        }

        // Also this...
        kaCounters.clear();
        for (Map.Entry<Integer, Integer> kaCounter : _kaCounts.entrySet()) {
            kaCounters.add(new KACount(kaCounter.getKey(), kaCounter.getValue()));
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Total topics: " + totalTopicCount);
        s.append("\n");
        s.append("Topic count: ");
        s.append("\n");
        for (Map.Entry<Integer, Integer> topicCount : _kaTopicCounts.entrySet()) {
            s.append("{" + topicCount.getKey() + "->" + topicCount.getValue() + "}");
            s.append("\n");
        }
        s.append("KA Counts: ");
        s.append("\n");
        for (Map.Entry<Integer, Integer> kaPercent : _kaCounts.entrySet()) {
            s.append("{" + kaPercent.getKey() + "->" + kaPercent.getValue() + "}");
            s.append("\n");
        }
        s.append("KA Percents: ");
        for (Map.Entry<Integer, Double> kaPercent : _kaPercents.entrySet()) {
            s.append("{" + kaPercent.getKey() + "->" + kaPercent.getValue() + "}");
            s.append("\n");
        }

        return s.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
