package pt.cmg.sweranker.ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.degrees.Degree;

/**
 * Created by Carlos on 27/03/2017.
 */

public class CombinationUtils {


    /**
     * Calculates and outputs all the possible combinations between a collection of ClassCombinations.
     * <p>
     * It basically expands each combination list of a ClassCombinationMatrix for every combination list of another
     * ClassCombinationMatrix and so on until all the ClassCombinations are consumed
     * <p>
     * Note: this is not particularly optimised, using a full ArrayList will likely create many objects but time is an issue
     * so I am not going to optimise this bit unless I find it to be really critical later on.
     *
     * @return
     */
    public static Map<String, DegreeClassCombination> generateAllDegreeCombinations(Degree degree, Map<Integer, List<AnnualClassCombination>> combinationsByYear) {

        int years = combinationsByYear.size();


        List<DegreeClassCombination> currentTotalCombinations = new ArrayList<>();

        for (AnnualClassCombination firstYearCombination : combinationsByYear.get(1)) {
            currentTotalCombinations.add(new DegreeClassCombination(firstYearCombination));
        }


        for (int i = 2; i <= years; i++) {

            List<AnnualClassCombination> currentYearCombinations = combinationsByYear.get(i);

            List<DegreeClassCombination> currentExpandedCombinations = new ArrayList<>();

            for (int j = 0; j < currentTotalCombinations.size(); j++) {
                for (int k = 0; k < currentYearCombinations.size(); k++) {
                    DegreeClassCombination newCombo = new DegreeClassCombination();
                    newCombo.setClassCombinationsByYear(currentTotalCombinations.get(j).getClassCombinationsByYear());
                    newCombo.addAnnualClassCombination(currentYearCombinations.get(k));

                    currentExpandedCombinations.add(newCombo);
                }
            }


            currentTotalCombinations = currentExpandedCombinations;


        }

        String degreeComboIdBase = "degree_" + degree.getId() + "_combo_";
        int degreeIdCounter = 0;

        Map<String, DegreeClassCombination> finalAllCombinations = new HashMap<>();
        for (DegreeClassCombination combination : currentTotalCombinations) {
            combination.setCombinationId(degreeComboIdBase + ++degreeIdCounter);
            finalAllCombinations.put(combination.getCombinationId(), combination);
        }
        currentTotalCombinations = null;


        return finalAllCombinations;
    }
}
