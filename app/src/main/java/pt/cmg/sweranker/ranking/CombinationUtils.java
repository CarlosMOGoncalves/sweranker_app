package pt.cmg.sweranker.ranking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carlos on 27/03/2017.
 */

public class CombinationUtils {


    /**
     * Calculates and outputs all the possible combinations between two smaller sets of combinations.
     * It basically expands each element of the first  parameter combination list and appends each combination of the second
     * parameter combination list, thus effectively multiplying exponentially the possible combinations.
     * <p>
     * Note: this is not particularly optimised, using a full ArrayList will likely create many objects but time is an issue
     * so I am not going to optimise this bit unless I find it to be really critical later on.
     *
     * @param originalCombinations
     * @param combinationsToAppend
     * @return
     */
    public static ClassCombination combineCombinations(ClassCombination originalCombinations, ClassCombination combinationsToAppend) {

        ClassCombination result = new ClassCombination();

        for (List<String> combo : originalCombinations.getCombinations()) {

            for (List<String> anotherCombo : combinationsToAppend.getCombinations()) {

                List<String> combinedCombo = new ArrayList<>(combo);
                combinedCombo.addAll(anotherCombo);

                result.addCombination(combinedCombo);
            }
        }

        return result;
    }
}
