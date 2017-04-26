package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;

/**
 * Created by Carlos on 29/03/2017.
 */

public class CalculationUtils {


    public static SweScore calculateAccumulatedScore(List<SweScore> sweScores) {

        Map<Byte, KATopicCount> kaTopicCounters = new HashMap<>();
        Map<Byte, KACount> kaCounters = new HashMap<>();

        for (SweScore currentScore : sweScores) {

            // First increment ALL the KA counters
            RealmList<KACount> currentKaCounters = currentScore.getKaCounters();
            for (KACount kaCounter : currentKaCounters) {

                if (kaCounters.containsKey(kaCounter.getKaId())) {
                    kaCounters.get(kaCounter.getKaId()).incrementCounter(kaCounter.getKaCount());
                } else {
                    KACount newCounter = new KACount(kaCounter.getKaId());
                    newCounter.setKaCount(kaCounter.getKaCount());
                    kaCounters.put(kaCounter.getKaId(), newCounter);
                }
            }

            // Then update ALL the KA Topic counters AND the total counter
            RealmList<KATopicCount> currentTopicCounters = currentScore.getTopicCounters();
            for (KATopicCount topicCounter : currentTopicCounters) {


                if (kaTopicCounters.containsKey(topicCounter.getTopicId())) {
                    kaTopicCounters.get(topicCounter.getTopicId()).incrementCounter(topicCounter.getTopicCount());
                } else {
                    KATopicCount newCounter = new KATopicCount(topicCounter.getTopicId());
                    newCounter.setTopicCount(topicCounter.getTopicCount());
                    kaTopicCounters.put(topicCounter.getTopicId(), newCounter);
                }
            }
        }


        // And finally build the resulting score object
        SweScore result = new SweScore();
        result.setKaCounters(kaCounters.values());
        result.setTopicCounters(kaTopicCounters.values());
        result.calculateScores();

        return result;
    }

}
