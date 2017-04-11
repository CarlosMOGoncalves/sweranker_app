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

    private static final int DEFAULT_ID = -1;

    @PrimaryKey
    private int combinationId;

    private int degreeId;

    private RealmList<AnnualClassCombination> annualClassCombinations;

    @Ignore
    private Map<Integer, AnnualClassCombination> classCombinationsByYear;


    public DegreeClassCombination() {
        combinationId = DEFAULT_ID;
        annualClassCombinations = new RealmList<>();
    }


    public DegreeClassCombination(int combinationId) {
        this.combinationId = combinationId;
        annualClassCombinations = new RealmList<>();
    }

    public DegreeClassCombination(AnnualClassCombination annualClassCombination) {
        combinationId = DEFAULT_ID;
        annualClassCombinations = new RealmList<>();
        annualClassCombinations.add(annualClassCombination);
        classCombinationsByYear = new HashMap<>();
        classCombinationsByYear.put(annualClassCombination.getYear(), annualClassCombination);
    }

    public int getCombinationId() {
        return combinationId;
    }

    public void setCombinationId(int combinationId) {
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
        this.classCombinationsByYear = classCombinationsByYear;
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
