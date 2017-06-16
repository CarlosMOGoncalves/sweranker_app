package pt.cmg.sweranker.swebok;

import android.arch.lifecycle.LiveData;

import java.util.List;


/**
 * This is the contract for any class that wants to provide data in the form of Knowledge Areas.
 * It the Repository Pattern as I got from Google.
 */
public interface SwebokRepository {
    LiveData<List<KnowledgeArea>> loadKnowledgeAreas();
}
