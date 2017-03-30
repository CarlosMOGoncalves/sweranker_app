package pt.cmg.sweranker.ranking;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an annual combination of classes.
 * It is a collection of degree class ids plus an id that (hopefully) uniquely identifies this
 * combination itself.
 */
public class AnnualClassCombination {


    private String _annualCombinationId;
    private int _year;
    private List<String> _degreeClassIds;

    public AnnualClassCombination(String annualCombinationId) {
        _annualCombinationId = annualCombinationId;
        _year = 0;
        _degreeClassIds = new ArrayList<>();
    }

    public AnnualClassCombination(AnnualClassCombination annualCombination) {
        _annualCombinationId = new String(annualCombination.getId());
        _year = annualCombination.getYear();
        _degreeClassIds = new ArrayList<>(annualCombination.getDegreeClassIds());
    }

    public String getId() {
        return _annualCombinationId;
    }

    public void setId(String annualCombinationId) {
        _annualCombinationId = annualCombinationId;
    }

    public int getYear() {
        return _year;
    }

    public void setYear(int year) {
        _year = year;
    }

    public List<String> getDegreeClassIds() {
        return _degreeClassIds;
    }

    public void setDegreeClasses(List<String> degreeClassIds) {
        _degreeClassIds = degreeClassIds;
    }

    public void addDegreeClasses(List<String> degreeClassIds) {
        _degreeClassIds.addAll(degreeClassIds);
    }

    public void addDegreeClass(String degreeClassId) {
        _degreeClassIds.add(degreeClassId);
    }
}
