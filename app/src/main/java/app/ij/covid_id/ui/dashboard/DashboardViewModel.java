package app.ij.covid_id.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import app.ij.covid_id.PatientDashboard;
import app.ij.covid_id.R;

public class DashboardViewModel extends ViewModel {

    private MutableLiveData<Pair<HashMap<String, Object>, ArrayList<HashMap<String, Object>>>> contents;
    Context context;
    public String username, documentId;
    public FirebaseFirestore db;
    HashMap<String, Object> generalInfo;
    ArrayList<HashMap<String, Object>> statusUpdates;
    Pair<HashMap<String, Object>, ArrayList<HashMap<String, Object>>> pair;
    View v;

    public DashboardViewModel(Context c, String username, String d, FirebaseFirestore ff, View root) {
        contents = new MutableLiveData<>();
        //contents.setValue("This is dashboard fragment");
        context = c;
        documentId = d;
        this.username = username;
        db = ff;
        v = root;
    }

    public LiveData<Pair<HashMap<String, Object>, ArrayList<HashMap<String, Object>>>> getInformation() {
        Log.wtf("*-*-- LOCATION: ", "getInformation() called");
        contents = new MutableLiveData<>();
        loadInformation();
        contents.setValue(pair);
        //makeSnackBar(3000, "WASSUP");
        return contents;
    }

    // INFO Do an asynchronous operation to fetch users.
    public LiveData<Pair<HashMap<String, Object>, ArrayList<HashMap<String, Object>>>> loadInformation() {
        generalInfo = new HashMap<>();
        statusUpdates = new ArrayList<>();
        Log.wtf("*-*-- LOCATION: ", "loadInformation() called");
        final DocumentReference docRef = db.collection("Patients").document(documentId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.wtf("ERROR", "Listen failed.", e);
                    generalInfo.put("ERROR STATE", "Fail");
                    pair = new Pair<>(generalInfo, statusUpdates);
                    return;
                }

                final CollectionReference updatesRef = db.collection("Patients" + "/" + snapshot.getId() + "/" + "Updates");
                updatesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        statusUpdates = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (e != null) {
                                Log.wtf("ERROR", "Listen failed.", e);
                                return;
                            }

                            String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                                    ? "Local" : "Server";
                            if (documentSnapshot != null && documentSnapshot.exists() && source.equals("Server")) {
                                statusUpdates.add((HashMap) documentSnapshot.getData());
                                Log.wtf("*------ INFO RETRIEVED -----", source + " data: " + documentSnapshot.getData());
                            } else {
                                Log.wtf("ERROR", source + " data: null");
                            }
                        }
                        pair = new Pair<>(generalInfo, statusUpdates);
                    }
                });

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";
                if (snapshot != null && snapshot.exists() && source.equals("Server")) {
                    generalInfo = (HashMap) snapshot.getData();
                    pair = new Pair<>(generalInfo, statusUpdates);
                    Log.wtf("*------ INFO RETRIEVED -----", source + " data: " + snapshot.getData());
                } else {
                    generalInfo.put("ERROR STATE", "Fail");
                    pair = new Pair<>(generalInfo, statusUpdates);
                    Log.wtf("ERROR", source + " data: null");
                }
            }
        });
        return contents;
    }

    Snackbar mySnackbar;

    private void makeSnackBar(int duration, String s) {
        Activity c = (Activity) context;
        mySnackbar = Snackbar.make((v).findViewById(R.id.screen), s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
    }
}