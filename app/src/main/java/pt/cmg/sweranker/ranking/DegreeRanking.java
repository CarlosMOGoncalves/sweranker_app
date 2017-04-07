package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.degrees.Degree;

/**
 * Created by Carlos on 28/03/2017.
 */

public class DegreeRanking {

    private Degree _degree;
    private int _degreeId;

    /**
     * Keys -> the id of the annual combination , Values -> the annual combination itself
     */
    private Map<String, AnnualClassCombination> _annualCombinations;

    /**
     * Keys -> the id of the annual combination , Values -> its matching calculation
     */
    private Map<String, KACalculation> _annualCalculations;

    /**
     * This is the master combination.
     * It is ALL the possible combinations of classes that one can take to complete this Degree.
     */
    private Map<Integer, DegreeClassCombination> _fullDegreeCombinations;

    /**
     * Keys -> full degree calculation id , Values -> its matching calculated score
     */
    private Map<Integer, KACalculation> _fullDegreeCalculations;

    public DegreeRanking(Degree degree) {
        _degree = degree;
        _degreeId = degree.getId();
        _annualCombinations = new HashMap<>();
        _fullDegreeCombinations = new HashMap<>();

        _annualCalculations = new HashMap<>();
        _fullDegreeCalculations = new HashMap<>();
    }


    public Degree getDegree() {
        return _degree;
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

    public Map<Integer, DegreeClassCombination> getFullDegreeCombinations() {
        return _fullDegreeCombinations;
    }

    public void setFullDegreeCombinations(Map<Integer, DegreeClassCombination> fullDegreeCombinations) {
        _fullDegreeCombinations = fullDegreeCombinations;
    }


    public void addAnnualCalculations(Map<String, KACalculation> annualCalculations) {
        _annualCalculations.putAll(annualCalculations);
    }

    public void updateCalculation(String annualCombinationId, KACalculation annualCalculation) {
        _annualCalculations.put(annualCombinationId, annualCalculation);
    }

    public void addFullDegreeCalculations(Map<Integer, KACalculation> fullDegreeCalculations) {
        _fullDegreeCalculations.putAll(fullDegreeCalculations);
    }

    public void updateFullDegreeCalculation(int degreeCombinationId, KACalculation degreeScore) {
        _fullDegreeCalculations.put(degreeCombinationId, degreeScore);
    }
}
