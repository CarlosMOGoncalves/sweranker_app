package pt.cmg.sweranker.ranking;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a List of the possible combinations of classes.
 * Each class is represented by a String and each combination is represented by a List of those Strings.
 * <p>
 * This is useful when representing the possible combinations of classes that any person can attend during a given
 * year in a degree. It arises from the fact that there exists Optional Degree Classes that somehow the student
 * must chose in order to complete that year. Since multiple combinations exist, this class is used as a simple
 * way to represent those combinations.
 * <p>
 * Created by Carlos on 27/03/2017.
 */
public class ClassCombinationMatrix {

    /**
     * This is in practice a Matrix where each element of the list is itself another list with a particular
     * non-repeatable combination of Degree Class Ids.
     * <p>
     * Something like this:
     * <p></p>
     * List = {
     * {class1, class2, class3},
     * {class1, class2, class4},
     * {class1, class3, class4},
     * {class2, class3, class4}
     * }
     * </p>
     */
    private List<List<String>> _currentCombinations;


    public ClassCombinationMatrix() {
        _currentCombinations = new ArrayList<>();
    }


    public ClassCombinationMatrix(List<String> combination) {
        _currentCombinations = new ArrayList<>();
        _currentCombinations.add(new ArrayList<>(combination));
    }

    public void setCombinations(List<List<String>> combos) {

        _currentCombinations = combos;
    }


    public void addCombination(List<String> combos) {

        _currentCombinations.add(combos);
    }

    public List<List<String>> getCombinations() {

        return _currentCombinations;
    }

    @Override
    public String toString() {

        StringBuilder s = new StringBuilder();

        for (List<String> combination : _currentCombinations) {
            s.append(combination.toString());
        }

        return s.toString();
    }
}
