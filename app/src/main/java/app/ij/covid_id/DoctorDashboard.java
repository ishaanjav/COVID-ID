package app.ij.covid_id;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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

public class DoctorDashboard extends AppCompatActivity {

    FirebaseFirestore db;
    RelativeLayout screen;
    double screenW;
    String updater;

    String firstDoctor = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        screen = findViewById(R.id.container);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_patients, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        overridePendingTransition(R.anim.medium_fade_in, R.anim.fast_fade_out);
        db = FirebaseFirestore.getInstance();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenW = metrics.widthPixels;
        //updater = readFromFile(getApplicationContext());
        //listenForUpdates();

        variable = 3;
        firstDoctor = readFirstDoctor(getApplicationContext());
        if (firstDoctor == null || firstDoctor.isEmpty() || firstDoctor.length() < 2) {
            //TODO This is the first time a doctor is signing in --> TourGuide and explanation of how to use app.

            //TODO Uncomment below when done testing TourGuide
            //writeToFirstDoctor("Doctor Finished");
        } else if (firstDoctor.contains("yes")) {
            //TODO This is the first time a doctor is signing in --> after creating an account.

            //TODO Uncomment below when done testing TourGuide
            //writeToFirstDoctor("Doctor Finished");
        }
    }

    private void writeToUpdate(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("update.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void listenForUpdates() {
        //showUpdate();
        makeSnackbar(5000, updater + " ");
        db.collection("Update").document("Update 1").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                Boolean update = Boolean.parseBoolean(document.get("Update").toString());
                if (update && updater.equals("yes")) {
                    long[] pattern = {0, 275, 25, 275, 25, 275, 25, 275, 25, 275, 25, 275, 25, 275, 25, 275, 25, 275, 25};
                    //vib.vibrate(3000);
                    if (vib.hasVibrator())
                        vib.vibrate(pattern, -1);
                    showUpdate();
                    writeToUpdate("no", getApplicationContext());
                } else if (update && updater.equals("yes")) {
                    writeToUpdate("no", getApplicationContext());
                    makeSnackbar(5500, "COVID-ID app updates are available! Check the Play Store.");
                    //makeSnackBar(2120, "Sorry. No updates available yet.");
                }
            }
        });
    }

    private void showUpdate() {
        final Dialog dialog = new Dialog(getApplicationContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.update_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .875);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        Button btn = (Button) dialog.findViewById(R.id.btn);
        ImageView image = (ImageView) dialog.findViewById(R.id.play);
        View.OnClickListener listen = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String appPackageName = getApplicationContext().getPackageName(); // getPackageName() from Context or Activity object
                Log.wtf("Package name", appPackageName);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        };
        btn.setOnClickListener(listen);
        image.setOnClickListener(listen);
        dialog.show();
    }

    private String readUsers(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("users.txt");

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

    Snackbar mySnackbar;

    private void makeSnackbar(int duration, String s) {
        mySnackbar = Snackbar.make(screen, s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        writeLogin("false", getApplicationContext());
        removeUncessaryFiles();
        startActivity(new Intent(DoctorDashboard.this, MainActivity.class));
    }

    public static int variable = 3;

    @Override
    public void onBackPressed() {
        variable--;
        String time = (variable == 1) ? " more time " : " more times ";
        if (variable == 0)
            signOut();
        else makeToast("Press back " + variable + time + "to sign out.");
    }

    Toast toast;

    private String readFirstDoctor(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("firstDoctor.txt");

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


    //README "firstDoctor.txt" is used to check whether this is the first time a doctor is signing in.
    private void writeToFirstDoctor(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("firstDoctor.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();

        } catch (IOException e) {
            //makeSnackBar(4000, "Could not load info. Try logging out and logging back in.");
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void makeToast(String s) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        toast.show();
    }
    @Override
    protected void onUserLeaveHint() {
        removeUncessaryFiles();
        super.onUserLeaveHint();
    }
    public boolean removeUncessaryFiles() {
        String directory = getApplicationContext().getApplicationInfo().dataDir + "/files";
        String readUsers = readUsers(getApplicationContext());
        List<String> users = Arrays.asList(readUsers.split("-------"));

        Log.wtf("*-* Users List:", "SIZE: " + users.size() + " " + users.toString());
        File location = new File(directory);
        File[] files = location.listFiles();
        ArrayList<String> locations = new ArrayList<>();
        //TODO See if below code works.
        int count = 0;
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.endsWith(".jpg")) {
                count ++;
                if (!users.contains(name.substring(0, name.length() - 4))) {
                    files[i].delete();
                    Log.wtf("*-* Deleting", users.contains(name.substring(0, name.length()-4)) + " " + name);
                    count --;
                }
                //locations.add(name);
            }
        }
        for (String s : locations) {
            File temp = new File(directory + "/" + s);
            Log.wtf("*-* REMOVING", s);
            temp.delete();
        }
        //TODO Remove Below 2 lines and basically all Log.wtf() that are not needed for deployment.
        files = location.listFiles();
        Log.wtf("*-* Files", "Path: " + directory + "  # of files: " + files.length + "   # of images: " + count);
        return true;
    }

    private void writeLogin(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("login.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}