package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;

/**
 * Created by Carlos on 11/04/2017.
 */

public class KAPercent extends RealmObject {

    private int kaId;
    private double kaPercent;

    public KAPercent() {
        this.kaId = 0;
        this.kaPercent = 0.0;
    }

    public KAPercent(int kaId, double kaPercent) {
        this.kaId = kaId;
        this.kaPercent = kaPercent;
    }

    public int getKaId() {
        return kaId;
    }

    public void setKaId(int kaId) {
        this.kaId = kaId;
    }

    public double getKaPercent() {
        return kaPercent;
    }

    public void setKaPercent(double kaPercent) {
        this.kaPercent = kaPercent;
    }
}
