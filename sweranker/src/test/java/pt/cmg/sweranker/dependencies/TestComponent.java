package pt.cmg.sweranker.dependencies;

import java.util.Map;

import javax.inject.Named;

import dagger.Component;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

@Component(modules = {TestModule.class})
public interface TestComponent {

    DegreeClass getDegreeClass();

    @Named("incomplete")
    DegreeClassMatch getUnmatchedDegreeClassMatch();

    @Named("complete")
    DegreeClassMatch getCompleteDegreeClassMatch();

    Map<Integer, KnowledgeAreaTopic> getTopicResolver();

}
