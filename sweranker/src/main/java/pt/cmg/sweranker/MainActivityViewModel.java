package pt.cmg.sweranker;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.realm.RealmList;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.degrees.DegreeMatchesRepository;
import pt.cmg.sweranker.degrees.DegreesRepository;
import pt.cmg.sweranker.ranking.AnnualClassCombination;
import pt.cmg.sweranker.ranking.CalculationUtils;
import pt.cmg.sweranker.ranking.DegreeClassCombination;
import pt.cmg.sweranker.ranking.DegreeClassId;
import pt.cmg.sweranker.ranking.ScoresRepository;
import pt.cmg.sweranker.ranking.SweScore;
import pt.cmg.sweranker.ranking.SweScoreFields;
import pt.cmg.sweranker.ranking.combinationstrategies.ClassCombinationStrategy;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;
import pt.cmg.sweranker.swebok.SwebokRepository;


public class MainActivityViewModel extends ViewModel {

    private LiveData<List<KnowledgeArea>> _knowledgeAreas = new MutableLiveData<>();
    private List<KnowledgeAreaTopic> _knowledgeAreaTopics = new ArrayList<>();

    /**
     * A different view of the Knowledge Area Topics data.
     * Keys -> Knowledge Area Topic Id , Values -> the actual Knowledge Area Topic
     */
    private Map<Integer, KnowledgeAreaTopic> _knowledgeAreaTopicsByTopicId;


    private LiveData<List<Degree>> _degrees = new MutableLiveData<>();

    /**
     * A different view of the degrees data.
     * Keys -> Degree Id , Values -> the actual Degree
     */
    private Map<Integer, Degree> _degreesById;

    /**
     * A custom view of the Degree Classes using its id as the key.
     * Keys -> Degree Class Id , Values -> the matching Degree Class
     */
    private Map<String, DegreeClass> _degreeClassesById;

    /**
     * Keys -> Degree Class Id , Values -> its current matches
     */
    private LiveData<Map<String, DegreeClassMatch>> _degreeClassMatches = new MutableLiveData<>();

    /**
     * Keys -> Degree Id , Values -> true if completely matched, false otherwise
     */
    private Map<Integer, Boolean> _matchedDegrees;

    /**
     * Keys -> Degree Id , Values -> true if there is at least one degree combination in the system, false otherwise
     */
    private Map<Integer, Boolean> _calculatedDegrees;

    /**
     * Very important, this are all the scores of all the different Degree Classes.
     * Although normally persisted, I will load them beforehand to be used as the dictionary
     * to calculated every other combination.
     * <p>
     * Keys -> the Degree Class Id ,  Values -> its matching score.
     */
    private Map<String, SweScore> _degreeClassScores;

    /**
     * Keys -> Degree combination name , Values -> a Degree image.
     */
    private MutableLiveData<LinkedHashMap<String, Integer>> _scoreImages = new MutableLiveData<>();

    private MutableLiveData<Boolean> _isLoaded = new MutableLiveData<>();

    /**
     * Used in inter-fragment communication between SwebokMasterFragment and SwebokDetailedFragment
     */
    private KnowledgeArea _selectedKnowledgeArea;

    /**
     * Used in inter-fragment communication between DegreeMasterFragment and DegreeDetailedFragment
     */
    private Degree _selectedDegree;

    /**
     * Used in inter-fragment communication between DegreeDetailedFragment and DegreeClassFragment
     */
    private DegreeClass _selectedDegreeClass;

    /**
     * Used in inter-fragment communication between ScoresMasterFragment and ScoreDetailedChartFragment
     * This will be used to load data about this particular combination.
     */
    private String _selectedDegreeCombinationId;

    /**
     * Used in inter-fragment communication between ScoresMasterFragment and MultiScoreDetailedChartFragment
     * This will be used in when comparing two degree combinations against each other
     */
    private List<String> _selectedDegreeCombinationsToCompare;

    private SwebokRepository _swebokRepository;
    private DegreesRepository _degreesRepository;
    private DegreeMatchesRepository _matchesRepository;
    private ScoresRepository _scoresRepository;

    @Inject
    public MainActivityViewModel(SwebokRepository repository, DegreesRepository degreesRepository, DegreeMatchesRepository matchesRepository, ScoresRepository scoresRepository) {
        _swebokRepository = repository;
        _degreesRepository = degreesRepository;
        _matchesRepository = matchesRepository;
        _scoresRepository = scoresRepository;
    }


