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
    private MutableLiveData<Map<String, DegreeClassMatch>> _systemMatches;
    private MutableLiveData<Map<String, DegreeClassMatch>> _customMatches;

    @Inject
    public ResourcesDegreeMatchesRepository(Context context) {
        _context = context;
        _systemMatches = new MutableLiveData<>();
        _customMatches = new MutableLiveData<>();
        createCustomMatchesDirectory();
    }

    /**
     * Creates the root directory for saving the matches.
     * The reason it is here is to that it creates the directory beforehand, if it already exists, it does nothing.
     * <p>
     * This is used to save matches that were input but the user.
     * It will save one single file that can be used to either replace the system defaults
     * or used as a future merge with the system defaults.
     */
    private File createCustomMatchesDirectory() {
        return _context.getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);
    }


    @Override
    public LiveData<Map<String, DegreeClassMatch>> loadMatches() {

        // E que tal um null-check no _systemMatches para não forçar o carregamento?
        new MatchesLoader().execute();
        return _systemMatches;
    }

    private class MatchesLoader extends AsyncTask<Void, Void, Map<String, DegreeClassMatch>> {

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


    @Override
    public boolean saveMatch(DegreeClassMatch classMatch) {

        Map<String, DegreeClassMatch> customMatches = _customMatches.getValue();

        if (customMatches == null) {
            customMatches = new HashMap<>();
        }
        customMatches.put(classMatch.getDegreeClassId(), classMatch);

        return saveMatchesToSingleFile(customMatches);
    }

    private boolean saveMatchesToSingleFile(Map<String, DegreeClassMatch> customMatches) {
        final String xmlFileName = "custom_matches.xml";

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
            for (Map.Entry<String, DegreeClassMatch> singleMatch : customMatches.entrySet()) {

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

            // In the end we update the variable and only in the end
            _customMatches.setValue(customMatches);

            return true;
        } catch (Exception e) {
            Log.e("SweRanker-Matches", e.getLocalizedMessage());
            return false;
        }
    }

}
