package pt.cmg.sweranker.ranking.combinationstrategies;

import java.util.List;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.ClassCombinationMatrix;

/**
 * This is the contract interface to be used in picking class combinations for a given year.
 * Its only function has the purpose of returning a ClassCombinationMatrix, which in itself is a List of
 * all possible combinations of degree class ids (another List of Strings)
 */
public interface ClassCombinationStrategy {

    /**
     * Returns all the possible combinations of classes created by running some sort of algorithm over
     * the complete List of classes for that given year.
     *
     * @param degreeClassesOfYear
     * @return
     */
    ClassCombinationMatrix getClassCombinations(List<DegreeClass> degreeClassesOfYear);

}
