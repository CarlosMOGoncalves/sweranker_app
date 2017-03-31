package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Carlos on 07/03/2017.
 */

public class KACalculation {

    private static final int INCREMENT = 1;
    private static final int PERCENT = 100;

    // Keys -> KA Topic ids, Values -> number of times this ka topic was matched in this Degree Class
    private Map<Integer, Integer> _kaTopicCounts;


    // Keys -> KA ids , Values -> number of times this same KA has been matched in this Degree Class;
    private Map<Integer, Integer> _kaCounts;

    // Keys -> KA ids , Values -> the percent that KA appears in matches on the overall Degree Class
    private Map<Integer, Float> _kaPercents;


    // Used to calculate the percents
    private int _topicCount;

    public KACalculation() {
        _kaTopicCounts = new HashMap<>();
        _kaPercents = new HashMap<>();
        _kaCounts = new HashMap<>();
    }

    public Map<Integer, Integer> getKaTopicCounters() {
        return _kaTopicCounts;
    }

    public Map<Integer, Integer> getKaCounters() {
        return _kaCounts;
    }

    public void setKaTopicCount(Map<Integer, Integer> kaTopicCount) {
        _kaTopicCounts = kaTopicCount;
    }

    public Map<Integer, Float> getKaPercents() {
        return _kaPercents;
    }

    public void setKaPercent(Map<Integer, Float> kaPercent) {
        _kaPercents = kaPercent;
    }

    public int getTopicCount() {
        return _topicCount;
    }

    public void setTopicCount(int topicCount) {
        _topicCount = topicCount;
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

            if (_kaTopicCounts.putIfAbsent(topicId, topicCount) != null) {
                _kaTopicCounts.put(topicId, _kaTopicCounts.get(topicId) + topicCount);
            } else {
                _topicCount += topicCount;
            }
        }

        for (Map.Entry<Integer, Integer> entry : newKaCounts.entrySet()) {

            Integer kaId = entry.getKey();
            Integer kaCount = entry.getValue();

            if (_kaCounts.putIfAbsent(kaId, kaCount) != null) {
                _kaCounts.put(kaId, _kaCounts.get(kaId) + kaCount);
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
        _topicCount++;

        calculateNewPercents(knowledgeAreaId);
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

        for (Map.Entry<Integer, Float> entry : _kaPercents.entrySet()) {
            entry.setValue((float) _kaCounts.get(entry.getKey()) / _topicCount * PERCENT);
        }
    }

    /**
     * This resets the percent calculations. Only really useful after every counter has been put into place and
     * no further data will be added.
     */
    public void resetPercentCalculations() {

        _kaPercents.clear();

        for (Map.Entry<Integer, Integer> entry : _kaCounts.entrySet()) {
            _kaPercents.put(entry.getKey(), (float) _kaCounts.get(entry.getKey()) / _topicCount * PERCENT);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Total topics: " + _topicCount);
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
        for (Map.Entry<Integer, Float> kaPercent : _kaPercents.entrySet()) {
            s.append("{" + kaPercent.getKey() + "->" + kaPercent.getValue() + "}");
            s.append("\n");
        }

        return s.toString();
    }

}
