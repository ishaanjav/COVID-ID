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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import app.ij.covid_id.ui.dashboard.DashboardFragment;
import app.ij.covid_id.ui.doctor_dashboard.DoctorDashboardFragment;
import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses3;
import app.ij.covid_id.ui.map.MapFragment;
import app.ij.covid_id.ui.settings.SettingsFragment;

public class PatientDashboard extends AppCompatActivity {
    FirebaseFirestore db;
    RelativeLayout screen;
    double screenW;
    String updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        overridePendingTransition(R.anim.medium_fade_in, R.anim.fast_fade_out);
        variable = 3;
    }
    @Override
    protected void onUserLeaveHint() {
        removeListeners();
        super.onUserLeaveHint();
    }
    public void removeListeners() {
        Log.wtf("INFO", "Patient Dashboard: Removing Listeners");
        if(DoctorDashboardFragment.listener != null)
            DoctorDashboardFragment.listener.remove();
        if(DoctorDashboardFragment.listener2 != null)
            DoctorDashboardFragment.listener2.remove();
        if(DashboardFragment.listener != null)
            DashboardFragment.listener.remove();
        if(DashboardFragment.listener2 != null)
            DashboardFragment.listener2.remove();
        if(DoctorStatuses3.userPassListener != null)
            DoctorStatuses3.userPassListener.remove();
        if(MapFragment.listener != null)
            MapFragment.listener.remove();
        if(SettingsFragment.listener != null)
            SettingsFragment.listener.remove();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard_menu, menu);

        return super.onCreateOptionsMenu(menu);
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

    private void makeToast(String s) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        toast.show();
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
        removeListeners();
        writeLogin("false", getApplicationContext());
        startActivity(new Intent(PatientDashboard.this, MainActivity.class));
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