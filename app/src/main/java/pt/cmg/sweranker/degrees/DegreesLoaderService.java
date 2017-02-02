package pt.cmg.sweranker.degrees;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.R;

public class DegreesLoaderService extends Service {

    private DegreesLoaderBinder _binder = new DegreesLoaderBinder();

    private List<Degree> _degrees;

    public DegreesLoaderService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _degrees = loadDegreesFromXML();
    }

    /**
     * Loads all the Degrees from an xml file located at res/raw.
     *
     * @return
     */
    private List<Degree> loadDegreesFromXML() {

        List<Degree> degrees = new ArrayList<>();

        try {
            XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
            InputStream reader = this.getResources().openRawResource(R.raw.degrees);

            xmlParser.setInput(reader, null);
            int eventType = xmlParser.getEventType();
            Degree degree = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {


                String xmlElementName;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        // ok, start document, let's keep going to next tag
                        break;
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        if (xmlElementName.equals("degree")) {
                            degree = new Degree();
                        } else if (degree != null) { // If degree exists then it was created above an it's time to parse it.
                            switch (xmlElementName) {
                                case "name":
                                    degree.setNameResource(getResources().getIdentifier(xmlParser.nextText(), "string", this.getPackageName()));
                                    break;
                                case "fullname":
                                    degree.setFullNameResource(getResources().getIdentifier(xmlParser.nextText(), "string", this.getPackageName()));
                                    break;
                                case "image":
                                    degree.setImageResource(getResources().getIdentifier(xmlParser.nextText(), "drawable", this.getPackageName()));
                                    break;
                                case "description":
                                    degree.setDescriptionResource(getResources().getIdentifier(xmlParser.nextText(), "string", this.getPackageName()));
                                    break;
                                case "id":
                                    degree.setId(Integer.valueOf(xmlParser.nextText()));
                                    break;
                                case "years":
                                    degree.setYears(Integer.valueOf(xmlParser.nextText()));
                                    break;
                                case "university":
                                    degree.setUniversityResource(getResources().getIdentifier(xmlParser.nextText(), "string", this.getPackageName()));
                                    break;
                                case "classes":
                                    Map<Integer, List<DegreeClass>> classes = parseClasses(xmlParser);
                                    degree.setClasses(classes);
                                    break;
                                default:
                                    throw new XmlPullParserException("Unknown tag: " + xmlElementName);
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        xmlElementName = xmlParser.getName();
                        if (xmlElementName.equalsIgnoreCase("degree") && degree != null) {
                            degrees.add(degree);
                        }
                        break;
                }
                eventType = xmlParser.next();
            }

        } catch (XmlPullParserException | Resources.NotFoundException |
                IOException e) {
            e.printStackTrace();
        }

        return degrees;
    }


    /**
     * Parses and returns each class of a degree of an xml file kept in res/raw
     *
     * @param xmlParser
     * @return
     */
    private Map<Integer, List<DegreeClass>> parseClasses(XmlPullParser xmlParser) {
        Map<Integer, List<DegreeClass>> classesByYear = new HashMap<>();
        try {

            int eventType = xmlParser.getEventType();
            String xmlElementName = xmlParser.getName();
            DegreeClass degreeClass = null;

            // if it is the first let's just go to next to start iteration
            if (xmlElementName.equalsIgnoreCase("classes") && eventType == XmlPullParser.START_TAG) {
                eventType = xmlParser.nextTag();
                xmlElementName = xmlParser.getName();
            } else {
                throw new XmlPullParserException("Malformed xml: there is no classes element, you screwed up.");
            }

            while (!xmlElementName.equalsIgnoreCase("classes")) {

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        // will not happen here because we are no longer at the root of the document but hey... it's 2 a.m.
                        break;
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        if (xmlElementName.equals("class")) {
                            degreeClass = new DegreeClass();

                        } else if (degreeClass != null) {
                            switch (xmlElementName) {
                                case "id":
                                    degreeClass.setId(xmlParser.nextText());
                                    break;
                                case "name":
                                    degreeClass.setNameResource(getResources().getIdentifier(xmlParser.nextText(), "string", this.getPackageName()));
                                    break;
                                case "year":
                                    degreeClass.setYear(Integer.valueOf(xmlParser.nextText()));
                                    break;
                                case "semester":
                                    degreeClass.setSemester(Integer.valueOf(xmlParser.nextText()));
                                    break;
                                case "optional":
                                    degreeClass.setOptionalClass(Boolean.valueOf(xmlParser.nextText()));
                                    break;
                                case "ects":
                                    degreeClass.setEctsCredits(Float.valueOf(xmlParser.nextText()));
                                    break;
                                case "program":
                                    Map<String, Integer> program = parseClassProgram(xmlParser);
                                    degreeClass.setProgram(program);
                                    break;
                                default:
                                    throw new XmlPullParserException("Unknown tag: " + xmlElementName);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        xmlElementName = xmlParser.getName();
                        if (xmlElementName.equalsIgnoreCase("class") && degreeClass != null) {

                            if (!classesByYear.containsKey(degreeClass.getYear())) {
                                classesByYear.put(Integer.valueOf(degreeClass.getYear()), new ArrayList<>());
                            }
                            classesByYear.get(degreeClass.getYear()).add(degreeClass);
                        }
                        break;
                }
                eventType = xmlParser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return classesByYear;
    }

    /**
     * Parses and returns each class of a degree of an xml file kept in res/raw
     *
     * @param xmlParser
     * @return
     */
    private Map<String, Integer> parseClassProgram(XmlPullParser xmlParser) {

        Map<String, Integer> program = new HashMap<>();

        try {

            int eventType = xmlParser.getEventType();
            String xmlElementName = xmlParser.getName();

            // if it is the first let's just go to next to start iteration
            if (xmlElementName.equalsIgnoreCase("program") && eventType == XmlPullParser.START_TAG) {
                eventType = xmlParser.nextTag();
                xmlElementName = xmlParser.getName();
            } else {
                throw new XmlPullParserException("Malformed xml: there is no 'program' element, you screwed up.");
            }

            String topicId = "";
            Integer descriptionResource = null;

            while (!xmlElementName.equalsIgnoreCase("program")) {

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        // this will not happen here because we are no longer at the root of the document but hey... it's 2 a.m.
                        break;
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        if (xmlElementName.equals("topic")) {
                            // nothing, this is just an opening tag
                            topicId = new String();
                        } else {
                            switch (xmlElementName) {
                                case "id":
                                    topicId = new String(xmlParser.nextText());
                                    break;
                                case "description":
                                    descriptionResource = new Integer(getResources().getIdentifier(xmlParser.nextText(), "string", this.getPackageName()));
                                    break;
                                default:
                                    throw new XmlPullParserException("Unknown tag: " + xmlElementName);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        xmlElementName = xmlParser.getName();
                        if (xmlElementName.equalsIgnoreCase("topic")) {
                            program.put(topicId, descriptionResource);
                        }
                        break;
                }
                eventType = xmlParser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return program;
    }

    public List<Degree> getDegrees() {
        return _degrees;
    }

    public Degree getDegree(int degreeId) {
        return _degrees.stream().filter(degree -> degree.getId() == degreeId).findFirst().orElse(new Degree());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    /**
     * Interface de comunicação com a Activity ou outro componente que a chamar.
     */
    public class DegreesLoaderBinder extends Binder {
        public DegreesLoaderService getService() {
            return DegreesLoaderService.this;
        }
    }
}
