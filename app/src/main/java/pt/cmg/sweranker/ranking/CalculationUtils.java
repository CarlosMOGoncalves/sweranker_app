package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;

/**
 * Created by Carlos on 29/03/2017.
 */

public class CalculationUtils {


    /**
     * Aggregates the calculations os a list of rankings in a single, aggregated ranking.
     *
     * @param sweScores
     * @return
     */
    public static SweScore calculateAccumulatedRankings(List<SweScore> sweScores) {

        SweScore result = new SweScore();

        for (int i = 0; i < sweScores.size(); i++) {
            result.addCounters(sweScores.get(i).getKaTopicCountersByTopicId(), sweScores.get(i).getKaCountersByKaId());
        }

        result.calculateScores();
        return result;
    }


    public static SweScore calculateAccumulatedScore(List<SweScore> sweScores) {

        Map<Integer, Integer> kaTopicCounters = new HashMap<>();
        Map<Integer, Integer> kaCounters = new HashMap<>();
        Map<Integer, Double> kaPercentages = new HashMap<>();
        int totalTopicCount = 0;

        for (SweScore currentScore : sweScores) {

            // First increment ALL the KA counters
            RealmList<KACount> currentKaCounters = currentScore.getKaCounters();
            for (KACount kaCounter : currentKaCounters) {
                if (kaCounters.containsKey(kaCounter.getKaId())) {
                    kaCounters.put(kaCounter.getKaId(), kaCounters.get(kaCounter.getKaId()) + 1);
                } else {
                    kaCounters.put(kaCounter.getKaId(), 1);
                }
            }

            // Then update ALL the KA Topic counters AND the total counter
            RealmList<KATopicCount> currentTopicCounters = currentScore.getTopicCounters();
            for (KATopicCount topicCounter : currentTopicCounters) {
                totalTopicCount++;
                if (kaTopicCounters.containsKey(topicCounter.getTopicId())) {
                    kaTopicCounters.put(topicCounter.getTopicId(), kaTopicCounters.get(topicCounter.getTopicId()) + 1);
                } else {
                    kaTopicCounters.put(topicCounter.getTopicId(), 1);
                }
            }
        }

        // Finally calculate the percentages of each KA
        for (Map.Entry<Integer, Integer> entry : kaCounters.entrySet()) {
            kaPercentages.put(entry.getKey(), ((double) entry.getValue()) / totalTopicCount * 100);
        }

        // And finally build the resulting score object
        SweScore result = new SweScore();
        for (Map.Entry<Integer, Integer> kas : kaCounters.entrySet()) {
            result.addKaCounter(new KACount(kas.getKey(), kas.getValue()));
        }
        for (Map.Entry<Integer, Integer> topic : kaTopicCounters.entrySet()) {
            result.addKATopicCounter(new KATopicCount(topic.getKey(), topic.getValue()));
        }
        for (Map.Entry<Integer, Double> topic : kaPercentages.entrySet()) {
            result.addKAPercent(new KAPercent(topic.getKey(), topic.getValue()));
        }
        result.setTotalTopicCount(totalTopicCount);

        return result;
    }

}
