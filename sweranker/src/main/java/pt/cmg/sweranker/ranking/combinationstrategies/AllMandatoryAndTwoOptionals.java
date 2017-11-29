package pt.cmg.sweranker.ranking.combinationstrategies;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.List;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;

/**
 * This strategy creates the combinations of classes obtained by combining all the optional classes, 3 at the time
 * and adding them the mandatory ones.
 */
public class AllMandatoryAndTwoOptionals implements ClassCombinationStrategy {


    private static String combinationIdBase;
    private static int idCounter;
    private static int currentYear;

    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(int year, List<DegreeClass> allDegreeClasses) {

        currentYear = year;
        combinationIdBase = "d_" + allDegreeClasses.get(0).getDegreeId() + "_y_" + year + "_c_";
        idCounter = 1;

        List<DegreeClass> classesOfYear = getDegreeClassesOfYear(allDegreeClasses);


        List<String> mandatoryClasses = new ArrayList<>();
        List<String> optionalClasses = new ArrayList<>();

        for (DegreeClass degreeClass : classesOfYear) {
            if (degreeClass.isMandatoryClass()) {
                mandatoryClasses.add(degreeClass.getId());
            } else {
                optionalClasses.add(degreeClass.getId());
            }
        }

        ICombinatoricsVector<String> optionalVector = Factory.createVector(optionalClasses.toArray(new String[optionalClasses.size()]));

        // creates the generator that can calculate the needed combinations.
        Generator<String> optionalCombinations = Factory.createSimpleCombinationGenerator(optionalVector, 2);


        List<AnnualClassCombination> resultingCombinations = new ArrayList<>();

        // Now iterate over all the possible combinations, add them the mandatory classes and add them to the final object.
        for (ICombinatoricsVector<String> comboVector : optionalCombinations.generateAllObjects()) {

            AnnualClassCombination currentCombination = new AnnualClassCombination(combinationIdBase + idCounter);
            currentCombination.setYear(year);

            currentCombination.addDegreeClasses(comboVector.getVector());
            currentCombination.addDegreeClasses(mandatoryClasses);

            resultingCombinations.add(currentCombination);

            // increment counter to create a new ID for this particular combo
            idCounter++;
        }


        return resultingCombinations;
    }


    /**
     * This function pretty much just returns a list with all the Degree Classes for THE CURRENT YEAR
     * we are trying to get the combinations for.
     */
    private List<DegreeClass> getDegreeClassesOfYear(List<DegreeClass> allDegreeClasses) {
        List<DegreeClass> classesOfYear = new ArrayList<>();
        for (DegreeClass degreeClass : allDegreeClasses) {
            if (degreeClass.getYear() == currentYear) {
                classesOfYear.add(degreeClass);
            }
        }
        return classesOfYear;
    }
}
