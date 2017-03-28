package pt.cmg.sweranker.ranking.pickingstrategies;

import java.util.List;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.ClassCombination;

/**
 * This is the contract interface to be used in picking class combinations for a given year.
 * Its only function has the purpose of returning a ClassCombination, which in itself is a List of
 * all possible combinations of degree class ids (another List of Strings)
 */
public interface ClassPickerStrategy {

    /**
     * Returns all the possible combinations of classes created by running some sort of algorithm over
     * the complete List of classes for that given year.
     *
     * @param degreeClassesOfYear
     * @return
     */
    ClassCombination getClassCombinations(List<DegreeClass> degreeClassesOfYear);

}
