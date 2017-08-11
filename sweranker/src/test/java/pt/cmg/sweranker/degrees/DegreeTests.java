package pt.cmg.sweranker.degrees;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Map;

import pt.cmg.sweranker.dependencies.DaggerTestComponent;
import pt.cmg.sweranker.dependencies.TestComponent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DegreeTests {

    static TestComponent _component;

    @BeforeClass
    public static void setUpDependencies() {
        _component = DaggerTestComponent.builder().build();
    }

    @Test
    public void degreeClassMatchShouldBeComplete() {

        DegreeClassMatch classMatch = _component.getUnmatchedDegreeClassMatch();
        assertFalse("DegreeClassMatch should be incomplete", classMatch.isCompleteMatch());

        // Pretty simple, I add topic matches until it finally full, then it should be a complete match
        for (Map.Entry<String, LinkedList<Integer>> entry : classMatch.getAllMatches().entrySet()) {
            assertFalse("DegreeClassMatch should be incomplete", classMatch.isCompleteMatch());
            classMatch.addKATopicToDegreeTopic(entry.getKey(), 1);
        }
        assertTrue("DegreeClassMatch should be a complete match", classMatch.isCompleteMatch());


        // Now the opposite, remove to see if it gets incomplete
        for (Map.Entry<String, LinkedList<Integer>> entry : classMatch.getAllMatches().entrySet()) {
            classMatch.removeLastKATopicAdded(entry.getKey());
            assertFalse("DegreeClassMatch should be incomplete", classMatch.isCompleteMatch());
        }
    }
}
