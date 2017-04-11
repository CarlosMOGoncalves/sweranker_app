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
public class AllMandatoryAndThreeOptionals implements ClassCombinationStrategy {


    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(List<DegreeClass> degreeClassesOfYear) {

        int year = degreeClassesOfYear.get(0).getYear();
        String combinationIdBase = "d_" + degreeClassesOfYear.get(0).getDegreeId() + "_y_" + degreeClassesOfYear.get(0).getYear() + "_c_";

        List<String> mandatoryClasses = new ArrayList<>();
        List<String> optionalClasses = new ArrayList<>();

        for (DegreeClass degreeClass : degreeClassesOfYear) {
            if (degreeClass.isMandatoryClass()) {
                mandatoryClasses.add(degreeClass.getId());
            } else {
                optionalClasses.add(degreeClass.getId());
            }
        }

        ICombinatoricsVector<String> optionalVector = Factory.createVector(optionalClasses.toArray(new String[optionalClasses.size()]));

        // creates the generator that can calculate the needed combinations.
        Generator<String> optionalCombinations = Factory.createSimpleCombinationGenerator(optionalVector, 3);


        List<AnnualClassCombination> resultingCombinations = new ArrayList<>();


        int idCounter = 1;

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
}
