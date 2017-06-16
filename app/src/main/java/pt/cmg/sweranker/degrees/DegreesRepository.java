package pt.cmg.sweranker.degrees;

import android.arch.lifecycle.LiveData;

import java.util.List;

/**
 * This is the contract for any class that wants to provide data in the form of Degrees.
 * It the Repository Pattern as I got from Google.
 */
public interface DegreesRepository {

    LiveData<List<Degree>> loadDegrees();

}
