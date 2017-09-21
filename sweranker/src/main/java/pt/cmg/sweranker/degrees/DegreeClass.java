package pt.cmg.sweranker.degrees;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A Degree Class is nothing more than a given class in a Degree where one subject is taught. Exactly,
 * like Calculus, Algebra, Object Oriented Programming and all that.
 * <p>
 * This is a very important concept in this whole application because what is actually EVALUATED is
 * the matches that EACH DEGREE CLASS has to the given SWEBOK.
 * <p>
 * The Degree Classes represent a challenge to represent because they are the computer data representation
 * of a quite complex real-world concept. Furthermore, this object must have a lot of information about
 * HOW these classes are organised in the degree they belong to. That is of the extreme importance because
 * depending on how they are organised there has to be a specific calculation for a given year structure
 * (i.e. what classes can I take this year? By what order? In what combination?). THIS was one of the
 * greatest challenges of this application.
 * <p>
 * Most of the attributes are pretty straightforward, ids, names, year, semester and so on are easy to get.
 * More complex ones though, require explanation.
 * </p>
 * <p>
 * The ECTS is a special concept under the Bologne Process that serves as the background for this thesis,
 * this is just a sort of score that is roughly translated to the importance of this degree class in the yearly
 * context - a higher value means that this class has a greater weight in the degree as a whole. This is fulcral
 * concept that I use to calculate how important is the score of the given class as a whole.
 * </p>
 * <p>
 * Optional means really that, if true, it means that this class can be taken or not, according to certain
 * business rules. The opposite means it MUST be taken in the given year.
 * </p>
 * <p>
 * The Path concept is a new one though.A path is an optional attribute of a Degree Class that is present only when
 * this Degree Class belongs to one or more Degree paths. This was created so that Degrees which have years
 * where one can take multiple paths, with or without exclusive classes, can be correctly calculated.
 * So a path is little more than a marker of a set of classes that just happen to be taught in a certain way.
 * <p>
 * When a degree class has one or more paths the _hasPaths variable is set to true and the array _paths will
 * have one or more entries. That means this class is available to those paths. The rules in which it is available
 * are further explained below.
 * </p>
 * <p>
 * This is more common in specialisation Master Degrees. For example, Universidade de Coimbra degree has
 * 4 paths in its Master Degree: Network, Software Engineering, IA and Information Systems. Although fancy,
 * that same Degree has absolutely ZERO classes exclusive to any given path, it is just a way to put together
 * a specific set of classes for a given path. Thanks a lot for that...
 * <p>
 * Now, some degree classes can be path specific, in which case the attribute _hasMandatoryPaths is true and
 * the array of _mandatoryPaths has at least one entry. This means that this class MUST be taken in a given path.
 * Automatically, when calculating an AnnualClassCombination this class will always be present in the mandatory
 * path, so it acts as a NON-OPTIONAL class for that year in that path.
 * </p>
 * <p>
 * Also, one class can be path-exclusive. That means it is mandatory ONLY for the given path and year.
 * It is NOT optional to any other path and can't even have more that one path in the _paths variable, apart
 * from the one it is exclusive to.
 * </p>
 */
public class DegreeClass {

    private String _id;
    private int _nameResource;
    private int _descriptionResource;
    private int _year;
    private int _semester;
    private float _ectsCredits;
    private boolean _isOptional;
    private int _degreeId;


    private boolean _hasPaths;
    private int[] _paths;

    private boolean _hasMandatoryPaths;
    private int[] _mandatoryPaths;

    private boolean _hasExclusivePath;
    private int _exclusivePath;


    // Keys -> program topic ID , Values -> topic description Resource
    private Map<String, Integer> _program;

    public DegreeClass() {
        _program = new HashMap<>();
    }

    public DegreeClass(int degreeId) {
        _program = new HashMap<>();
        _degreeId = degreeId;
        _hasPaths = false;
        _hasMandatoryPaths = false;
        _hasExclusivePath = false;
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

    public int getDescriptionResource() {
        return _descriptionResource;
    }

    public void setDescriptionResource(int descriptionResource) {
        _descriptionResource = descriptionResource;
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

    public int getDegreeId() {
        return _degreeId;
    }

    public void addProgramTopic(String topicId, int topicDescriptionResource) {
        _program.put(topicId, topicDescriptionResource);
    }

    public int getTopicCount() {
        return _program.keySet().size();
    }

    public boolean isOptionalClass() {
        return _isOptional;
    }

    public void setOptionalClass(boolean isOptional) {
        _isOptional = isOptional;
    }

    public boolean isMandatoryClass() {
        return !_isOptional;
    }

    public int[] getMandatoryPaths() {
        return _mandatoryPaths;
    }

    public void setMandatoryPaths(int[] paths) {
        _mandatoryPaths = paths;
    }

    public boolean isPathMandatory(int path) {
        return Arrays.binarySearch(_mandatoryPaths, path) >= 0;
    }

    public boolean hasExclusivePaths() {
        return _hasExclusivePath;
    }

    public void setHasExclusivePaths(boolean hasExclusivePaths) {
        _hasExclusivePath = hasExclusivePaths;
    }

    public int getExclusivePath() {
        return _exclusivePath;
    }

    public void setExclusivePath(int pathExclusive) {
        _exclusivePath = pathExclusive;
    }

    public boolean isPathExclusive(int path) {
        return _exclusivePath == path;
    }


    /**
     * Returns true if this degree class is an optional class for a given path, false otherwise.
     */
    public boolean isOptionalForPath(int path) {

        //Since this has a lot of awkward clauses it will be fully documented.

        // If if does not have paths (i.e. it is a regular common degree class)
        // it it a no-brainer, it cannot be optional for a path
        if (!_hasPaths) {
            return false;
        }

        // If this class is not available in this path it cannot also be optional for this path
        if (!isPathAvailable(path)) {
            return false;
        }

        // If it HAS paths and IS available on this path then we keep on searching for a clause

        // If it has mandatory paths I have to check if it this path is one of them
        if (_hasMandatoryPaths) {

            // and if it is, then obviously is not optional
            if (isPathMandatory(path)) {
                return false;
            }

            // The special case is if it has a mandatory path which is not the one I'm in and also an
            // exclusive path but it is also not the one I'm in. In that case, although this is not a mandatory
            // path, it is also NOT optional because it is in fact exclusive to another path
            if (_hasExclusivePath && (_exclusivePath != path)) {
                return false;
            }
        } else {

            // Now, if it does NOT have mandatory paths it can still be OPTIONAL to and exclusive path
            if (_hasExclusivePath) {

                //... but only if that path is the one we're in.
                if (_exclusivePath != path) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean hasPaths() {
        return _hasPaths;
    }

    public void setHasPaths(boolean hasPaths) {
        _hasPaths = hasPaths;
    }

    public boolean hasMandatoryPaths() {
        return _hasMandatoryPaths;
    }

    public void setHasMandatoryPaths(boolean hasMandatoryPaths) {
        _hasMandatoryPaths = hasMandatoryPaths;
    }


    public int[] getPaths() {
        return _paths;
    }

    public boolean isPathAvailable(int path) {
        return Arrays.binarySearch(_paths, path) >= 0;
    }

    public void setPaths(int[] paths) {
        _paths = paths;
    }
}
