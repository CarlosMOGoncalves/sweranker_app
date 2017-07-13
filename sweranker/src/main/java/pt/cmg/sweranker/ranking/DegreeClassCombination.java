package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 05/04/2017.
 */

public class DegreeClassCombination extends RealmObject {

    @PrimaryKey
    private String combinationId;

    private int degreeId;

    private RealmList<AnnualClassCombination> annualClassCombinations;


    public DegreeClassCombination() {
        combinationId = "Inv";
        annualClassCombinations = new RealmList<>();
    }


    public DegreeClassCombination(String combinationId) {
        this.combinationId = combinationId;
        annualClassCombinations = new RealmList<>();
    }

    public DegreeClassCombination(int degreeId, String combinationId) {
        this.degreeId = degreeId;
        this.combinationId = combinationId;
        annualClassCombinations = new RealmList<>();
    }

    public DegreeClassCombination(AnnualClassCombination annualClassCombination) {
        combinationId = "Inv";
        annualClassCombinations = new RealmList<>();
        annualClassCombinations.add(annualClassCombination);
    }

    public String getCombinationId() {
        return combinationId;
    }

    public void setCombinationId(String combinationId) {
        this.combinationId = combinationId;
    }

    public RealmList<AnnualClassCombination> getAnnualClassCombinations() {
        return annualClassCombinations;
    }

    public void setAnnualClassCombinations(List<AnnualClassCombination> annualClassCombinations) {
        for (AnnualClassCombination combo : annualClassCombinations) {
            this.annualClassCombinations.add(combo);
        }

    }

    public Map<Integer, AnnualClassCombination> getClassCombinationsByYear() {

        Map<Integer, AnnualClassCombination> classCombinationsByYear = new HashMap<>();

        for (AnnualClassCombination annualCombination : annualClassCombinations) {
            classCombinationsByYear.put(annualCombination.getYear(), annualCombination);
        }
        return classCombinationsByYear;
    }

    public void addAnnualClassCombination(AnnualClassCombination newAnnualClassCombination) {
        annualClassCombinations.add(newAnnualClassCombination);
    }

    public AnnualClassCombination getAnnualCombination(int year) {
        AnnualClassCombination result = null;
        for (AnnualClassCombination annualCombo : annualClassCombinations) {
            if (annualCombo.getYear() == year) {
                result = annualCombo;
            }
        }

        return result;
    }

    public int getDegreeId() {
        return degreeId;
    }

    public void setDegreeId(int degreeId) {
        this.degreeId = degreeId;
    }
}
