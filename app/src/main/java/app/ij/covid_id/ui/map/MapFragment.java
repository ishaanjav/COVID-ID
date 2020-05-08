package app.ij.covid_id.ui.map;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.ij.covid_id.DoctorDashboard;
import app.ij.covid_id.MainActivity;
import app.ij.covid_id.MyDebug;
import app.ij.covid_id.PatientDashboard;
import app.ij.covid_id.R;
import app.ij.covid_id.ui.doctor_dashboard.DoctorDashboardFragment;

public class MapFragment extends Fragment {
    View root;
    RelativeLayout screen;
    //private MapViewModel mapViewModel;
    Button update;

    public View findViewById(int id) {
        return root.findViewById(id);
    }

    FirebaseFirestore db;
    String userPassID;
    String status, username;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_map, container, false);
        update = (Button) findViewById(R.id.update);
        screen = (RelativeLayout) findViewById(R.id.screen);
        db = FirebaseFirestore.getInstance();
        DoctorDashboard.variable = 3;
        PatientDashboard.variable = 3;
        readStorage();
        //updateInfoTxt();
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
        if (MyDebug.LOG) Log.wtf("*Logger", logger);
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
        if (MyDebug.LOG)
            Log.wtf("*readUpdate()", username + " " + match + ": " + matchingStatus + ", " + stat + "--" + info + "  b4:--" + before + "--af: " + after);
        if (match) {
            //README Status right now (updated when they hit the login button)
            // is different from status from last sign in.
            if (!matchingStatus.equals(stat)) {
                //DONE Make notification
                String replaceCurrentUser = before + username + "-----" + stat + "-----" + after;
                if (MyDebug.LOG) Log.wtf("*replaceCurrentUser", replaceCurrentUser);
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
            if (MyDebug.LOG) Log.wtf("*Exception", "File write failed: " + e.toString());
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        updateInfoTxt();
        if (MyDebug.LOG) Log.wtf("*-((( onStart", "CAlled");
    }

    protected boolean isSafe() {
        return !(this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }

    public static ListenerRegistration listener;

    @Override
    public void onDestroy() {
        if (listener != null)
            listener.remove();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (listener != null)
            listener.remove();
        super.onDestroyView();
    }

    public void updateInfoTxt() {
        final DocumentReference docRef = db.collection("userPass").document(userPassID);
        listener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (isSafe()) {
                    if (e != null) {
                        if (MyDebug.LOG) Log.wtf("USERPATH ERROR", "Listen failed.", e);
                        makeSnackBar(3000, "Could not load your data. Are you connected to the internet?");
                        return;
                    }
                    String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    String source2 = snapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    //README Local changes (taking place in Settings) will update info.txt in Settings
                    if (snapshot != null && snapshot.exists() && source.equals("Server")) {
                        //TODO Write new info to info.txt
                        //makeToast("HI");
                        if (!status.equals(snapshot.getString("Status"))) {
                            DoctorDashboardFragment.status = snapshot.getString("Status");
                            //TODO Status changed --> Consider making a notification. Do vibrations at very least.
                            Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                            long[] pattern = {0, 800, 250, 800, 250, 800, 250, 800, 250};
                            if (vib.hasVibrator())
                                vib.vibrate(pattern, -1);
                            largeToast("Your COVID Status was updated! Check the dashboard.");
                            updateStatustxt(snapshot.getString("Status").toString());
                        }
                        //writeNewInfo(snapshot.getData());
                        if (MyDebug.LOG)
                            Log.wtf("*------ INFO RETRIEVED (Map) -----", source + " data: " + snapshot.getData());
                    } else if (source2.contains("cach")) {
                        makeSnackBar(4000, "Loaded offline data. Connect to the internet for updated information.");
                        //writeNewInfo(snapshot.getData());
                    } else if (snapshot == null) {
                        if (MyDebug.LOG) Log.wtf("ERROR", source + " data: null");
                    } else {
                        if (MyDebug.LOG) Log.wtf("ERROR", source + " data: null");
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

    private void readStorage() {
        String info = readFromFile("info.txt", getContext());
        String[] contents = info.split("___________");
        if (MyDebug.LOG) Log.wtf("Read Status-", contents[8]);
        userPassID = (contents[9]);
        username = (contents[2]);
        status = (contents[8]);
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
            if (MyDebug.LOG) Log.wtf("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            if (MyDebug.LOG) Log.wtf("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateButton();
    }

    private void updateButton() {
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()) {
                    //https://play.google.com/store/apps/details?id=app.anany.faceanalyzer
                    final String appPackageName = getContext().getPackageName(); // getPackageName() from Context or Activity object
                    if (MyDebug.LOG) Log.wtf("Package name", appPackageName);
                    makeToast("Connect to the internet first.");
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("samsungapps://ProductDetail/" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://galaxy.store/covidi")));
                    }
                } else {
                    db.collection("Update").document("Update 1").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                            Boolean update = Boolean.parseBoolean(snapshot.get("Update").toString());
                            if (update) {
                                final String appPackageName = getContext().getPackageName(); // getPackageName() from Context or Activity object
                                if (MyDebug.LOG) Log.wtf("Package name", appPackageName);
                                makeToast("Updates available!.");
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("samsungapps://ProductDetail/" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://galaxy.store/covidi")));
                                }
                            } else {
                                makeSnackBar(2125, "Sorry. No updates available yet.");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            makeSnackBar(2600, "Couldn't check for updates. Try again.");
                        }
                    });
                }
            }
        });
    }


    Snackbar mySnackbar;

    private void makeSnackBar(int duration, String s) {
        mySnackbar = Snackbar.make(screen, s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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