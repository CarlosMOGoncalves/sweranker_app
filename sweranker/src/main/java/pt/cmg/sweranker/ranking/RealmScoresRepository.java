package pt.cmg.sweranker.ranking;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;


public class RealmScoresRepository implements ScoresRepository {

    private Map<Long, Realm> _databaseInstances;

    public RealmScoresRepository() {
        _databaseInstances = new HashMap<>();
    }


    @Override
    public void open() {
        _databaseInstances.put(Thread.currentThread().getId(), Realm.getDefaultInstance());
    }

    @Override
    public void close() {
        _databaseInstances.get(Thread.currentThread().getId()).close();
        _databaseInstances.remove(Thread.currentThread().getId());
    }


    private Realm getRealmnstanceOfThread() {
        return _databaseInstances.get(Thread.currentThread().getId());
    }

    /**
     * Saves a really long list of objects in smaller batches to Realm.
     * This is needed because Realm sucks so much at inserting in bulk so a batch strategy had to be developed.
     * I hope these guys fix this... it is really a bottleneck.
     * <p>
     * NOTE: this function WILL CONSUME the parameter list. You have been warned.
     *
     * @param objects
     */
    @Override
    public void saveObjects(List<?> objects) {

        open();

        int batchSize = 10000;

        ListIterator<? extends RealmObject> iterator = ((List<RealmObject>) objects).listIterator(objects.size());
        List<RealmObject> batch = new ArrayList<>(batchSize);

        while (iterator.hasPrevious()) {

            // Add 10000 to batch
            for (int i = 0; i < batchSize && iterator.hasPrevious(); i++) {
                batch.add(iterator.previous());
                iterator.set(null);
            }

            getRealmnstanceOfThread().executeTransaction(realm -> realm.insertOrUpdate(batch));

            batch.clear();

            // This part is just a small sleep to give some time to trigger the GC and clean the mess it is creating.
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        close();

    }

    @Override
    public List<AnnualClassCombination> getAnnualCombinationsOfDegree(int degreeId) {
        return getRealmnstanceOfThread().where(AnnualClassCombination.class)
                .equalTo("degreeId", degreeId)
                .findAll();
    }

    @Override
    public long getScoreCount() {
        return getRealmnstanceOfThread().where(SweScore.class)
                .equalTo(SweScoreFields.SCORE_TYPE, SweScore.TYPE_DEGREE_SCORE)
                .count();
    }

    @Override
    public List<SweScore> getAllScores() {
        return getRealmnstanceOfThread().where(SweScore.class)
                .equalTo(SweScoreFields.SCORE_TYPE, SweScore.TYPE_DEGREE_SCORE)
                .findAll();
    }

    @Override
    public List<SweScore> getClassScoresOfDegree(int degreeId) {
        return getRealmnstanceOfThread().where(SweScore.class)
                .equalTo("scoreType", SweScore.TYPE_CLASS_SCORE)
                .equalTo("degreeId", (byte) degreeId)
                .findAll();
    }

    @Override
    public void insertOrUpdateObjectsInTransaction(List<?> objects) {
        getRealmnstanceOfThread().executeTransaction(r -> r.insertOrUpdate((List<RealmObject>) objects));
        Log.i("SweRanker:Database", "Saved or updated " + objects.size() + " objects of type " + objects.get(0).getClass().getSimpleName());
    }

    @Override
    public List<SweScore> getAnnualScoresOfDegree(int degreeId) {
        return getRealmnstanceOfThread().where(SweScore.class)
                .equalTo("scoreType", SweScore.TYPE_ANNUAL_SCORE)
                .equalTo("degreeId", degreeId)
                .findAll();
    }

    @Override
    public List<SweScore> getScoresOfDegree(int degreeId) {
        return getRealmnstanceOfThread().where(SweScore.class)
                .equalTo(SweScoreFields.SCORE_TYPE, SweScore.TYPE_DEGREE_SCORE)
                .equalTo(SweScoreFields.DEGREE_ID, degreeId)
                .findAll();
    }

    @Override
    public List<SweScore> getScoresOfDegreeOrderedBy(int degreeId, String orderedFieldName, Sort order) {
        return getRealmnstanceOfThread().where(SweScore.class)
                .equalTo(SweScoreFields.SCORE_TYPE, SweScore.TYPE_DEGREE_SCORE)
                .equalTo(SweScoreFields.DEGREE_ID, degreeId)
                .findAll().sort(orderedFieldName, io.realm.Sort.valueOf(order.name()));
    }

    @Override
    public int countAllCombinationsOfDegree(int degreeId) {
        return getRealmnstanceOfThread().where(DegreeClassCombination.class)
                .equalTo("degreeId", degreeId)
                .findAll()
                .size();
    }


    @Override
    public List<DegreeClassCombination> getAllCombinationsOfDegree(int degreeId) {
        return getRealmnstanceOfThread().where(DegreeClassCombination.class)
                .equalTo("degreeId", degreeId)
                .findAll();
    }

    @Override
    public SweScore getScore(String degreeCombinationId) {
        SweScore score = getRealmnstanceOfThread().where(SweScore.class)
                .equalTo(SweScoreFields.ID, degreeCombinationId)
                .findFirst();

        return getRealmnstanceOfThread().copyFromRealm(score);
    }

    @Override
    public List<SweScore> getScores(Collection<String> scoreIds) {
        List<SweScore> scores = getRealmnstanceOfThread().where(SweScore.class)
                .in(SweScoreFields.ID, scoreIds.toArray(new String[scoreIds.size()]))
                .findAll();

        return getRealmnstanceOfThread().copyFromRealm(scores);
    }

    @Override
    public DegreeClassCombination getDegreeClassCombination(String degreeCombinationId) {
        DegreeClassCombination degreeClassCombination = getRealmnstanceOfThread()
                .where(DegreeClassCombination.class)
                .equalTo(DegreeClassCombinationFields.COMBINATION_ID, degreeCombinationId)
                .findFirst();
        return getRealmnstanceOfThread().copyFromRealm(degreeClassCombination);
    }


    @Override
    public AnnualClassCombination getAnnualClassCombination(String annualClassCombinationId) {
        AnnualClassCombination annualClassCombination = getRealmnstanceOfThread()
                .where(AnnualClassCombination.class)
                .equalTo(AnnualClassCombinationFields.ANNUAL_COMBINATION_ID, annualClassCombinationId)
                .findFirst();
        return getRealmnstanceOfThread().copyFromRealm(annualClassCombination);
    }


}