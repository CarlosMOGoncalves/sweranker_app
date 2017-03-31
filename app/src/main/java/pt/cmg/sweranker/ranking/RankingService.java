package pt.cmg.sweranker.ranking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.ranking.combinationstrategies.ClassCombinationStrategy;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

public class RankingService extends Service {

    private static final String STANDARD_DIRECTORY = "default_matches";

    private RankingBinder _binder = new RankingBinder();


    // Keys -> Degree Class Id , Values -> its current matches
    private Map<String, DegreeClassMatch> _degreeMatches;

    // Keys -> Degree Class Id , Values -> its current ranking
    private Map<String, KACalculation> _degreeClassRankings;

    private Map<Integer, KnowledgeArea> _knowledgeAreasById;

    private Map<Integer, KnowledgeAreaTopic> _knowledgeAreaTopicsByTopicId;

    private Map<Integer, Degree> _degreesById;

    private Map<String, DegreeClass> _degreeClassesById;

    // Keys -> Degree Id , Values -> the ranking information of that degree.
    private Map<Integer, DegreeRanking> _rankingsByDegree;

    public RankingService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        File rootMatchesDir = createMatchFilesDirectory();
        _degreeMatches = loadDefaultMatches();
        _rankingsByDegree = new HashMap<>();
//        _degreeMatches = loadSystemMatches(rootMatchesDir);
    }


    private void saveMatchesToSingleFile() {
        final String xmlFileName = "all_matches.xml";

        File directory = getApplicationContext().getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);

        File targetFile = new File(directory, xmlFileName);

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {


            XmlSerializer xmlSerializer = Xml.newSerializer();

            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag(null, "all_matches");

            for (Map.Entry<String, DegreeClassMatch> singleMatch : _degreeMatches.entrySet()) {

                xmlSerializer.startTag(null, "match");
                xmlSerializer.startTag(null, "degree_class_id");
                xmlSerializer.text(singleMatch.getKey());
                xmlSerializer.endTag(null, "degree_class_id");

                xmlSerializer.startTag(null, "topic_matches");
                for (Map.Entry<String, LinkedList<Integer>> entry : singleMatch.getValue().getAllMatches().entrySet()) {
                    xmlSerializer.startTag(null, "topic_match");

                    xmlSerializer.startTag(null, "class_topic_id");
                    xmlSerializer.text(entry.getKey());
                    xmlSerializer.endTag(null, "class_topic_id");

                    xmlSerializer.startTag(null, "ka_topics");
                    for (Integer kaTopicId : entry.getValue()) {
                        xmlSerializer.startTag(null, "id");
                        xmlSerializer.text(kaTopicId.toString());
                        xmlSerializer.endTag(null, "id");
                    }
                    xmlSerializer.endTag(null, "ka_topics");

                    xmlSerializer.endTag(null, "topic_match");
                }

                xmlSerializer.endTag(null, "topic_matches");

                xmlSerializer.endTag(null, "match");
//                Log.i("RankingService", "Successfully saved match for:" + singleMatch.getValue().getDegreeClassId());
//                _degreeMatches.put(singleMatch.getValue().getDegreeClassId(), singleMatch.getValue());
                _degreeClassRankings.put(singleMatch.getValue().getDegreeClassId(), evaluateClass(singleMatch.getValue()));
            }

            xmlSerializer.endTag(null, "all_matches");
            xmlSerializer.endDocument();


            String dataWrite = writer.toString();
            Log.i("Cenas", dataWrite);
            outputStream.write(dataWrite.getBytes());

        } catch (Exception e) {
            Log.e("Cenas", e.getLocalizedMessage());
        }

    }

    /**
     * Creates the root directory for saving the matches.
     */
    public File createMatchFilesDirectory() {
        return getApplicationContext().getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);
    }


    /**
     * Loads all the default matches of the system. These come with the resources.
     * This function loads it to a class structure so that it acts as a cache.
     *
     * @return
     */
    private Map<String, DegreeClassMatch> loadDefaultMatches() {

        Map<String, DegreeClassMatch> matches = new HashMap<>();

        try {

            XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
            InputStream reader = this.getResources().openRawResource(R.raw.default_matches);
            xmlParser.setInput(reader, null);
            int eventType = xmlParser.getEventType();

            DegreeClassMatch currentMatch = null;
            String xmlElementName;

            while (eventType != XmlPullParser.END_DOCUMENT) {

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        // ok, start document, let's keep going to next tag
                        break;
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();
                        switch (xmlElementName) {
                            case "all_matches":
                                // It's the beginning, skip
                                break;
                            case "match":
                                currentMatch = loadDegreeMatch(xmlParser);
                                matches.put(currentMatch.getDegreeClassId(), currentMatch);
                                break;
                            default:
                                throw new XmlPullParserException("Unknown tag: " + xmlElementName);
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        // Don't care, not going to pass through here because all the meaningful end tags have been consumed.
                        break;
                }
                eventType = xmlParser.next();
            }

        } catch (XmlPullParserException | Resources.NotFoundException | IOException e) {
            e.printStackTrace();
        }
        return matches;
    }

    /**
     * Loads the default evaluations for the default matches. It basically executes a ranking calculation
     * for each degree class that it loaded from the resources.
     *
     * @return Keys -> Degree Class Id , Values -> its evaluation
     */
    private Map<String, KACalculation> loadDefaultRankings() {

        Map<String, KACalculation> rankings = new HashMap<>();
        for (DegreeClassMatch match : _degreeMatches.values()) {
            rankings.put(match.getDegreeClassId(), evaluateClass(match));
        }
        return rankings;
    }

    private DegreeClassMatch loadDegreeMatch(XmlPullParser xmlParser) {

        DegreeClassMatch currentMatch = new DegreeClassMatch();

        try {
            // We just entered a match tag, let's consume it
            xmlParser.nextTag();

            int eventType = xmlParser.getEventType();
            String xmlElementName = xmlParser.getName();

            while (!xmlElementName.equalsIgnoreCase("match")) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        switch (xmlElementName) {
                            case "degree_class_id":
                                currentMatch = new DegreeClassMatch(xmlParser.nextText());
                                break;
                            case "topic_matches":
                                loadTopicMatches(currentMatch, xmlParser);
                                break;
                            default:
                                throw new XmlPullParserException("Unknown tag: " + xmlElementName);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        xmlElementName = xmlParser.getName();
                        break;
                }
                eventType = xmlParser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            Log.e("RankingService", e.getLocalizedMessage());
        }

        return currentMatch;
    }

    private void loadTopicMatches(DegreeClassMatch currentMatch, XmlPullParser xmlParser) {

        try {
            // We just entered a topic_matches tag, let's consume it
            xmlParser.nextTag();

            int eventType = xmlParser.getEventType();
            String xmlElementName = xmlParser.getName();
            String classTopicId = "";
            List<Integer> kaTopicIds = new ArrayList<>();

            while (!xmlElementName.equalsIgnoreCase("topic_matches")) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        switch (xmlElementName) {
                            case "topic_match":
                                // Don't care, skip it
                                break;
                            case "class_topic_id":
                                classTopicId = xmlParser.nextText();
                                break;
                            case "ka_topics":
                                // Another tag used just to make some sense of the xml, skip it
                                break;
                            case "id":
                                kaTopicIds.add(Integer.valueOf(xmlParser.nextText()));
                                break;
                            default:
                                throw new XmlPullParserException("Unknown tag: " + xmlElementName);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        xmlElementName = xmlParser.getName();
                        switch (xmlElementName) {
                            case "topic_match":
                                currentMatch.addAllTopicsToDegreeTopic(classTopicId, kaTopicIds);
                                kaTopicIds = new ArrayList<>();
                                break;
                        }
                        break;
                }
                eventType = xmlParser.next();
            }


        } catch (XmlPullParserException | IOException e) {
            Log.e("RankingService", e.getLocalizedMessage());
        }
    }

    /**
     * Loads all the matches already saved in the system.
     * This function loads it to a class structure so that it acts as a cache.
     *
     * @param rootMatchesDir
     * @return
     */
    private Map<String, DegreeClassMatch> loadSystemMatches(File rootMatchesDir) {

        Map<String, DegreeClassMatch> matches = new HashMap<>();
        String[] matchFiles = rootMatchesDir.list();

        for (int i = 0; i < matchFiles.length; i++) {

            String degreeClassIdFileName = matchFiles[i];
            String degreeClassId = degreeClassIdFileName.substring(0, degreeClassIdFileName.length() - 4);
            File currentFile = new File(rootMatchesDir, matchFiles[i]);

            try {

                XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
                InputStream reader = new FileInputStream(currentFile);

                xmlParser.setInput(reader, null);
                int eventType = xmlParser.getEventType();
                DegreeClassMatch match = new DegreeClassMatch(degreeClassId);
                String currentDegreeTopicId = "";

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    String xmlElementName;
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            // ok, start document, let's keep going to next tag
                            break;
                        case XmlPullParser.START_TAG:
                            xmlElementName = xmlParser.getName();
                            switch (xmlElementName) {
                                case "matches":
                                    break;
                                case "match":
                                    break;
                                case "class_topic_id":
                                    currentDegreeTopicId = xmlParser.nextText();
                                    match.addDegreeClassTopic(currentDegreeTopicId);
                                    break;
                                case "ka_topics":
                                    List<Integer> kaTopicIds = getKaTopicsFromXml(xmlParser);
                                    match.addAllTopicsToDegreeTopic(currentDegreeTopicId, kaTopicIds);
                                    break;
                                default:
                                    throw new XmlPullParserException("Unknown tag: " + xmlElementName);
                            }
                            break;

                        case XmlPullParser.END_TAG:
                            xmlElementName = xmlParser.getName();
                            if (xmlElementName.equalsIgnoreCase("matches")) {
                                matches.put(degreeClassId, match);
                            }
                            break;
                    }
                    eventType = xmlParser.next();
                }

            } catch (XmlPullParserException | Resources.NotFoundException | IOException e) {
                e.printStackTrace();
            }

        }

        return matches;
    }


    /**
     * Evaluates a degreeClass based on the matches that were made and saved, finally
     *
     * @param classMatch
     */

    private KACalculation evaluateClass(DegreeClassMatch classMatch) {

        KACalculation ranking = new KACalculation();
        KnowledgeAreaTopic currentTopic = null;


        for (Integer kaTopicId : classMatch.getAllMatchesAsList()) {
            currentTopic = _knowledgeAreaTopicsByTopicId.get(kaTopicId);
            ranking.addTopic(currentTopic.getId(), currentTopic.getKnowledgeAreaId());
        }

        return ranking;
    }


    /**
     * Parses and returns all the ka topic ids of the xml.
     *
     * @param xmlParser
     * @return
     */
    private List<Integer> getKaTopicsFromXml(XmlPullParser xmlParser) {
        List<Integer> ids = new LinkedList<>();

        try {
            int eventType = xmlParser.getEventType();
            String xmlElementName = xmlParser.getName();
            Integer kaTopicId = null;

            // if it is the first let's just go to next to start iteration
            if (xmlElementName.equalsIgnoreCase("ka_topics") && eventType == XmlPullParser.START_TAG) {
                eventType = xmlParser.nextTag();
                xmlElementName = xmlParser.getName();
            } else {
                throw new XmlPullParserException("Malformed xml: there is no ka_topic_ids element, you screwed up.");
            }

            while (!xmlElementName.equalsIgnoreCase("ka_topics")) {

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        if (xmlElementName.equals("id")) {
                            ids.add(Integer.valueOf(xmlParser.nextText()));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlParser.next();
                xmlElementName = xmlParser.getName();
            }

        } catch (XmlPullParserException | IOException e) {
            Log.e("RankingService", e.getLocalizedMessage());
        }

        return ids;
    }


    /**
     * Saves or overwrites a Degree Class Match into the internal storage system.
     *
     * @param classMatch
     * @return true if saving was successful, false otherwise.
     */
    public boolean saveMatch(DegreeClassMatch classMatch) {

        final String xmlFileName = classMatch.getDegreeClassId() + ".xml";

        File directory = getApplicationContext().getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);

        File targetFile = new File(directory, xmlFileName);

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {


            XmlSerializer xmlSerializer = Xml.newSerializer();

            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag(null, "matches");

            for (Map.Entry<String, LinkedList<Integer>> entry : classMatch.getAllMatches().entrySet()) {
                xmlSerializer.startTag(null, "match");

                xmlSerializer.startTag(null, "class_topic_id");
                xmlSerializer.text(entry.getKey());
                xmlSerializer.endTag(null, "class_topic_id");

                xmlSerializer.startTag(null, "ka_topics");
                for (Integer kaTopicId : entry.getValue()) {
                    xmlSerializer.startTag(null, "id");
                    xmlSerializer.text(kaTopicId.toString());
                    xmlSerializer.endTag(null, "id");
                }
                xmlSerializer.endTag(null, "ka_topics");

                xmlSerializer.endTag(null, "match");
            }

            xmlSerializer.endTag(null, "matches");

            xmlSerializer.endDocument();

            String dataWrite = writer.toString();
            outputStream.write(dataWrite.getBytes());

            Log.i("RankingService", "Successfully saved match for:" + classMatch.getDegreeClassId());
            _degreeMatches.put(classMatch.getDegreeClassId(), classMatch);
            _degreeClassRankings.put(classMatch.getDegreeClassId(), evaluateClass(classMatch));

            saveMatchesToSingleFile();
            return true;
        } catch (FileNotFoundException e) {
            Log.e("RankingService", "File " + xmlFileName + " not found.");
            return false;
        } catch (IOException e) {
            Log.e("RankingService", "Couldn't save file due to: ", e);
            return false;
        }
    }


    /**
     * Returns true if the given degree class id has a match (i.e. if its degree class has been already matched, each of its topics
     * to one or more KnowledgeArea Topics)
     *
     * @param degreeClassId
     * @return
     */
    public boolean hasMatches(String degreeClassId) {
        return _degreeMatches.get(degreeClassId) != null;
    }

    public DegreeClassMatch getDegreeClassMatches(String degreeClassId) {
        return _degreeMatches.get(degreeClassId);
    }


    /**
     * This setter is used so that other data source can put the KAs in this service.
     * This all needs to be reformulated so that it can be loaded directly to here.
     *
     * @param knowledgeAreas
     */
    public void setKnowledgeAreas(List<KnowledgeArea> knowledgeAreas) {
        _knowledgeAreasById = createKaByIdView(knowledgeAreas);
        _knowledgeAreaTopicsByTopicId = createKaTopicByKaIdView(knowledgeAreas);

        // TODO: this is here because only after the KA are loaded I have info enough to evaluate them. This needs to be reviewed
        _degreeClassRankings = loadDefaultRankings();
    }

    /**
     * Just gets an HashMap from the List of KAs to that I can access them faster.
     *
     * @return
     */
    private Map<Integer, KnowledgeArea> createKaByIdView(List<KnowledgeArea> knowledgeAreas) {
        Map<Integer, KnowledgeArea> kaById = new HashMap<>();
        for (KnowledgeArea ka : knowledgeAreas) {
            kaById.put(ka.getId(), ka);
        }
        return kaById;
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
     * This setter is used so that other data source can put the Degrees in this service.
     * This all needs to be reformulated so that it can be loaded directly to here.
     *
     * @param degrees
     */
    public void setDegreeClasses(List<Degree> degrees) {
        _degreesById = createDegreesByIdView(degrees);
        _degreeClassesById = createDegreesClassByIdView(degrees);
    }


    /**
     * Just gets an HashMap from the List of Degrees to that I can access them faster.
     *
     * @return
     */
    private Map<Integer, Degree> createDegreesByIdView(List<Degree> degrees) {
        Map<Integer, Degree> degreesById = new HashMap<>();
        for (Degree degree : degrees) {
            degreesById.put(degree.getId(), degree);
        }
        return degreesById;
    }

    /**
     * Returns the HashMap needed to fast access DegreeClasses by it ID.
     *
     * @return
     */
    private Map<String, DegreeClass> createDegreesClassByIdView(List<Degree> degrees) {

        Map<String, DegreeClass> degreeClassById = new HashMap<>();

        for (Degree degree : degrees) {
            for (DegreeClass degreeClass : degree.getClassesAsList()) {
                degreeClassById.put(degreeClass.getId(), degreeClass);
            }
        }
        return degreeClassById;
    }

    public Map<String, KACalculation> getDegreeClassRankings() {
        return _degreeClassRankings;
    }

    public Map<Integer, DegreeRanking> getDegreeRankings() {
        return _rankingsByDegree;
    }


    public void calculateDegreesRankings() {
        new YearlyRankingCalculator().execute();
    }


    private class YearlyRankingCalculator extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            //for (Degree degree : _degreesById.values()) {
            for (int i = 1; i <= 1; i++) {

                Degree degree = _degreesById.get(i);

                Map<Integer, List<AnnualClassCombination>> combinationsByYear = calculateDegreeAnnualCombinations(degree);
                Map<String, DegreeClassCombination> allDegreeCombinations = calculateAllDegreeClassCombinations(degree, combinationsByYear);


                DegreeRanking degreeRanking = new DegreeRanking(degree);
                for (List<AnnualClassCombination> combos : combinationsByYear.values()) {
                    degreeRanking.addAnnualCombinations(combos);
                }
                degreeRanking.setFullDegreeCombinations(allDegreeCombinations);


                Map<String, KACalculation> scoresByAnnualCombination = calculateAnnualScores(combinationsByYear.values());
                degreeRanking.addAnnualCalculations(scoresByAnnualCombination);

                Map<String, KACalculation> scoresByDegreeCombination = calculateDegreeScores(allDegreeCombinations.values(), scoresByAnnualCombination);
                degreeRanking.addFullDegreeCalculations(scoresByDegreeCombination);

                _rankingsByDegree.put(degreeRanking.getDegreeId(), degreeRanking);
            }

            return null;
        }


        private Map<String, KACalculation> calculateDegreeScores(Collection<DegreeClassCombination> degreeCombinations, Map<String, KACalculation> annualScores) {
            Map<String, KACalculation> scoresByDegreeCombination = new HashMap<>();
            List<KACalculation> annualCalculations = new ArrayList<>();
            try {
                for (DegreeClassCombination degreeCombination : degreeCombinations) {

                    annualCalculations = new ArrayList<>();
                    for (AnnualClassCombination annualCombination : degreeCombination.getAnnualCombinations()) {
                        annualCalculations.add(annualScores.get(annualCombination.getId()));
                    }

                    scoresByDegreeCombination.put(degreeCombination.getCombinationId(), CalculationUtils.calculateAccumulatedRankings(annualCalculations));
                }
            } catch (OutOfMemoryError e) {
                Log.e("CalcScores", "Size calculated: " + scoresByDegreeCombination.size());
                Log.e("CalcScores", "Last calculation: " + annualCalculations.toString());
            }

            return scoresByDegreeCombination;
        }

        private Map<String, KACalculation> calculateAnnualScores(Collection<List<AnnualClassCombination>> classCombinations) {

            Map<String, KACalculation> scoresByAnnualCombination = new HashMap<>();

            for (List<AnnualClassCombination> annualCombinations : classCombinations) {
                // Isto calcula para um ano inteiro
                for (AnnualClassCombination annualCombination : annualCombinations) {
                    List<String> degreeClassIds = annualCombination.getDegreeClassIds();

                    List<KACalculation> degreeClassScore = new ArrayList<>();
                    for (String degreeClassId : degreeClassIds) {
                        degreeClassScore.add(_degreeClassRankings.get(degreeClassId));
                    }

                    scoresByAnnualCombination.put(annualCombination.getId(), CalculationUtils.calculateAccumulatedRankings(degreeClassScore));
                }
            }

            return scoresByAnnualCombination;
        }

        /**
         * Returns a Map with all the different possible annual combinations of classes for each year of a degree.
         * <p></p>
         * <br/>
         * This Maps has the form of:
         * <p>
         * Keys -> The numerical Year (e.g. 3) , Values -> A list with all possible combinations of classes for that year.
         * </p>
         *
         * @param degree
         * @return
         */
        private Map<Integer, List<AnnualClassCombination>> calculateDegreeAnnualCombinations(Degree degree) {

            Map<Integer, List<AnnualClassCombination>> combinationsByYear = new HashMap<>();

            for (Map.Entry<Integer, ClassCombinationStrategy> classCombinationStrategy : degree.getClassCombinationStrategies().entrySet()) {

                Integer yearOfDegree = classCombinationStrategy.getKey();
                ClassCombinationStrategy combinationStrategy = classCombinationStrategy.getValue();

                // Here, using each year's strategy to unfold all possible combinations for this year
                List<AnnualClassCombination> classCombinationsMatrix = combinationStrategy.getAnnualClassCombinations(degree.getClasses().get(yearOfDegree));

                combinationsByYear.put(yearOfDegree, classCombinationsMatrix);
            }

            return combinationsByYear;

        }

        /**
         * Uses all combinations of all the years that compose a degree to further unfold the class combinations to ALL possible ones.
         * This is heavy processing method, a good multi-threading strategy must be developed.
         * <br/>
         * Returns a Map where:
         * <p>
         * Keys -> A generated unique degree combination ID , Values -> a single complete combination of classes of all years that compose the degree
         * </p>
         *
         * @param degreeCombinationsByYear
         * @return
         */
        private Map<String, DegreeClassCombination> calculateAllDegreeClassCombinations(Degree degree, Map<Integer, List<AnnualClassCombination>> degreeCombinationsByYear) {
            return CombinationUtils.generateAllDegreeCombinations(degree, degreeCombinationsByYear);
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            sendBroadcast(new Intent(RankingFragment.ACTION_RECEIVER));

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }


    /**
     * Interface de comunicação com a Activity ou outro componente que a chamar.
     */
    public class RankingBinder extends Binder {
        public RankingService getService() {
            return RankingService.this;
        }
    }
}
