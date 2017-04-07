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
     * Note: this will create DegreeClassCombination instances that SHARE the total pool of AnnualClassCombinations.
     * This was done on purpose as, depending on the degree, there can be hundreds of thousands of combinations, so
     * creating an instance for each would eat up memory like it was cookies.
     *
     * @return
     */
    public static Map<Integer, DegreeClassCombination> generateAndSaveAllDegreeCombinations(Degree degree, Map<Integer, List<AnnualClassCombination>> combinationsByYear) {

        int years = combinationsByYear.size();

        //First I create the list that will be UPDATED on each calculation of a new years combinations
        List<DegreeClassCombination> currentTotalCombinations = new ArrayList<>();

        // And I use the first year as a base for combinations, any year would do, just following some logic here
        for (AnnualClassCombination firstYearCombination : combinationsByYear.get(1)) {
            currentTotalCombinations.add(new DegreeClassCombination(firstYearCombination));
        }


        // Then I shall iterate over the remaining years...
        for (int i = 2; i <= years; i++) {

            // I get the available combinations of classes for THAT year...
            List<AnnualClassCombination> currentYearCombinations = combinationsByYear.get(i);

            List<DegreeClassCombination> currentExpandedCombinations = new ArrayList<>();

            // Then I iterate over my temporary structure, this will naturally grow as I add more year combinations.
            // Because for each of those combinations I am going to create a new number of new combinations which are ALL
            // the different ones for the year I am iterating now...
            for (int j = 0; j < currentTotalCombinations.size(); j++) {

                // So for the current year combinations...
                for (int k = 0; k < currentYearCombinations.size(); k++) {

                    // I create a new Degree combination..
                    DegreeClassCombination newCombo = new DegreeClassCombination();

                    // That has all the combinations it already has...
                    newCombo.setClassCombinationsByYear(currentTotalCombinations.get(j).getClassCombinationsByYear());
                    //Plus the current combination I am looking at, of the year  am iterating over.
                    newCombo.addAnnualClassCombination(currentYearCombinations.get(k));

                    currentExpandedCombinations.add(newCombo);
                }
            }

            // After I run through the year, then my new base set of Degree Class Combinations is ready to be updated.
            currentTotalCombinations = currentExpandedCombinations;

        }

        // Now all the combinations have been calculated, it is time to put an ID on it.
        // I used an integer because a string, albeit nice, was heavy on memory (since I have so many combinations...)
        int degreeIdCounter = 1;

        // This is one last pass to add an ID to all the combinations
        Map<Integer, DegreeClassCombination> finalAllCombinations = new HashMap<>();
        for (DegreeClassCombination combination : currentTotalCombinations) {
            combination.setCombinationId(++degreeIdCounter);
            finalAllCombinations.put(combination.getCombinationId(), combination);
        }


        return finalAllCombinations;
    }


    /**
     * Calculates and outputs all the possible combinations between a collection of ClassCombinations.
     * <p>
     * It basically expands each combination list of a ClassCombinationMatrix for every combination list of another
     * ClassCombinationMatrix and so on until all the ClassCombinations are consumed
     * <p>
     * Note: this will create DegreeClassCombination instances that SHARE the total pool of AnnualClassCombinations.
     * This was done on purpose as, depending on the degree, there can be hundreds of thousands of combinations, so
     * creating an instance for each would eat up memory like it was cookies.
     *
     * @return
     */
    public static List<RealmDegreeCombination> generateAllDegreeCombinations(Degree degree, List<RealmAnnualCombination> allAnnualCombinations) {

        Map<Integer, List<RealmAnnualCombination>> combinationsByYear = new HashMap<>();

        for (RealmAnnualCombination annualCombo : allAnnualCombinations) {
            Integer comboYear = annualCombo.getYear();

            if (combinationsByYear.containsKey(comboYear)) {
                combinationsByYear.get(comboYear).add(annualCombo);
            } else {
                List<RealmAnnualCombination> annualComboList = new ArrayList<>();
                annualComboList.add(annualCombo);
                combinationsByYear.put(comboYear, annualComboList);
            }
        }


        int years = combinationsByYear.size();

        //First I create the list that will be UPDATED on each calculation of a new years combinations
        List<RealmDegreeCombination> currentTotalCombinations = new ArrayList<>();

        // And I use the first year as a base for combinations, any year would do, just following some logic here
        for (RealmAnnualCombination firstYearCombination : combinationsByYear.get(1)) {
            currentTotalCombinations.add(new RealmDegreeCombination(firstYearCombination));
        }


        // Then I shall iterate over the remaining years...
        for (int i = 2; i <= years; i++) {

            // I get the available combinations of classes for THAT year...
            List<RealmAnnualCombination> currentYearCombinations = combinationsByYear.get(i);

            List<RealmDegreeCombination> currentExpandedCombinations = new ArrayList<>();

            // Then I iterate over my temporary structure, this will naturally grow as I add more year combinations.
            // Because for each of those combinations I am going to create a new number of new combinations which are ALL
            // the different ones for the year I am iterating now...
            for (int j = 0; j < currentTotalCombinations.size(); j++) {

                // So for the current year combinations...
                for (int k = 0; k < currentYearCombinations.size(); k++) {

                    // I create a new Degree combination..
                    RealmDegreeCombination newCombo = new RealmDegreeCombination();

                    // That has all the combinations it already has...
                    newCombo.setClassCombinationsByYear(currentTotalCombinations.get(j).getClassCombinationsByYear());
                    //Plus the current combination I am looking at, of the year  am iterating over.
                    newCombo.addAnnualClassCombination(currentYearCombinations.get(k));

                    currentExpandedCombinations.add(newCombo);
                }
            }

            // After I run through the year, then my new base set of Degree Class Combinations is ready to be updated.
            currentTotalCombinations = currentExpandedCombinations;

        }

        // Now all the combinations have been calculated, it is time to put an ID on it.
        // I used an integer because a string, albeit nice, was heavy on memory (since I have so many combinations...)
        int degreeIdCounter = 1;

        // This is one last pass to add an ID to all the combinations
        for (RealmDegreeCombination combination : currentTotalCombinations) {
            combination.setCombinationId(++degreeIdCounter);
        }


        return currentTotalCombinations;
    }

}
