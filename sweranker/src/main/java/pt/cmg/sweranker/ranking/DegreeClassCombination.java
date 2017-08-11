package pt.cmg.sweranker.ranking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * A DegreeClassCombination is a data class that store all the different Degree Classes from all the
 * years that compose a unique Degree Combination.
 * <p>
 * It is composed by its unique ID, the Degree ID that this combination belongs to and a List of
 * Annual Class Combinations, which is in itself another list of Degree Classes
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

    /**
     * Returns a Map where entries are the years and the values are the Annual Combination that match
     * those years.
     * <p>
     * In a typical Degree this should be a collection with around 5 entries, one annual combination
     * for each.
     */
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
