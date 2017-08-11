package pt.cmg.sweranker.dependencies;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.degrees.DegreeClassMatch;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;

@Module
public class TestModule {


    public static final String DEGREE_CLASS_PROGRAM_ITEM_ID_1 = "classProgramItem1";
    public static final String DEGREE_CLASS_PROGRAM_ITEM_ID_2 = "classProgramItem2";
    public static final String DEGREE_CLASS_PROGRAM_ITEM_ID_3 = "classProgramItem3";

    public static final String DEGREE_CLASS_PROGRAM_ITEM_ID_4 = "classProgramItem4";

    public static final int DEGREE_CLASS_PROGRAM_ITEM_ID_RESOURCE_1 = 1;
    public static final int DEGREE_CLASS_PROGRAM_ITEM_ID_RESOURCE_2 = 1;

    @Provides
    @Named("standard")
    public DegreeClass degreeClass() {

        DegreeClass testClass = new DegreeClass();
        testClass.setId("degreeClassId1");
        testClass.setNameResource(1);
        testClass.setDescriptionResource(1);
        testClass.setOptionalClass(false);
        testClass.setEctsCredits(5f);
        testClass.setSemester(1);
        testClass.setYear(1);

        Map<String, Integer> classProgram = new HashMap<>();
        classProgram.put(DEGREE_CLASS_PROGRAM_ITEM_ID_1, DEGREE_CLASS_PROGRAM_ITEM_ID_RESOURCE_1);
        classProgram.put(DEGREE_CLASS_PROGRAM_ITEM_ID_2, DEGREE_CLASS_PROGRAM_ITEM_ID_RESOURCE_1);
        classProgram.put(DEGREE_CLASS_PROGRAM_ITEM_ID_3, DEGREE_CLASS_PROGRAM_ITEM_ID_RESOURCE_1);

        testClass.setProgram(classProgram);

        return testClass;
    }

    @Provides
    @Named("alternative")
    public DegreeClass degreeClassAlternative() {

        DegreeClass testClass = new DegreeClass();
        testClass.setId("degreeClassId2");
        testClass.setNameResource(2);
        testClass.setDescriptionResource(2);
        testClass.setOptionalClass(false);
        testClass.setEctsCredits(10f);
        testClass.setSemester(1);
        testClass.setYear(1);

        Map<String, Integer> classProgram = new HashMap<>();
        classProgram.put(DEGREE_CLASS_PROGRAM_ITEM_ID_4, DEGREE_CLASS_PROGRAM_ITEM_ID_RESOURCE_2);

        testClass.setProgram(classProgram);

        return testClass;
    }

    @Provides
    @Named("incomplete")
    public DegreeClassMatch incompleteMatch(@Named("standard") DegreeClass degreeClass) {
        return new DegreeClassMatch(degreeClass);
    }


    @Provides
    @Named("complete")
    public DegreeClassMatch completeMatch(@Named("standard") DegreeClass degreeClass) {
        DegreeClassMatch degreeClassMatch = new DegreeClassMatch(degreeClass);

        degreeClassMatch.addKATopicToDegreeTopic(DEGREE_CLASS_PROGRAM_ITEM_ID_1, 1);

        List<Integer> topicsForProgramItem2 = Arrays.asList(2, 3, 4, 5, 8);
        degreeClassMatch.addAllTopicsToDegreeTopic(DEGREE_CLASS_PROGRAM_ITEM_ID_2, topicsForProgramItem2);

        List<Integer> topicsForProgramItem3 = Arrays.asList(3, 7, 8, 9);
        degreeClassMatch.addAllTopicsToDegreeTopic(DEGREE_CLASS_PROGRAM_ITEM_ID_3, topicsForProgramItem3);

        return degreeClassMatch;
    }

    @Provides
    @Named("alternative")
    public DegreeClassMatch alternativeCompleteMatch(@Named("alternative") DegreeClass degreeClass) {
        DegreeClassMatch degreeClassMatch = new DegreeClassMatch(degreeClass);

        degreeClassMatch.addKATopicToDegreeTopic(DEGREE_CLASS_PROGRAM_ITEM_ID_1, 6);

        List<Integer> topicsForProgramItem2 = Arrays.asList(7);
        degreeClassMatch.addAllTopicsToDegreeTopic(DEGREE_CLASS_PROGRAM_ITEM_ID_2, topicsForProgramItem2);

        List<Integer> topicsForProgramItem3 = Arrays.asList(10);
        degreeClassMatch.addAllTopicsToDegreeTopic(DEGREE_CLASS_PROGRAM_ITEM_ID_3, topicsForProgramItem3);

        return degreeClassMatch;
    }

    @Provides
    public Map<Integer, KnowledgeAreaTopic> knowledgeAreaTopicResolver() {


        KnowledgeAreaTopic topic1 = new KnowledgeAreaTopic(1, 1);
        KnowledgeAreaTopic topic2 = new KnowledgeAreaTopic(2, 1);
        KnowledgeAreaTopic topic3 = new KnowledgeAreaTopic(3, 1);
        KnowledgeAreaTopic topic4 = new KnowledgeAreaTopic(4, 1);
        KnowledgeAreaTopic topic5 = new KnowledgeAreaTopic(5, 2);
        KnowledgeAreaTopic topic6 = new KnowledgeAreaTopic(6, 2);
        KnowledgeAreaTopic topic7 = new KnowledgeAreaTopic(7, 2);
        KnowledgeAreaTopic topic8 = new KnowledgeAreaTopic(8, 3);
        KnowledgeAreaTopic topic9 = new KnowledgeAreaTopic(9, 3);
        KnowledgeAreaTopic topic10 = new KnowledgeAreaTopic(10, 4);

        Map<Integer, KnowledgeAreaTopic> topicResolver = new HashMap<>();
        topicResolver.put(topic1.getId(), topic1);
        topicResolver.put(topic2.getId(), topic2);
        topicResolver.put(topic3.getId(), topic3);
        topicResolver.put(topic4.getId(), topic4);
        topicResolver.put(topic5.getId(), topic5);
        topicResolver.put(topic6.getId(), topic6);
        topicResolver.put(topic7.getId(), topic7);
        topicResolver.put(topic8.getId(), topic8);
        topicResolver.put(topic9.getId(), topic9);
        topicResolver.put(topic10.getId(), topic10);

        return topicResolver;
    }

    @Provides
    public Map<String, DegreeClass> degreeClassResolver(@Named("standard") DegreeClass degreeClass1, @Named("alternative") DegreeClass degreeClass2) {
        Map<String, DegreeClass> degreeClassResolver = new HashMap<>();
        degreeClassResolver.put(degreeClass1.getId(), degreeClass1);
        degreeClassResolver.put(degreeClass2.getId(), degreeClass2);

        return degreeClassResolver;
    }
}
