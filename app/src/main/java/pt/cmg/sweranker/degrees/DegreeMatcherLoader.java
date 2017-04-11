package pt.cmg.sweranker.degrees;


public interface DegreeMatcherLoader {


    /**
     * Returns true if this particular Degree Class has a Degree Class Match in the system.
     *
     * @param degreeClassId
     * @return
     */
    boolean hasMatch(String degreeClassId);


    /**
     * Retrieves the system Degree Class Match that matches the parameter Degree Class.
     *
     * @param degreeClassId
     * @return
     */
    DegreeClassMatch getDegreeClassMatches(String degreeClassId);


    /**
     * Saves or overwrites to the system the parameter Degree Class Match.
     *
     * @param newlySubmittedMatch
     */
    void saveMatch(DegreeClassMatch newlySubmittedMatch);


    /**
     * Returns true if the parameter degree has ALL of its classes already matched.
     * False if at least one of them is not matched.
     *
     * @param degreeId
     * @return
     */
    boolean isDegreeMatched(int degreeId);

}
