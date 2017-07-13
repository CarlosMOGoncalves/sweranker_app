package pt.cmg.sweranker;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import javax.inject.Inject;


public class MainActivityViewModelFactory implements ViewModelProvider.Factory {

    private ViewModel mainActivityViewModel;

    @Inject
    public MainActivityViewModelFactory(ViewModel viewModel) {
        mainActivityViewModel = viewModel;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {

        if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
            return (T) mainActivityViewModel;
        }
        throw new IllegalArgumentException("Unknown class name");
    }
}
