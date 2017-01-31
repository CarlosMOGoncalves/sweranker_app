package pt.cmg.sweranker.degrees;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Carlos on 24/01/2017.
 */

public class DegreeClass {

    private String _id;
    private int _nameResource;
    private int _year;
    private int _semester;
    private float _ectsCredits;
    private Map<String, Integer> _program;

    public DegreeClass() {
        _program = new HashMap<>();
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public int getNameResource() {
        return _nameResource;
    }

    public void setNameResource(int nameResource) {
        _nameResource = nameResource;
    }

    public int getYear() {
        return _year;
    }

    public void setYear(int year) {
        _year = year;
    }

    public int getSemester() {
        return _semester;
    }

    public void setSemester(int semester) {
        _semester = semester;
    }

    public float getEctsCredits() {
        return _ectsCredits;
    }

    public void setEctsCredits(float ectsCredits) {
        _ectsCredits = ectsCredits;
    }

    public Map<String, Integer> getProgram() {
        return _program;
    }

    public void setProgram(Map<String, Integer> program) {
        _program = program;
    }

    public void addProgramTopic(String topicId, int topicDescriptionResource) {
        _program.put(topicId, topicDescriptionResource);
    }
}
