package pt.cmg.sweranker.ranking;

import java.util.List;

/**
 * Created by Carlos on 29/03/2017.
 */

public class CalculationUtils {


    /**
     * Aggregates the calculations os a list of rankings in a single, aggregated ranking.
     *
     * @param kaCalculations
     * @return
     */
    public static KACalculation calculateAccumulatedRankings(List<KACalculation> kaCalculations) {

        KACalculation result = new KACalculation();

        for (int i = 0; i < kaCalculations.size(); i++) {
            result.addCounters(kaCalculations.get(i).getKaTopicCounters(), kaCalculations.get(i).getKaCounters());
        }

        result.resetPercentCalculations();
        return result;
    }


}
