package pt.cmg.sweranker.degrees;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import pt.cmg.sweranker.MainActivity;
import pt.cmg.sweranker.MainActivityViewModel;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ranking.AnnualClassCombination;
import pt.cmg.sweranker.ranking.CalculationUtils;
import pt.cmg.sweranker.ranking.DegreeClassCombination;
import pt.cmg.sweranker.ranking.DegreeClassId;
import pt.cmg.sweranker.ranking.SweScore;
import pt.cmg.sweranker.ranking.combinationstrategies.ClassCombinationStrategy;

/**
 * Created by Carlos on 25/01/2017.
 */

public class DegreeDetailsFragment extends Fragment implements LifecycleRegistryOwner {


    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    private static final String DEGREE_ID = "degree_id";

    private View _myView;

    private int _degreeId;
    private Degree _degree;

    private String _degreeCombinationIdBase;
    private int _combinationIdCounter;
    private int _totalPossibleCombinations = 1;

    private MainActivityViewModel _sharedViewModel;

    private DegreeDetailsFragmentInteractionListener _parentActivity;

    public DegreeDetailsFragment() {

    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DegreeDetailsFragment.
     */
    public static DegreeDetailsFragment newInstance() {
        return new DegreeDetailsFragment();
    }


    /**
     * Communication Interface used to communicate between this fragment and its parent Activity.
     * <p>
     * Write here any method needed to trigger in the Activity
     */
    public interface DegreeDetailsFragmentInteractionListener {
        /**
         * Loads the class fragment whose item was chosen.
         */
        void loadDegreeClassFragment(View selectedView);
    }

    @Override
    public void onAttach(Context parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreeDetailsFragmentInteractionListener) {
            _parentActivity = (DegreeDetailsFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreeDetailsFragmentInteractionListener");
        }
        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);
        _degree = _sharedViewModel.getSelectedDegree();
        _degreeId = _degree.getId();
    }

    @Override
    public void onAttach(Activity parentActivity) {
        super.onAttach(parentActivity);
        if (parentActivity instanceof DegreeDetailsFragmentInteractionListener) {
            _parentActivity = (DegreeDetailsFragmentInteractionListener) parentActivity;
        } else {
            throw new RuntimeException(parentActivity.toString() + " must implement DegreeDetailsFragmentInteractionListener");
        }
        _sharedViewModel = ViewModelProviders.of((MainActivity) getActivity()).get(MainActivityViewModel.class);
        _degree = _sharedViewModel.getSelectedDegree();
        _degreeId = _degree.getId();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.app_bar_degree_calculate_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_calculate:
                createAndShowFilterDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Creates and displays the Calculation Prompt Dialog.
     * This is a very very important functionality that launches a load of background processing
     * that will crunch the matching data and calculate a score for each degree class, then
     * for each yearly combination and finally for every possible degree combination.
     * <p>
     * TODO: a way to inform the user that processing is taking place, namely using notifications.
     */
    private void createAndShowFilterDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());


