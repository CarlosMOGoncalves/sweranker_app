package pt.cmg.sweranker;

import android.arch.lifecycle.LiveData;

import java.util.List;

import pt.cmg.sweranker.swebok.KnowledgeArea;

/**
 * Created by Carlos on 09/06/2017.
 */

public interface SwebokRepository {
    LiveData<List<KnowledgeArea>> loadKnowledgeAreas();
}
