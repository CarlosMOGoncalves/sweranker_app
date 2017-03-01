package pt.cmg.sweranker.degrees;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DegreeMatcherService extends Service {

    private static final String STANDARD_DIRECTORY = "default_matches";

    private DegreeMatcherBinder _binder = new DegreeMatcherBinder();


    // Keys -> Degree Class Id , Values -> its current matches
    private Map<String, DegreeClassMatch> _currentMatches;

    public DegreeMatcherService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        File rootMatchesDir = createMatchFilesDirectory();
//        _currentMatches = new HashMap<>();
        _currentMatches = loadSystemMatches(rootMatchesDir);
    }


    /**
     * Creates the root directory for saving the matches.
     */
    public File createMatchFilesDirectory() {
        return getApplicationContext().getDir(STANDARD_DIRECTORY, Context.MODE_PRIVATE);
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
                                    List<Integer> kaTopicIds = getKaTopics(xmlParser);
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
    private List<Integer> getKaTopics(XmlPullParser xmlParser) {
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
            Log.e("DegreeMatcherService", e.getLocalizedMessage());
        }

        return ids;
    }


    /**
     * Saves or overwrites a Degree Class Match into the internal storage system.
     *
     * @param classMatch
     */
    public void saveMatch(DegreeClassMatch classMatch) {

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

            Log.i("DegreeMatcherService", "Successfully saved match for:" + classMatch.getDegreeClassId());
            _currentMatches.put(classMatch.getDegreeClassId(), classMatch);

        } catch (FileNotFoundException e) {
            Log.e("DegreeMatcherService", "File " + xmlFileName + " not found.");
        } catch (IOException e) {
            Log.e("DegreeMatcherService", "Couldn't save file due to: ", e);
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
        return _currentMatches.get(degreeClassId) != null;
    }

    public DegreeClassMatch getDegreeClassMatches(String degreeClassId) {
        return _currentMatches.get(degreeClassId);
    }


    @Override

    public IBinder onBind(Intent intent) {
        return _binder;
    }


    /**
     * Interface de comunicação com a Activity ou outro componente que a chamar.
     */
    public class DegreeMatcherBinder extends Binder {
        public DegreeMatcherService getService() {
            return DegreeMatcherService.this;
        }
    }
}
