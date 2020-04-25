package app.ij.covid_id.ui.dashboard;

import android.app.Activity;
import android.app.Application;
import android.view.View;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {
    public Activity mApplication;
    public String username, documentID;
    public FirebaseFirestore db;
    View root;


    public DashboardViewModelFactory(Activity application, String param, String d, FirebaseFirestore ff, View root) {
        mApplication = application;
        username = param;
        documentID = d;
        db = ff;
        this.root = root;
    }


    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new DashboardViewModel(mApplication, username, documentID, db, root);
    }
}
