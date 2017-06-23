package pt.cmg.sweranker.ranking;

import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

/**
 * Created by Carlos on 29/03/2017.
 */

public class CalculationUtils {


    /**
     * Calculates the SweScore for a given DegreeClassMatch.
     * This will basically sweep the match for all its topic that were matched and pretty much count them
     * discretely so that in the end a percentage can be calculated
     * <p>
     * This function is the very essence of the calculation. ALL other scores are just accumulated from here
     * so if anytime in the future the calculation algorithm is changed, it is here that it must be done.
     *
     * @param classMatch    This is the matching for a given class.
     * @param topicResolver This is a map whose keys are topic ids and their matching topic. This is used as a dictionary to feed data to calculation
     * @return
     */
    public static SweScore calculateScore(Map<Integer, KnowledgeAreaTopic> topicResolver, DegreeClassMatch classMatch) {

        short[] kaTopicCounters = new short[102];
        short[] kaCounters = new short[16];


        int currentKnowledgeAreaId = 0;
        short totalTopicCounters = 0;

        for (Integer kaTopicId : classMatch.getAllMatchesAsList()) {

            currentKnowledgeAreaId = topicResolver.get(kaTopicId).getKnowledgeAreaId();

            kaCounters[currentKnowledgeAreaId - 1]++;
            kaTopicCounters[kaTopicId - 1]++;
            totalTopicCounters++;
        }

        SweScore ranking = new SweScore(classMatch.getDegreeClassId(), (byte) classMatch.getDegreeId(), SweScore.TYPE_CLASS_SCORE);
        ranking.setKaCounters(kaCounters);
        ranking.setTopicCounters(kaTopicCounters);
        ranking.setTotalTopicCount(totalTopicCounters);

        ranking.calculateScores();

        return ranking;
    }


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
