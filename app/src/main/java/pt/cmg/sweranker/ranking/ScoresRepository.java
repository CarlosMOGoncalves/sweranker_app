package pt.cmg.sweranker.ranking;


import java.util.List;

public interface ScoresRepository {


    void open();

    void close();

    void saveObjects(List<?> objects);

    List<AnnualClassCombination> getAnnualCombinationsOfDegree(int degreeId);

    List<DegreeClassCombination> getAllCombinationsOfDegree(int degreeId);

    List<SweScore> getClassScoresOfDegree(int degreeId);

    List<SweScore> getAnnualScoresOfDegree(int degreeId);

    int countAllCombinationsOfDegree(int degreeId);

    void insertOrUpdateObjectsInTransaction(List<?> objects);

}
