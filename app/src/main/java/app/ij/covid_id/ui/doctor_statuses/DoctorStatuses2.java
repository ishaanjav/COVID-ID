package app.ij.covid_id.ui.doctor_statuses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.ij.covid_id.InfoRecyclerViewAdapter;
import app.ij.covid_id.R;

public class DoctorStatuses2 extends Fragment {

    FirebaseFirestore db;
    ScrollView screen;
    View root;
    public String documentID, username, name, userPassID, type, password, accountCreated, phone, email, status;
    String statusLastUpdated;
    String doctorsPath;
    RecyclerView patientRecycler;
    Button update;
    TextView message;
    public String TAG = "DoctorStatuses2";
    String city, state, country;
    float screenW, screenH, maxHeightPatient, maxHeightDoctor;

    public View findViewById(int id) {
        return root.findViewById(id);
    }

    String directory;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_doctor_statuses, container, false);
        screen = root.findViewById(R.id.screen);
        patientRecycler = root.findViewById(R.id.patientRecycler);
        doctorsPath = "Doctor";

        patientNested0 = new ArrayList<>();
        patientNested1 = new ArrayList<>();
        patientNested2 = new ArrayList<>();
        patientNested3 = new ArrayList<>();
        patientNested4 = new ArrayList<>();
        readStorage();
        return root;
    }

    ArrayList<HashMap<String, Object>> patientInfo;
    ArrayList<HashMap<String, Object>> patientNested0, patientNested1, patientNested2, patientNested3, patientNested4;

    InfoRecyclerViewAdapter adapter;
    long totalStartTime, imageStartTime;
    ArrayList<String> patientUsernames;
    boolean foreign;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        patientUsernames = new ArrayList<>();
        patientInfo = new ArrayList<>();
        foreign = state.isEmpty() || state.length() < 2;
        maxSize = 80;

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        db = FirebaseFirestore.getInstance();
        directory = getContext().getApplicationInfo().dataDir + "/files";
        screenW = metrics.widthPixels;
        maxHeightPatient = screenH * 3f / 5f;
        screenH = metrics.heightPixels;
        maxHeightDoctor = screenH * 11f / 20f;
        Log.wtf("HEIGHT", "" + screenH);

        p1 = p2 = p3 = p4 = p5 = false;
        //foreign = true;
        //country = "IN: India";
        //foreign = false;
        //removeUncessaryFiles();
        updateInfoTxt();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (foreign) {
                    loadForeignPatientInfo();
                } else {
                    loadPatientInfo();
                }
            }
        }, 200);
        /*HashMap<String, Object> temp = new HashMap<>();
        temp.put("Number", 7);
        temp.put("Username", "patient7");
        patientInfo.add(temp);
        HashMap<String, Object> temp2 = new HashMap<>();
        temp2.put("Number", 8);
        temp2.put("Username", "patient4");
        patientInfo.add(temp2);
        HashMap<String, Object> temp3 = new HashMap<>();
        temp3.put("Number", 9);
        temp3.put("Username", "patient6");
        patientInfo.add(temp3);
        Log.wtf(TAG, "LIST: " + patientInfo.toString());
        startTime = System.currentTimeMillis();
        patientUsernames.add("patient7");
        patientUsernames.add("patient4");
        patientUsernames.add("patient6");
        Log.wtf("-_--START", "" + startTime);*/

        //OLD
        //getfile();

        if (!isNetworkAvailable()) {
            makeSnackBar(6000, "You are not connected to the internet. Therefore, you will not receive updates unless you connect.");
        } else {
        }

    }

    Query patientQuery;
    public static ProgressDialog loadingResults;
    int size0, size1, size2, size3, size4;
    boolean p1, p2, p3, p4, p5;

    //IDEA What I am thinking is just have this 1 time function, set a listener. If listener changes, call 1 time function
    int maxSize;
    DocumentSnapshot lastVisible, firstVisible;

    private void loadPatientInfo() {
        maxSize = 30;
        city = "Allen";
        db.collection("userPass")
                .whereEqualTo("State", state)
                //.orderBy("State")
                .whereEqualTo("City", city)
                .orderBy("Name")
                //.orderBy("Status", Query.Direction.DESCENDING)
                .limit(maxSize)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        //makeSnackBar(3000, "CHANGE");
                        Log.wtf("*-_LASTVISIBLE --------", "-------------------------------------------------------------------------------");

                        if (e != null) {
                            Log.wtf("Loading Patient Snapshot ERROR", e.toString());
                        } else {
                            //TODO Uncomment below
        /*if (isSafe()) {
            loadingResults = ProgressDialog.show(getContext(), "Loading Patients",
                    "Retrieving Data. Please wait...", true);
            loadingResults.setCancelable(true);
        }*/
                            //patientInfo = new ArrayList<>();
                            totalStartTime = System.currentTimeMillis();
                            Log.wtf("-_--Total Start", "" + totalStartTime);
                            patientNested0 = new ArrayList<>();
                            size0 = 0;
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                //patientInfo.add((HashMap<String, Object>) document.getData());
                                //patientUsernames.add(document.get("User").toString());
                                if (!document.get("User").equals(username)) {
                                    size0++;
                                    patientNested0.add((HashMap<String, Object>) document.getData());
                                    Log.wtf("*--READING ", document.getId() + " => " + document.getData());
                                }
                            }
                            //size0 = queryDocumentSnapshots.size() - 1;
                            Log.wtf("*-_NESTED 0", "Size: " + size0);
                            if (size0 == 0) {
                                if (isSafe() && loadingResults != null) loadingResults.cancel();
                                makeSnackBar(6100, "It appears there are no patients in your city. Share the app with others and wait for more users to create their accounts.");
                            } else {
                                if (adapter != null) adapter.notifyDataSetChanged();
                                getfile();
                            }
                            //    }
                            //README Size of specific city and state is smaller.
                            //INFO We are now going to read cities from state where cities are smaller than current city.
                            if (size0 < maxSize) {
                                lastVisible = queryDocumentSnapshots.getDocuments()
                                        .get(queryDocumentSnapshots.size() - 1);
                                firstVisible = queryDocumentSnapshots.getDocuments().get(0);
                                Log.wtf("*-_FIRSTVISIBLE 0", firstVisible.get("City") + " " +
                                        firstVisible.get("State") + " "+firstVisible.get("Name"));
                                Log.wtf("*-_LASTVISIBLE 0", lastVisible.get("City") + " " +
                                        lastVisible.get("State") + " "+lastVisible.get("Name"));

                                db.collection("userPass")
                                        .whereEqualTo("State", state)
                                        .orderBy("City")
                                        .orderBy("Name")
                                        .limit(maxSize - size0)
                                        .endBefore(firstVisible)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                //makeSnackBar(3000, "CHANGE INSIDE 1");
                                                if (e != null) {
                                                    Log.wtf("Loading Patient Snapshot Nested 1 ERROR", e.toString());
                                                } else {
                                                    Log.wtf("*-_NESTED 1", "Size: " + queryDocumentSnapshots.size());
                                                    patientNested1 = new ArrayList<>();
                                                    size1 = 0;
                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                        //patientInfo.add((HashMap<String, Object>) document.getData());
                                                        //patientUsernames.add(document.get("User").toString());
                                                        if (!document.get("User").equals(username)) {
                                                            size1++;
                                                            patientNested1.add((HashMap<String, Object>) document.getData());
                                                            Log.wtf("*--READING 1 ", document.getId() + " => " + document.getData());
                                                        } //size++;
                                                        // }
                                                    }
                                                    //size1 = queryDocumentSnapshots.size();
                                                    if (size1 == 0) {
                                                        if (isSafe() && loadingResults != null)
                                                            loadingResults.cancel();
                                                        //makeSnackBar(5000, "It appears there are no patients in your state.");
                                                    } else {
                                                        //INFO size2 is not 0 which means we just got the cities in same state that
                                                        // are smaller than current city.
                                                        //README Because we have smaller cities, we have to update firstVisible
                                                        firstVisible = queryDocumentSnapshots.getDocuments().get(0);
                                                        Log.wtf("*-_FIRSTVISIBLE 1", firstVisible.get("City") + " " +
                                                                firstVisible.get("State") + " "+firstVisible.get("Name"));
                                                        //if (adapter != null)
                                                        //  adapter.notifyDataSetChanged();
                                                        getfile();
                                                    }
                                                    //README Size of cities that are <= current city is not maximum
                                                    //INFO We are now going to read all cities after current city that are still same state
                                                    if (size1 + size0 < maxSize) {
                                                        db.collection("userPass")
                                                                .whereEqualTo("State", state)
                                                                .orderBy("City")
                                                                .orderBy("Name")
                                                                .limit(maxSize - size0 - size1)
                                                                .startAfter(lastVisible)
                                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                                        //makeSnackBar(3000, "CHANGE INSIDE 2");
                                                                        if (e != null) {
                                                                            Log.wtf("Loading Patient Snapshot Nested 2 ERROR", e.toString());
                                                                        } else {
                                                                            Log.wtf("*-_NESTED 2", "Size: " + queryDocumentSnapshots.size());
                                                                            patientNested2 = new ArrayList<>();
                                                                            size2 = 0;
                                                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                //patientInfo.add((HashMap<String, Object>) document.getData());
                                                                                //patientUsernames.add(document.get("User").toString());
                                                                                if (!document.get("User").equals(username)) {
                                                                                    size2++;
                                                                                    patientNested2.add((HashMap<String, Object>) document.getData());
                                                                                    Log.wtf("*--READING 2 ", document.getId() + " => " + document.getData());
                                                                                }    //size++;
                                                                                // }
                                                                            }
                                                                            //size2 = queryDocumentSnapshots.size();
                                                                            if (size2 == 0) {
                                                                                if (isSafe() && loadingResults != null)
                                                                                    loadingResults.cancel();
                                                                                if (size0 == 0 && size1 == 0)
                                                                                    makeSnackBar(6400, "It appears there are no patients in your state! Share the app with others and wait for more users to create their accounts.");
                                                                            } else {
                                                                                //INFO size3 is not 0 which means we just got the cities in same state that
                                                                                // are greater than current city.
                                                                                //README Because we have larger cities, we have to update lastVisible
                                                                                lastVisible = queryDocumentSnapshots.
                                                                                        getDocuments().get(queryDocumentSnapshots.size() - 1);
                                                                                Log.wtf("*-_LASTVISIBLE 2", lastVisible.get("City") + " " +
                                                                                        lastVisible.get("State") + " "+lastVisible.get("Name"));
                                                                                //    if (adapter != null)
                                                                                //        adapter.notifyDataSetChanged();
                                                                                getfile();
                                                                            }

                                                                            //INFO Now we move onto querying in the whole nation.
                                                                            if (size2 + size1 + size0 < maxSize) {
                                                                                /*db.collection("userPass")
                                                                                        .whereEqualTo("Country", country)
                                                                                        .orderBy("State")
                                                                                        .orderBy("City")
                                                                                        .orderBy("Name")
                                                                                        .limit(maxSize - size0)
                                                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                                                                //makeSnackBar(3000, "CHANGE INSIDE 5");
                                                                                                if (e != null) {
                                                                                                    Log.wtf("Loading Patient Snapshot Nested 3 ERROR", e.toString());
                                                                                                } else {
                                                                                                    Log.wtf("*-_NESTED 3", "Size: " + queryDocumentSnapshots.size());
                                                                                                    patientNested3 = new ArrayList<>();
                                                                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                                        //patientInfo.add((HashMap<String, Object>) document.getData());
                                                                                                        //patientUsernames.add(document.get("User").toString());

                                                                                                        patientNested3.add((HashMap<String, Object>) document.getData());
                                                                                                        Log.wtf("*--READING 4 ", document.getId() + " => " + document.getData());
                                                                                                        //size++;
                                                                                                        // }
                                                                                                    }
                                                                                                    size3 = queryDocumentSnapshots.size();
                                                                                                    if (size3 == 0) {
                                                                                                        loadingResults.cancel();
                                                                                                        if (size0 == 0 && size1 == 0 && size2 == 0)
                                                                                                            makeSnackBar(7000, "It appears there are no patients in your country! Wait for more users to create their accounts.");
                                                                                                    } else {
                                                                                                        //INFO size3 is not 0 which means we just got the cities in same state that
                                                                                                        // are greater than current city.
                                                                                                        //README Because we have larger cities, we have to update lastVisible
                                                                                                        firstVisible = queryDocumentSnapshots.getDocuments().get(0);
                                                                                                        if (adapter != null)
                                                                                                            adapter.notifyDataSetChanged();
                                                                                                        getfile();
                                                                                                    }

                                                                                                }
                                                                                            }
                                                                                        });*/
                                                                                Object[] objects = firstVisible.getData().values().toArray();
                                                                                Log.wtf("*-_FIRSTVISIBLE", firstVisible.get("City") + " " + firstVisible.get("Name"));
                                                                                db.collection("userPass")
                                                                                        .whereEqualTo("Country", country)
                                                                                        .orderBy("State")
                                                                                        .orderBy("City")
                                                                                        .orderBy("Name")
                                                                                        .limit(maxSize - size0 - size1 - size2)
                                                                                        //.endBefore("ZZZZ", "ZZZ")
                                                                                        .endBefore(firstVisible)
                                                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                                                                //makeSnackBar(3000, "CHANGE INSIDE 3");
                                                                                                if (e != null) {
                                                                                                    Log.wtf("Loading Patient Snapshot Nested 3 ERROR", e.toString());
                                                                                                } else {
                                                                                                    Log.wtf("*-_NESTED 3", "Size: " + queryDocumentSnapshots.size());
                                                                                                    patientNested3 = new ArrayList<>();
                                                                                                    size3 = 0;
                                                                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                                        //patientInfo.add((HashMap<String, Object>) document.getData());
                                                                                                        //patientUsernames.add(document.get("User").toString());
                                                                                                        if (!document.get("User").equals(username)) {
                                                                                                            size3++;
                                                                                                            patientNested3.add((HashMap<String, Object>) document.getData());
                                                                                                            Log.wtf("*--READING 3 ", document.getId() + " => " + document.getData());
                                                                                                        }            //size++;
                                                                                                        // }
                                                                                                    }
                                                                                                    //size3 = queryDocumentSnapshots.size();
                                                                                                    if (size3 == 0) {
                                                                                                        if (isSafe() && loadingResults != null)
                                                                                                            loadingResults.cancel();
                                                                                                        /*if (size == 0 && size2 == 0)
                                                                                                            makeSnackBar(5000, "It appears there are no patients in your state.");*/
                                                                                                    } else {
                                                                                                        //INFO size3 is not 0 which means we just got the cities in same state that
                                                                                                        // are greater than current city.
                                                                                                        //README Because we have larger cities, we have to update lastVisible
                                                                                                        //firstVisible = queryDocumentSnapshots.getDocuments().get(0);
                                                                                                        //                 if (adapter != null)
                                                                                                        //                     adapter.notifyDataSetChanged();
                                                                                                        getfile();
                                                                                                    }

                                                                                                    if (size3 + size2 + size1 + size0 < maxSize) {
                                                                                                        Log.wtf("*-_LASTVISIBLE", lastVisible.get("City") + " " + lastVisible.get("Name"));
                                                                                                        db.collection("userPass")
                                                                                                                .whereEqualTo("Country", country)
                                                                                                                .orderBy("State")
                                                                                                                .orderBy("City")
                                                                                                                .orderBy("Name")
                                                                                                                .limit(maxSize - size0)
                                                                                                                .startAfter(lastVisible)
                                                                                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                                                    @Override
                                                                                                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                                                                                        //makeSnackBar(3000, "CHANGE INSIDE 5");
                                                                                                                        if (e != null) {
                                                                                                                            Log.wtf("Loading Patient Snapshot Nested 4 ERROR", e.toString());
                                                                                                                        } else {
                                                                                                                            Log.wtf("*-_NESTED 4", "Size: " + queryDocumentSnapshots.size());
                                                                                                                            patientNested4 = new ArrayList<>();
                                                                                                                            size4 = 0;
                                                                                                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                                                                //patientInfo.add((HashMap<String, Object>) document.getData());
                                                                                                                                //patientUsernames.add(document.get("User").toString());
                                                                                                                                if (!document.get("User").equals(username)) {
                                                                                                                                    size4++;
                                                                                                                                    patientNested4.add((HashMap<String, Object>) document.getData());
                                                                                                                                    Log.wtf("*--READING 4 ", document.getId() + " => " + document.getData());
                                                                                                                                }  //size++;
                                                                                                                                // }
                                                                                                                            }
                                                                                                                            //size4 = queryDocumentSnapshots.size();
                                                                                                                            if (size4 == 0) {
                                                                                                                                if (isSafe() && loadingResults != null)
                                                                                                                                    loadingResults.cancel();
                                                                                                                                if (size0 == 0 && size1 == 0 && size2 == 0)
                                                                                                                                    makeSnackBar(6200, "It appears there are no patients in your country! Share the app with others and wait for more users to create their accounts.");
                                                                                                                            } else {
                                                                                                                                //INFO size3 is not 0 which means we just got the cities in same state that
                                                                                                                                // are greater than current city.
                                                                                                                                //README Because we have larger cities, we have to update lastVisible
                                                                                                                                //firstVisible = queryDocumentSnapshots.getDocuments().get(0);
                                                                                                                                //    if (adapter != null)
                                                                                                                                //      adapter.notifyDataSetChanged();
                                                                                                                                getfile();
                                                                                                                            }

                                                                                                                        }
                                                                                                                    }
                                                                                                                });
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                    }

                                                }
                                            }
                                        });

                            }
                        }
                    }
                });
        //db.collection("userPass").add
    }

    private void loadForeignPatientInfo() {
        //makeToast(city +":" + country);
        db.collection("userPass")
                .whereEqualTo("Country", country)
                .whereEqualTo("City", city)
                .orderBy("Name")
                //.orderBy("Status", Query.Direction.DESCENDING)
                .limit(maxSize)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        //makeSnackBar(3000, "CHANGE");
                        if (e != null) {
                            Log.wtf("Loading Patient Snapshot ERROR", e.toString());
                        } else {
                            loadingResults = ProgressDialog.show(getContext(), "Loading Patients",
                                    "Retrieving Data. Please wait...", true);
                            loadingResults.setCancelable(true);
                            patientInfo = new ArrayList<>();
                            totalStartTime = System.currentTimeMillis();
                            Log.wtf("-_--Total Start", "" + totalStartTime);
                            patientNested0 = new ArrayList<>();
                            size0 = 0;
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                /*patientInfo.add((HashMap<String, Object>) document.getData());
                                patientUsernames.add(document.get("User").toString());*/
                                Log.wtf("*--READING ", document.getId() + " => " + document.getData());
                                if (!document.get("User").equals(username)) {
                                    size0++;
                                    patientNested0.add((HashMap<String, Object>) document.getData());
                                    //Log.wtf("*--READING ", document.getId() + " => " + document.getData());
                                }
                            }
                            Log.wtf("*-_NESTED 0", "Size: " + size0);
                            if (size0 == 0) {
                                if (isSafe() && loadingResults != null) loadingResults.cancel();
                                makeSnackBar(6300, "It appears there are no patients in your city. Share the app with others and wait for more users to create their accounts.");
                            } else {
                                //if (size > 84) {
                                if (adapter != null) adapter.notifyDataSetChanged();
                                getfile();
                            }

                            if (size0 < maxSize) {
                                lastVisible = queryDocumentSnapshots.getDocuments()
                                        .get(queryDocumentSnapshots.size() - 1);
                                firstVisible = queryDocumentSnapshots.getDocuments().get(0);
                                Log.wtf("*-_FIRSTVISIBLE 2 unneces", firstVisible.get("City") + " " +
                                        firstVisible.get("State") + " "+firstVisible.get("Name"));
                                Log.wtf("*-_FIRSTVISIBLE 1", firstVisible.get("City") + " " +
                                        firstVisible.get("State") + " "+firstVisible.get("Name"));

                                db.collection("userPass")
                                        .whereEqualTo("Country", country)
                                        .orderBy("City")
                                        .orderBy("Name")
                                        .limit(maxSize - size0)
                                        .endBefore(firstVisible)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                //makeSnackBar(3000, "CHANGE INSIDE 5");
                                                if (e != null) {
                                                    Log.wtf("Loading Patient Snapshot Nested 1 ERROR", e.toString());
                                                } else {
                                                    Log.wtf("*-_NESTED 1", "Size: " + queryDocumentSnapshots.size());
                                                    patientNested1 = new ArrayList<>();
                                                    size1 = 0;
                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                        //patientInfo.add((HashMap<String, Object>) document.getData());
                                                        //patientUsernames.add(document.get("User").toString());
                                                        if (!document.get("User").equals(username)) {
                                                            size1++;
                                                            patientNested1.add((HashMap<String, Object>) document.getData());
                                                            Log.wtf("*--READING 1 ", document.getId() + " => " + document.getData());
                                                        }  //size++;
                                                        // }
                                                    }
                                                    //size1 = queryDocumentSnapshots.size();
                                                    if (size1 == 0) {
                                                        if (isSafe() && loadingResults != null)
                                                            loadingResults.cancel();
                                                        /*if (size0 == 0 && size1 == 0 && size2 == 0)
                                                            makeSnackBar(7000, "It appears there are no patients in your country! Wait for more users to create their accounts.");
                                                    */
                                                    } else {
                                                        //INFO size3 is not 0 which means we just got the cities in same state that
                                                        // are greater than current city.
                                                        //README Because we have larger cities, we have to update lastVisible
                                                        firstVisible = queryDocumentSnapshots.getDocuments().get(0);
                                                        Log.wtf("*-_FIRSTVISIBLE 1", firstVisible.get("City") + " " +
                                                                firstVisible.get("State") + " "+firstVisible.get("Name"));
                                                        //if (adapter != null)
                                                        //    adapter.notifyDataSetChanged();
                                                        getfile();
                                                    }
                                                    if (size1 + size0 < maxSize) {
                                                        db.collection("userPass")
                                                                .whereEqualTo("Country", country)
                                                                .orderBy("City")
                                                                .orderBy("Name")
                                                                .limit(maxSize - size0 - size1)
                                                                .startAfter(lastVisible)
                                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                                        //makeSnackBar(3000, "CHANGE INSIDE 5");
                                                                        if (e != null) {
                                                                            Log.wtf("Loading Patient Snapshot Nested 4 ERROR", e.toString());
                                                                        } else {
                                                                            Log.wtf("*-_NESTED 2", "Size: " + queryDocumentSnapshots.size());
                                                                            patientNested2 = new ArrayList<>();
                                                                            size2 = 0;
                                                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                //patientInfo.add((HashMap<String, Object>) document.getData());
                                                                                //patientUsernames.add(document.get("User").toString());
                                                                                if (!document.get("User").equals(username)) {
                                                                                    size2++;
                                                                                    patientNested2.add((HashMap<String, Object>) document.getData());
                                                                                    Log.wtf("*--READING 2 ", document.getId() + " => " + document.getData());
                                                                                }  //size++;
                                                                                // }
                                                                            }
                                                                            //size1 = queryDocumentSnapshots.size();
                                                                            if (size2 == 0) {
                                                                                if (isSafe() && loadingResults != null)
                                                                                    loadingResults.cancel();
                                                                                if (size0 == 0 && size1 == 0 && size2 == 0)
                                                                                    makeSnackBar(6600, "It appears there are no patients in your country! Share the app with others and wait for more users to create their accounts.");
                                                                            } else {
                                                                                //INFO size3 is not 0 which means we just got the cities in same state that
                                                                                // are greater than current city.
                                                                                //README Because we have larger cities, we have to update lastVisible
                                                                                firstVisible = queryDocumentSnapshots.getDocuments().get(0);
                                                                                if (adapter != null)
                                                                                    adapter.notifyDataSetChanged();
                                                                                getfile();
                                                                            }
                                                                        }
                                                                    }
                                                                });

                                                    }
                                                }
                                            }
                                        });

                            }
                        }
                    }
                });
        //db.collection("userPass").add
    }

    private ArrayList<String> fileList;


    public void getfile() {
        imageStartTime = System.currentTimeMillis();
        Log.wtf("-_--Image Start", "" + imageStartTime);
        fileList = new ArrayList<>();
        File location = new File(directory);
        File[] files = location.listFiles();
        //Log.wtf("** Files", "Path: " + directory + "  # of files: " + files.length);
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.endsWith(".jpg")) {
                //Log.wtf("**Files", "FileName:" + name);
                fileList.add(name.substring(0, name.length() - 4));
                //files[i].delete();
            }
        }
        patientInfo = new ArrayList<>();
        patientInfo.addAll(patientNested0);
        patientInfo.addAll(patientNested1);
        patientInfo.addAll(patientNested2);
        patientInfo.addAll(patientNested3);
        patientInfo.addAll(patientNested4);
        writeImages();
    }

    int count = 0;
    ArrayList<Boolean> goodToGo;

    public void writeImages() {
        final boolean[] set = {false};
        count = patientInfo.size();
        goodToGo = new ArrayList<>();
        Log.wtf("**List Size", "" + count);
        for (int i = 0; i < patientInfo.size(); i++) {
            //final String username = patientUsernames.get(i);
            final String username = patientInfo.get(i).get("User").toString();
            try {
                final StorageReference mImageRef = FirebaseStorage.getInstance().getReference(username + ".jpg");
                final long ONE_MEGABYTE = 1300 * 1300;
                boolean entered = false;
                if (fileList.isEmpty() || !fileList.contains(username)) {
                    //TODO Take a look into getBytes and whether it can be used to get smaller images.
                    final int finalI = i;
                    //entered = i == patientUsernames.size() - 1;
                    mImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            saveImage(getContext(), bm, username, "jpg");
                            count++;
                            //Log.wtf("**SAVED IMAGE", "IMAGE " + username + " SAVED  " + count + "  " + fileList.toString());
                            goodToGo.add(true);

                            //if (username.equals(patientUsernames.get(0)) ) {
                            if (goodToGo.size() == patientInfo.size()) {
                                adapter = new InfoRecyclerViewAdapter(getContext(), patientInfo, patientRecycler);
                                //setMaxHeight();
                                if (isSafe() && loadingResults != null)
                                    if (isSafe() && loadingResults != null) loadingResults.cancel();
                                adapter.notifyDataSetChanged();
                                patientRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                                patientRecycler.setAdapter(adapter);
                                long end = System.currentTimeMillis();
                                //Log.wtf("-_--END ", "Docs: " + (totalStartTime - imageStartTime) + "  Image: " + imageStartTime + "  TOTAL: " + totalStartTime);
                                set[0] = true;
                            /*Log.wtf("-_--Image Retrieval Time 1", "" + (end - totalStartTime));
                            Log.wtf("**CONTAINS IMAGES: ", containsFile("patient1")
                                    + " " + containsFile("patient2")
                                    + " " + containsFile("patient3"))*/
                                ;

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            if (isSafe() && loadingResults != null)
                                if (isSafe() && loadingResults != null) loadingResults.cancel();
//                            Toast.makeText(getContext(), "FAILED", Toast.LENGTH_LONG).show();
                            Log.wtf("**FAILED 2 SAVE IMAGE", exception.toString());
                        }
                    });
                } else {
                    goodToGo.add(true);
                }
            /*adapter = new InfoRecyclerViewAdapter(getContext(), patientInfo, patientRecycler);
            adapter.notifyDataSetChanged();
            patientRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            patientRecycler.setAdapter(adapter);*/
                // if (!entered && !set[0] && i == patientUsernames.size() - 1) {
                if (goodToGo.size() == patientInfo.size()) {
                    if (isSafe() && loadingResults != null)
                        if (isSafe() && loadingResults != null) loadingResults.cancel();
                    adapter = new InfoRecyclerViewAdapter(getContext(), patientInfo, patientRecycler);
                    patientRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                    patientRecycler.setAdapter(adapter);
                    long end = System.currentTimeMillis();
                    //Log.wtf("-_--END", "" + end);
                    //Log.wtf("-_--Image Retrieval Time 2", "" + (end - totalStartTime));
                    set[0] = true;
                }
            } catch (Exception e) {
                //makeSnackBar(16000, e.toString());
                Log.wtf("**EXCEPTION IN STORAGE READING", e.toString());
            }
        }

    }

    float height;


    //IDEA error will come up when results not loaded yet --> Progressdialog will officially dismiss in
    //  another fragment --> null

    public void setMaxHeight() {
        float currentHeight = patientRecycler.getHeight();
        ViewGroup.LayoutParams params = patientRecycler.getLayoutParams();
        params.height = 100;
        patientRecycler.setLayoutParams(params);
        //patientRecycler.setLayoutParams(new ViewGroup.LayoutParams(patientRecycler.getWidth(), (int) Math.min(currentHeight, maxHeightPatient)));
    }

    public boolean containsFile(String name) {
        File location = new File(directory);
        File[] files = location.listFiles();
        for (int i = 0; i < files.length; i++) {
            String n = files[i].getName();
            if (n.substring(0, n.length() - 4).equals(name)) return true;
        }
        return false;
    }

    public void saveImage(Context context, Bitmap bitmap, String name, String extension) {
        name = name + "." + extension;
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 97, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateList() {
        //TODO Write code to set Adapter on recyclerview
    }


    public void updateLayout() {
        //TODO Update Layout whatever.
    }

    int counter = 0;
    ListenerRegistration userPassListener;

    public void updateInfoTxt() {
        final DocumentReference docRef = db.collection("userPass").document(userPassID);
        userPassListener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (isSafe() && screen != null) {
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
                        //makeToast("Hi");
                        if (!status.equals(snapshot.getString("Status"))) {
                            //TODO Status changed --> Consider making a notification. Do vibrations at very least.
                        }

                        Log.wtf("*------ INFO RETRIEVED -----", source + " data: " + snapshot.getData());
                    } else if (snapshot == null) {
                        makeSnackBar(2000, "Could not load new data.");
                        Log.wtf("ERROR", source + " data: null");
                    } else {
                        Log.wtf("ERROR", source + " data: null");
                    }
                }

            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        userPassListener.remove();
        Log.wtf("*-*REMOVING UNCESSARY", "" + removeUncessaryFiles());
    }

    public boolean removeUncessaryFiles() {
        if (patientUsernames.isEmpty() || fileList.isEmpty())
            return false;
        File location = new File(directory);
        File[] files = location.listFiles();
        Log.wtf("*-* Files", "Path: " + directory + "  # of files: " + files.length + " " + patientUsernames);
        ArrayList<String> locations = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.endsWith(".jpg") && !patientUsernames.contains(name.substring(0, name.length() - 4))) {
                locations.add(name);
            }
        }
        for (String s : locations) {
            File temp = new File(directory + "/" + s);
            Log.wtf("*-* REMOVING", s);
            temp.delete();
        }
        //TODO Remove Below 2 lines and basically all Log.wtf() that are not needed for deployment.
        files = location.listFiles();
        Log.wtf("*-* Files", "Path: " + directory + "  # of files: " + files.length);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userPassListener.remove();
        Log.wtf("*-*REMOVING UNCESSARY", "" + removeUncessaryFiles());
    }

    protected boolean isSafe() {
        return !(this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }

    private void writeNewInfo(Map<String, Object> data) {
        String state = data.get("State").toString();
        if (state.isEmpty() || state.length() == 0)
            state = " ";
        String e = data.get("Email").toString();
        if (e.isEmpty() || e.length() == 0)
            e = " ";
        String toWrite = "";
        String tempStatus = data.get("Status").toString();
        toWrite += data.get("Type");
        toWrite += "___________";
        toWrite += data.get("Doc ID");
        toWrite += "___________";
        toWrite += data.get("User");
        toWrite += "___________";
        toWrite += data.get("Pass");
        toWrite += "___________";
        toWrite += data.get("Updated");
        toWrite += "___________";
        toWrite += data.get("Name");
        toWrite += "___________";
        toWrite += data.get("Phone");
        toWrite += "___________";
        /*toWrite += data.get("Email");
        toWrite += "___________";*/
        toWrite += data.get("Doc ID");
        toWrite += "___________";
        toWrite += tempStatus;
        toWrite += "___________";
        toWrite += userPassID;
        toWrite += "___________";
        toWrite += data.get("Created");
        toWrite += "___________";
        toWrite += data.get("City");
        toWrite += "___________";
        toWrite += state;
        toWrite += "___________";
        toWrite += data.get("Country");
        toWrite += "___________";
        toWrite += e;

        username = data.get("User").toString();
        documentID = data.get("Doc ID").toString();
        userPassID = userPassID;
        name = data.get("Name").toString();
        type = data.get("Type").toString();
        password = data.get("Pass").toString();
        accountCreated = data.get("Created").toString();
        statusLastUpdated = data.get("Updated").toString();
        phone = data.get("Phone").toString();
        //email = data.get("Email").toString();

        status = tempStatus;
        writeToInfo(toWrite);
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

    private void readStorage() {
        String info = readFromFile("info.txt", getContext());
        String[] contents = info.split("___________");
        Log.wtf("Read Status-", contents[8]);
        type = (contents[0]);
        documentID = (contents[1]);
        username = (contents[2]);
        password = (contents[3]);
        statusLastUpdated = (contents[4]);
        name = (contents[5]);
        phone = (contents[6]);
        //next.putExtra("Email", contents[7]);
        documentID = (contents[7]);
        status = (contents[8]);
        userPassID = (contents[9]);
        accountCreated = (contents[10]);
        city = (contents[11]);
        state = (contents[12]);
        country = (contents[13]);
        email = (contents[14]);
    }

    private String readFromFile(String file, Context context) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.wtf("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.wtf("login activity", "Can not read file: " + e.toString());
        }

        return ret;
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


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    Snackbar mySnackbar;

    private void makeSnackBar(int duration, String s) {
        if (isSafe()) {
            mySnackbar = Snackbar.make(screen, s, duration);
            View snackbarView = mySnackbar.getView();
            TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setMaxLines(4);
            mySnackbar.show();
        }
    }

    public void makeToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }
}
