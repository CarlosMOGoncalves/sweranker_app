package pt.cmg.sweranker.ranking;

import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.degrees.DegreeClass;
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

        ranking.setKaPercents(calculatePercents(kaCounters, totalTopicCounters));

        return ranking;
    }


    /**
     * The calculation of a simple percent is pretty straightforward.
     * One basically divides the results of a given KA counter by the total number of all
     * KA counters (that is, the sum of all the topics that were matched) and then just mutiplies
     * by 100.
     *
     * @param kaCounters        the discrete values of each KA (i.e. how many times the KA has been matched)
     * @param totalTopicCounter the total number of ALL the KA topics that were matched.
     * @return an array containing the final percentage values of each KA in the whole of the SweScore this is used to calculate.
     */
    private static float[] calculatePercents(short[] kaCounters, int totalTopicCounter) {

        float[] kaPercents = new float[kaCounters.length];

        for (int i = 0; i < kaCounters.length; i++) {
            kaPercents[i] = (float) kaCounters[i] / totalTopicCounter * 100;
        }

        return kaPercents;
    }


    /**
     * Calculates an aggregate SweScore with many other scores as base.
     * This is simpler than it looks, it mostly just sums the values on all the scores.
     * <p>
     * The tricky part is using a Weighted Average.
     * Whenever an accumulated score is calculated I used a Weighted Average to calculate the percentages.
     * This is very important: this was used so that whenever a Degree Class was more important than another,
     * its Topics Matched would be more valuable to the Average.
     * The way the importance is calculated is basically using the ECTS of each Degree Class, as this credit system
     * rewards more the Degree Classes that are more important and that take up more time of the student.
     *
     * @param sweScores
     * @return
     */
    public static SweScore calculateAccumulatedScore(Map<String, DegreeClass> degreeClasses, List<SweScore> sweScores) {

        short[] kaTopicCounters = new short[102];
        short[] kaCounters = new short[16];

        int[] kaWeightedCounters = new int[16];

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

                // This here! The weighted counter is actually the value of the original counter TIMES the ECTS of this class...
                kaWeightedCounters[i] += currentScoreKaCounters[i] * degreeClasses.get(currentScore.getId()).getEctsCredits();
            }
        }

        // ... and so, the total used to calculate the percent is also the total number of topics but the ones that were already weighed.
        int kaWeightedTotalCounter = 0;
        for (int i = 0; i < kaWeightedCounters.length; i++) {
            kaWeightedTotalCounter += kaWeightedCounters[i];
        }


        // And finally build the resulting score object
        SweScore result = new SweScore();
        result.setKaCounters(kaCounters);
        result.setTopicCounters(kaTopicCounters);
        result.setTotalTopicCount(totalTopicCounter);

        result.setKaPercents(calculateWeighedPercents(kaWeightedCounters, kaWeightedTotalCounter));

        return result;
    }

    /**
     * The calculation of a simple percent is pretty straightforward.
     * One basically divides the results of a given KA counter by the total number of all
     * KA counters (that is, the sum of all the topics that were matched) and then just mutiplies
     * by 100.
     * <p>
     * NOTE: this is exactly the same as the above. I just used a different function name to distinguish and
     * not to cast the array from o to short in the parameters.
     * <p>
     *
     * @param kaCounters        the discrete values of each KA (i.e. how many times the KA has been matched)
     * @param totalTopicCounter the total number of ALL the KA topics that were matched.
     * @return an array containing the final percentage values of each KA in the whole of the SweScore this is used to calculate.
     */
    private static float[] calculateWeighedPercents(int[] kaCounters, int totalTopicCounter) {

        float[] kaPercents = new float[kaCounters.length];

        for (int i = 0; i < kaCounters.length; i++) {
            kaPercents[i] = (float) kaCounters[i] / totalTopicCounter * 100;
        }

        return kaPercents;
    }


}
