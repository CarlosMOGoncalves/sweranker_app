package pt.cmg.sweranker.ranking;

import java.util.Collection;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * An AnnualClassCombination is little more than a List of Degree Class Ids.
 * These are the different classes that a person can take in a given Year of a degree.
 * The system will have a lot of these, all different, to attest for the different ways one student
 * can complete a given year in a given degree.
 */
public class AnnualClassCombination extends RealmObject {

    @PrimaryKey
    private String annualCombinationId;

    private int year;

    private int degreeId;

    private RealmList<DegreeClassId> degreeClassIds;

    public AnnualClassCombination() {
        this.annualCombinationId = "";
        year = 0;
        degreeClassIds = new RealmList<>();
        degreeId = 0;
    }

    public AnnualClassCombination(String annualCombinationId) {
        this.annualCombinationId = annualCombinationId;
        year = 0;
        degreeClassIds = new RealmList<>();
        degreeId = 0;
    }

    public String getId() {
        return annualCombinationId;
    }

    public void setId(String annualCombinationId) {
        this.annualCombinationId = annualCombinationId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public RealmList<DegreeClassId> getDegreeClassIds() {
        return degreeClassIds;
    }

    public void setDegreeClasses(RealmList<DegreeClassId> degreeClassIds) {
        this.degreeClassIds = degreeClassIds;
    }

    public void addDegreeClasses(RealmList<DegreeClassId> degreeClassIds) {
        this.degreeClassIds.addAll(degreeClassIds);
    }

    public void addDegreeClasses(Collection<String> degreeClassIds) {
        RealmList<DegreeClassId> classIds = new RealmList<>();

        for (String id : degreeClassIds) {
            classIds.add(new DegreeClassId(id));
        }

        this.degreeClassIds.addAll(classIds);
    }

    public void addDegreeClass(String degreeClassId) {
        degreeClassIds.add(new DegreeClassId(degreeClassId));
    }

    public void addDegreeClass(DegreeClassId degreeClassId) {
        degreeClassIds.add(degreeClassId);
    }

    public int getDegreeId() {
        return degreeId;
    }

    public void setDegreeId(int degreeId) {
        this.degreeId = degreeId;
    }
}
