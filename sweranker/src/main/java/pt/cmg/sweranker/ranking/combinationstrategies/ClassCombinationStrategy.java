package pt.cmg.sweranker.ranking.combinationstrategies;

import java.util.List;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;

/**
 * This is the contract interface to be used in picking class combinations for a given year.
 * Its only function has the purpose of returning a ClassCombinationMatrix, which in itself is a List of
 * all possible combinations of degree class ids (another List of Strings)
 */
public interface ClassCombinationStrategy {


    /**
     * Returns all the possible combinations of classes created by running some sort of algorithm over
     * the complete List of classes for a particular degree.
     *
     * @param targetYear       The year that this strategy will be used for. It will most likely be used to
     *                         limit or filter the classes from the second parameter
     * @param allDegreeClasses All the classes for this degree. This will be used as a dictionary to pick the
     *                         classes from.
     * @return A List with all possible AnnualClassCombinations for this particular year.
     */
    List<AnnualClassCombination> getAnnualClassCombinations(int targetYear, List<DegreeClass> allDegreeClasses);


}
