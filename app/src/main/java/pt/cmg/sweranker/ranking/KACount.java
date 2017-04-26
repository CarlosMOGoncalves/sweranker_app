package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;

/**
 * Created by Carlos on 11/04/2017.
 */

public class KACount extends RealmObject {

    private byte kaId;
    private short kaCount;

    public KACount() {
        this.kaCount = 0;
        this.kaId = 0;
    }

    public KACount(byte kaId) {
        this.kaId = kaId;
        this.kaCount = 1;
    }

    public KACount(byte kaId, short kaCount) {
        this.kaId = kaId;
        this.kaCount = kaCount;
    }

    public byte getKaId() {
        return kaId;
    }

    public void setKaId(byte kaId) {
        this.kaId = kaId;
    }

    public short getKaCount() {
        return kaCount;
    }

    public void setKaCount(short kaCount) {
        this.kaCount = kaCount;
    }

    public void incrementCounter(short increment) {
        kaCount += increment;
    }

}
