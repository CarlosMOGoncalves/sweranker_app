package pt.cmg.sweranker.degrees;


import java.util.List;
import java.util.Map;

public class Degree {

    private int _id;
    private int _nameResource;
    private int _fullNameResource;
    private int _imageResource;
    private int _descriptionResource;
    private int _years;
    private int _universityResource;

    private Map<Integer, List<DegreeClass>> _classesByYear;

    public Degree() {
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public int getNameResource() {
        return _nameResource;
    }

    public void setNameResource(int nameResource) {
        _nameResource = nameResource;
    }

    public int getImageResource() {
        return _imageResource;
    }

    public void setImageResource(int imageResource) {
        _imageResource = imageResource;
    }

    public int getDescriptionResource() {
        return _descriptionResource;
    }

    public void setDescriptionResource(int descriptionResource) {
        _descriptionResource = descriptionResource;
    }

    public int getYears() {
        return _years;
    }

    public void setYears(int years) {
        _years = years;
    }

    public int getUniversityResource() {
        return _universityResource;
    }

    public void setUniversityResource(int universityResource) {
        _universityResource = universityResource;
    }

    public Map<Integer, List<DegreeClass>> getClasses() {
        return _classesByYear;
    }

    public void setClasses(Map<Integer, List<DegreeClass>> classes) {
        _classesByYear = classes;
    }

    /**
     * Returns the total amount of classes in this degree using some lambda magic.
     *
     * @return
     */
    public int getClassesCount() {
        return _classesByYear.entrySet().stream().mapToInt(entry -> entry.getValue().size()).sum();
    }


    public List<DegreeClass> getClassesOfYear(int year) {
        if (year > getYears()) {
            throw new RuntimeException("Target years greater than the maximum number of availble years for this degree, input : " + year + " but maximum expected is " + getYears());
        }

        return _classesByYear.get(year);

    }

    public int getFullNameResource() {
        return _fullNameResource;
    }

    public void setFullNameResource(int fullNameResource) {
        _fullNameResource = fullNameResource;
    }
}
