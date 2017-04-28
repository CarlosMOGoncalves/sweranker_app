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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

public class RankingService extends Service {

    private static final String STANDARD_DIRECTORY = "default_matches";

    private RankingBinder _binder = new RankingBinder();


    // Keys -> Degree Class Id , Values -> its current matches
    private Map<String, DegreeClassMatch> _degreeMatches;

    // Keys -> Degree Class Id , Values -> its current ranking
    private Map<String, SweScore> _degreeClassRankings;

    private Map<Integer, KnowledgeArea> _knowledgeAreasById;

    private Map<Integer, KnowledgeAreaTopic> _knowledgeAreaTopicsByTopicId;

    private Map<Integer, Degree> _degreesById;

    private Map<String, DegreeClass> _degreeClassesById;

    // Keys -> Degree Id , Values -> true if completely matched, false otherwise
    private Map<Integer, Boolean> _matchedDegrees;


    public RankingService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        File rootMatchesDir = createMatchFilesDirectory();
        _degreeMatches = loadDefaultMatches();
    }

    /**
     * Creates the root directory for saving the matches.
     */
    public File createMatchFilesDirectory() {
        return getApplicationContext().getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);
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

                xmlSerializer.startTag(null, "degree_id");
                xmlSerializer.text(String.valueOf(singleMatch.getValue().getDegreeId()));
                xmlSerializer.endTag(null, "degree_id");

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
     * Loads an individual degree match from its XML. This is used by loadDefaultMatches.
     *
     * @param xmlParser
     * @return
     */
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
                            case "degree_id":
                                currentMatch.setDegreeId(Integer.valueOf(xmlParser.nextText()));
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

    /**
     * Loads the set of topic matches from the XML and appends it to the currently loading DegreeClassMatch.
     *
     * @param currentMatch
     * @param xmlParser
     */
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
                                case "degree_id":
                                    match.setDegreeId(Integer.valueOf(xmlParser.nextText()));
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

            xmlSerializer.startTag(null, "degree_id");
            xmlSerializer.text(String.valueOf(classMatch.getDegreeId()));
            xmlSerializer.endTag(null, "degree_id");

            xmlSerializer.startTag(null, "matches");

            for (Map.Entry<String, LinkedList<Integer>> entry : classMatch.getAllMatches().entrySet()) {
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

            xmlSerializer.endTag(null, "matches");

            xmlSerializer.endDocument();

            String dataWrite = writer.toString();
            outputStream.write(dataWrite.getBytes());

            Log.i("RankingService", "Successfully saved match for:" + classMatch.getDegreeClassId());
            _degreeMatches.put(classMatch.getDegreeClassId(), classMatch);
//            _degreeClassRankings.put(classMatch.getDegreeClassId(), evaluateClass(classMatch));

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

        saveIndividualClassScores();

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
     * Loads the default evaluations for the default matches. It basically executes a ranking calculation
     * for each degree class that it loaded from the resources.
     *
     * @return Keys -> Degree Class Id , Values -> its evaluation
     */
    private Map<String, SweScore> loadDefaultRankings() {

        Map<String, SweScore> rankings = new HashMap<>();
        for (DegreeClassMatch match : _degreeMatches.values()) {
            rankings.put(match.getDegreeClassId(), evaluateClass(match));
        }
        return rankings;
    }


    /**
     * Calculates the SweScore for a given DegreeClassMatch.
     * This will basically sweep the match for all its topic that were matched and pretty much count them
     * discretely so that in the end a percentage can be calculated
     * <p>
     * TODO: this is the basis of the calculation. This should be in CalculationUtils. It is here because here I have access to the Knowledge Areas which I need in order to caculate stuff. Which also means that the KAs should not be here as well...
     *
     * @param classMatch
     * @return
     */
    private SweScore evaluateClass(DegreeClassMatch classMatch) {

        short[] kaTopicCounters = new short[102];
        short[] kaCounters = new short[16];


        int currentKnowledgeAreaId = 0;
        short totalTopicCounters = 0;

        for (Integer kaTopicId : classMatch.getAllMatchesAsList()) {

            currentKnowledgeAreaId = _knowledgeAreaTopicsByTopicId.get(kaTopicId).getKnowledgeAreaId();

            kaCounters[currentKnowledgeAreaId - 1]++;
            kaTopicCounters[kaTopicId - 1]++;
            totalTopicCounters++;
        }

        SweScore ranking = new SweScore(classMatch.getDegreeClassId(), (byte) classMatch.getDegreeId(), SweScore.TYPE_CLASS_SCORE);
        ranking.setKaCounters(kaCounters);
        ranking.setTopicCounters(kaTopicCounters);
        ranking.setTotalTopicCount(totalTopicCounters);

        ranking.calculateScores();

        return ranking;
    }


    /**
     * Saves all the current degree class rankings that were calculated using the degree class matches as their base.
     */
    private void saveIndividualClassScores() {

        Realm realmInstance = Realm.getDefaultInstance();

        realmInstance.executeTransaction(realm -> realm.insertOrUpdate(_degreeClassRankings.values()));

        List<SweScore> savedScores = realmInstance.where(SweScore.class)
                .equalTo("scoreType", SweScore.TYPE_CLASS_SCORE)
                .findAll();

        Log.i("Realm", "Saved or updated " + savedScores.size() + " individual Class Scores.");

        realmInstance.close();
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


        _matchedDegrees = calculateMatchedDegrees();
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


    /**
     * Calculates the degrees that have complete matches.
     * <p>
     * NOTE: This is awful. It may be a good idea to be in this service, but it
     * suffers from the fact that the data for the degrees have to be put here
     * by another service in an ugly ugly trick in the activity. If not for the time
     * constraints this MUST be changed.
     *
     * @return
     */
    private Map<Integer, Boolean> calculateMatchedDegrees() {

        Map<Integer, Boolean> matchedDegrees = new HashMap<>(_degreesById.values().size());

        for (Map.Entry<Integer, Degree> degree : _degreesById.entrySet()) {

            matchedDegrees.put(degree.getKey(), true);

            for (DegreeClass degreeClass : degree.getValue().getClassesAsList()) {

                if (!_degreeMatches.containsKey(degreeClass.getId())) {
                    matchedDegrees.put(degree.getKey(), false);
                    break; // found one not matched, jump to next degree
                }
            }

        }

        return matchedDegrees;

    }

    public Map<String, SweScore> getDegreeClassRankings() {
        return _degreeClassRankings;
    }


    public boolean hasCompleteMatch(int degreeId) {
        return _matchedDegrees.get(degreeId);
    }


    public void calculateDegreesRankings() {
        new YearlyRankingCalculator().execute();
    }


    private class YearlyRankingCalculator extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {


            return null;
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
