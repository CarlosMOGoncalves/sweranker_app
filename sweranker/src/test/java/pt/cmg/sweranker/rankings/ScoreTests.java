package pt.cmg.sweranker.rankings;


import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.dependencies.DaggerTestComponent;
import pt.cmg.sweranker.dependencies.TestComponent;
import pt.cmg.sweranker.ranking.CalculationUtils;
import pt.cmg.sweranker.ranking.SweScore;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ScoreTests {

    static TestComponent _injector;

    @BeforeClass
    public static void setUpDependeciesInjector() {
        _injector = DaggerTestComponent.builder().build();
    }

    @Test
    public void calculatedScoreShouldBeAccurate() {

        Map<Integer, KnowledgeAreaTopic> topicResolver = _injector.getTopicResolver();
        DegreeClassMatch degreeClassMatch = _injector.getCompleteDegreeClassMatch();

        SweScore classScore = CalculationUtils.calculateScore(topicResolver, degreeClassMatch);

        assertNotNull("Score cannot be null", classScore);

        assertEquals("Score should have as many counters as matches", degreeClassMatch.getAllMatchesAsList().size(), classScore.getTotalTopicCount());

        assertEquals("Expected value for KA 1 is 5", 5, classScore.getKaCounter(1));
        assertEquals("Expected value for KA 2 is 2", 2, classScore.getKaCounter(2));
        assertEquals("Expected value for KA 3 is 3", 3, classScore.getKaCounter(3));
        assertEquals("Expected value for KA 4 is 0", 0, classScore.getKaCounter(4));

        assertEquals("Expected value is 50 percent", 50f, classScore.getKaPercent(1), 0.01f);
        assertEquals("Expected value is 20 percent", 20f, classScore.getKaPercent(2), 0.01f);
        assertEquals("Expected value is 30 percent", 30f, classScore.getKaPercent(3), 0.01f);
        assertEquals("Expected value is 0 percent", 0f, classScore.getKaPercent(4), 0f);
    }

    @Test
    public void calculatedAccumulatedScoreShouldBeAccurate() {

        Map<Integer, KnowledgeAreaTopic> topicResolver = _injector.getTopicResolver();
        DegreeClassMatch degreeClassMatch = _injector.getCompleteDegreeClassMatch();

        SweScore classScore = CalculationUtils.calculateScore(topicResolver, degreeClassMatch);

        assertNotNull("Score cannot be null", classScore);

        assertEquals("Score should have as many counters as matches", degreeClassMatch.getAllMatchesAsList().size(), classScore.getTotalTopicCount());

        assertEquals("Expected value for KA 1 is 5", 5, classScore.getKaCounter(1));
        assertEquals("Expected value for KA 2 is 2", 2, classScore.getKaCounter(2));
        assertEquals("Expected value for KA 3 is 3", 3, classScore.getKaCounter(3));
        assertEquals("Expected value for KA 4 is 0", 0, classScore.getKaCounter(4));

        assertEquals("Expected value is 50 percent", 50f, classScore.getKaPercent(1), 0.01f);
        assertEquals("Expected value is 20 percent", 20f, classScore.getKaPercent(2), 0.01f);
        assertEquals("Expected value is 30 percent", 30f, classScore.getKaPercent(3), 0.01f);
        assertEquals("Expected value is 0 percent", 0f, classScore.getKaPercent(4), 0f);
    }
}
