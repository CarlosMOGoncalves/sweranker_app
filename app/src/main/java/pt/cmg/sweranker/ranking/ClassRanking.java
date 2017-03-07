package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Carlos on 07/03/2017.
 */

public class ClassRanking {

    private static final int INCREMENT = 1;
    private static final int PERCENT = 100;

    // Keys -> KA Topic ids, Values -> number of times this ka topic was matched in this Degree Class
    private Map<Integer, Integer> _kaTopicCount;


    // Keys -> KA ids , Values -> number of times this same KA has been matched in this Degree Class;
    private Map<Integer, Integer> _kaCount;

    // Keys -> KA ids , Values -> the percent that KA appears in matches on the overall Degree Class
    private Map<Integer, Float> _kaPercent;


    // Used to calculate the percents
    private int _topicCount;

    public ClassRanking() {
        _kaTopicCount = new HashMap<>();
        _kaPercent = new HashMap<>();
        _kaCount = new HashMap<>();
    }

    public Map<Integer, Integer> getKaTopicCount() {
        return _kaTopicCount;
    }

    public void setKaTopicCount(Map<Integer, Integer> kaTopicCount) {
        _kaTopicCount = kaTopicCount;
    }

    public Map<Integer, Float> getKaPercents() {
        return _kaPercent;
    }

    public void setKaPercent(Map<Integer, Float> kaPercent) {
        _kaPercent = kaPercent;
    }

    public int getTopicCount() {
        return _topicCount;
    }

    public void setTopicCount(int topicCount) {
        _topicCount = topicCount;
    }


    /**
     * Adds a topic, or increments its count if it already exists.
     * Increments the overall topic count and recalculates all the percents.
     *
     * @param knowledgeAreaTopicId
     */
    public void addTopic(int knowledgeAreaTopicId, int knowledgeAreaId) {

        // first update the topic counter
        if (_kaTopicCount.containsKey(knowledgeAreaTopicId)) {
            _kaTopicCount.put(knowledgeAreaTopicId, _kaTopicCount.get(knowledgeAreaTopicId) + INCREMENT);
        } else {
            _kaTopicCount.put(knowledgeAreaTopicId, 1);
        }

        // then update the KA counter
        if (_kaCount.containsKey(knowledgeAreaId)) {
            _kaCount.put(knowledgeAreaId, _kaCount.get(knowledgeAreaId) + INCREMENT);
        } else {
            _kaCount.put(knowledgeAreaId, 1);
        }

        // then global topic counter
        _topicCount++;

        calculateNewPercents(knowledgeAreaId);
    }

    /**
     * Recalculates all the percents again.
     * This is obviously needed everytime a new degree topic is matched with any ka topic.
     *
     * @param knowledgeAreaId
     */
    private void calculateNewPercents(int knowledgeAreaId) {

        if (!_kaPercent.containsKey(knowledgeAreaId)) {
            _kaPercent.put(knowledgeAreaId, null);
        }

        for (Map.Entry<Integer, Float> entry : _kaPercent.entrySet()) {
            entry.setValue((float) _kaCount.get(entry.getKey()) / _topicCount * PERCENT);
        }
    }

}
