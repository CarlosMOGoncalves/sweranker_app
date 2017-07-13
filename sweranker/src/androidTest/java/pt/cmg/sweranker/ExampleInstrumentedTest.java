package pt.cmg.sweranker;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.List;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void combinationVector() throws Exception {

        ICombinatoricsVector<String> forthGradeOptional = Factory.createVector(new String[]{"mieic_c1", "mieic_c2", "mieic_c3", "mieic_c4", "mieic_c5"});

        // Create a simple combination generator to generate 3-combinations of the initial vector
        Generator<String> fourthCombos = Factory.createSimpleCombinationGenerator(forthGradeOptional, 3);

        List<ICombinatoricsVector<String>> allOptionalCombos = fourthCombos.generateAllObjects();

        for (ICombinatoricsVector<String> combo : allOptionalCombos) {
            combo.addValue("mieic_c50");
        }

        for (ICombinatoricsVector<String> combination : allOptionalCombos) {
            Log.i("Combinator-Fourth", combination.toString());
        }

        ICombinatoricsVector<String> fifthGradeOptional = Factory.createVector(new String[]{"mieic_c6", "mieic_c7", "mieic_c8", "mieic_c9", "mieic_c10"});

        // Create a simple combination generator to generate 3-combinations of the initial vector
        Generator<String> fifthCombos = Factory.createSimpleCombinationGenerator(fifthGradeOptional, 4);

        List<ICombinatoricsVector<String>> moreCombos = fifthCombos.generateAllObjects();

        for (ICombinatoricsVector<String> combo : moreCombos) {
            combo.addValue("mieic_c60");
        }

        for (ICombinatoricsVector<String> combination : moreCombos) {
            Log.i("Combinator-Fifth", combination.toString());
        }


    }
}
