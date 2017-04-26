package pt.cmg.sweranker.ranking;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

/**
 * Created by Carlos on 11/04/2017.
 */

public class KAPercent extends RealmObject {

    private byte kaId;
    private float kaPercent;

    @LinkingObjects("kaPercents")
    private final RealmResults<SweScore> score = null;

    public KAPercent() {
        this.kaId = 0;
        this.kaPercent = 0.0f;
    }

    public KAPercent(byte kaId, float kaPercent) {
        this.kaId = kaId;
        this.kaPercent = kaPercent;
    }

    public byte getKaId() {
        return kaId;
    }

    public void setKaId(byte kaId) {
        this.kaId = kaId;
    }

    public float getKaPercent() {
        return kaPercent;
    }

    public void setKaPercent(float kaPercent) {
        this.kaPercent = kaPercent;
    }

    public SweScore getScore() {
        return score.first();
    }


}
