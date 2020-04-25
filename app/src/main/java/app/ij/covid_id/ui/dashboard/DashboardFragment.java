package app.ij.covid_id.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.ij.covid_id.R;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    FirebaseFirestore db;
    ScrollView screen;
    View root;
    HashMap<String, Object> generalInfo;
    ArrayList<HashMap<String, Object>> statusUpdates;
    Pair<HashMap<String, Object>, ArrayList<HashMap<String, Object>>> pair;
    public String documentID, username, name, userPassID, type, password, accountCreated, phone, email, status;
    TextView textView;
    String statusLastUpdated;

    public View findViewById(int id) {
        return root.findViewById(id);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //README Original, auto-generated code.
        /*dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);*/
        //dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    Log.wtf("HI", "HIiiiiiiioioiioioioiioipodfi asidf oisd foi ");
        db = FirebaseFirestore.getInstance();
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        screen = root.findViewById(R.id.screen);
        textView = (TextView) findViewById(R.id.text_dashboard);

        //getIntent();


        //dashboardViewModel = ViewModelProviders.of(this, new DashboardViewModelFactory(getActivity(), username, documentID, db, root)).get(DashboardViewModel.class);

        //textView.setText(name);
        /*dashboardViewModel.loadInformation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dashboardViewModel.loadInformation().observe(getViewLifecycleOwner(), new Observer<Pair<HashMap<String, Object>, ArrayList<HashMap<String, Object>>>>() {
                    @Override
                    public void onChanged(@Nullable Pair<HashMap<String, Object>, ArrayList<HashMap<String, Object>>> p) {
                        textView.setText("HI");
                        if (p != null) {
                            HashMap<String, Object> content = p.first;
                            ArrayList<HashMap<String, Object>> updates = p.second;
                            if (content != null) {
                                if (content.containsKey("ERROR STATE")) {
                                    makeSnackBar(4000, "ERROR");
                                } else {
                                    makeSnackBar(4000, "WASSUP");
                                }
                            }
                        }
                    }
                });
            }
        }, 5000);*/

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //makeSnackBar(3000, "HI");
        //TODO Using the status from intent, immediately go and use that to set the status text/color in layout
        // before loadInformation()
        /*if (!isNetworkAvailable()) {
            makeSnackBar(6000, "You are not connected to the internet. Therefore, you will not receive updates unless you connect.");
        } else {
            //TODO Read from userPass 1-time and update info.txt.
            //TODO If not too much, add a listener on userPass and everytime it is changed
            // only write to info.txt. That is the only purpose of listener ----> UI will be for loadInformation();
            updateInfoTxt();
            loadInformation();
        }*/
    }

    Snackbar mySnackbar;

    public void updateLayout() {
        //TODO use info from the maps and arraylsts to display the stuff.
        statusLastUpdated = generalInfo.get("Date 1").toString();
    }

    public void loadInformation() {
        generalInfo = new HashMap<>();
        statusUpdates = new ArrayList<>();
        Log.wtf("*-*-- LOCATION: ", "loadInformation() called");
        //TODO Make notification onEvent
        final DocumentReference docRef = db.collection("Patients").document(documentID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.wtf("ERROR", "Listen failed.", e);
                    makeSnackBar(3000, "Could not load your data. Are you connected to the internet?");
                    return;
                }
                //counter++;

                final CollectionReference updatesRef = db.collection("Patients" + "/" + snapshot.getId() + "/" + "Updates");
                updatesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        statusUpdates = new ArrayList<>();
                        //counter++;
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if (e != null) {
                                Log.wtf("ERROR", "Listen failed.", e);
                                return;
                            }

                            String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                                    ? "Local" : "Server";
                            if (documentSnapshot != null && documentSnapshot.exists() && source.equals("Server")) {
                                statusUpdates.add((HashMap) documentSnapshot.getData());
                                //textView.setText("TEST: " + counter);
                                Log.wtf("*------ INFO RETRIEVED -----", source + " data: " + documentSnapshot.getData());
                            } else {
                                Log.wtf("ERROR", source + " data: null");
                                makeSnackBar(3000, "No data could be found. Are you connected to the internet?");
                            }
                        }
                        pair = new Pair<>(generalInfo, statusUpdates);
                        updateLayout();
                    }
                });

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";
                if (snapshot != null && snapshot.exists() && source.equals("Server")) {
                    generalInfo = (HashMap) snapshot.getData();
                    pair = new Pair<>(generalInfo, statusUpdates);
                    updateLayout();
                    //textView.setText("TEST: " + counter);
                    Log.wtf("*------ INFO RETRIEVED -----", source + " data: " + snapshot.getData());
                } else if (snapshot == null) {
                    //generalInfo.put("ERROR STATE", "Fail");
                    makeSnackBar(2000, "Could not load new data.");
                    Log.wtf("ERROR", source + " data: null");
                } else {
                    Log.wtf("ERROR", source + " data: null");
                }
            }
        });
    }

    //IMPORTANT If you do decide on writing Hello "NAME" on dashboard, then you have to listen to local changes
    //  and update UI for local changes to say Hello "new name" or whatever.
    // For things like status, don't need to worry since they can't change it in settings
    public void updateInfoTxt() {
        final DocumentReference docRef = db.collection("userPass").document(userPassID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.wtf("USERPATH ERROR", "Listen failed.", e);
                    makeSnackBar(3000, "Could not load your data. Are you connected to the internet?");
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                //README Local changes (taking place in Settings) will update info.txt in Settings
                if (snapshot != null && snapshot.exists() && source.equals("Server")) {
                    //TODO Write new info to info.txt
                    writeNewInfo(snapshot.getData());
                    //textView.setText(snapshot.getData().get("Status").toString());
                    Log.wtf("*------ INFO RETRIEVED -----", source + " data: " + snapshot.getData());
                } else if (snapshot == null) {
                    Log.wtf("ERROR", source + " data: null");
                } else {
                    Log.wtf("ERROR", source + " data: null");
                }

            }
        });

    }

    private void writeNewInfo(Map<String, Object> data) {
        String toWrite = "";
        toWrite += data.get("Type");
        toWrite += "___________";
        toWrite += data.get("Document ID");
        toWrite += "___________";
        toWrite += data.get("Username");
        toWrite += "___________";
        toWrite += data.get("Password");
        toWrite += "___________";
        toWrite += data.get("Account Created");
        toWrite += "___________";
        toWrite += data.get("Name");
        toWrite += "___________";
        toWrite += data.get("Password");
        toWrite += "___________";
        toWrite += data.get("Email");
        toWrite += "___________";
        toWrite += data.get("Document ID");
        toWrite += "___________";
        toWrite += data.get("Status");
        toWrite += "___________";
        toWrite += userPassID;
        username = data.get("Username").toString();
        documentID = data.get("Document ID").toString();
        userPassID = userPassID;
        name = data.get("Name").toString();
        type = data.get("Type").toString();
        password = data.get("Password").toString();
        accountCreated = data.get("Account Created").toString();
        phone = data.get("Phone").toString();
        email = data.get("Email").toString();
        status = data.get("Status").toString();
        writeToInfo(toWrite);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void writeToInfo(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("info.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void getIntent() {
        Intent intent = getActivity().getIntent();
        username = intent.getStringExtra("Username");
        documentID = intent.getStringExtra("Document ID");
        userPassID = intent.getStringExtra("userPass ID");
        name = intent.getStringExtra("Name");
        type = intent.getStringExtra("Type");
        password = intent.getStringExtra("Password");
        accountCreated = intent.getStringExtra("Account Created");
        phone = intent.getStringExtra("Phone");
        email = intent.getStringExtra("Email");
        status = intent.getStringExtra("Status");
    }

    private void makeSnackBar(int duration, String s) {
        mySnackbar = Snackbar.make(screen, s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
    }
}