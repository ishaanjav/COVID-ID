package app.ij.covid_id.ui.map;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import app.ij.covid_id.R;

public class MapFragment extends Fragment {
    View root;
    RelativeLayout screen;
    private MapViewModel mapViewModel;
    Button update;

    public View findViewById(int id) {
        return root.findViewById(id);
    }
    FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =
                ViewModelProviders.of(this).get(MapViewModel.class);
        root = inflater.inflate(R.layout.fragment_map, container, false);
        update = (Button) findViewById(R.id.update);
        screen = (RelativeLayout) findViewById(R.id.screen);
        db = FirebaseFirestore.getInstance();

        return root;
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
                    Log.wtf("Package name", appPackageName);
                    makeToast("Connect to the internet first.");
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                } else {
                    db.collection("Update")
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Boolean update = Boolean.parseBoolean(document.get("Update").toString());
                                if (update) {
                                    final String appPackageName = getContext().getPackageName(); // getPackageName() from Context or Activity object
                                    Log.wtf("Package name", appPackageName);
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                } else {
                                    makeSnackBar(3000, "Sorry. No updates available yet.");
                                }
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

    public void makeToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }
}