package app.ij.covid_id.ui.doctor_dashboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
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

import app.ij.covid_id.DoctorDashboard;
import app.ij.covid_id.R;
import app.ij.covid_id.ui.dashboard.DashboardViewModel;

public class DoctorDashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    FirebaseFirestore db;
    ScrollView screen;
    View root;
    HashMap<String, Object> generalInfo;
    ArrayList<HashMap<String, Object>> statusUpdates;
    Pair<HashMap<String, Object>, ArrayList<HashMap<String, Object>>> pair;
    public String documentID, username, name, userPassID, medicalCenter, type, password, accountCreated, phone, email;
    public static String status;
    String statusLastUpdated;
    TextView statusTextView, lastUpdated, city;
    String currentCity;
    String doctorPath;
    RelativeLayout statusColor1/*, statuScolor2*/;
    RecyclerView list;
    Button update;
    TextView message;
    TextView lastCity, lastMedicalCenter;
    String cityLast, centerLast;

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
        lastCity = (TextView) findViewById(R.id.providerCity);
        lastMedicalCenter = (TextView) findViewById(R.id.providerCenter);
        statusColor1 = (RelativeLayout) findViewById(R.id.statusColor1);
        //statuScolor2 = (RelativeLayout) findViewById(R.id.statusColor2);
        update = (Button) findViewById(R.id.update);
        status = "";
        readStorage();
        //readUpdate();
        DoctorDashboard.variable = 3;
        Log.wtf("*-((( onCreated", "CAlled");

        //dashboardViewModel = ViewModelProviders.of(this, new DashboardViewModelFactory(getActivity(), username, documentID, db, root)).get(DashboardViewModel.class);
        return root;
    }
    public void updateStatustxt(String stat) {
        String info = readFromFile("statusUpdate.txt", getContext());
        String[] contents = info.split("-----|\\W+|\\n|\\r");
        boolean match = false;
        String matchingStatus = "";
        //Bob-----Unknown-----
        int position = 0;
        String before = "";
        String after = "";
        String logger = "";
        //for (String s : contents) logger += "-" + s + "-\n";
        //before = contents[0] + "-----";
        ArrayList<String> al = new ArrayList<>();
        for (String s : contents) {
            if (s.isEmpty() || s.length() < 5) {

            } else {
                al.add(s);
                logger += "-" + s + "-\n";
            }
        }
        //contents = (String[]) al.toArray();
        contents = al.toArray(new String[al.size()]);
        Log.wtf("*Logger", logger);
        for (int i = 0; i < contents.length - 1; i += 2) {
            if (contents[i].equals(username)) {
                match = true;
                position = i;
                if (i + 1 < contents.length)
                    matchingStatus = contents[i + 1];
                //break;
            } else if (match) {
                after += contents[i] + "-----" + contents[i + 1] + "-----";
            } else {
                before += contents[i] + "-----" + contents[i + 1] + "-----";
            }
        }
        Log.wtf("*readUpdate()", username + " " + match + ": " + matchingStatus + ", " + stat + "--" + info + "  b4:--" + before + "--af: " + after);
        if (match) {
            //README Status right now (updated when they hit the login button)
            // is different from status from last sign in.
            if (!matchingStatus.equals(stat)) {
                //DONE Make notification
                String replaceCurrentUser = before + username + "-----" + stat + "-----" + after;
                Log.wtf("*replaceCurrentUser", replaceCurrentUser);
                //writeToInfo("statusUpdate.txt", replaceCurrentUser);
                writeToStatusUpdate(replaceCurrentUser);


            }
        } else {
            //DONE Write whatever their current username and status is.
            //README They are a new user and their info is not in statusUpdate.txt
            String writeNewUser = username + "-----" + stat + "-----" + before;
            writeToStatusUpdate(writeNewUser);

        }
    }
    private void writeToStatusUpdate(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("statusUpdate.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.wtf("*Exception", "File write failed: " + e.toString());
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateLayout();
        if (!isNetworkAvailable()) {
            makeSnackBar(6000, "You are not connected to the internet. Therefore, you will not receive updates unless you connect.");
        } else {
        }
        //TODO Uncomment below when you release update with recyclerview.
        //loadInformation();

        //makeToast("Inside doctor dashboard");
        //updateInfoTxt();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateInfoTxt();
        Log.wtf("*-((( onStart", "CAlled");
    }

    private void updateList() {
        //TODO Write code to set Adapter on recyclerview
    }

    private void loadInformation() {
        final Query updatesRef = db.collection(doctorPath + "/" + documentID + "/" + "Updates").whereEqualTo("3", 3);
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
                    String source2 = documentSnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    if (documentSnapshot != null && documentSnapshot.exists() && source.equals("Server")) {
                        statusUpdates.add((HashMap) documentSnapshot.getData());
                        //textView.setText("TEST: " + counter);
                        Log.wtf("*------ UPDATE INFO RETRIEVED -----", source + " data: " + documentSnapshot.getData());
                    } else if (source2.contains("cach")) {
                        makeSnackBar(4000, "Loaded offline data. Connect to the internet for updated information.");
                        statusUpdates.add((HashMap) documentSnapshot.getData());
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
        //README Uncomment below for quick testing
        //status = "Unknown";
        //statusLastUpdated = "4/23/20 22:48:25";

        if (isSafe()) {
            statusTextView.setText(cleanStatus());
            lastUpdated.setText(cleanDate());
            lastCity.setText("My City: " + currentCity);
            lastMedicalCenter.setText("Center: " + centerLast);
        }
        //TODO use info from ArrayList to fill recycler.
    }

    int counter = 0;
    public static ListenerRegistration listener, listener2;


    //IMPORTANT If you do decide on writing Hello "NAME" on dashboard, then you have to listen to local changes
    //  and update UI for local changes to say Hello "new name" or whatever.
    // For things like status, don't need to worry since they can't change it in settings
    public void updateInfoTxt() {
        Log.wtf("Update Info Txt CAlled", "CALLED");
        final DocumentReference docRef = db.collection("userPass").document(userPassID);
        listener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (statusTextView != null && isSafe()) {
                    if (e != null) {
                        Log.wtf("USERPATH ERROR", "Listen failed.", e);
                        makeSnackBar(3000, "Could not load your data. Are you connected to the internet?");
                        return;
                    }
                    String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    String source2 = snapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    //README Local changes (taking place in Settings) will update info.txt in Settings
                    if (snapshot != null && snapshot.exists() && source.equals("Server")) {
                        if (!status.equals(snapshot.getString("Status"))) {
                            //TODO Status changed --> Consider making a notification. Do vibrations at very least.
                            Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                            long[] pattern = {0, 800, 250, 800, 250, 800, 250, 800, 250};
                            if (vib.hasVibrator())
                                vib.vibrate(pattern, -1);
                            largeToast("Your COVID Status was updated!");
                            updateStatustxt(snapshot.getString("Status").toString());
                        }
                        status = snapshot.get("Status").toString();
                        writeNewInfo(snapshot.getData());
                        updateLayout();
                        Log.wtf("*------ INFO RETRIEVED (Doctor) -----", source + " data: " + snapshot.getData());
                    } else if (source2.contains("cach")) {
                        makeSnackBar(4000, "Loaded offline data. Connect to the internet for updated information.");
                        writeNewInfo(snapshot.getData());
                        updateLayout();
                    } else if (snapshot == null) {
                        makeSnackBar(2000, "Could not load new data.");
                        Log.wtf("ERROR", source + " data: null");
                    } else {
                        Log.wtf("ERROR", source + " data: null");
                    }
                }
                // IMPORTANT -- Logic for notification
                //  If status is not "" and !tempStatus.equals(status)
                //  Then do vibration notification
                //NOTES For above, may have to call isSafe() before doing vibration.
                //String tempStatus = snapshot.getData().get("Status").toString();
                //if (!tempStatus.equals(status)) {
                //TODO Status changed --> Consider making a notification.
                //makeToast("IMPORTANT: Your COVID Status changed. Visit the dashboard for more info.");
                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //makeToast("IMPORTANT: Your COVID Status changed. Visit the dashboard for more info.");
                    }
                }, 3500);*/
                //}

            }
        });

    }

    protected boolean isSafe() {
        return !(this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }

    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isSafe() && listener != null)
            listener.remove();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isSafe() && listener != null)
            listener.remove();
    }*/
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
        toWrite += "___________";
        toWrite += data.get("Center");
        toWrite += "___________" + data.get("CityU").toString() + "___________" + data.get("CenterU").toString();

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
        medicalCenter = data.get("Center").toString();
        cityLast = data.get("CityU").toString();
        centerLast = data.get("CenterU").toString();
        currentCity = data.get("City").toString();
        status = tempStatus;
        writeToInfo(toWrite);
    }

    public String cleanStatus() {
        if (status.equals("Unknown")) {
            statusColor1.setBackgroundResource(R.drawable.gradient_border_grey);
            // statuScolor2.setBackgroundResource(R.drawable.grey_bottom);
            statusTextView.setTextColor(Color.parseColor("#ffffff"));
            statusTextView.setTextSize(36);
            String temp = "This status means that either you have not been tested or the test results have not been received yet.\n" +
                    "You can contact your Medical Provider to get your status updated.\n" +
                    "\nNote: Ensure your provider has the app downloaded. If the provider does not have the app, reach out to covid.ijapps@gmail.com mentioning the doctor/provider name, street address and phone/email information.\n";
            SpannableString ss = new SpannableString(temp);
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#8b02ed")), temp.indexOf("Note"), temp.indexOf("Note") + 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(Typeface.BOLD), temp.indexOf("Note"), temp.indexOf("Note") + 5, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(Typeface.BOLD), temp.indexOf("covid"), temp.indexOf("covid") + 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#1599e6")), temp.indexOf("covid"), temp.indexOf("covid") + 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{"covid.ijapps@gmail.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT, "COVID Testing Status Update");
                    //TODO Change app name below based on which one you choose to go with.
                    email.putExtra(Intent.EXTRA_TEXT, "Hello,\n\tI would like to know when I can receive an update regarding my COVID Status."
                            + "\n\tMy medical provider does not have the COVID-ID app.\n\tMy medical provider is ____,  their phone number is ____, and their email is ____.\n\nThank you");
                    email.setType("message/rfc822");
                    startActivity(Intent.createChooser(email, "Choose an Email app:"));
                }
            };
            ss.setSpan(clickableSpan, temp.indexOf("covid"), temp.indexOf("covid") + 22, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            message.setText(ss);

            message.setMovementMethod(LinkMovementMethod.getInstance());
            /*message.setText("This status means that either you have not been tested or the test results have not been received yet.\n" +
                    "You can contact your Medical Provider to get your status updated.\n" +
                    "Note: Ensure your provider has the app downloaded. If the provider does not have the app, reach out to ijappscovid@gmail.com mentioning the doctor/provider name, street address and phone/email information.");
            */
            return "Unknown\nor\nUntested";
        } /*else if(status.equals("Deceased")){
            statusColor1.setBackgroundResource(R.drawable.gradient_border_grey);
            statusTextView.setTextSize(37);
            statusTextView.setTextColor(Color.parseColor("#000000"));
            message.setText("Unfortunately, the patient has passed away from COVID.\nWe send our regards.");
        }*/ else if (status.equals("Recovered")) {
            statusColor1.setBackgroundResource(R.drawable.gradient_border_green);
            statusTextView.setTextSize(37);
            statusTextView.setTextColor(Color.parseColor("#000000"));
            message.setText("This status implies that you were infected with COVID and more importantly you have recovered (as recorded by your doctor).\nAsk your doctor how you can conduct future activities and or give plasma to help others.\n");
            //  statuScolor2.setBackgroundResource(R.drawable.green_bottom);
        } else if (status.equals("Uninfected")) {
            statusColor1.setBackgroundResource(R.drawable.gradient_border_yellow);
            statusTextView.setTextSize(37);
            statusTextView.setTextColor(Color.parseColor("#000000"));
            message.setText("This status implies that your COVID test has returned uninfected as recorded by your doctor.\nContact your doctor for more information.\n");
            // statuScolor2.setBackgroundResource(R.drawable.yellow_bottom);
        } else if (status.equals("Infected")) {
            statusTextView.setTextColor(Color.parseColor("#ffffff"));
            statusTextView.setTextSize(37);
            message.setText("This status implies that you are infected with COVID as recorded by your doctor.\nPlease contact your doctor immediately.\n");

            statusColor1.setBackgroundResource(R.drawable.gradient_border_red);
            // statuScolor2.setBackgroundResource(R.drawable.red_bottom);
        }
        return status +"\n(Tested)";
    }

    public String cleanDate() {
        String currentDate = new SimpleDateFormat("M/d/yy", Locale.getDefault()).format(new Date());
        String[] split = statusLastUpdated.split(" ");
        String time = cleanTime(split[1]);
        if (currentDate.equals(split[0]))
            return "Today," + time;
        int curDay = Integer.parseInt(currentDate.substring(currentDate.indexOf("/") + 1, currentDate.indexOf("/", currentDate.indexOf("/") + 1)));
        int previousDay = Integer.parseInt(split[0].substring(split[0].indexOf("/") + 1, split[0].indexOf("/", split[0].indexOf("/") + 1)));
        if (curDay == previousDay + 1)
            return "Yesterday," + time;
        return split[0].substring(0, split[0].length() - 3) + "," + time;
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
        if (isSafe()) {
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("info.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
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
        if (status == null || status.length() < 1)
            status = (contents[8]);
        currentCity = (contents[12]);
        userPassID = (contents[9]);
        medicalCenter = (contents[15]);
        cityLast = (contents[16]);
        centerLast = (contents[17]);
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

    @Override
    public void onDestroy() {
        Log.wtf("INFO", "DoctorDashboardFragment: removing:");
        /*if (listener != null)
            listener.remove();
        if (listener2 != null)
            listener2.remove();*/
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.wtf("INFO", "DoctorDashboardFragment: removing:");
       /* if (listener != null)
            listener.remove();
        if (listener2 != null)
            listener2.remove();*/
        super.onDestroyView();
    }

    public void loadInformationOld() {
        generalInfo = new HashMap<>();
        statusUpdates = new ArrayList<>();
        Log.wtf("*-*-- LOCATION: ", "loadInformation() called");
        //TODO Make notification onEvent
        final DocumentReference docRef = db.collection(doctorPath).document(documentID);
        final CollectionReference updatesRef = db.collection(doctorPath + "/" + documentID + "/" + "Updates");
        listener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.wtf("ERROR", "Listen failed.", e);
                    makeSnackBar(3000, "Could not load your data. Are you connected to the internet?");
                    return;
                }
                counter++;

                listener2 = updatesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                            String source2 = documentSnapshot.getMetadata().isFromCache() ?
                                    "local cache" : "server";
                            if (documentSnapshot != null && documentSnapshot.exists() && source.equals("Server")) {
                                statusUpdates.add((HashMap) documentSnapshot.getData());
                                //textView.setText("TEST: " + counter);
                                Log.wtf("*------ INFO RETRIEVED -----", source + " data: " + documentSnapshot.getData());
                            } else if (source2.contains("cac")) {
                                makeSnackBar(4000, "Loaded offline data. Connect to the internet for updated information.");
                                statusUpdates.add((HashMap) documentSnapshot.getData());
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

    Toast toast;

    public void makeToast(String s) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(getContext(), s, Toast.LENGTH_LONG);
        toast.show();
    }

    private void largeToast(String s) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(getContext(), s, Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        toast.show();

    }
}