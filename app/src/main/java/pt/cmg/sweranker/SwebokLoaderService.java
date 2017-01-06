package pt.cmg.sweranker;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class SwebokLoaderService extends Service {

    private List<KnowledgeArea> _knowledgeAreas;

    private SwebokLoaderBinder _binder = new SwebokLoaderBinder();


    @Override
    public void onCreate() {
        super.onCreate();
        _knowledgeAreas = loadKnowledgeAreasFromXML();

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
            InputStream reader = this.getResources().openRawResource(R.raw.knowledge_areas);

            xmlParser.setInput(reader, null);
            int eventType = xmlParser.getEventType();
            KnowledgeArea knowledgeArea = null;

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
                            if (xmlElementName.equals("name")) {
                                knowledgeArea.setName(xmlParser.nextText());
                            } else if (xmlElementName.equals("description")) {
                                knowledgeArea.setDescription(xmlParser.nextText());
                            } else if (xmlElementName.equals("image")) {
                                knowledgeArea.setImage(getResources().getIdentifier(xmlParser.nextText(), "drawable", this.getPackageName()));
                            } else if (xmlElementName.equalsIgnoreCase("topics")) {
                                List<KnowledgeAreaTopic> topics = parseTopics(xmlParser);
                                knowledgeArea.setTopics(topics);
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        xmlElementName = xmlParser.getName();
                        if (xmlElementName.equalsIgnoreCase("knowledge-area") && knowledgeArea != null) {
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
    private List<KnowledgeAreaTopic> parseTopics(XmlPullParser xmlParser) {
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
                            topic = new KnowledgeAreaTopic();
                        } else if (topic != null) {
                            if (xmlElementName.equals("name")) {
                                topic.setName(xmlParser.nextText());
                            } else if (xmlElementName.equals("description")) {
                                topic.setDescription(xmlParser.nextText());
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    protected List<KnowledgeArea> getKnowledgeAreas() {
        return _knowledgeAreas;
    }


    /**
     * Interface de comunicação com a Activity ou outro componente que a chamar.
     */
    protected class SwebokLoaderBinder extends Binder {
        public SwebokLoaderService getService() {
            return SwebokLoaderService.this;
        }
    }

}
