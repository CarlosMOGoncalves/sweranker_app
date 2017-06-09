package pt.cmg.sweranker;

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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

/**
 * Created by Carlos on 08/06/2017.
 */
@Singleton
public class ResourcesSwebokRepository implements SwebokRepository {

    private Context _context;
    private MutableLiveData<List<KnowledgeArea>> _knowledgeAreas;
    private MutableLiveData<List<KnowledgeAreaTopic>> _knowledgeAreaTopics;

    @Inject
    public ResourcesSwebokRepository(Context context) {
        _context = context;
    }


    @Override
    public LiveData<List<KnowledgeArea>> loadKnowledgeAreas() {

        new AsyncTask<Void, Void, List<KnowledgeArea>>() {

            @Override
            protected List<KnowledgeArea> doInBackground(Void... voids) {
                return loadKnowledgeAreasFromXML();
            }


            @Override
            protected void onPostExecute(List<KnowledgeArea> knowledgeAreas) {
                _knowledgeAreas.setValue(knowledgeAreas);
            }
        }.execute();

        return _knowledgeAreas;
    }


    /**
     * Loads all the Knowledge Areas from an xml file located at res/raw.
     *
     * @return
     */
    private List<KnowledgeArea> loadKnowledgeAreasFromXML() {

        List<KnowledgeArea> knowledgeAreas = new ArrayList<>();

        try {
            XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
            InputStream reader = _context.getResources().openRawResource(R.raw.knowledge_areas);

            xmlParser.setInput(reader, null);
            int eventType = xmlParser.getEventType();
            KnowledgeArea knowledgeArea = null;
            int currentKAId = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {


                String xmlElementName;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        // Just do your stuff
                        break;
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        if (xmlElementName.equals("knowledge-area")) {
                            knowledgeArea = new KnowledgeArea();
                        } else if (knowledgeArea != null) {
                            switch (xmlElementName) {
                                case "name":
                                    knowledgeArea.setNameResource(_context.getResources().getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                case "image":
                                    knowledgeArea.setImageResource(_context.getResources().getIdentifier(xmlParser.nextText(), "drawable", _context.getPackageName()));
                                    break;
                                case "imageBackgroundColour":
                                    knowledgeArea.setColourResource(_context.getResources().getIdentifier(xmlParser.nextText(), "color", _context.getPackageName()));
                                    break;
                                case "description":
                                    knowledgeArea.setDescriptionResource(_context.getResources().getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                // NOTE: since XMLParser is iterator based the id MUST come before topics (in the XML), otherwise this all falls apart
                                case "id":
                                    currentKAId = Integer.valueOf(xmlParser.nextText());
                                    knowledgeArea.setId(currentKAId);
                                    break;
                                case "topics":
                                    List<KnowledgeAreaTopic> topics = parseTopics(currentKAId, xmlParser);
                                    knowledgeArea.setTopics(topics);
                                    break;
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        xmlElementName = xmlParser.getName();
                        if (xmlElementName.equalsIgnoreCase("knowledge-area") && knowledgeArea != null) {
                            //One last colour added so that I can use it in the future
                            for (KnowledgeAreaTopic topic : knowledgeArea.getTopics()) {
                                topic.setColorResource(knowledgeArea.getColourResource());
                            }
                            knowledgeAreas.add(knowledgeArea);
                        }
                        break;
                }
                eventType = xmlParser.next();
            }

        } catch (XmlPullParserException | Resources.NotFoundException | IOException e) {
            e.printStackTrace();
        }

        return knowledgeAreas;
    }

    /**
     * Parses and returns each topic on a knowledge area of an xml file kept in res/raw
     *
     * @param xmlParser
     * @return
     */
    private List<KnowledgeAreaTopic> parseTopics(int currentKnowledgeAreaId, XmlPullParser xmlParser) {
        List<KnowledgeAreaTopic> topics = new ArrayList<>();
        try {

            int eventType = xmlParser.getEventType();
            String xmlElementName = xmlParser.getName();
            KnowledgeAreaTopic topic = null;

            // if it is the first let's just go to next to start iteration
            if (xmlElementName.equalsIgnoreCase("topics") && eventType == XmlPullParser.START_TAG) {
                eventType = xmlParser.nextTag();
                xmlElementName = xmlParser.getName();
            } else {
                throw new XmlPullParserException("Malformed xml: there is no topics element, you screwed up.");
            }

            while (!xmlElementName.equalsIgnoreCase("topics")) {

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        if (xmlElementName.equals("topic")) {
                            topic = new KnowledgeAreaTopic(currentKnowledgeAreaId);
                        } else if (topic != null) {
                            switch (xmlElementName) {
                                case "name":
                                    topic.setNameResource(_context.getResources().getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                case "id":
                                    topic.setId(Integer.valueOf(xmlParser.nextText()));
                                    break;
                                case "description":
                                    topic.setDescriptionResource(_context.getResources().getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                default:
                                    Log.e("SwebokLoaderService", "No known element: " + xmlElementName);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        xmlElementName = xmlParser.getName();
                        if (xmlElementName.equalsIgnoreCase("topic") && topic != null) {
                            topics.add(topic);
                        }
                        break;
                }

                eventType = xmlParser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return topics;
    }

    /**
     * Loads all KnowledgeAreaTopics from the previously load KnowledgeAreas.
     * This will be used mostly as an accelerator for future data questioning.
     *
     * @return
     */
    private List<KnowledgeAreaTopic> loadKnowledgeAreaTopics() {
        List<KnowledgeAreaTopic> allTopics = new ArrayList<>();
        for (KnowledgeArea ka : _knowledgeAreas.getValue()) {
            allTopics.addAll(ka.getTopics());
        }

        return allTopics;
    }

}
