package pt.cmg.sweranker.ranking;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 05/04/2017.
 */

public class RealmAnnualCombination extends RealmObject {

    @PrimaryKey
    private String annualCombinationId;

    private int year;

    private RealmList<RealmDegreeClassId> degreeClassIds;

    public RealmAnnualCombination() {
        this.annualCombinationId = "";
        year = 0;
        degreeClassIds = new RealmList<>();
    }

    public RealmAnnualCombination(String annualCombinationId) {
        this.annualCombinationId = annualCombinationId;
        year = 0;
        degreeClassIds = new RealmList<>();
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

    public RealmList<RealmDegreeClassId> getDegreeClassIds() {
        return degreeClassIds;
    }

    public void setDegreeClasses(RealmList<RealmDegreeClassId> degreeClassIds) {
        this.degreeClassIds = degreeClassIds;
    }

    public void addDegreeClasses(RealmList<RealmDegreeClassId> degreeClassIds) {
        this.degreeClassIds.addAll(degreeClassIds);
    }

    public void addDegreeClasses(List<String> degreeClassIds) {
        RealmList<RealmDegreeClassId> classIds = new RealmList<>();

        for (String id : degreeClassIds) {
            classIds.add(new RealmDegreeClassId(id));
        }

        this.degreeClassIds.addAll(classIds);
    }

    public void addDegreeClass(String degreeClassId) {
        degreeClassIds.add(new RealmDegreeClassId(degreeClassId));
    }

    public void addDegreeClass(RealmDegreeClassId degreeClassId) {
        degreeClassIds.add(degreeClassId);
    }
}
