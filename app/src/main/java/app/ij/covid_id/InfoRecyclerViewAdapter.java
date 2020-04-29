package app.ij.covid_id;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoRecyclerViewAdapter extends RecyclerView.Adapter<InfoRecyclerViewAdapter.ViewHolder> {

    Context context;
    ArrayList<HashMap<String, Object>> list;
    int mExpandedPosition;
    View details;
    RecyclerView recyclerView;
    public String TAG = "RecyclerViewAdapter";

    public InfoRecyclerViewAdapter(Context context, ArrayList<HashMap<String, Object>> list, RecyclerView recyclerView) {
        this.context = context;
        this.list = list;
        this.recyclerView = recyclerView;
    }

    View view;

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        RelativeLayout screen;
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            screen = itemView.findViewById(R.id.screen);
            mExpandedPosition = -1;
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        details = LayoutInflater.from(context).inflate(R.layout.extended_list_item, parent, false);

        return holder;
    }

    int previousExpandedPosition;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //Log.wtf(TAG, "onBindViewHolder --  called");

        HashMap<String, Object> map = list.get(position);
        //Log.wtf(TAG, "onBindViewHolder List - " + list.toString() + " \n\t\t\\t\t\t\t\\t\tt\t\t\t\t\t" + map.toString());

        holder.text.setText("TEST: " + map.get("Number").toString());

        //TODO See if there is a way to load all the text stuff first and then load images once it is retrieved.
        //INFO Right now we have to load all the stuff after images are retrieved and it takes a while for
        // 3 images so search alternatives.
        // 3 images = 1457
        holder.image.setImageBitmap(loadImageBitmap(context, map.get("Username").toString(), "jpg"));


        final boolean isExpanded = position == mExpandedPosition;
        details.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        if (isExpanded)
            previousExpandedPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1 : position;
                //Approach 1
                TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();

                //Approach 2

            }
        });
    }

    public Bitmap loadImageBitmap(Context context, String name, String extension) {
        name = name + "." + extension;
        FileInputStream fileInputStream;
        Bitmap bitmap = null;
        try {
            fileInputStream = context.openFileInput(name);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void makeSnackBar(int duration, String s) {
        Snackbar mySnackbar = Snackbar.make(view.findViewById(R.id.screen), s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
    }

    public void makeToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    public void shortToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
