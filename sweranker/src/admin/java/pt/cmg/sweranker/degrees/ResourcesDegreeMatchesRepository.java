package pt.cmg.sweranker.degrees;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import pt.cmg.sweranker.R;

public class ResourcesDegreeMatchesRepository implements DegreeMatchesRepository {

    private static final String STANDARD_DIRECTORY = "custom_matches";

    private Context _context;

    /**
     * Keys -> Degree Class IDs : Values -> its DegreeClassMatch
     */
    private MutableLiveData<Map<String, DegreeClassMatch>> _systemMatches;

    /**
     * Keys -> Degree Class IDs : Values -> its DegreeClassMatch
     */
    private MutableLiveData<Map<String, DegreeClassMatch>> _customMatches;

    @Inject
    public ResourcesDegreeMatchesRepository(Context context) {
        _context = context;
        _systemMatches = new MutableLiveData<>();
        _customMatches = new MutableLiveData<>();
    }


    @Override
    public LiveData<Map<String, DegreeClassMatch>> loadMatches() {

        // E que tal um null-check no _systemMatches para não forçar o carregamento?
        new SystemDefaultMatchesLoader().execute();
        new SystemCustomMatchesLoader().execute();
        return _systemMatches;
    }

    /**
     * This is an AsyncTask whose goal is just to load ALL the SYSTEM DEFAULT matches from the Resources to this repository
     */
    private class SystemDefaultMatchesLoader extends AsyncTask<Void, Void, Map<String, DegreeClassMatch>> {

