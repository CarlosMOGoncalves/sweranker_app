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

}
