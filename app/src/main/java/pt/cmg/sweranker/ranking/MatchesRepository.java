package pt.cmg.sweranker.ranking;

import android.arch.lifecycle.LiveData;

import java.util.Map;

import pt.cmg.sweranker.degrees.DegreeClassMatch;

/**
 * This is the contract for any class that wants to provide data in the form of Degree Class Matches.
 * It the Repository Pattern as I got from Google.
 */
public interface MatchesRepository {
    LiveData<Map<String, DegreeClassMatch>> loadMatches();
}
