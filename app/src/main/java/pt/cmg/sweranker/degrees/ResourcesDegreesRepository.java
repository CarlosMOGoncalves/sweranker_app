package pt.cmg.sweranker.degrees;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import pt.cmg.sweranker.R;
import pt.cmg.sweranker.ranking.combinationstrategies.ClassCombinationStrategy;

public class ResourcesDegreesRepository implements DegreesRepository {


    private Context _context;
    private MutableLiveData<List<Degree>> _degrees;

    @Inject
    public ResourcesDegreesRepository(Context context) {
        _context = context;
        _degrees = new MutableLiveData<>();
    }


    @Override
    public LiveData<List<Degree>> loadDegrees() {

        // E que tal um null-check no _degrees.getValue() para não carregar sempre?
        new AsyncTask<Void, Void, List<Degree>>() {

            @Override
            protected List<Degree> doInBackground(Void... voids) {
                return loadDegreesFromXML();
            }


            @Override
            protected void onPostExecute(List<Degree> returnedDegrees) {
                _degrees.postValue(returnedDegrees);
            }
        }.execute();

        return _degrees;
    }


    /**
     * Loads all the Degrees from an xml file located at res/raw.
     *
     * @return
     */
    private List<Degree> loadDegreesFromXML() {

        List<Degree> degrees = new ArrayList<>();
        Resources resources = _context.getResources();

        try {
            XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
            InputStream reader = _context.getResources().openRawResource(R.raw.degrees);

            xmlParser.setInput(reader, null);
            int eventType = xmlParser.getEventType();
            Degree degree = null;
            int currentDegreeId = 0;

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
                                    degree.setNameResource(resources.getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                case "fullname":
                                    degree.setFullNameResource(resources.getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                case "image":
                                    degree.setImageResource(resources.getIdentifier(xmlParser.nextText(), "drawable", _context.getPackageName()));
                                    break;
                                case "description":
                                    degree.setDescriptionResource(resources.getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                case "id":
                                    currentDegreeId = Integer.valueOf(xmlParser.nextText());
                                    degree.setId(currentDegreeId);
                                    break;
                                case "years":
                                    degree.setYears(Integer.valueOf(xmlParser.nextText()));
                                    break;
                                case "yearly_combinators":
                                    parseCombinationStrategies(degree, xmlParser);
                                    break;
                                case "university":
                                    degree.setUniversityResource(resources.getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                case "classes":
                                    Map<Integer, List<DegreeClass>> classes = parseClasses(currentDegreeId, xmlParser);
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
    private Map<Integer, List<DegreeClass>> parseClasses(int degreeId, XmlPullParser xmlParser) {
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
                            degreeClass = new DegreeClass(degreeId);

                        } else if (degreeClass != null) {
                            switch (xmlElementName) {
                                case "id":
                                    degreeClass.setId(xmlParser.nextText());
                                    break;
                                case "name":
                                    degreeClass.setNameResource(_context.getResources().getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
                                    break;
                                case "description":
                                    degreeClass.setDescriptionResource(_context.getResources().getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
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

        Map<String, Integer> program = new LinkedHashMap<>();

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
                                    descriptionResource = new Integer(_context.getResources().getIdentifier(xmlParser.nextText(), "string", _context.getPackageName()));
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

    private void parseCombinationStrategies(Degree degree, XmlPullParser xmlParser) {

        try {

            int eventType = xmlParser.getEventType();
            String xmlElementName = xmlParser.getName();

            // if it is the first let's just go to next to start iteration
            if (xmlElementName.equalsIgnoreCase("yearly_combinators") && eventType == XmlPullParser.START_TAG) {
                eventType = xmlParser.nextTag();
                xmlElementName = xmlParser.getName();
            } else {
                throw new XmlPullParserException("Malformed xml: there is no 'yearly_combinations' element, you screwed up.");
            }

            String topicId = "";
            Integer descriptionResource = null;

            while (!xmlElementName.equalsIgnoreCase("yearly_combinators")) {

                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        // this will not happen here because we are no longer at the root of the document but hey... it's 2 a.m.
                        break;
                    case XmlPullParser.START_TAG:
                        xmlElementName = xmlParser.getName();

                        if (xmlElementName.equals("combinator")) {
                            Integer year;
                            ClassCombinationStrategy pickerStrategy;

                            //Go to next tag = year
                            xmlParser.nextTag();
                            //Get year this also puts the parser in the end tag = year
                            year = Integer.valueOf(xmlParser.nextText());

                            // Go to next tag = class
                            xmlParser.nextTag();

                            Class<?> strategy = Class.forName(StringUtils.trim(xmlParser.nextText()));
                            pickerStrategy = (ClassCombinationStrategy) strategy.newInstance();

                            degree.addClassCombinatonStrategy(year, pickerStrategy);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlParser.nextTag();
                xmlElementName = xmlParser.getName();
            }

        } catch (XmlPullParserException | IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Log.e("DegreeLoader", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

}
