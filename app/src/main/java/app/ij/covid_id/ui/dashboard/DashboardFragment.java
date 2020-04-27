package app.ij.covid_id.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
    String statusLastUpdated;
    TextView statusTextView, lastUpdated;
    String patientsPath;
    RelativeLayout statusColor1/*, statuScolor2*/;
    RecyclerView list;
    Button update;
    TextView message;

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
        message = (TextView) findViewById(R.id.information);
        //textView = (TextView) findViewById(R.id.text_dashboard);
        statusTextView = (TextView) findViewById(R.id.status);
        lastUpdated = (TextView) findViewById(R.id.lastUpdated);
        statusColor1 = (RelativeLayout) findViewById(R.id.statusColor1);
        //statuScolor2 = (RelativeLayout) findViewById(R.id.statusColor2);
        list = (RecyclerView) findViewById(R.id.list);
        //TODO Change below to Patient after deleting collection and creating new dummy accounts.
        patientsPath = "Patients";
        update = (Button) findViewById(R.id.update);
        readStorage();

        dashboardViewModel = ViewModelProviders.of(this, new DashboardViewModelFactory(getActivity(), username, documentID, db, root)).get(DashboardViewModel.class);
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateLayout();
        if (!isNetworkAvailable()) {
            makeSnackBar(6000, "You are not connected to the internet. Therefore, you will not receive updates unless you connect.");
        } else {
            //TODO Read from userPass 1-time and update info.txt.
            //TODO If not too much, add a listener on userPass and everytime it is changed
            // only write to info.txt. That is the only purpose of listener ----> UI will be for loadInformation();
        }
        //TODO Uncomment below when you release update with recyclerview.
        //loadInformation();
        updateInfoTxt();
    }

    private void updateList() {
        //TODO Write code to set Adapter on recyclerview
    }

    private void loadInformation() {
        final CollectionReference updatesRef = db.collection(patientsPath + "/" + documentID + "/" + "Updates");
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
                    counter++;

                    String source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    if (documentSnapshot != null && documentSnapshot.exists() && source.equals("Server")) {
                        statusUpdates.add((HashMap) documentSnapshot.getData());
                        //textView.setText("TEST: " + counter);
                        Log.wtf("*------ UPDATE INFO RETRIEVED -----", source + " data: " + documentSnapshot.getData());
                    } else {
                        Log.wtf("ERROR", source + " data: null");
                        makeSnackBar(3000, "No data could be found. Are you connected to the internet?");
                    }
                }
                updateList();
                Log.wtf("COUNTER VALUE", "-------------------- " + counter);
            }
        });
    }

    public void updateLayout() {
        //TODO use info from the maps and arraylsts to display the stuff.
        statusTextView.setText(cleanStatus());
        lastUpdated.setText(cleanDate());
    }

    int counter = 0;

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
                    updateLayout();
                    //textView.setText(snapshot.getData().get("Status").toString());
                    Log.wtf("*------ INFO RETRIEVED -----", source + " data: " + snapshot.getData());
                } else if (snapshot == null) {
                    makeSnackBar(2000, "Could not load new data.");
                    Log.wtf("ERROR", source + " data: null");
                } else {
                    Log.wtf("ERROR", source + " data: null");
                }

            }
        });

    }

    private void writeNewInfo(Map<String, Object> data) {
        String toWrite = "";
        String tempStatus = data.get("Status").toString();
        toWrite += data.get("Type");
        toWrite += "___________";
        toWrite += data.get("Document ID");
        toWrite += "___________";
        toWrite += data.get("Username");
        toWrite += "___________";
        toWrite += data.get("Password");
        toWrite += "___________";
        toWrite += data.get("Last Updated");
        toWrite += "___________";
        toWrite += data.get("Name");
        toWrite += "___________";
        toWrite += data.get("Password");
        toWrite += "___________";
        /*toWrite += data.get("Email");
        toWrite += "___________";*/
        toWrite += data.get("Document ID");
        toWrite += "___________";
        toWrite += tempStatus;
        toWrite += "___________";
        toWrite += userPassID;
        toWrite += "___________";
        toWrite += data.get("Account Created");
        username = data.get("Username").toString();
        documentID = data.get("Document ID").toString();
        userPassID = userPassID;
        name = data.get("Name").toString();
        type = data.get("Type").toString();
        password = data.get("Password").toString();
        accountCreated = data.get("Account Created").toString();
        statusLastUpdated = data.get("Last Updated").toString();
        phone = data.get("Phone").toString();
        //email = data.get("Email").toString();
        if (!tempStatus.equals(status)) {
            //TODO Status changed --> Consider making a notification.
        }
        status = tempStatus;
        writeToInfo(toWrite);
    }

    public String cleanStatus() {
        if (status.equals("Unknown")) {
            statusColor1.setBackgroundResource(R.drawable.gradient_border_grey);
           // statuScolor2.setBackgroundResource(R.drawable.grey_bottom);
            statusTextView.setTextColor(Color.parseColor("#ffffff"));
            statusTextView.setTextSize(36);
            return "Unknown\nor\nUntested";
        } else if (status.equals("Recovered")) {
            statusColor1.setBackgroundResource(R.drawable.gradient_border_green);
            statusTextView.setTextSize(37);
            statusTextView.setTextColor(Color.parseColor("#000000"));
            //  statuScolor2.setBackgroundResource(R.drawable.green_bottom);
        } else if (status.equals("Uninfected")) {
            statusColor1.setBackgroundResource(R.drawable.gradient_border_yellow);
            statusTextView.setTextSize(37);
            statusTextView.setTextColor(Color.parseColor("#000000"));
            // statuScolor2.setBackgroundResource(R.drawable.yellow_bottom);
        } else if (status.equals("Infected")) {
            statusTextView.setTextColor(Color.parseColor("#ffffff"));
            statusTextView.setTextSize(37);
            statusColor1.setBackgroundResource(R.drawable.gradient_border_red);
           // statuScolor2.setBackgroundResource(R.drawable.red_bottom);
        }
        return status;
    }

    public String cleanDate() {
        String currentDate = new SimpleDateFormat("M/d/yy", Locale.getDefault()).format(new Date());
        String[] split = statusLastUpdated.split(" ");
        String time = cleanTime(split[1]);
        if (currentDate.equals(split[0]))
            return "Today, " + time;
        return "Last Updated: " + split[0].substring(0, split[0].length() - 3) + time;
    }

    public String cleanTime(String s) {
        Integer a = Integer.parseInt(s.substring(0, s.indexOf(":")));
        String end = "AM";
        if (a > 12) {
            a -= 12;
            end = "PM";
        } else if (a == 0) {
            a += 12;
        }
        int firstIndex = s.indexOf(":");
        int secondIdex = s.indexOf(":", firstIndex + 1);
        return " " + a + s.substring(s.indexOf(":"), secondIdex) + " " + end;
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
        accountCreated = (contents[10]);
        statusLastUpdated = (contents[4]);
        name = (contents[5]);
        phone = (contents[6]);
        //next.putExtra("Email", contents[7]);
        documentID = (contents[7]);
        status = (contents[8]);
        userPassID = (contents[9]);
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

    public void loadInformationOld() {
        generalInfo = new HashMap<>();
        statusUpdates = new ArrayList<>();
        Log.wtf("*-*-- LOCATION: ", "loadInformation() called");
        //TODO Make notification onEvent
        final DocumentReference docRef = db.collection(patientsPath).document(documentID);
        final CollectionReference updatesRef = db.collection(patientsPath + "/" + documentID + "/" + "Updates");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.wtf("ERROR", "Listen failed.", e);
                    makeSnackBar(3000, "Could not load your data. Are you connected to the internet?");
                    return;
                }
                counter++;

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
                            counter++;

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
                        Log.wtf("COUNTER VALUE", "-------------------- " + counter);
                        pair = new Pair<>(generalInfo, statusUpdates);
                        //updateLayout();
                    }
                });

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";
                if (snapshot != null && snapshot.exists() && source.equals("Server")) {
                    generalInfo = (HashMap) snapshot.getData();
                    pair = new Pair<>(generalInfo, statusUpdates);
                    //updateLayout();
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
        mySnackbar = Snackbar.make(screen, s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
    }

    public void makeToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }
}