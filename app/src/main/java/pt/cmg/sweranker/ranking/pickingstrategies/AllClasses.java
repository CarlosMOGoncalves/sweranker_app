package pt.cmg.sweranker.ranking.pickingstrategies;

import java.util.ArrayList;
import java.util.List;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.ClassCombination;

/**
 * This strategy is plainly simple: it basically picks ALL classes of a single year.
 * This means that the resulting combination is really just one list with the ids of all the
 * given degree classes for that year.
 */
public class AllClasses implements ClassPickerStrategy {

    @Override
    public ClassCombination getClassCombinations(List<DegreeClass> degreeClassesOfYear) {

        List<String> classCombination = new ArrayList<>();
        for (DegreeClass degreeClass : degreeClassesOfYear) {
            classCombination.add(degreeClass.getId());
        }
        return new ClassCombination(classCombination);

    }
}
