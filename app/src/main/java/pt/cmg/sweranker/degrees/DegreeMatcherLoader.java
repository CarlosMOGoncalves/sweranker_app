package pt.cmg.sweranker.degrees;

/**
 * Created by Carlos on 01/03/2017.
 */

public interface DegreeMatcherLoader {

    boolean hasMatch(String degreeClassId);

    DegreeClassMatch getDegreeClassMatches(String degreeClassId);

    void saveMatch(DegreeClassMatch newlySubmittedMatch);

}
