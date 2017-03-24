package pt.cmg.sweranker.ranking;

import java.util.Map;


public interface RankingLoader {

    Map<String, ClassRanking> getAllDegreeClassRankings();
}
