package pt.cmg.sweranker.ranking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a full combination of degree classes for a given Degree.
 * This means: one single combination of classes that guarantees the conclusion of a degree.
 * Depending on the type of degrees, these can have - an most will have - different combinations
 * of optional classes that they can take to complete it.
 * This class represents ONE single combination of classes, as a collection of the concrete classes
 * a student can take in each year.
 */
public class DegreeClassCombination {

    private static final int DEFAULT_ID = -1;


    private int _combinationId;
    private Map<Integer, AnnualClassCombination> _classCombinationsByYear;

    public DegreeClassCombination() {
        _combinationId = DEFAULT_ID;
        _classCombinationsByYear = new HashMap<>();
    }

    public DegreeClassCombination(int combinationId) {
        _combinationId = combinationId;
        _classCombinationsByYear = new HashMap<>();
    }

    public DegreeClassCombination(AnnualClassCombination annualClassCombination) {
        _combinationId = DEFAULT_ID;
        _classCombinationsByYear = new HashMap<>();
        _classCombinationsByYear.put(annualClassCombination.getYear(), annualClassCombination);
    }

    public int getCombinationId() {
        return _combinationId;
    }

    public void setCombinationId(int combinationId) {
        _combinationId = combinationId;
    }


    public Map<Integer, AnnualClassCombination> getClassCombinationsByYear() {
        return _classCombinationsByYear;
    }


    public void addAnnualClassCombination(AnnualClassCombination newAnnualClassCombination) {
        _classCombinationsByYear.put(newAnnualClassCombination.getYear(), newAnnualClassCombination);
    }

    public void setClassCombinationsByYear(Map<Integer, AnnualClassCombination> classCombinationsByYear) {
        _classCombinationsByYear = classCombinationsByYear;
    }

    public AnnualClassCombination getAnnualCombination(int year) {
        return _classCombinationsByYear.get(year);
    }

    public Collection<AnnualClassCombination> getAnnualCombinations() {
        return _classCombinationsByYear.values();
    }
}
