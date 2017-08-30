package pt.cmg.sweranker.degrees;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class represents a Class Match.
 * This means that it is used to store the matches between each item of a degree class program AND
 * one or more knowledge area topics.
 * <p>
 * As such, each Degree Class Match is composed of the Degree Class it it about and then a collection
 * that maps each program item to a list of knowledge area topic ids.
 */
public class DegreeClassMatch {

    private DegreeClass _degreeClass;
    private String _degreeClassId;

    private int _degreeId;

    /**
     * Keys -> Degree Program Item ID , Values -> the IDs of each KnowledgeAreaTopic matched.
     * I used the LinkedList because it is easier to remove the last element, which will be done
     * a bit.
     */
    private Map<String, LinkedList<Integer>> _selectedKATopicsByProgramItem;

    private boolean _isCompleteMatch;

    public DegreeClassMatch() {
        _degreeClass = new DegreeClass();
        _degreeClassId = "";
        _degreeId = 0;
        _selectedKATopicsByProgramItem = new HashMap<>();
        _isCompleteMatch = false;
    }

    public DegreeClassMatch(String degreeClassId) {
        _degreeClass = new DegreeClass();
        _degreeClassId = degreeClassId;
        _selectedKATopicsByProgramItem = new HashMap<>();
        _isCompleteMatch = false;
    }

    public DegreeClassMatch(DegreeClass degreeClass) {
        _degreeClass = degreeClass;
        _degreeClassId = degreeClass.getId();
        _degreeId = degreeClass.getDegreeId();
        _selectedKATopicsByProgramItem = initialiseMatchTrackers(_degreeClass);
        _isCompleteMatch = false;
    }

    /**
     * Initialises the collection that stores the matchings. This will pretty much just expand the
     * all the IDs that compose this class program (each topic of the program) to Map and then
     * append to its value an empty Linked List that will store in the future the matching KA Topic
     * Ids.
     */
    private Map<String, LinkedList<Integer>> initialiseMatchTrackers(DegreeClass degreeClass) {

        Map<String, Integer> program = degreeClass.getProgram();
        Set<String> degreeTopicIds = program.keySet();

        Map<String, LinkedList<Integer>> matches = new HashMap<>(degreeTopicIds.size());

        // Arrays.stream(_degreeTopicIds).forEach(topicId -> matches.put(topicId, new LinkedList<>()));
        for (String degreeTopicId : degreeTopicIds) {
            matches.put(degreeTopicId, new LinkedList<>());
        }

        return matches;
    }

    /**
     * Returns this Degree Class ID.
     *
     * @return
     */
    public String getDegreeClassId() {
        return _degreeClassId;
    }


    public int getDegreeId() {
        return _degreeId;
    }

    public void setDegreeId(int degreeId) {
        _degreeId = degreeId;
    }

    /**
     * Returns true if there is at least one KA Topic already selected for the given Degree topic
     *
     * @param degreeTopicId
     * @return
     */
    public boolean hasTopicsSelected(String degreeTopicId) {
        return !_selectedKATopicsByProgramItem.get(degreeTopicId).isEmpty();
    }


    public int getKATopicsCount(String degreeTopicId) {
        return _selectedKATopicsByProgramItem.get(degreeTopicId).size();
    }


    /**
     * Returns true if and only if ALL the Degree Class Topics have AT LEAST one KA Topic associated.
     *
     * @return
     */
    public boolean isCompleteMatch() {
        return _isCompleteMatch;
    }


    /**
     * Adds a list of Knowledge Area Topic Ids to a given Program Item Id.
     * In terms of business logic this translates to: the given degree class program topic IS MATCHED
     * by this list of KA Topics.
     * <p>
     * For example: The program item "Relational Algebra as Query Language" from the Degree Class "Databases"
     * is matched by the KA Topics "Databases and Data Management" as well as "Basic Logic".
     * <p>
     * So, given the degree class program topic id for "Relational Algebra as Query Language" is, for example, "d1c120t4"
     * and the KA Topic IDs for "Databases and Data Management" and "Basic Logic" are "3" and "45", this function will be used
     * as "addAllTopicsToDegreeTopic("d1c120t4" , [3,45]).
     *
     * @param degreeClassProgramTopicId
     * @param kaTopicIds
     */
    public void addAllTopicsToDegreeTopic(String degreeClassProgramTopicId, List<Integer> kaTopicIds) {

        if (_selectedKATopicsByProgramItem.get(degreeClassProgramTopicId) == null) {
            _selectedKATopicsByProgramItem.put(degreeClassProgramTopicId, new LinkedList<>());
        }

        _selectedKATopicsByProgramItem.get(degreeClassProgramTopicId).addAll(kaTopicIds);

        // Whenever we add we check if none of them have an empty list, if not, they all have at least one match so we are OK
        boolean isComplete = true;
        for (LinkedList<Integer> matches : _selectedKATopicsByProgramItem.values()) {
            if (matches.isEmpty()) {
                isComplete = false;
            }
        }
        _isCompleteMatch = isComplete;

        // Whenever we add we check if none of them have an empty list, if not, they all have at least one match so we are OK
        // This stream would be so cool... damn it!
//        if (_selectedKATopicsByProgramItem.values().stream().noneMatch(List::isEmpty)) {
//            _isCompleteMatch = true;
//        }
    }


