package pt.cmg.sweranker.ranking;

import java.util.List;

/**
 * Created by Carlos on 29/03/2017.
 */

public class CalculationUtils {


    /**
     * Calculates an aggregate SweScore with many other scores as base.
     * This is simpler than it looks, it mostly just sums the values on all the scores.
     *
     * @param sweScores
     * @return
     */
    public static SweScore calculateAccumulatedScore(List<SweScore> sweScores) {

        short[] kaTopicCounters = new short[102];
        short[] kaCounters = new short[16];
        short totalTopicCounter = 0;

        for (SweScore currentScore : sweScores) {

            short[] currentScoreTopicCounters = currentScore.getTopicCounters();
            for (int i = 0; i < currentScoreTopicCounters.length; i++) {
                kaTopicCounters[i] += currentScoreTopicCounters[i];
                totalTopicCounter += currentScoreTopicCounters[i];
            }

            short[] currentScoreKaCounters = currentScore.getKaCounters();
            for (int i = 0; i < currentScoreKaCounters.length; i++) {
                kaCounters[i] += currentScoreKaCounters[i];
            }
        }


        // And finally build the resulting score object
        SweScore result = new SweScore();
        result.setKaCounters(kaCounters);
        result.setTopicCounters(kaTopicCounters);
        result.setTotalTopicCount(totalTopicCounter);

        // I am starting to think this method should be outside the SweScore class and be directly here
        // The SweScore would just be the class holding the data and not calculating it. To review.
        result.calculateScores();

        return result;
    }

}
