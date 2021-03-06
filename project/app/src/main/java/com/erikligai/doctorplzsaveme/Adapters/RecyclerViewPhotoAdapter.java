package com.erikligai.doctorplzsaveme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.erikligai.doctorplzsaveme.Activities.UploadBodyLocationActivity;
import com.erikligai.doctorplzsaveme.Models.Patient;
import com.erikligai.doctorplzsaveme.R;
import com.erikligai.doctorplzsaveme.backend.Backend;

import java.util.ArrayList;

public class RecyclerViewPhotoAdapter extends RecyclerView.Adapter<RecyclerViewPhotoAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewPhotoAdapter";

    private Patient patient;
    private ArrayList<String> photoIds;
    private ArrayList<String> photos;
    private ArrayList<String> photoLables;
    private Context mContext;

    public RecyclerViewPhotoAdapter(Context mContext) {
        this.patient = Backend.getInstance().getPatientProfile();
        this.photos = patient.getPhotos();
        this.photoIds = patient.getPhotoIds();
        this.photoLables = patient.getPhotoLabels();
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Log.d(TAG, "onBindViewHolder called");
        viewHolder.imgViewIcon.setScaleType((ImageView.ScaleType.CENTER_CROP));
        if(i == 0){
            viewHolder.imgViewIcon.setImageResource(R.drawable.front);
        } else if (i==1){
            viewHolder.imgViewIcon.setImageResource(R.drawable.back);
        } else {
            viewHolder.imgViewIcon.setImageBitmap(getBitmapFromString(photos.get(i)));
        }
        viewHolder.label.setText(photoLables.get(i));
        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();
                final EditText input = new EditText(v.getContext());
                alertDialog.setView(input);
                alertDialog.setTitle(photoLables.get(i));
                alertDialog.setMessage("Would you like to delete the photo you clicked?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Add Label",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //photoLables.add(i,input.getText().toString());
                                Log.d("PhotoLabel",input.getText().toString());
                                Backend.getInstance().getPatientProfile().addPhotoLabel(i,input.getText().toString());
                                Backend.getInstance().UpdatePatientRunnable();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "DELETE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (i <= 1){
                                    Toast.makeText(mContext, "Default photo cannot be deleted!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                                else{
                                    Toast.makeText(mContext, "photo: "+ photoIds.get(i)+" removed!", Toast.LENGTH_SHORT).show();
                                    photos.remove(i);
                                    photoIds.remove(i);
                                    photoLables.remove(i);
                                    Intent intent = new Intent(v.getContext(), UploadBodyLocationActivity.class);
                                    v.getContext().startActivity(intent);
                                    ((Activity)v.getContext()).finish();
                                    Backend.getInstance().UpdatePatientRunnable();
                                    dialog.dismiss();
                                }

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgViewIcon;
        public TextView label;

        ConstraintLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.label);
            imgViewIcon = itemView.findViewById(R.id.photo);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    private Bitmap getBitmapFromString(String stringPicture) {
        /*
         * This Function converts the String back to Bitmap
         * */
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }
}