    /**
     * Adds a new KA Topic to a given Degree Class. This represents an association between both, which is the point of this app.
     * Note that these KA Topics are inserted in and ordered fashion and returned as such.
     * The only reason for this it to be useful for pt.cmg.sweranker.degrees.DegreeTopicMatcherAdapter, that relies on item positioning for its logic,
     * at least at the current moment.
     * Otherwise, the order of the association is irrelevant.
     *
     * @param degreeClassProgramTopicId
     * @param kaTopicId
     */
    public void addKATopicToDegreeTopic(String degreeClassProgramTopicId, int kaTopicId) {

        if (_selectedKATopicsByProgramItem.get(degreeClassProgramTopicId) == null) {
            _selectedKATopicsByProgramItem.put(degreeClassProgramTopicId, new LinkedList<>());
        }

        _selectedKATopicsByProgramItem.get(degreeClassProgramTopicId).add(kaTopicId);

        // Whenever we add we check if none of them have an empty list, if not, they all have at least one match so we are OK
        boolean isComplete = true;
        for (LinkedList<Integer> matches : _selectedKATopicsByProgramItem.values()) {
            if (matches.isEmpty()) {
                isComplete = false;
            }
        }
        _isCompleteMatch = isComplete;

        // Whenever we add we check if none of them have an empty list, if not, they all have at least one match so we are OK
        // This stream would be so cool... damn it!
//        if (_selectedKATopicsByProgramItem.values().stream().noneMatch(List::isEmpty)) {
//            _isCompleteMatch = true;
//        }
    }


    /**
     * Replaces the KA Topic previously associated IN A GIVEN INDEX to a given Degree Class.
     * This is where order comes in handy. This way it is possible to use some ordering logic
     * on the Adapter to keep the previously selected topics in order.
     *
     * @param degreeTopicId
     * @param updatedKaTopicId
     * @param indexToReplace
     */
    public void replaceSelectedKATopic(String degreeTopicId, int updatedKaTopicId, int indexToReplace) {
        _selectedKATopicsByProgramItem.get(degreeTopicId).remove(indexToReplace);
        _selectedKATopicsByProgramItem.get(degreeTopicId).add(indexToReplace, updatedKaTopicId);
    }


    /**
     * Removes the last KA Topic associated with the given Degree Class.
     * Again, this is ordering comes in handy. In the Adapter it is sometimes needed
     * to delete the last one (due to the View placement logic). That's what this does.
     *
     * @param degreeTopicId
     */
    public void removeLastKATopicAdded(String degreeTopicId) {
        _selectedKATopicsByProgramItem.get(degreeTopicId).removeLast();

        // If this removal implied that this Degree Topic was left without match then it is an automatic NOT OK
        if (_selectedKATopicsByProgramItem.get(degreeTopicId).isEmpty()) {
            _isCompleteMatch = false;
        }
    }


    public LinkedList<Integer> getMatches(String degreeTopicId) {
        return _selectedKATopicsByProgramItem.get(degreeTopicId);
    }


    public Map<String, LinkedList<Integer>> getAllMatches() {
        return _selectedKATopicsByProgramItem;
    }


    /**
     * Returns the complete list of ALL the KA Topics that were matched in this Class.
     * I care not for the specific Program Item that matched whichever topic or topics,
     * I just get a list with all that were matched.
     *
     * @return
     */
    public LinkedList<Integer> getAllMatchesAsList() {
        LinkedList<Integer> allMatches = new LinkedList<>();
        for (List<Integer> topicList : _selectedKATopicsByProgramItem.values()) {
            allMatches.addAll(topicList);
        }
        return allMatches;
    }

}
