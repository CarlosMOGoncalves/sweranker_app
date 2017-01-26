package pt.cmg.sweranker.degrees;


import java.util.List;

public class Degree {

    private int _id;
    private int _nameResource;
    private int _fullNameResource;
    private int _imageResource;
    private int _descriptionResource;
    private int _years;
    private int _universityResource;

    private List<DegreeClass> _classes;

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

    public List<DegreeClass> getClasses() {
        return _classes;
    }

    public void setClasses(List<DegreeClass> classes) {
        _classes = classes;
    }

    public int getFullNameResource() {
        return _fullNameResource;
    }

    public void setFullNameResource(int fullNameResource) {
        _fullNameResource = fullNameResource;
    }
}
