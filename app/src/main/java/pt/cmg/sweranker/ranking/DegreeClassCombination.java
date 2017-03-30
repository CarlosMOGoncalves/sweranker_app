package pt.cmg.sweranker.ranking;

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

    private static final String DEFAULT_ID = "INCONSISTENT";


    private String _combinationId;
    private Map<Integer, AnnualClassCombination> _classCombinationsByYear;

    public DegreeClassCombination() {
        _combinationId = DEFAULT_ID;
        _classCombinationsByYear = new HashMap<>();
    }

    public DegreeClassCombination(String combinationId) {
        _combinationId = combinationId;
        _classCombinationsByYear = new HashMap<>();
    }

    public DegreeClassCombination(AnnualClassCombination annualClassCombination) {
        _combinationId = DEFAULT_ID;
        _classCombinationsByYear = new HashMap<>();
//        AnnualClassCombination newCombination = new AnnualClassCombination(annualClassCombination);
        _classCombinationsByYear.put(annualClassCombination.getYear(), annualClassCombination);
    }

    public String getCombinationId() {
        return _combinationId;
    }

    public void setCombinationId(String combinationId) {
        _combinationId = combinationId;
    }


    public Map<Integer, AnnualClassCombination> getClassCombinationsByYear() {
        return _classCombinationsByYear;
    }


    public void addAnnualClassCombination(AnnualClassCombination newAnnualClassCombination) {
        _classCombinationsByYear.put(newAnnualClassCombination.getYear(), newAnnualClassCombination);
    }

    public void setClassCombinationsByYear(Map<Integer, AnnualClassCombination> classCombinationsByYear) {
        _classCombinationsByYear = new HashMap<>(classCombinationsByYear);
    }

    public AnnualClassCombination getAnnualCombination(int year) {
        return _classCombinationsByYear.get(year);
    }
}
