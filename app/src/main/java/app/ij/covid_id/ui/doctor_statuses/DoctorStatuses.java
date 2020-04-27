package app.ij.covid_id.ui.doctor_statuses;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import app.ij.covid_id.ui.dashboard.DashboardViewModel;

public class DoctorStatuses extends Fragment {

    FirebaseFirestore db;
    ScrollView screen;
    View root;
    public String documentID, username, name, userPassID, type, password, accountCreated, phone, email, status;
    String statusLastUpdated;
    String doctorsPath;
    RecyclerView list;
    Button update;
    TextView message;

    public View findViewById(int id) {
        return root.findViewById(id);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        root = inflater.inflate(R.layout.fragment_doctor_statuses, container, false);
        screen = root.findViewById(R.id.screen);
        //TODO Chagne this to Doctor
        doctorsPath = "Doctors";

        readStorage();
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


    public void updateLayout() {
        //TODO Update Layout whatever.
    }

    int counter = 0;

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