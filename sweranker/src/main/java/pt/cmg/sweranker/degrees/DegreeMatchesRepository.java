package pt.cmg.sweranker.degrees;

import android.arch.lifecycle.LiveData;

import java.util.Map;

/**
 * This is the contract for any class that wants to provide data in the form of Degree Class Matches.
 * It the Repository Pattern as I got from Google.
 */
public interface DegreeMatchesRepository {

    LiveData<Map<String, DegreeClassMatch>> loadMatches();

    /**
     * Saves or overwrites a Degree Class Match into the system.
     *
     * @param classMatch The DegreeClassMatch to save
     * @return true if successfully saved, false otherwise
     */
    boolean saveMatch(DegreeClassMatch classMatch);

    /**
     * Saves or overwrites all the Degree Class Match for a single degree into the system as a file.
     * This is useful only in the admin version of the app since with this it is possible to flush all
     * the changes made to system matches into a single file which will then fill the resources with
     * new matches.
     *
     * @param degreeId The Degree id to save from
     * @return true if successfully saved, false otherwise
     */
    boolean saveMatchesToSingleFile(int degreeId);

    /**
     * Returns true if there is a match in this repository for the given Degree Class Id
     *
     * @param degreeClassId the Degree Class Id that will be tested
     * @return true if there is one match, false otherwise
     */
    boolean hasMatch(String degreeClassId);

    /**
     * Returns any found Degree Class Match in this repository.
     * Should not be used before checking if it has a match before
     *
     * @param degreeClassId the Degree Class Id parameter to be searched
     * @return a found degree class match or NULL if no one is found
     */
    DegreeClassMatch getDegreeClassMatch(String degreeClassId);

}
