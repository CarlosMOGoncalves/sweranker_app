package pt.cmg.sweranker.ranking;


import java.util.Collection;
import java.util.List;


public interface ScoresRepository {


    void open();

    void close();

    void saveObjects(List<?> objects);

    List<AnnualClassCombination> getAnnualCombinationsOfDegree(int degreeId);

    List<DegreeClassCombination> getAllCombinationsOfDegree(int degreeId);

    long getScoreCount();

    List<SweScore> getAllScores();

    List<SweScore> getClassScoresOfDegree(int degreeId);

    List<SweScore> getAnnualScoresOfDegree(int degreeId);

    List<SweScore> getScoresOfDegreeOrderedBy(int degreeId, String orderedFieldName, Sort order);

    List<SweScore> getScoresOfDegree(int degreeId);

    /**
     * Loads the SweScore using the ID as parameter.
     * This method should return a deep copy of the object.
     *
     * @param scoreId the ID of the SweScore to load.
     * @return a deep copy of the SweScore whose ID is the same as the parameter.
     */
    SweScore getScore(String scoreId);


    /**
     * Loads a list of SweScores using the IDs as parameter.
     * This method should return a deep copy of the object.
     * Use judiciously because a lot of objects might be loaded, have pity on your phone...
     *
     * @param scoreIds the IDs of the SweScores to load.
     * @return a deep copy of the SweScore whose ID is the same as the parameter.
     */
    List<SweScore> getScores(Collection<String> scoreIds);

    /**
     * Loads the Degree Class Combination using the ID as parameter.
     * This method should return a deep copy of the object.
     *
     * @param degreeCombinationId the ID of the Degree Class Combination to load.
     * @return a deep copy of the Degree Class Combination whose ID is the same as the parameter.
     */
    DegreeClassCombination getDegreeClassCombination(String degreeCombinationId);

    /**
     * Loads the Annual Score Combination using the ID as parameter.
     * This method should return a deep copy of the object.
     *
     * @param combinationId the ID of the Annual Class Combination to load.
     * @return a deep copy of the Annual Class Combination whose ID is the same as the parameter.
     */
    AnnualClassCombination getAnnualClassCombination(String combinationId);

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
