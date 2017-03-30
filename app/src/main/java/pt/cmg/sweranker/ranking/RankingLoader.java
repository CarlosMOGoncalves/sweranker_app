package pt.cmg.sweranker.ranking;

import java.util.Map;


public interface RankingLoader {

    Map<String, KACalculation> getAllDegreeClassRankings();

    Map<Integer, DegreeRanking> getDegreeRankings();
}