        dialogBuilder
                .setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.calculation_dialog_message))
                .setPositiveButton(getResources().getString(R.string.proceed), (dialog, which) -> {
                    // The positive button launches a calculation
                    _sharedViewModel.calculateDegreeScores(_degree);
                    dialog.cancel();

                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, id) ->
                        dialog.cancel()
                );

        dialogBuilder.create().show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _myView = inflater.inflate(R.layout.degree_details_fragment, container, false);

        ImageView degreeImage = (ImageView) _myView.findViewById(R.id.degree_image);
        TextView universityName = (TextView) _myView.findViewById(R.id.university_name);
        TextView degreeName = (TextView) _myView.findViewById(R.id.degree_name);
        TextView isDegreeEvaluated = (TextView) _myView.findViewById(R.id.evaluated_status);

        degreeImage.setImageDrawable(this.getResources().getDrawable(_degree.getImageResource(), null));
        universityName.setText(this.getResources().getText(_degree.getUniversityResource()));
        degreeName.setText(this.getResources().getText(_degree.getFullNameResource()));

        if (_sharedViewModel.isDegreeMatched(_degreeId)) {
            isDegreeEvaluated.setText(R.string.matched);
            isDegreeEvaluated.setTextColor((this.getResources().getColor(R.color.materialAffirmative)));
        } else {
            isDegreeEvaluated.setText(R.string.notMatched);
            isDegreeEvaluated.setTextColor((this.getResources().getColor(R.color.materialNegative)));
        }

        ViewPager viewPager = (ViewPager) _myView.findViewById(R.id.degree_viewPager);
        viewPager.setAdapter(new DegreeViewPagerAdapter(this.getActivity(), _degree, (view, degreeClass) -> {
            _sharedViewModel.setSelectedDegreeClass(degreeClass);
            _parentActivity.loadDegreeClassFragment(view);
        }));

        TabLayout tabLayout = (TabLayout) _myView.findViewById(R.id.degree_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return _myView;
    }


    /**
     * This is a background thread whose purpose is to calculate and save all the degree combinations and its scores.
     * It is heavy stuff although I am still debating if I need it to be here (which I don't think I do).
     */
    private class CombinationCalculator extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {

            _degreeCombinationIdBase = "d" + _degreeId + "c";
            _combinationIdCounter = 1;

            List<AnnualClassCombination> annualCombinations = calculateAnnualCombinations(_degree);

            saveObjectsByBatch(annualCombinations);

            calculateAndSaveAnnualScores();

            calculateAndSaveDegreeClassCombinations(_degree);

            calculateAndSaveDegreeScores();


            return null;

        }


        /**
         * This will calculate all the possible annual combinations for a given degree.
         * After executing this will return a list that is composed by a set of degree classes
         * that is possible to take in a given year and it calculates the combinations for each
         * year by using a calculation strategy that must be defined by each degree for each year.
         *
         * @param degree
         * @return
         */
        private List<AnnualClassCombination> calculateAnnualCombinations(Degree degree) {
            List<AnnualClassCombination> annualCombinations = new ArrayList<>();

            for (Map.Entry<Integer, ClassCombinationStrategy> classCombinationStrategy : degree.getClassCombinationStrategies().entrySet()) {

                Integer yearOfDegree = classCombinationStrategy.getKey();
                ClassCombinationStrategy combinationStrategy = classCombinationStrategy.getValue();

                // Here, using each year's strategy to unfold all possible combinations for this year
                annualCombinations.addAll(combinationStrategy.getAnnualClassCombinations(degree.getClasses().get(yearOfDegree)));

            }

            // Last pass to set the degree id
            for (AnnualClassCombination annualCombination : annualCombinations) {
                annualCombination.setDegreeId(_degreeId);
            }

            Log.i("Calculation", "Found " + annualCombinations.size() + " annual combinations for degree " + _degreeId);

            return annualCombinations;
        }

        /**
         * This does a lot. It calculates the scores for each annual combination of this degree by aggregating the scores
         * of all the classes that compose the annual combination.
         * <p>
         * Then saves each calculation on a batch level because Realm is seriously pathetic at inserting data in bulk.
         */
        private void calculateAndSaveAnnualScores() {

            int batchsize = 10000;

            Realm databaseConnection = Realm.getDefaultInstance();

            // First get the saved annual combinations for this degree
            List<AnnualClassCombination> savedAnnualClassCombinations = databaseConnection.where(AnnualClassCombination.class)
                    .equalTo("degreeId", _degreeId)
                    .findAll();


            // Then get the saved Scores for the classes of this degree
            List<SweScore> individualClassScores = databaseConnection.where(SweScore.class)
                    .equalTo("scoreType", SweScore.TYPE_CLASS_SCORE)
                    .equalTo("degreeId", (byte) _degreeId)
                    .findAll();


            // Now turn it into something easier to work with. As a Map I won't have to traverse the list whenever I want a value.
            // Keys -> the score ID which here is also the degree class id , Values -> the actual score
            Map<String, SweScore> scoresByDegreeClassId = new HashMap<>(individualClassScores.size());
            for (SweScore score : individualClassScores) {
                scoresByDegreeClassId.put(score.getId(), score);
            }


            // I am saving in batches to alleviate memory
            List<SweScore> batch = new ArrayList<>(batchsize);

            Iterator<AnnualClassCombination> iterator = savedAnnualClassCombinations.iterator();
            while (iterator.hasNext()) {

                for (int i = 0; i < batchsize && iterator.hasNext(); i++) {

                    AnnualClassCombination currentAnnualCombination = iterator.next();
                    // First fetch the class ids of the annual combo
                    RealmList<DegreeClassId> degreeClassIds = currentAnnualCombination.getDegreeClassIds();

                    // Then gets their individual that was fetched up there
                    List<SweScore> degreeClassScore = new ArrayList<>();
                    for (DegreeClassId degreeClassId : degreeClassIds) {
                        degreeClassScore.add(scoresByDegreeClassId.get(degreeClassId.getDegreeClassId()));
                    }

                    // And now calculate its combined score
                    SweScore calculatedScore = CalculationUtils.calculateAccumulatedScore(degreeClassScore);
                    calculatedScore.setDegreeId(_degreeId);
                    calculatedScore.setScoreType(SweScore.TYPE_ANNUAL_SCORE);
                    calculatedScore.setId(currentAnnualCombination.getId());

                    batch.add(calculatedScore);

                }

                databaseConnection.executeTransaction(r -> r.insertOrUpdate(batch));
                batch.clear();

                // This part is just a small sleep to give some time to trigger the GC and clean the mess it is creating.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

            databaseConnection.close();

        }


        /**
         * This function will load all the annual combinations for this degree and then will calculate all the possible combinations
         * between those throughout the years that compose this degree.
         * <p>
         * As this are combinations that means the final number of possible ways to complete a degree are calculated as
         * AnnualCombinationsOfYear1 x AnnualCombinationsOfYear2 x AnnualCombinationsOfYear3 x ...
         * <p>
         * Since this is usually a big number a recursive algorithm was used to save memory. Furthermore, everytime a complete batch
         * of X combinations was found they were immediately saved to the Realm database freeing up the memory ASAP.
         * <p>
         *
         * @param degree
         */
        private void calculateAndSaveDegreeClassCombinations(Degree degree) {

            Realm databaseConnection = Realm.getDefaultInstance();

            List<AnnualClassCombination> allAnnualCombinations = databaseConnection.where(AnnualClassCombination.class)
                    .equalTo("degreeId", _degreeId)
                    .findAll();

            // This part just transforms the List into something easier to access, namely a Map where Keys -> Year , Values -> all annual combinations for that particular year
            // This step was unnecessary as a good stream could aggregate them for me, but for one I can't use streams in this API and also this was a legacy bit of code that
            // was actually useful anyway
            Map<Integer, List<AnnualClassCombination>> combinationsByYear = new HashMap<>();

            for (AnnualClassCombination annualCombo : allAnnualCombinations) {
                Integer comboYear = annualCombo.getYear();

                if (combinationsByYear.containsKey(comboYear)) {
                    combinationsByYear.get(comboYear).add(annualCombo);
                } else {
                    List<AnnualClassCombination> annualComboList = new ArrayList<>();
                    annualComboList.add(annualCombo);
                    combinationsByYear.put(comboYear, annualComboList);
                }
            }

            // Now I for a List of Lists, where each element (A List<AnnualCombination> are the possible combinations for a given year.
            List<List<AnnualClassCombination>> combinationPool = new ArrayList<>();
            for (int i = 1; i <= combinationsByYear.size(); i++) {
                combinationPool.add(combinationsByYear.get(i));
                _totalPossibleCombinations *= combinationsByYear.get(i).size();
            }


            // Logging
            long startTime = System.currentTimeMillis();
            Log.i("Calculation", "Started the calculation of Degree combinations.");

            // Now it is the recursive call, this one is very very tricky so I will explain it in the comments
            generateAndSaveRecursively(databaseConnection, combinationPool, new ArrayList<>(), 0, new ArrayList<>());

            // Logging
            long endtime = System.currentTimeMillis();
            Log.i("Calculation", "Ended at the calculation of Degree combinations.");
            Log.i("Calculation", "Found " + _combinationIdCounter + " degree combinations!");
            Log.i("Calculation", "Took " + ((endtime - startTime) / 1000) + " seconds to calculate and save combinations");


            databaseConnection.close();
        }

        /**
         * Recursive function that will calculate all the combinations for a Degree based on the Annual Combinations it has.
         * Every time a certain number of combinations is calculated I will save them immediately to Realm so that I can free
         * memory for another batch and no massive amount of memory is spent when the full combinations collection is completely calculated.
         * <p>
         * This is hard to get it, thanks Internet.<br/>
         * Each time is enters this part it means that I am getting an element on a further year.<br/>
         * To better explain: let's imagine 3 lists<br/>
         * List 1 = [A,B] , List 2 = [D,E] and List 3 = [F,G]<br/><br/>
         * These lists are put into a List of Lists like this ListFinal = [[A,B].[C,D].[E,F]]<br/><br/>
         * 1) This starts by getting [A], then another iteration it gets [A,C] and then another iteration it get [A,C,E]<br/>
         * 2) Now it matches the stopping condition (depth = 3 = ListFinal.size())<br/>
         * 3) So it enters the stop condition part and collects the result [A,C,E] and then returns<br/>
         * 4) And returns to the recursive call which is still on [A,C] but now incremented in i = 1, so it collects [A,C,F]<br/>
         * 5) Again it reaches stop condition and returns<br/>
         * 6) But now it also completes the recursive condition because it has reached its limit on the last list [E,F], so it jumps to the previous recursive call
         * that is still on [A]. So it now gets it i = 1 and collects D, so it has [A,D]<br/>
         * 7) It does another recursive iteration and gets [A,D,E], and in the next it gets [A,D,F]<br/>
         * 8) And it goes like this until it has passed all the elements<br/>
         *
         * @param combinationPool
         * @param result
         * @param depth
         * @param current
         */
        private void generateAndSaveRecursively(Realm databaseConnection,
                                                List<List<AnnualClassCombination>> combinationPool,
                                                List<DegreeClassCombination> result,
                                                int depth,
                                                List<AnnualClassCombination> current) {

            // This is the stopping condition.
            // It stops the recursive call when we have reached a depth (= max number of years)
            // which in turn means we already have a new list of annual combinations ready (i.e. a new Degree Class Combination).
            if (depth == combinationPool.size()) {

                // There it is, the new one. One must calculate its id
                DegreeClassCombination newCombination = new DegreeClassCombination(_degreeId, _degreeCombinationIdBase + _combinationIdCounter);
                newCombination.setAnnualClassCombinations(current);
                result.add(newCombination);

                // Now if this object has reached the batch size OR it is the last bunch I will save it and empty it (to save memory).
                if ((_combinationIdCounter == _totalPossibleCombinations) || result.size() == 10000) {

                    long sMillis = System.currentTimeMillis();
                    databaseConnection.executeTransaction(r -> r.insertOrUpdate(result));
                    long endMillis = System.currentTimeMillis();
                    Log.i("Calculation", "Saved a batch of " + result.size() + " combinations in " + ((endMillis - sMillis) / 1000) + " seconds");

                    result.clear();

                    // This part is just a small sleep to give some time to trigger the GC and clean the mess it is creating.
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // This is only a small escape so I won't increment the value of the combination counter at the end of calculation
                if (_combinationIdCounter != _totalPossibleCombinations) {
                    _combinationIdCounter++;
                }
                return;
            }

            // This is the recursive call.
            for (int i = 0; i < combinationPool.get(depth).size(); ++i) {
                List<AnnualClassCombination> currentAnnual = new ArrayList<>(current);
                currentAnnual.add(combinationPool.get(depth).get(i));
                generateAndSaveRecursively(databaseConnection, combinationPool, result, depth + 1, currentAnnual);
            }

        }


        /**
         * This one is tough.<br/>
         * This function does the heavy lifting job of calculating the SweScores for ALL the possible combinations
         * of this degree.<br/>
         * Naturally this is a hard work so I use some multi-threading to speed up things.<br/>
         * <p>
         * Whenever in the future I look back at this application THIS is where I spent at the very least one month, from March to April, 2017.
         * Here I realised that I needed a DB to save the calculations because it was too much to keep in memory so I spent at least one
         * week researching what to use. After I decided it would be Realm I took some more time to learn how the hell that worked and how
         * green it still was. Then, when I finally organised this stuff I understood that the way the objects were programmed was still too much
         * for the mobile to handle in a realistic time (first time it took nearly two hours and the phone battery died out).
         * So I took some more time refactoring the objects until I came up with an ugly ugly solution but that worked.
         * There it is, end of April 2017.
         * </p>
         * <p>
         * If, anytime in the future you come here with your new Google Pixel 5000 (or whatever shit they call it these days, I liked Nexus name better),
         * with your fancy 1TB of RAM, remember these old days when, with a Nexus 5X of 2Gb of RAM and 16Gb of disk size these calculations humiliated
         * that piece of hardware and forced the performance Engineer ou of you. Peace bro.
         * </p>
         */
        private void calculateAndSaveDegreeScores() {

            Realm databaseConnection = Realm.getDefaultInstance();

            List<SweScore> annualScores = databaseConnection.where(SweScore.class)
                    .equalTo("scoreType", SweScore.TYPE_ANNUAL_SCORE)
                    .equalTo("degreeId", _degreeId)
                    .findAll();


            // Now turn it into something easier to work with. As a Map I won't have to traverse the list whenever I want a value.
            // This map has Keys ->
            Map<String, SweScore> scoresByAnnualCombination = new HashMap<>(annualScores.size());
            for (SweScore score : annualScores) {
                SweScore copiedScore = new SweScore(score);
                scoresByAnnualCombination.put(copiedScore.getId(), copiedScore);
            }


            long startTime = System.currentTimeMillis();
            Log.i("Calculation", "Started calculation of degree scoring.");

            int allCombinationsCount = databaseConnection.where(DegreeClassCombination.class)
                    .equalTo("degreeId", _degreeId)
                    .findAll()
                    .size();

            // Funny this, AS SOON as an intance of Realm is not needed anymore, just close it
            // Previously this was on the end of the method. As a result the memory would explode with uncollected objects
            // when calling the below executor each with its own Realm instance.
            databaseConnection.close();

            // This is a long debate that I had with myself.
            // After several samples I reached the conclusion that a batch size of 7500 and two threads are the best option
            // This is because Realm can only save in one Thread at the same time so a lot of threads would be pointless.
            // Therefore I just use two so that whenever Realm is free to insert a new batch I already have the calculation ready.
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            int executionId = 1;
            int startPosition = 0;
            int batchSize = 7500;

            while (startPosition < allCombinationsCount) {

                // If by adding the default window surpasses the total size of the collection
                if (startPosition + batchSize >= allCombinationsCount) {
                    // Then the batch size will have the size of what is left, hence the difference here
                    batchSize = allCombinationsCount - startPosition;
                }

                executorService.submit(new DegreeScoreCalculator(executionId, scoresByAnnualCombination, startPosition, batchSize));
                executionId++;
                startPosition += batchSize;

            }

            // Now I just wait for the results. I am keeping it waiting, I am outside of the main thread anyway.
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long endTime = System.currentTimeMillis();
            Log.i("Calculation", "Ended calculation of degree scoring.");
            Log.i("Calculation", "Took " + ((endTime - startTime) / 1000) + " seconds");


        }

        /**
         * This class is used to calculate and save a batch of calculations from the same batch of combinations.
         * Since it works on a finite window it will never mess with other thread's work, which is nice.
         * This was inspired on my previous job when I worked at IB. Yuck...
         */
        private class DegreeScoreCalculator implements Runnable {

            private int _id;
            private Map<String, SweScore> _scoresByAnnualCombination;
            private int _startPosition;
            private int _endPosition;

            DegreeScoreCalculator(int id, Map<String, SweScore> scoresByAnnualCombination, int startPosition, int batchSize) {
                _id = id;
                _scoresByAnnualCombination = scoresByAnnualCombination;
                _startPosition = startPosition;
                _endPosition = startPosition + batchSize;
            }

            @Override
            public void run() {

                Realm database = Realm.getDefaultInstance();

                List<DegreeClassCombination> allCombinations = database.where(DegreeClassCombination.class)
                        .equalTo("degreeId", _degreeId)
                        .findAll();

                List<SweScore> results = new ArrayList<>();
                for (int i = _startPosition; i < _endPosition; i++) {

                    DegreeClassCombination currentDegreeCombination = allCombinations.get(i);
                    // First fetch annual combinations for this particular degree combination
                    RealmList<AnnualClassCombination> degreeAnnualCombinations = currentDegreeCombination.getAnnualClassCombinations();

                    // Then gets their individual score
                    List<SweScore> currentAnnualScores = new ArrayList<>();
                    for (AnnualClassCombination annualCombo : degreeAnnualCombinations) {
                        currentAnnualScores.add(_scoresByAnnualCombination.get(annualCombo.getId()));
                    }

                    // And now calculate its combined score
                    SweScore calculatedScore = CalculationUtils.calculateAccumulatedScore(currentAnnualScores);
                    calculatedScore.setDegreeId(_degreeId);
                    calculatedScore.setScoreType(SweScore.TYPE_DEGREE_SCORE);
                    calculatedScore.setId(currentDegreeCombination.getCombinationId());

                    results.add(calculatedScore);

                }
                database.executeTransaction(r -> r.insertOrUpdate(results));
                Log.i("Calculation", "Ended saving a batch of Degree Scores with id: " + _id);
                results.clear();

                database.close();
            }

        }


        /**
         * Saves a really long list of objects in smaller batches to Realm.
         * This is needed because Realm sucks so much at inserting in bulk so a batch strategy had to be developed.
         * I hope this guys fix this... it is really a bottleneck.
         * <p>
         * NOTE: this function WILL CONSUME the parameter list. You have been warned.
         *
         * @param objectsToSave
         * @param <E>
         */

        private <E extends RealmObject> void saveObjectsByBatch(List<E> objectsToSave) {
            int batchSize = 10000;

            Realm database = Realm.getDefaultInstance();

            ListIterator<E> iterator = objectsToSave.listIterator(objectsToSave.size());
            List<E> batch = new ArrayList<>(batchSize);

            while (iterator.hasPrevious()) {

                // Add 10000 to batch
                for (int i = 0; i < batchSize && iterator.hasPrevious(); i++) {
                    batch.add(iterator.previous());
                    iterator.set(null);
                }

                database.executeTransaction(realm -> realm.insertOrUpdate(batch));

                batch.clear();

                // This part is just a small sleep to give some time to trigger the GC and clean the mess it is creating.
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

            database.close();


        }


    }


}
