package pt.cmg.sweranker.ranking;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.degrees.DegreeClassMatch;

public class ResourcesMatchesRepository implements MatchesRepository {


    private Context _context;
    private MutableLiveData<Map<String, DegreeClassMatch>> _systemMatches;

    @Inject
    public ResourcesMatchesRepository(Context context) {
        _context = context;
        _systemMatches = new MutableLiveData<>();
    }


    @Override
    public LiveData<Map<String, DegreeClassMatch>> loadMatches() {

        // E que tal um null-check no _systemMatches para não forçar o carregamento?
        new AsyncTask<Void, Void, Map<String, DegreeClassMatch>>() {
            @Override
            protected Map<String, DegreeClassMatch> doInBackground(Void... voids) {
                return loadDefaultMatches();
            }

            @Override
            protected void onPostExecute(Map<String, DegreeClassMatch> loadedMatches) {
                _systemMatches.setValue(loadedMatches);
            }
        }.execute();

        return _systemMatches;
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
            Log.e("Matches Repository", e.getLocalizedMessage());
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
            Log.e("Matches Repository", e.getLocalizedMessage());
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
            Log.e("Matches Repository", e.getLocalizedMessage());
        }
    }

}
