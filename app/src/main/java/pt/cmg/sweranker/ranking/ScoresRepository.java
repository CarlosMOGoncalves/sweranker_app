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

    List<SweScore> getScoresOfDegreeOrderedBy(int degreeId, String orderedFieldName, Sort order);

    List<SweScore> getScoresOfDegree(int degreeId);

    SweScore getDegreeCombinationScore(String degreeCombinationId);

    DegreeClassCombination getDegreeClassCombination(String degreeCombinationId);

    int countAllCombinationsOfDegree(int degreeId);

    void insertOrUpdateObjectsInTransaction(List<?> objects);


    enum Sort {
        ASCENDING(true),
        DESCENDING(false);

        private final boolean value;

        Sort(boolean value) {
            this.value = value;
        }

        public boolean getValue() {
            return this.value;
        }
    }

}
