package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;

/**
 * Created by Carlos on 11/04/2017.
 */

public class KACount extends RealmObject {

    private int kaId;
    private int kaCount;

    public KACount() {
        this.kaCount = 0;
        this.kaId = 0;
    }

    public KACount(int kaId) {
        this.kaId = kaId;
        this.kaCount = 1;
    }

    public KACount(int kaId, int kaCount) {
        this.kaId = kaId;
        this.kaCount = kaCount;
    }

    public int getKaId() {
        return kaId;
    }

    public void setKaId(int kaId) {
        this.kaId = kaId;
    }

    public int getKaCount() {
        return kaCount;
    }

    public void setKaCount(int kaCount) {
        this.kaCount = kaCount;
    }

    public void incrementCounter(int increment) {
        kaCount += increment;
    }

}
