package pt.cmg.sweranker.ranking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carlos on 27/03/2017.
 */

public class CombinationUtils {


    /**
     * Calculates and outputs all the possible combinations between a collection of ClassCombinations.
     * <p>
     * It basically expands each combination list of a ClassCombinationMatrix for every combination list of another
     * ClassCombinationMatrix and so on until all the ClassCombinations are consumed
     * <p>
     * Note: this is not particularly optimised, using a full ArrayList will likely create many objects but time is an issue
     * so I am not going to optimise this bit unless I find it to be really critical later on.
     *
     * @return
     */
    public static ClassCombinationMatrix combineCombinations(ClassCombinationMatrix... combinations) {

        int combinationsSize = combinations.length;


        List<List<String>> accumulatorList = new ArrayList<>();
        accumulatorList.add(new ArrayList<>());

        int counter = 0;
        while (counter < combinationsSize - 1) {

            ClassCombinationMatrix currentClassCombinationsMatrix = combinations[counter];

            List<List<String>> temporary = new ArrayList<>();

            for (int i = 0; i < accumulatorList.size(); i++) {

                for (int j = 0; j < currentClassCombinationsMatrix.getCombinations().size(); j++) {
                    List<String> appendedList = new ArrayList<>(accumulatorList.get(i));
                    appendedList.addAll(currentClassCombinationsMatrix.getCombinations().get(j));
                    temporary.add(appendedList);
                }
            }

            accumulatorList = temporary;

            counter++;

        }

        ClassCombinationMatrix result = new ClassCombinationMatrix();
        result.setCombinations(accumulatorList);

        return result;
    }
}
