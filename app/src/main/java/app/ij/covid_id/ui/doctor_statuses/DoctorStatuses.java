package app.ij.covid_id.ui.doctor_statuses;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import app.ij.covid_id.InfoRecyclerViewAdapter;
import app.ij.covid_id.R;

public class DoctorStatuses extends Fragment {

    FirebaseFirestore db;
    ScrollView screen;
    View root;
    public String documentID, username, name, userPassID, type, password, accountCreated, phone, email, status;
    String statusLastUpdated;
    String doctorsPath;
    RecyclerView patientRecycler;
    Button update;
    TextView message;
    public String TAG = "DoctorStatuses";

    public View findViewById(int id) {
        return root.findViewById(id);
    }

    String directory;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        root = inflater.inflate(R.layout.fragment_doctor_statuses, container, false);
        screen = root.findViewById(R.id.screen);
        patientRecycler = root.findViewById(R.id.patientRecycler);
        doctorsPath = "Doctor";

        directory = getContext().getApplicationInfo().dataDir + "/files";

        /*PackageManager m = getContext().getPackageManager();
        String s = getContext().getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            directory = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("yourtag", "Error Package name not found ", e);
        }*/

        readStorage();
        return root;
    }

    ArrayList<HashMap<String, Object>> patientInfo;
    InfoRecyclerViewAdapter adapter;
    long startTime;
    ArrayList<String> patientUsernames;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        patientUsernames = new ArrayList<>();
        patientInfo = new ArrayList<>();
        HashMap<String, Object> temp = new HashMap<>();
        temp.put("Number", 7);
        temp.put("Username", "patient1");
        patientInfo.add(temp);
        HashMap<String, Object> temp2 = new HashMap<>();
        temp2.put("Number", 8);
        temp2.put("Username", "patient2");
        patientInfo.add(temp2);
        HashMap<String, Object> temp3 = new HashMap<>();
        temp3.put("Number", 9);
        temp3.put("Username", "patient3");
        patientInfo.add(temp3);
        Log.wtf(TAG, "LIST: " + patientInfo.toString());
        startTime = System.currentTimeMillis();
        patientUsernames.add("patient1");
        patientUsernames.add("patient2");
        patientUsernames.add("patient3");
        Log.wtf("-_--START", "" + startTime);

        getfile();
        writeImages();

        updateLayout();
        if (!isNetworkAvailable()) {
            makeSnackBar(6000, "You are not connected to the internet. Therefore, you will not receive updates unless you connect.");
        } else {
        }

        updateInfoTxt();
    }

    private ArrayList<String> fileList = new ArrayList<>();


    public void getfile() {
        Log.wtf("**IMAGE Files", "Path: " + directory);
        File location = new File(directory);
        File[] files = location.listFiles();
        Log.wtf("**Files", "Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.endsWith(".jpg")) {
                Log.wtf("**Files", "FileName:" + name);
                fileList.add(name.substring(0, name.length() - 4));
                //    files[i].delete();
            }
        }
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

    public void writeImages() {
        final boolean[] set = {false};
        for (int i = 0; i < patientUsernames.size(); i++) {
            final String username = patientUsernames.get(i);
            final StorageReference mImageRef = FirebaseStorage.getInstance().getReference("Patient/" + username + ".jpg");
            final long ONE_MEGABYTE = 3000 * 3000;
            boolean entered = false;
            if (!fileList.contains(username)) {
                //TODO Take a look into getBytes and whether it can be used to get smaller images.
                final int finalI = i;
                entered = i == patientUsernames.size() - 1;
                mImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        saveImage(getContext(), bm, username, "jpg");
                        Log.wtf("**SAVED IMAGE", "IMAGE " + username + " SAVED   " + fileList.toString());
                        if (finalI == patientUsernames.size() - 1) {
                            adapter = new InfoRecyclerViewAdapter(getContext(), patientInfo, patientRecycler);
                            patientRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                            patientRecycler.setAdapter(adapter);
                            long end = System.currentTimeMillis();
                            Log.wtf("-_--END", "" + end);
                            set[0] = true;
                            Log.wtf("-_--Image Retrieval Time 1", "" + (end - startTime));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getContext(), "FAILED", Toast.LENGTH_LONG).show();
                        Log.wtf("**FAILED 2 SAVE IMAGE", exception.toString());
                    }
                });
            }
            /*adapter = new InfoRecyclerViewAdapter(getContext(), patientInfo, patientRecycler);
            adapter.notifyDataSetChanged();
            patientRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            patientRecycler.setAdapter(adapter);*/
            if (!entered && !set[0] && i == patientUsernames.size() - 1) {
                adapter = new InfoRecyclerViewAdapter(getContext(), patientInfo, patientRecycler);
                patientRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                patientRecycler.setAdapter(adapter);
                long end = System.currentTimeMillis();
                Log.wtf("-_--END", "" + end);
                Log.wtf("-_--Image Retrieval Time 2", "" + (end - startTime));
                set[0] = true;
            }
        }

    }

    public void saveImage(Context context, Bitmap bitmap, String name, String extension) {
        name = name + "." + extension;
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);
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
                        //updateLayout();
                        if (!status.equals(snapshot.getString("Status"))) {
                            //TODO Status changed --> Consider making a notification. Do vibrations at very least.
                        }
                        //textView.setText(snapshot.getData().get("Status").toString());
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userPassListener.remove();
    }

    protected boolean isSafe() {
        return !(this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }

    private void writeNewInfo(Map<String, Object> data) {
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
        toWrite += data.get("Pass");
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