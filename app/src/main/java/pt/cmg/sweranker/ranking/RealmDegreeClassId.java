package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Realm-ready class to wrap a String... pathetic.
 */
public class RealmDegreeClassId extends RealmObject {

    @PrimaryKey
    private String degreeClassId;

    public RealmDegreeClassId() {
        degreeClassId = "";
    }

    public RealmDegreeClassId(String degreeClassId) {
        this.degreeClassId = degreeClassId;
    }

    public String getDegreeClassId() {
        return degreeClassId;
    }

    public void setDegreeClassId(String degreeClassId) {
        this.degreeClassId = degreeClassId;
    }
}
