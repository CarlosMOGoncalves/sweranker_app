package pt.cmg.sweranker;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import pt.cmg.sweranker.degrees.Degree;
import pt.cmg.sweranker.degrees.DegreesRepository;
import pt.cmg.sweranker.swebok.KnowledgeArea;
import pt.cmg.sweranker.swebok.KnowledgeAreaTopic;
import pt.cmg.sweranker.swebok.SwebokRepository;


public class MainActivityViewModel extends ViewModel {

    private LiveData<List<KnowledgeArea>> _knowledgeAreas = new MutableLiveData<>();
    private List<KnowledgeAreaTopic> _knowledgeAreaTopics = null;

    // Used in inter-fragment communication between SwebokMasterFragment and SwebokDetailedFragment
    private MutableLiveData<KnowledgeArea> _selectedKnowledgeArea = new MutableLiveData<>();

    private LiveData<List<Degree>> _degrees = new MutableLiveData<>();

    private SwebokRepository _swebokRepository;
    private DegreesRepository _degreesRepository;

    @Inject
    public MainActivityViewModel(SwebokRepository repository, DegreesRepository degreesRepository) {
        _swebokRepository = repository;
        _degreesRepository = degreesRepository;
    }

    public void init() {
        _knowledgeAreas = _swebokRepository.loadKnowledgeAreas();
        // This here: whenever the KAs are loaded, so are the Topics.
        _knowledgeAreas.observeForever(new Observer<List<KnowledgeArea>>() {
            @Override
            public void onChanged(@Nullable List<KnowledgeArea> knowledgeAreas) {
                loadKnowledgeAreaTopics();
            }
        });

        _degrees = _degreesRepository.loadDegrees();
    }

    /**
     * Loads all KnowledgeAreaTopics from the previously load KnowledgeAreas.
     * This will be used mostly as an accelerator for future data questioning.
     *
     * @return
     */
    private void loadKnowledgeAreaTopics() {
        _knowledgeAreaTopics = new ArrayList<>();
        for (KnowledgeArea ka : _knowledgeAreas.getValue()) {
            _knowledgeAreaTopics.addAll(ka.getTopics());
        }
    }

    public LiveData<List<KnowledgeArea>> getKnowledgeAreas() {
        return _knowledgeAreas;
    }

    public LiveData<KnowledgeArea> getKnowledgeArea(int knowledgeAreaId) {
        MutableLiveData<KnowledgeArea> result = new MutableLiveData<>();

        for (KnowledgeArea ka : _knowledgeAreas.getValue()) {
            if (ka.getId() == knowledgeAreaId) {
                result.postValue(ka);
            }
        }
        return result;
    }

    public LiveData<List<Degree>> getDegrees() {
        return _degrees;
    }


    public void setSelectedKnowledgeArea(KnowledgeArea knowledgeArea) {
        _selectedKnowledgeArea.setValue(knowledgeArea);
    }

    public LiveData<KnowledgeArea> getSelectedKnowledgeArea() {
        return _selectedKnowledgeArea;
    }
}