        @Override
        protected Map<String, DegreeClassMatch> doInBackground(Void... voids) {
            return loadDefaultMatches();
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
                InputStream reader = _context.getResources().openRawResource(R.raw.default_matches);
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
                Log.e("SweRanker-Matches", e.getLocalizedMessage());
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
                Log.e("SweRanker-Matches", e.getLocalizedMessage());
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
                Log.e("SweRanker-Matches", e.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(Map<String, DegreeClassMatch> loadedMatches) {
            _systemMatches.setValue(loadedMatches);
        }
    }

    /**
     * This is an AsyncTask whose goal is just to load ALL the CUSTOM matches from the Resources to this repository
     */
    private class SystemCustomMatchesLoader extends AsyncTask<Void, Void, Map<String, DegreeClassMatch>> {

        @Override
        protected Map<String, DegreeClassMatch> doInBackground(Void... voids) {
            return loadCustomMatches();
        }

        /**
         * This loads the custom matches saved into the system.
         * This is ONLY AVAILABLE in the Admin variant of the app.
         * It will search through this app data, under the correct folder, by all the files
         * that match the regular expression for each degree class and load each of them for the custom matches variable.
         *
         * @return a Map whose keys are the Degree Class Ids and the values are the matching Degree Class Match
         */
        private Map<String, DegreeClassMatch> loadCustomMatches() {

            Map<String, DegreeClassMatch> matches = new HashMap<>();

            try {

                File directory = _context.getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);
                File[] matchFiles = directory.listFiles();

                for (File matchFile : matchFiles) {
                    // It will match something like this "uc_miei_c120.xml"
                    if (matchFile.getName().matches(".*_.*_c\\d+.xml")) {

                        DegreeClassMatch matchRead = loadDegreeMatch(matchFile);
                        matches.put(matchRead.getDegreeClassId(), matchRead);
                    }
                }
            } catch (Resources.NotFoundException e) {
                Log.e("SweRanker-Matches", e.getLocalizedMessage());
            }
            return matches;
        }


        /**
         * Loads an individual degree match from its XML.
         *
         * @return
         */
        private DegreeClassMatch loadDegreeMatch(File sourceFile) {


            DegreeClassMatch currentMatch = new DegreeClassMatch();

            try {

                XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
                FileInputStream reader = new FileInputStream(sourceFile);
                xmlParser.setInput(reader, null);

                int eventType = xmlParser.getEventType();

                xmlParser.nextTag();

                String xmlElementName;

                while (eventType != XmlPullParser.END_DOCUMENT) {

                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            // ok, start document, let's keep going to next tag
                            break;
                        case XmlPullParser.START_TAG:
                            xmlElementName = xmlParser.getName();

                            switch (xmlElementName) {
                                case "match":
                                    break;
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
                            break;
                    }
                    eventType = xmlParser.next();
                }

            } catch (XmlPullParserException | IOException e) {
                Log.e("SweRanker-Matches", e.getLocalizedMessage());
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
                Log.e("SweRanker-Matches", e.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(Map<String, DegreeClassMatch> loadedMatches) {
            _customMatches.setValue(loadedMatches);
        }
    }


    @Override
    public boolean hasMatch(String degreeClassId) {

        // Basically it returns true if I found a match in either the system defaults or the custom matches
        // Yes, it could be a single statement but it would be really hard to understand and I have some self-respect over the future me
        if (_systemMatches.getValue() != null && _systemMatches.getValue().containsKey(degreeClassId)) {
            return true;
        }
        if (_customMatches.getValue() != null && _customMatches.getValue().containsKey(degreeClassId)) {
            return true;
        }
        return false;
    }

    @Override
    public DegreeClassMatch getDegreeClassMatch(String degreeClassId) {

        if (_customMatches.getValue() != null && _customMatches.getValue().containsKey(degreeClassId)) {
            return _customMatches.getValue().get(degreeClassId);
        }
        if (_systemMatches.getValue() != null && _systemMatches.getValue().containsKey(degreeClassId)) {
            return _systemMatches.getValue().get(degreeClassId);
        }

        // Oops
        return null;
    }

    @Override
    public boolean saveMatch(DegreeClassMatch classMatch) {

        Map<String, DegreeClassMatch> customMatches = _customMatches.getValue();

        if (customMatches == null) {
            customMatches = new HashMap<>();
        }

        boolean isSaved = saveMatchToFile(classMatch);

        if (isSaved) {
            customMatches.put(classMatch.getDegreeClassId(), classMatch);
        }

        return isSaved;
    }

    /**
     * Saves a Degree Match to a single file in this app data reserved space.
     * This is a feature used ONLY IN ADMIN variant.
     * Saving custom matches has two goals: to actually fill the data in the first place
     * and potentially to be able to use custom matches if unhappy with the current state of things.
     * In any case, the only purpose for now is merely the input of matches to be consumed in the client version.
     *
     * @param classMatch the Degree Match to save
     * @return true if saving was successful, false otherwise
     */
    private boolean saveMatchToFile(DegreeClassMatch classMatch) {

        String xmlFileName = classMatch.getDegreeClassId() + ".xml";

        File directory = _context.getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);

        File targetFile = new File(directory, xmlFileName);

        // FileOutputStream guarantees the overwrite of the file, neat.
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {

            XmlSerializer xmlSerializer = Xml.newSerializer();

            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);


            xmlSerializer.startTag(null, "match");
            xmlSerializer.startTag(null, "degree_class_id");
            xmlSerializer.text(classMatch.getDegreeClassId());
            xmlSerializer.endTag(null, "degree_class_id");

            xmlSerializer.startTag(null, "degree_id");
            xmlSerializer.text(String.valueOf(classMatch.getDegreeId()));
            xmlSerializer.endTag(null, "degree_id");

            xmlSerializer.startTag(null, "topic_matches");
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

            xmlSerializer.endTag(null, "topic_matches");

            xmlSerializer.endTag(null, "match");

            xmlSerializer.endDocument();

            outputStream.write(writer.toString().getBytes());

            return true;
        } catch (Exception e) {
            Log.e("SweRanker-Matches", e.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean saveMatchesToSingleFile(int degreeId) {

        Map<String, DegreeClassMatch> matchesToSave = getMatchesOfDegree(degreeId);

        final String xmlFileName = degreeId + "_all_" + "matches.xml";

        File directory = _context.getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);

        File targetFile = new File(directory, xmlFileName);

        // FileOutputStream guarantees the overwrite of the file, neat.
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {

            XmlSerializer xmlSerializer = Xml.newSerializer();

            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag(null, "all_matches");

            // Here it will save each and every entry on the custom matches variable
            for (Map.Entry<String, DegreeClassMatch> singleMatch : matchesToSave.entrySet()) {

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
            }

            xmlSerializer.endTag(null, "all_matches");
            xmlSerializer.endDocument();

            outputStream.write(writer.toString().getBytes());
            return true;
        } catch (Exception e) {
            Log.e("SweRanker-Matches", e.getLocalizedMessage());
            return false;
        }
    }


    /**
     * Returns a Map with all the matches found for this degree.
     * As an important nuance this will overwrite all the system default matches
     * IF there is one custom match.
     * <p>
     * This is relevant because it will serve the file saving function with the most up-to-date
     * matches.
     *
     * @param degreeId the degree id that the matches belong to
     * @return a Map where the keys are the Degree Class Ids and the values are the matching Matches (yeah, pun)
     */
    private Map<String, DegreeClassMatch> getMatchesOfDegree(int degreeId) {

        Map<String, DegreeClassMatch> matchesFound = new HashMap<>();

        if (_systemMatches.getValue() != null) {
            for (DegreeClassMatch degreeMatch : _systemMatches.getValue().values()) {
                if (degreeMatch.getDegreeId() == degreeId) {
                    matchesFound.put(degreeMatch.getDegreeClassId(), degreeMatch);
                }
            }
        }

        // Custom Matches have precedence, so I will overwrite it
        if (_customMatches.getValue() != null) {
            for (DegreeClassMatch degreeMatch : _customMatches.getValue().values()) {
                if (degreeMatch.getDegreeId() == degreeId) {
                    matchesFound.put(degreeMatch.getDegreeClassId(), degreeMatch);
                }
            }
        }

        return matchesFound;
    }

}
