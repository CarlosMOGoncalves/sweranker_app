package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Realm-ready class to wrap a String... pathetic.
 */
public class DegreeClassId extends RealmObject {

    @PrimaryKey
    private String degreeClassId;

    public DegreeClassId() {
        degreeClassId = "";
    }

    public DegreeClassId(String degreeClassId) {
        this.degreeClassId = degreeClassId;
    }

    public String getDegreeClassId() {
        return degreeClassId;
    }

    public void setDegreeClassId(String degreeClassId) {
        this.degreeClassId = degreeClassId;
    }
}
