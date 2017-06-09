package pt.cmg.sweranker;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import pt.cmg.sweranker.swebok.KnowledgeArea;

/**
 * Created by Carlos on 08/06/2017.
 */

public class MainActivityViewModel extends ViewModel {

    private LiveData<List<KnowledgeArea>> _knowledgeAreas;
    @Inject
    ResourcesSwebokRepository _swebokRepository;


    public LiveData<List<KnowledgeArea>> getKnowledgeAreas() {
        if (_knowledgeAreas == null) {
            _knowledgeAreas = _swebokRepository.loadKnowledgeAreas();
        }

        return _knowledgeAreas;
    }
}
