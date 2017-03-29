package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Carlos on 28/03/2017.
 */

public class DegreeRanking {

    private int _degreeId;

    /**
     * Keys -> the Year of the Degree , Values -> the available class combinations for that year
     */
    private Map<Integer, ClassCombinationMatrix> _combinationsByYear;

    private Map<Integer, String> _calculationsByYear;

    /**
     * This is the master combination.
     * It is ALL the possible combinations of classes that one can take to complete this Degree.
     */
    private ClassCombinationMatrix _fullDegreeCombinations;

    private String _fullDegreeCalculation;

    public DegreeRanking(int degreeId) {
        _degreeId = degreeId;
        _combinationsByYear = new HashMap<>();
        _fullDegreeCombinations = new ClassCombinationMatrix();
    }


    public int getDegreeId() {
        return _degreeId;
    }

    public void addYearCombination(int year, ClassCombinationMatrix combinations) {
        _combinationsByYear.put(year, combinations);
    }


    public Map<Integer, ClassCombinationMatrix> getCombinationsByYear() {
        return _combinationsByYear;
    }

    public void setCombinationsByYear(Map<Integer, ClassCombinationMatrix> combinationsByYear) {
        _combinationsByYear = combinationsByYear;
    }

    public ClassCombinationMatrix getYearlyCombinations() {
        return _fullDegreeCombinations;
    }

    public void setFullDegreeCombinations(ClassCombinationMatrix fullDegreeCombinations) {
        _fullDegreeCombinations = fullDegreeCombinations;
    }
}
