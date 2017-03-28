package pt.cmg.sweranker.ranking.pickingstrategies;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.List;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.ClassCombination;

/**
 * This strategy creates the combinations of classes obtained by combining all the optional classes, 3 at the time
 * and adding them the mandatory ones.
 */
public class AllMandatoryAndThreeOptionals implements ClassPickerStrategy {


    @Override
    public ClassCombination getClassCombinations(List<DegreeClass> degreeClassesOfYear) {
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


        ClassCombination resultingCombinations = new ClassCombination();

        // Now iterate over all the possible combinations, add them the mandatory classes and add them to the final object.
        for (ICombinatoricsVector<String> comboVector : optionalCombinations.generateAllObjects()) {
            List<String> combination = new ArrayList<>();
            combination.addAll(comboVector.getVector());
            combination.addAll(mandatoryClasses);

            resultingCombinations.addCombination(combination);
        }

        return resultingCombinations;
    }
}
