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

}
