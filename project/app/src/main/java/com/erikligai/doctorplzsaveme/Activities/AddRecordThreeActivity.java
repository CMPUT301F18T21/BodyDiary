package com.erikligai.doctorplzsaveme.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.erikligai.doctorplzsaveme.Models.RecordBuffer;
import com.erikligai.doctorplzsaveme.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AddRecordThreeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button backBtn3, nextBtn3;
    private ImageView addBtn,zoomImg;
    private int problem_index;
    private ArrayList<String> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record_three);

        Intent intent = getIntent();
        problem_index = intent.getIntExtra("Pos", 0);
        backBtn3 = findViewById(R.id.backButton3);
        nextBtn3 = findViewById(R.id.nextButton3);
        addBtn = findViewById(R.id.imageView12);
        zoomImg = findViewById(R.id.imageView13);
        backBtn3.setOnClickListener(this);
        nextBtn3.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        zoomImg.setOnClickListener(this);

        photos = RecordBuffer.getInstance().getRecord().getPhotos();

        //date = intent.getLongExtra("date",-1);
        // Get buttons
//        backBtn2 = findViewById(R.id.backButton2);
//        nextBtn2 = findViewById(R.id.nextButton2);
//        addPhotoBtn  =findViewById(R.id.addPhotoButton);
//        addBodylocationBtn = findViewById(R.id.addBodylocationButton);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backButton3:
                openAddRecordTwoActivity();
                break;
            case R.id.nextButton3:
                openAddRecordFourActivity();
                break;
            case R.id.imageView1:
                zoomImage(0);
                break;
            case R.id.imageView2:
                zoomImage(1);
                break;
            case R.id.imageView3:
                zoomImage(2);
                break;
            case R.id.imageView4:
                zoomImage(3);
                break;
            case R.id.imageView5:
                zoomImage(4);
                break;
            case R.id.imageView6:
                zoomImage(5);
                break;
            case R.id.imageView7:
                zoomImage(6);
                break;
            case R.id.imageView8:
                zoomImage(7);
                break;
            case R.id.imageView9:
                zoomImage(8);
                break;
            case R.id.imageView10:
                zoomImage(9);
                break;
            case R.id.imageView11:
                zoomImage(10);
                break;

            case R.id.imageView12:
                Log.d("size", Integer.toString(photos.size()));
                if (photos.size()<11) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(getApplicationContext(), "Only a maximum of 11 photos per Record!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.imageView13:
                zoomImg.setVisibility(View.GONE);
                break;
        }
    }

    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        displayPhotos();
    }

    static final int ACTION_IMAGE_CAPTURE = 1;
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;;

    private void dispatchTakePictureIntent() {

        // finish current display before calling camera.
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
        else{
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, ACTION_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, ACTION_IMAGE_CAPTURE);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Toast.makeText(getApplicationContext(), "Camera denied", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Bitmap photo;
        if (requestCode == ACTION_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            String photoStr = getStringFromBitmap(photo);
            photos.add(photoStr);
            Log.i("photo", photoStr);
            Toast.makeText(getApplicationContext(), "Photo added!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
        /*
         * This functions converts Bitmap picture to a string which can be
         * JSONified.
         * */
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    public Bitmap StringToBitMap(String encodedString) {
        byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        return bitmap;
    }

    private void openAddRecordTwoActivity() {
        RecordBuffer.getInstance().getRecord().setPhotos(photos);
        Intent intent = new Intent(this, AddRecordTwoActivity.class);
        intent.putExtra("Pos", problem_index);
        finish();
        startActivity(intent);
    }

    private void openAddRecordFourActivity() {
        RecordBuffer.getInstance().getRecord().setPhotos(photos);
        Intent intent = new Intent(this, AddRecordFourActivity.class);
        intent.putExtra("Pos", problem_index);
        finish();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        openAddRecordTwoActivity();
    }

    public void displayPhotos() {
        for (int i = 0; i < photos.size(); i++) {
            ImageView imageView = setImageView(i);
            Bitmap bitmap = StringToBitMap(photos.get(i));
            imageView.setImageBitmap(bitmap);
        }

    }

    public ImageView setImageView(int index) {

        ImageView imageView;
        switch (index) {
            case 0:
                imageView = findViewById(R.id.imageView1);
                break;
            case 1:
                imageView = findViewById(R.id.imageView2);
                break;
            case 2:
                imageView = findViewById(R.id.imageView3);
                break;
            case 3:
                imageView = findViewById(R.id.imageView4);
                break;
            case 4:
                imageView = findViewById(R.id.imageView5);
                break;
            case 5:
                imageView = findViewById(R.id.imageView6);
                break;
            case 6:
                imageView = findViewById(R.id.imageView7);
                break;
            case 7:
                imageView = findViewById(R.id.imageView8);
                break;
            case 8:
                imageView = findViewById(R.id.imageView9);
                break;
            case 9:
                imageView = findViewById(R.id.imageView10);
                break;
            case 10:
                imageView = findViewById(R.id.imageView11);
                break;
            default:
                imageView = null;
                break;
        }
        imageView.setOnClickListener(this);
        imageView.setClickable(true);
        return imageView;
    }

    public void zoomImage(int index){
        Bitmap bitmap = StringToBitMap(photos.get(index));
        zoomImg.setImageBitmap(bitmap);
        zoomImg.setVisibility(View.VISIBLE);
    }

}

