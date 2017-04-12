package pt.cmg.sweranker.ranking;

import java.util.List;

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
            result.addCounters(sweScores.get(i).getKaTopicCounters(), sweScores.get(i).getKaCounters());
        }

        result.resetPercentCalculations();
        return result;
    }


}