    private static final LifecycleOwner ALWAYS_ON = new LifecycleOwner() {

        private LifecycleRegistry mRegistry = init();

        private LifecycleRegistry init() {
            LifecycleRegistry registry = new LifecycleRegistry(this);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START);
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
            return registry;
        }

        @Override
        public Lifecycle getLifecycle() {
            return mRegistry;
        }
    };

    /**
     * Loads all the baseline data needed to operate the application.
     * Will load all the variables in their right order, although these will be loaded in
     * background threads so that it won't slow the startup time.
     */
    public void init() {
        // This here: whenever the KAs are loaded, a chain of the remaining data is loaded as well
        // in the order they are needed. This is because most of this data is dependant on the other
        // and they are expected to be loaded asynchronously so each depends on the correctly loading of the one before.
        // Furthermore, once a variable has been loaded once it listens to further changes so that it can be reloaded if needed.
        _knowledgeAreas = _swebokRepository.loadKnowledgeAreas();
        _knowledgeAreas.observe(ALWAYS_ON, knowledgeAreas -> {
            _knowledgeAreaTopics = createKnowledgeAreaTopicsView(knowledgeAreas);
            _knowledgeAreaTopicsByTopicId = createKaTopicByKaIdView(knowledgeAreas);

            _degrees = _degreesRepository.loadDegrees();
            _degrees.observe(ALWAYS_ON, degrees -> {
                _degreesById = createDegreesByIdView(degrees);
                _degreeClassesById = createDegreeClassByIdView(degrees);
                _degreeClassMatches = _matchesRepository.loadMatches();
                _degreeClassMatches.observe(ALWAYS_ON, degreeMatches -> {
                    _matchedDegrees = calculateMatchedDegrees(degreeMatches);
                    List<SweScore> baselineScores = calculateDegreeClassScores();
                    _degreeClassScores = createDegreeClassScoresView(baselineScores);
                    saveBaselineDegreeClassScores(baselineScores);

                    _calculatedDegrees = getCalculatedDegrees();

                    _isLoaded.setValue(true);
                });
            });
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        _knowledgeAreas.removeObservers(ALWAYS_ON);
        _degrees.removeObservers(ALWAYS_ON);
        _degreeClassMatches.removeObservers(ALWAYS_ON);
    }

    public LiveData<Boolean> isLoaded() {
        return _isLoaded;
    }

    /**
     * Loads all KnowledgeAreaTopics from the previously load KnowledgeAreas.
     * This will be used mostly as an accelerator for future data questioning.
     */
    private List<KnowledgeAreaTopic> createKnowledgeAreaTopicsView(List<KnowledgeArea> knowledgeAreas) {
        List<KnowledgeAreaTopic> knowledgeAreaTopics = new ArrayList<>();
        for (KnowledgeArea ka : knowledgeAreas) {
            knowledgeAreaTopics.addAll(ka.getTopics());
        }

        return knowledgeAreaTopics;
    }

    /**
     * Loads a different data view of the degrees.
     * Useful to inspect the degree without traversing the list.
     */
    private Map<Integer, Degree> createDegreesByIdView(List<Degree> degrees) {
        Map<Integer, Degree> degreesById = new HashMap<>();
        for (Degree degree : degrees) {
            degreesById.put(degree.getId(), degree);
        }
        return degreesById;
    }

    /**
     * Loads a different data view of the degree classes.
     * Useful to inspect the degree classes without traversing the list.
     */
    private Map<String, DegreeClass> createDegreeClassByIdView(List<Degree> degrees) {
        Map<String, DegreeClass> classesById = new HashMap<>();
        for (Degree degree : degrees) {
            for (DegreeClass degreeClass : degree.getClassesAsList()) {
                classesById.put(degreeClass.getId(), degreeClass);
            }
        }
        return classesById;
    }

    /**
     * Returns the HashMap needed to fast access KATopics by it ID.
     *
     * @return
     */
    private Map<Integer, KnowledgeAreaTopic> createKaTopicByKaIdView(List<KnowledgeArea> knowledgeAreas) {

        Map<Integer, KnowledgeAreaTopic> kaTopicByTopicId = new HashMap<>();

        for (KnowledgeArea ka : knowledgeAreas) {
            for (KnowledgeAreaTopic kaTopic : ka.getTopics()) {
                kaTopicByTopicId.put(kaTopic.getId(), kaTopic);
            }
        }
        return kaTopicByTopicId;
    }


    /**
     * Calculates the degrees that have complete matches.
     */
    private Map<Integer, Boolean> calculateMatchedDegrees(Map<String, DegreeClassMatch> degreeMatches) {

        Map<Integer, Boolean> matchedDegrees = new HashMap<>(_degreesById.values().size());

        for (Map.Entry<Integer, Degree> degree : _degreesById.entrySet()) {

            matchedDegrees.put(degree.getKey(), true);

            for (DegreeClass degreeClass : degree.getValue().getClassesAsList()) {

                if (!degreeMatches.containsKey(degreeClass.getId())) {
                    matchedDegrees.put(degree.getKey(), false);
                    break; // found one not matched, jump to next degree
                }
            }

        }
        return matchedDegrees;
    }


    private Map<String, SweScore> createDegreeClassScoresView(List<SweScore> degreeClassScores) {

        Map<String, SweScore> classScores = new HashMap<>();

        for (SweScore score : degreeClassScores) {
            classScores.put(score.getId(), score);
        }
        return classScores;
    }

    /**
     * Saves all the current degree class rankings that were calculated using the degree class matches as their base.
     */
    private void saveBaselineDegreeClassScores(List<SweScore> baselineScores) {
        _scoresRepository.open();
        _scoresRepository.insertOrUpdateObjectsInTransaction(baselineScores);
        _scoresRepository.close();
    }


    private Map<Integer, Boolean> getCalculatedDegrees() {

        Map<Integer, Boolean> calculatedDegrees = new TreeMap<>();

        _scoresRepository.open();

        for (Degree degree : _degrees.getValue()) {
            if (_scoresRepository.getAllCombinationsOfDegree(degree.getId()).size() > 0) {
                calculatedDegrees.put(degree.getId(), true);
            } else {
                calculatedDegrees.put(degree.getId(), false);
            }
        }

        _scoresRepository.close();

        return calculatedDegrees;
    }


    /**
     * Calculates the list with ALL the BASELINE scores for all possible Degree Classes of all the
     * Degrees in the systems baseline.
     * This is the very core of this application as the data that is calculated here serves as the basis for
     * every result.
     *
     * @return
     */
    private List<SweScore> calculateDegreeClassScores() {

        List<SweScore> allDegreeClassScores = new ArrayList<>();
        for (DegreeClassMatch match : _degreeClassMatches.getValue().values()) {
            allDegreeClassScores.add(CalculationUtils.calculateScore(_knowledgeAreaTopicsByTopicId, match));
        }

        return allDegreeClassScores;
    }

    /**
     * Returns all the Knowledge Areas in the system in the form of LiveData
     *
     * @return
     */
    public LiveData<List<KnowledgeArea>> getKnowledgeAreas() {
        return _knowledgeAreas;
    }


    public List<KnowledgeAreaTopic> getKnowledgeAreaTopics() {
        return _knowledgeAreaTopics;
    }

    /**
     * Returns a single Knowledge Area from the previously loaded system Knowledge Areas
     *
     * @param knowledgeAreaId
     * @return
     */
    public LiveData<KnowledgeArea> getKnowledgeArea(int knowledgeAreaId) {

        MutableLiveData<KnowledgeArea> result = new MutableLiveData<>();

        for (KnowledgeArea ka : _knowledgeAreas.getValue()) {
            if (ka.getId() == knowledgeAreaId) {
                result.postValue(ka);
            }
        }
        return result;
    }


    /**
     * Returns all the Degress from the systems as LiveData
     *
     * @return
     */
    public LiveData<List<Degree>> getDegrees() {
        return _degrees;
    }

    /**
     * Returns a single Degree from the previously loaded Degrees of the system.
     *
     * @param degreeId
     * @return
     */
    public Degree getDegree(int degreeId) {
        Degree result = new Degree();

        for (Degree degree : _degrees.getValue()) {
            if (degree.getId() == degreeId) {
                result = degree;
            }
        }
        return result;
    }


    /**
     * Returns a Degree Class given its ID.
     *
     * @param degreeClassId
     * @return
     */
    public DegreeClass getDegreeClass(String degreeClassId) {
        DegreeClass degreeClass = new DegreeClass();
        for (Degree degree : _degrees.getValue()) {
            if (degree.hasDegreeClass(degreeClassId)) {
                degreeClass = degree.getDegreeClass(degreeClassId);
            }
        }
        return degreeClass;
    }

    public LiveData<Map<String, DegreeClassMatch>> getDegreeMatches() {
        return _degreeClassMatches;
    }

    /**
     * Returns true if all the classes in a degree have been previously matched.
     * False otherwise.
     */
    public boolean isDegreeMatched(int degreeId) {
        return _matchedDegrees.get(degreeId);
    }

    /**
     * Returns true if the given degree class id has a match (i.e. if its degree class has been already matched, each of its topics
     * to one or more KnowledgeArea Topics)
     *
     * @param degreeClassId
     * @return
     */
    public boolean hasMatches(String degreeClassId) {
        return _matchesRepository.hasMatch(degreeClassId);
    }


    /**
     * Saves a Degree Class Match into the system.
     *
     * @param newMatchToSave the match that will be saved
     * @return true if it was successfully saved, false otherwise.
     */
    public boolean saveMatch(DegreeClassMatch newMatchToSave) {
        return _matchesRepository.saveMatch(newMatchToSave);
    }

    public DegreeClassMatch getDegreeClassMatch(String degreeClassId) {
        return _matchesRepository.getDegreeClassMatch(degreeClassId);
    }

    /**
     * Admin utility to save all the current matches for a degree into a single xml file.
     * It is useful to build new configurations.
     *
     * @param degreeId the degree id of the matches to save
     */
    public boolean flushMatchesToFile(int degreeId) {
        return _matchesRepository.saveMatchesToSingleFile(degreeId);
    }


    public SweScore getDegreeScore(String degreeCombinationId) {
        _scoresRepository.open();
        SweScore result = _scoresRepository.getDegreeCombinationScore(degreeCombinationId);
        _scoresRepository.close();
        return result;
    }

    public DegreeClassCombination getDegreeClassCombination(String degreeCombinationId) {
        _scoresRepository.open();
        DegreeClassCombination result = _scoresRepository.getDegreeClassCombination(degreeCombinationId);
        _scoresRepository.close();
        return result;
    }


    public void setRandomlySelectedDegreeScore() {

        SweScore result = new SweScore();
        _scoresRepository.open();

        int maxscores = (int) _scoresRepository.getScoreCount();
        if (maxscores == 0) {
            setSelectedDegreeCombinationId(null);
        } else {
            result = _scoresRepository.getAllScores().get(new Random().nextInt(maxscores));
        }

        setSelectedDegreeCombinationId(result.getId());
        _scoresRepository.close();
    }


    /**
     * Executes an async calculation of the each degree possible combination as well as each of those combinations'
     * scores. This is a very very intensive operation.
     *
     * @param degree   the target degree to calculation
     * @param listener the handler is used to communicate the progress of this operation to the outside, caller thread.
     */
    public void calculateDegreeScores(Degree degree, @Nullable ProgressHandler listener) {
        new CombinationCalculator(degree, listener).execute();
    }


    /**
     * This is a background thread whose purpose is to calculate and save all the degree combinations and its scores.
     * It is heavy stuff.
     * It uses and Handler so that it can communicate its progress to the outside caller thread.
     */
    private class CombinationCalculator extends AsyncTask<Void, Void, Void> {

        private Degree _degree;
        private int _degreeId;
        private int _combinationIdCounter;
        private String _degreeCombinationIdBase;
        private int _totalPossibleCombinations = 1;

        private Optional<ProgressHandler> _listener;

        public CombinationCalculator(Degree degree, @Nullable ProgressHandler listener) {
            _degree = degree;
            _degreeId = degree.getId();
            _combinationIdCounter = 1;
            _listener = Optional.fromNullable(listener);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (_listener.isPresent()) {
                _listener.get().startProgress();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            _degreeCombinationIdBase = "d" + _degreeId + "c";
            _combinationIdCounter = 1;

            calculateAndSaveAnnualCombinations(_degree);
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
        private void calculateAndSaveAnnualCombinations(Degree degree) {
            List<AnnualClassCombination> annualCombinations = new ArrayList<>();

            if (_listener.isPresent()) {
                _listener.get().startProgressAction(R.string.calculation_progress_annual_combo, degree.getYears());
            }

            publishProgress();

            for (Map.Entry<Integer, ClassCombinationStrategy> classCombinationStrategy : degree.getClassCombinationStrategies().entrySet()) {

                Integer yearOfDegree = classCombinationStrategy.getKey();
                ClassCombinationStrategy combinationStrategy = classCombinationStrategy.getValue();

                // Here, using each year's strategy to unfold all possible combinations for this year
                annualCombinations.addAll(combinationStrategy.getAnnualClassCombinations(yearOfDegree, degree.getClassesAsList()));

                if (_listener.isPresent()) {
                    _listener.get().updateProgressAction(1);
                }
            }

            // Last pass to set the degree id
            for (AnnualClassCombination annualCombination : annualCombinations) {
                annualCombination.setDegreeId(_degreeId);
            }

            Log.i("SweRanker:Calculation", "Found " + annualCombinations.size() + " annual combinations for degree " + _degreeId);


            if (_listener.isPresent()) {
                _listener.get().startProgressAction(R.string.calculation_progress_saving_annual_combos, degree.getYears());
            }

            _scoresRepository.saveObjects(annualCombinations);

        }

        /**
         * This does a lot. It calculates the scores for each annual combination of this degree by aggregating the scores
         * of all the classes that compose the annual combination.
         * <p>
         * Then saves each calculation on a batch level because Realm is seriously pathetic at inserting data in bulk.
         */
        private void calculateAndSaveAnnualScores() {

            int batchsize = 10000;

            _scoresRepository.open();

            // First get the saved annual combinations for this degree
            List<AnnualClassCombination> savedAnnualClassCombinations = _scoresRepository.getAnnualCombinationsOfDegree(_degreeId);

            if (_listener.isPresent()) {
                _listener.get().startProgressAction(R.string.calculation_progress_calc_saving_annual_scores, savedAnnualClassCombinations.size());
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
                        degreeClassScore.add(_degreeClassScores.get(degreeClassId.getDegreeClassId()));
                    }

                    // And now calculate its combined score
                    SweScore calculatedScore = CalculationUtils.calculateAccumulatedScore(_degreeClassesById, degreeClassScore);
                    calculatedScore.setDegreeId(_degreeId);
                    calculatedScore.setScoreType(SweScore.TYPE_ANNUAL_SCORE);
                    calculatedScore.setId(currentAnnualCombination.getId());

                    batch.add(calculatedScore);


                }

                _scoresRepository.insertOrUpdateObjectsInTransaction(batch);

                if (_listener.isPresent()) {
                    _listener.get().updateProgressAction(batchsize);
                }

                batch.clear();

                // This part is just a small sleep to give some time to trigger the GC and clean the mess it is creating.
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

            _scoresRepository.close();

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

            _scoresRepository.open();

            List<AnnualClassCombination> allAnnualCombinations = _scoresRepository.getAnnualCombinationsOfDegree(_degreeId);

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
            Log.i("SweRanker:Calculation", "Started the calculation of Degree combinations.");

            // This is just progress update, please ignore
            if (_listener.isPresent()) {
                _listener.get().startProgressAction(R.string.calculation_progress_calc_saving_degree_combos, _totalPossibleCombinations);
            }

            // Now it is the recursive call, this one is very very tricky so I will explain it in the comments
            generateAndSaveRecursively(combinationPool, new ArrayList<>(), 0, new ArrayList<>());

            // Logging
            long endtime = System.currentTimeMillis();
            Log.i("SweRanker:Calculation", "Ended at the calculation of Degree combinations.");
            Log.i("SweRanker:Calculation", "Found " + _combinationIdCounter + " degree combinations!");
            Log.i("SweRanker:Calculation", "Took " + ((endtime - startTime) / 1000) + " seconds to calculate and save combinations");


            _scoresRepository.close();
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
        private void generateAndSaveRecursively(
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

                    _scoresRepository.insertOrUpdateObjectsInTransaction(result);

                    long endMillis = System.currentTimeMillis();

                    // This is just a progress update, please ignore
                    if (_listener.isPresent()) {
                        _listener.get().updateProgressAction(10000);
                    }

                    Log.i("SweRanker:Calculation", "Saved a batch of " + result.size() + " combinations in " + ((endMillis - sMillis) / 1000) + " seconds");

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
                generateAndSaveRecursively(combinationPool, result, depth + 1, currentAnnual);
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

            _scoresRepository.open();

            long startTime = System.currentTimeMillis();
            Log.i("SweRanker:Calculation", "Started calculation of degree scoring.");

            int allCombinationsCount = _scoresRepository.countAllCombinationsOfDegree(_degreeId);

            // Funny this, AS SOON as an instance of Realm is not needed anymore, just close it
            // Previously this was on the end of the method. As a result the memory would explode with uncollected objects
            // when calling the below executor each with its own Realm instance.
            _scoresRepository.close();

            // This is a long debate that I had with myself.
            // After several samples I reached the conclusion that a batch size of 7500 and two threads are the best option
            // This is because Realm can only save in one Thread at the same time so a lot of threads would be pointless.
            // Therefore I just use two so that whenever Realm is free to insert a new batch I already have the calculation ready.
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            int executionId = 1;
            int startPosition = 0;
            int batchSize = 7500;

            // This is just progress update, please ignore
            if (_listener.isPresent()) {
                _listener.get().startProgressAction(R.string.calculation_progress_saving_degree_scores, allCombinationsCount);
            }

            while (startPosition < allCombinationsCount) {

                // If by adding the default window surpasses the total size of the collection
                if (startPosition + batchSize >= allCombinationsCount) {
                    // Then the batch size will have the size of what is left, hence the difference here
                    batchSize = allCombinationsCount - startPosition;
                }

                executorService.submit(new DegreeScoreCalculator(executionId, startPosition, batchSize));
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
            Log.i("SweRanker:Calculation", "Ended calculation of degree scoring.");
            Log.i("SweRanker:Calculation", "Took " + ((endTime - startTime) / 1000) + " seconds");


        }

        /**
         * This class is used to calculate and save a batch of calculations from the same batch of combinations.
         * Since it works on a finite window it will never mess with other thread's work, which is nice.
         * This was inspired on my previous job when I worked at IB. Yuck...
         */
        private class DegreeScoreCalculator implements Runnable {

            private int _id;
            private int _startPosition;
            private int _endPosition;

            DegreeScoreCalculator(int id, int startPosition, int batchSize) {
                _id = id;
                _startPosition = startPosition;
                _endPosition = startPosition + batchSize;
            }

            @Override
            public void run() {

                _scoresRepository.open();

                List<DegreeClassCombination> allCombinations = _scoresRepository.getAllCombinationsOfDegree(_degreeId);

                List<SweScore> results = new ArrayList<>();
                for (int i = _startPosition; i < _endPosition; i++) {

                    DegreeClassCombination currentDegreeCombination = allCombinations.get(i);
                    // First fetch annual combinations for this particular degree combination
                    RealmList<AnnualClassCombination> degreeAnnualCombinations = currentDegreeCombination.getAnnualClassCombinations();

                    // Then gets their individual class scores
                    List<SweScore> currentAnnualScores = new ArrayList<>();
                    for (AnnualClassCombination annualCombo : degreeAnnualCombinations) {
                        for (DegreeClassId degreeClassId : annualCombo.getDegreeClassIds()) {
                            currentAnnualScores.add(_degreeClassScores.get(degreeClassId.getDegreeClassId()));
                        }
                    }

                    // And now calculate its combined score
                    SweScore calculatedScore = CalculationUtils.calculateAccumulatedScore(_degreeClassesById, currentAnnualScores);
                    calculatedScore.setDegreeId(_degreeId);
                    calculatedScore.setScoreType(SweScore.TYPE_DEGREE_SCORE);
                    calculatedScore.setId(currentDegreeCombination.getCombinationId());

                    results.add(calculatedScore);

                }
                _scoresRepository.insertOrUpdateObjectsInTransaction(results);

                // This is just a progress update, please ignore
                if (_listener.isPresent()) {
                    _listener.get().updateProgressAction(_endPosition - _startPosition);
                }

                Log.i("SweRanker:Calculation", "Ended saving a batch of Degree Scores with id: " + _id);
                results.clear();

                _scoresRepository.close();
            }

        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (_listener.isPresent()) {
                _listener.get().terminateProgress();
            }
        }
    }


    /**
     * Loads a data structure that is just a visual representation of the degree scores.
     * It basically consists of a Degree Combination name and its Image Resource.
     * This is the data needed to be fed to the ScoresMasterFragment grid.
     *
     * @return
     */
    public LiveData<LinkedHashMap<String, Integer>> getOrderedScoresImages() {

        if (_scoreImages.getValue() == null) {
            new DegreeComboQueryLoader().execute();
        }
        return _scoreImages;
    }

    /**
     * Loads a data structure that is just a visual representation of the degree scores.
     * It basically consists of a Degree Combination name and its Image Resource.
     * This is the data needed to be fed to the ScoresMasterFragment grid.
     *
     * @param kaId
     * @param order
     * @param degreeId
     * @param limit
     * @return
     */
    public LiveData<LinkedHashMap<String, Integer>> getOrderedScoresImages(int kaId, ScoresRepository.Sort order, int degreeId, int limit) {

        if (_scoreImages.getValue() == null) {
            new DegreeComboQueryLoader(kaId, order, degreeId, limit).execute();
        }
        return _scoreImages;
    }

    public void applyFilterToScores(int kaId, ScoresRepository.Sort order, int degreeId, int limit) {
        new DegreeComboQueryLoader(kaId, order, degreeId, limit).execute();
    }

    /**
     * This AsyncTask's job is to load the scores from the system to finally show them in an ordered way.
     * It executes a parameterised query against the Realm database.
     * These parameters are passed by the Filter Dialog where the user can set how many results he wants
     * from the system, as well as their order in relation to a specific Knowledge Area.
     */
    private class DegreeComboQueryLoader extends AsyncTask<Void, Void, LinkedHashMap<String, Integer>> {

        private int _kaId;
        private String _kaFieldName;
        private ScoresRepository.Sort _sortOrder;
        private int _degreeid;
        private int _limit;


        private DegreeComboQueryLoader() {
            _kaId = 1;
            _kaFieldName = "";
            _sortOrder = null;
            _degreeid = 0;
            _limit = 10;
        }

        private DegreeComboQueryLoader(int kaId, ScoresRepository.Sort order, int degreeId, int limit) {
            _kaId = kaId;
            _kaFieldName = getKaFieldName(kaId);
            _sortOrder = order;
            _degreeid = degreeId;
            _limit = limit;
        }

        /**
         * Just translates an ID to a specific field name used for the query.
         */
        private String getKaFieldName(int kaId) {
            // This is ugly and most likely shouldn't be here. However I am almost finished with this and
            // I won't spend my time now organising this.
            switch (kaId) {
                case 1:
                    return SweScoreFields.KA_PERCENT1;
                case 2:
                    return SweScoreFields.KA_PERCENT2;
                case 3:
                    return SweScoreFields.KA_PERCENT3;
                case 4:
                    return SweScoreFields.KA_PERCENT4;
                case 5:
                    return SweScoreFields.KA_PERCENT5;
                case 6:
                    return SweScoreFields.KA_PERCENT6;
                case 7:
                    return SweScoreFields.KA_PERCENT7;
                case 8:
                    return SweScoreFields.KA_PERCENT8;
                case 9:
                    return SweScoreFields.KA_PERCENT9;
                case 10:
                    return SweScoreFields.KA_PERCENT10;
                case 11:
                    return SweScoreFields.KA_PERCENT11;
                case 12:
                    return SweScoreFields.KA_PERCENT12;
                case 13:
                    return SweScoreFields.KA_PERCENT13;
                case 14:
                    return SweScoreFields.KA_PERCENT14;
                case 15:
                    return SweScoreFields.KA_PERCENT15;
                case 16:
                    return SweScoreFields.KA_PERCENT16;
                default:
                    return SweScoreFields.KA_PERCENT1;
            }
        }

        @Override
        protected LinkedHashMap<String, Integer> doInBackground(Void... voids) {


            _scoresRepository.open();

            Collection<SweScore> queryResults = queryForScores();

            /*
             * TODO : Ha aqui uma grande grande falha. Devido ao facto de o Realm cortar o acesso aos dados quando se faz close na
             * ligacao eu criei uma copia dos mesmos( ja que nao e mais do que um inteiro e uma string).
             * No entanto nao e assim que se deve fazer, o ideal e devolver um "ResultSet" que vai sendo iterado ate nao se pretender
             * mais dados. Isso seria feito, presumo, pelo Adapter desta coisa. Logo a connection estaria aberta ate sair desse ecra...
             * Tenho que pensar melhor nisto e implementar no futuro.
             * */
            LinkedHashMap<String, Integer> temporaryData = new LinkedHashMap<>();
            if (!queryResults.isEmpty()) {
                for (SweScore score : queryResults) {
                    temporaryData.put(score.getId(), _degreesById.get((int) score.getDegreeId()).getImageResource());
                }
            }

            _scoresRepository.close();

            return temporaryData;
        }


        private Collection<SweScore> queryForScores() {

            Collection<SweScore> scoresFetched;

            if (_degreeid == 0) {
                scoresFetched = fetchAllDegreeScores();
            } else {
                scoresFetched = fetchSingleDegreeScores();
            }

            return scoresFetched;
        }

        private Collection<SweScore> fetchAllDegreeScores() {

            Map<Integer, List<SweScore>> multiQueryResults = new TreeMap<>();
            if (_sortOrder == null) {
                for (Integer matchedDegreeId : _calculatedDegrees.keySet()) {
                    if (_calculatedDegrees.get(matchedDegreeId)) {
                        multiQueryResults.put(matchedDegreeId, _scoresRepository.getScoresOfDegree(matchedDegreeId));
                    }
                }
            } else {
                for (Integer matchedDegreeId : _calculatedDegrees.keySet()) {
                    if (_calculatedDegrees.get(matchedDegreeId)) {
                        multiQueryResults.put(matchedDegreeId, _scoresRepository.getScoresOfDegreeOrderedBy(matchedDegreeId, _kaFieldName, _sortOrder));
                    }
                }
            }

            Set<SweScore> orderedScores = new TreeSet<>(createQueryComparator());
            for (List<SweScore> resultSet : multiQueryResults.values()) {

                int resultsLimit = resultSet.size() <= _limit ? resultSet.size() : _limit;
                for (int i = 0; i < resultsLimit; i++) {
                    orderedScores.add(resultSet.get(i));
                }
            }

            return orderedScores;

        }

        private Comparator<SweScore> createQueryComparator() {

            if (_sortOrder == ScoresRepository.Sort.DESCENDING) {
                return (o1, o2) -> {

                    if (o1.getKaPercent(_kaId) == o2.getKaPercent(_kaId)) {
                        return o1.getId().compareTo(o2.getId());
                    }
                    if (o1.getKaPercent(_kaId) < o2.getKaPercent(_kaId)) {
                        return 1;
                    }
                    return -1;
                };
            }

            return (o1, o2) -> {
                if (o1.getKaPercent(_kaId) == o2.getKaPercent(_kaId)) {
                    return o1.getId().compareTo(o2.getId());
                }
                if (o1.getKaPercent(_kaId) < o2.getKaPercent(_kaId)) {
                    return -1;
                }
                return 1;
            };
        }


        private Collection<SweScore> fetchSingleDegreeScores() {

            List<SweScore> queryResults;
            if (_sortOrder == null) {
                queryResults = _scoresRepository.getScoresOfDegree(_degreeid);
            } else {
                queryResults = _scoresRepository.getScoresOfDegreeOrderedBy(_degreeid, _kaFieldName, _sortOrder);
            }

            int resultsLimit = queryResults.size() <= _limit ? queryResults.size() : _limit;

            List<SweScore> resultsToDisplay = new ArrayList<>(resultsLimit);
            for (int i = 0; i < resultsLimit; i++) {
                resultsToDisplay.add(queryResults.get(i));
            }

            return resultsToDisplay;
        }

        @Override
        protected void onPostExecute(LinkedHashMap<String, Integer> temporaryData) {
            super.onPostExecute(temporaryData);
            _scoreImages.setValue(temporaryData);
        }
    }


    // BELOW IS FRAGMENT INTERACTION ZONE

    public void setSelectedKnowledgeArea(KnowledgeArea knowledgeArea) {
        _selectedKnowledgeArea = knowledgeArea;
    }

    public KnowledgeArea getSelectedKnowledgeArea() {
        return _selectedKnowledgeArea;
    }

    public void setSelectedDegree(Degree degree) {
        _selectedDegree = degree;
    }

    public Degree getSelectedDegree() {
        return _selectedDegree;
    }

    public void setSelectedDegreeClass(DegreeClass degreeClass) {
        _selectedDegreeClass = degreeClass;
    }

    public DegreeClass getSelectedDegreeClass() {
        return _selectedDegreeClass;
    }

    public void setSelectedDegreeCombinationId(String degreeCombinationId) {
        _selectedDegreeCombinationId = degreeCombinationId;
    }

    public String getSelectedDegreeCombinationId() {
        return _selectedDegreeCombinationId;
    }

    public void setMultiSelectedDereeCombinationIds(List<String> selectedDegreeCombinationsToCompare) {
        _selectedDegreeCombinationsToCompare = selectedDegreeCombinationsToCompare;
    }

    public List<String> getMultiSelectedDegreeCombinationIds() {
        return _selectedDegreeCombinationsToCompare;
    }

}
