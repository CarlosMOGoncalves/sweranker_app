package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

/**
 * Created by Carlos on 11/04/2017.
 */

public class KAPercent extends RealmObject {

    private int kaId;
    private double kaPercent;

    @LinkingObjects("kaPercents")
    private final RealmResults<SweScore> score = null;

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

    public SweScore getScore() {
        return score.first();
    }


}
