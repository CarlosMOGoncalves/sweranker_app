package pt.cmg.sweranker.degrees;

import java.util.List;

/**
 * This interface is meant to be implemented by an Activity to facilitate communication with any
 * of its child components (such as fragments), or more likely to be extended by any
 * OnScoreChartFragmentInteractionListener according to the data that it needs from the activity.
 */
public interface DegreeLoader {

    /**
     * Loads all the Degrees from the system.
     *
     * @return
     */
    List<Degree> getAllDegrees();


    /**
     * Loads a Degree from the system passing its id.
     *
     * @param degreeId
     * @return
     */
    Degree getDegree(int degreeId);

    /**
     * Loads a Degree Class from the system.
     *
     * @param degreeId
     * @param degreeClassId
     * @return
     */
    DegreeClass getDegreeClass(int degreeId, String degreeClassId);


    /**
     * Loads a Degree Class from the system.
     *
     * @param degreeClassId
     * @return
     */
    DegreeClass getDegreeClass(String degreeClassId);
}
