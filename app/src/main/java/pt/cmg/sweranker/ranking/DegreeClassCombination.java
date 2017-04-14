package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 05/04/2017.
 */

public class DegreeClassCombination extends RealmObject {

    @PrimaryKey
    private String combinationId;

    private int degreeId;

    private RealmList<AnnualClassCombination> annualClassCombinations;

    @Ignore
    private Map<Integer, AnnualClassCombination> classCombinationsByYear;


    public DegreeClassCombination() {
        combinationId = "Inv";
        annualClassCombinations = new RealmList<>();
        classCombinationsByYear = new HashMap<>();
    }


    public DegreeClassCombination(String combinationId) {
        this.combinationId = combinationId;
        annualClassCombinations = new RealmList<>();
        classCombinationsByYear = new HashMap<>();
    }

    public DegreeClassCombination(AnnualClassCombination annualClassCombination) {
        combinationId = "Inv";
        annualClassCombinations = new RealmList<>();
        annualClassCombinations.add(annualClassCombination);
        classCombinationsByYear = new HashMap<>();
        classCombinationsByYear.put(annualClassCombination.getYear(), annualClassCombination);
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

    public void setAnnualClassCombinations(RealmList<AnnualClassCombination> annualClassCombinations) {
        this.annualClassCombinations = annualClassCombinations;

    }

    public Map<Integer, AnnualClassCombination> getClassCombinationsByYear() {

        if (classCombinationsByYear.isEmpty()) {
            for (AnnualClassCombination annualCombination : annualClassCombinations) {
                classCombinationsByYear.put(annualCombination.getYear(), annualCombination);
            }
        }
        return classCombinationsByYear;
    }

    public void addAnnualClassCombination(AnnualClassCombination newAnnualClassCombination) {
        annualClassCombinations.add(newAnnualClassCombination);
        classCombinationsByYear.put(newAnnualClassCombination.getYear(), newAnnualClassCombination);
    }

    public void setClassCombinationsByYear(Map<Integer, AnnualClassCombination> classCombinationsByYear) {
        for (Map.Entry<Integer, AnnualClassCombination> entry : classCombinationsByYear.entrySet()) {
            this.classCombinationsByYear.put(entry.getKey(), entry.getValue());
        }
        for (AnnualClassCombination annualCombo : classCombinationsByYear.values()) {
            annualClassCombinations.add(annualCombo);
        }
    }

    public AnnualClassCombination getAnnualCombination(int year) {
        return classCombinationsByYear.get(year);
    }

    public int getDegreeId() {
        return degreeId;
    }

    public void setDegreeId(int degreeId) {
        this.degreeId = degreeId;
    }
}
