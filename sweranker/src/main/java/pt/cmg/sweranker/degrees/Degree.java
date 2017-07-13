package pt.cmg.sweranker.degrees;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.cmg.sweranker.ranking.combinationstrategies.ClassCombinationStrategy;

public class Degree {

    private int _id;
    private int _nameResource;
    private int _fullNameResource;
    private int _imageResource;
    private int _descriptionResource;
    private int _years;
    private int _universityResource;

    private Map<Integer, List<DegreeClass>> _classesByYear;

    /**
     * Keys -> the year of the degree , Values -> the strategy used to calculate the combination of classes for this particular year.
     */
    private Map<Integer, ClassCombinationStrategy> _classCombinationStrategyByYear;

    public Degree() {
        _classesByYear = new HashMap<>();
        _classCombinationStrategyByYear = new HashMap<>();
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

    /**
     * Returns a Map where each key is one year of the degree and its values is the total list of
     * classes for that year.
     *
     * @return Keys -> Year of the degree (e.g. 1) , Values -> List of classes for that year
     */
    public Map<Integer, List<DegreeClass>> getClasses() {
        return _classesByYear;
    }

    public List<DegreeClass> getClassesAsList() {
        List<DegreeClass> flatClasses = new ArrayList<>();
        for (List<DegreeClass> classes : _classesByYear.values()) {
            flatClasses.addAll(classes);
        }
        return flatClasses;
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
        int count = 0;
        for (List<DegreeClass> classes : _classesByYear.values()) {
            count += classes.size();
        }
        return count;
        // Lambdas just screw the Jack compiler, so let's stick without it for a while
//        return _classesByYear.entrySet().stream().mapToInt(entry -> entry.getValue().size()).sum();
    }


    public List<DegreeClass> getClassesOfYear(int year) {
        if (year > getYears()) {
            throw new RuntimeException("Target years greater than the maximum number of availble years for this degree, input : " + year + " but maximum expected is " + getYears());
        }

        return _classesByYear.get(year);
    }


    public boolean hasDegreeClass(String degreeClassId) {

//                Again, Jack compiler screwed this...
//                Optional<DegreeClass> foundIt = classes.stream().filter(dc -> dc.getId().equals(degreeClassId)).findAny();
//                if (foundIt.isPresent()) {
//                    return true;
//                }

        for (List<DegreeClass> classes : _classesByYear.values()) {
            for (DegreeClass dClass : classes) {
                if (dClass.getId().equals(degreeClassId)) {
                    return true;
                }
            }
        }

        return false;
    }

    public DegreeClass getDegreeClass(String degreeClassId) {

//            Again, Jack compiler screwed this...
//                Optional<DegreeClass> foundIt = classes.stream().filter(dc -> dc.getId().equals(degreeClassId)).findAny();
//                if (foundIt.isPresent()) {
//                    return foundIt.get();
//                }
        for (List<DegreeClass> classes : _classesByYear.values()) {
            for (DegreeClass dClass : classes) {
                if (dClass.getId().equals(degreeClassId)) {
                    return dClass;
                }
            }
        }

        return new DegreeClass();
    }

    public int getFullNameResource() {
        return _fullNameResource;
    }

    public void setFullNameResource(int fullNameResource) {
        _fullNameResource = fullNameResource;
    }

    public void addClassCombinatonStrategy(int year, ClassCombinationStrategy combinationStrategy) {
        _classCombinationStrategyByYear.put(year, combinationStrategy);
    }

    public Map<Integer, ClassCombinationStrategy> getClassCombinationStrategies() {
        return _classCombinationStrategyByYear;
    }
}
