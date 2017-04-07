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

public class RealmDegreeCombination extends RealmObject {

    private static final int DEFAULT_ID = -1;

    @PrimaryKey
    private int combinationId;

    private int degreeId;

    private RealmList<RealmAnnualCombination> classCombinations;

    @Ignore
    private Map<Integer, RealmAnnualCombination> classCombinationsByYear;


    public RealmDegreeCombination() {
        combinationId = DEFAULT_ID;
        classCombinations = new RealmList<>();
    }


    public RealmDegreeCombination(int combinationId) {
        this.combinationId = combinationId;
        classCombinations = new RealmList<>();
    }

    public RealmDegreeCombination(RealmAnnualCombination annualClassCombination) {
        combinationId = DEFAULT_ID;
        classCombinations = new RealmList<>();
        classCombinations.add(annualClassCombination);
        classCombinationsByYear = new HashMap<>();
        classCombinationsByYear.put(annualClassCombination.getYear(), annualClassCombination);
    }

    public int getCombinationId() {
        return combinationId;
    }

    public void setCombinationId(int combinationId) {
        this.combinationId = combinationId;
    }

    public RealmList<RealmAnnualCombination> getClassCombinations() {
        return classCombinations;
    }

    public void setClassCombinations(RealmList<RealmAnnualCombination> classCombinations) {
        this.classCombinations = classCombinations;

    }

    public Map<Integer, RealmAnnualCombination> getClassCombinationsByYear() {

        if (classCombinationsByYear.isEmpty()) {
            for (RealmAnnualCombination annualCombination : classCombinations) {
                classCombinationsByYear.put(annualCombination.getYear(), annualCombination);
            }
        }
        return classCombinationsByYear;
    }

    public void addAnnualClassCombination(RealmAnnualCombination newAnnualClassCombination) {
        classCombinations.add(newAnnualClassCombination);
        classCombinationsByYear.put(newAnnualClassCombination.getYear(), newAnnualClassCombination);
    }

    public void setClassCombinationsByYear(Map<Integer, RealmAnnualCombination> classCombinationsByYear) {
        this.classCombinationsByYear = classCombinationsByYear;
        for (RealmAnnualCombination annualCombo : classCombinationsByYear.values()) {
            classCombinations.add(annualCombo);
        }
    }

    public RealmAnnualCombination getAnnualCombination(int year) {
        return classCombinationsByYear.get(year);
    }

}
