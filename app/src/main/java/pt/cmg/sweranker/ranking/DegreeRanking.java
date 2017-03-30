package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Carlos on 28/03/2017.
 */

public class DegreeRanking {

    private int _degreeId;

    /**
     * Keys -> the id of the annual combination , Values -> the annual combination itself
     */
    private Map<String, AnnualClassCombination> _annualCombinations;

    private Map<Integer, KACalculation> _calculationsByYear;

    /**
     * This is the master combination.
     * It is ALL the possible combinations of classes that one can take to complete this Degree.
     */
    private Map<String, DegreeClassCombination> _fullDegreeCombinations;

    private KACalculation _fullDegreeCalculation;

    public DegreeRanking(int degreeId) {
        _degreeId = degreeId;
        _annualCombinations = new HashMap<>();
        _fullDegreeCombinations = new HashMap<>();
    }


    public int getDegreeId() {
        return _degreeId;
    }

    public void addYearCombination(String annualComboId, AnnualClassCombination annualCombination) {
        _annualCombinations.put(annualComboId, annualCombination);
    }


    public Map<String, AnnualClassCombination> getCombinationsByYear() {
        return _annualCombinations;
    }

    public void addAnnualCombinations(List<AnnualClassCombination> annualCombinations) {
        for (AnnualClassCombination combo : annualCombinations) {
            _annualCombinations.put(combo.getId(), combo);
        }
    }

    public void setAnnualCombinations(List<AnnualClassCombination> annualCombinations) {
        for (AnnualClassCombination combo : annualCombinations) {
            _annualCombinations.put(combo.getId(), combo);
        }
    }

    public void setAnnualCombinations(Map<String, AnnualClassCombination> combinationsByYear) {
        _annualCombinations = combinationsByYear;
    }

    public Map<String, DegreeClassCombination> getFullDegreeCombinations() {
        return _fullDegreeCombinations;
    }

    public void setFullDegreeCombinations(Map<String, DegreeClassCombination> fullDegreeCombinations) {
        _fullDegreeCombinations = fullDegreeCombinations;
    }
}
