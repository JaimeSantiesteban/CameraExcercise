package com.mac.training.cameraexcercise;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;
    private static final int REQUEST_ID_IMAGE_CAPTURE = 100;
    private static final int REQUEST_ID_VIDEO_CAPTURE = 101;

    private VideoView videoView;
    private ImageView imageView;

    private Uri imageUri;
    private Uri videoUri;

    /*Lifecycle methods*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.videoView = (VideoView) this.findViewById(R.id.videoView);
        this.imageView = (ImageView) this.findViewById(R.id.imageView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_READ_WRITE_PERMISSION: {
                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (read/write).
                if (grantResults.length > 1 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_LONG).show();
                    this.captureImage();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    // When results returned
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ID_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Image saved to:\n" + imageUri, Toast.LENGTH_LONG).show();
                try {
                    Bitmap bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    this.imageView.setImageBitmap(bp);
                } catch (IOException ioe) {
                    Toast.makeText(this, "There was an error: " + ioe.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.action_canceled, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.smth_went_wrong, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_ID_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video saved to:\n" + videoUri, Toast.LENGTH_LONG).show();
                this.videoView.setVideoURI(videoUri);
                this.videoView.start();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.action_canceled, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.smth_went_wrong, Toast.LENGTH_LONG).show();
            }
        }

    }

    /*Listeners*/
    public void onTakeAPic(View view) {
        askPermissions();
        captureImage();
    }

    public void onRecordVideo(View view) {
        askPermissions();
        captureVideo();
    }

    /*Camera actions*/
    private void captureImage() {
        // Create an implicit intent, for image capture.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = saveFileProcess(intent, "/jamesVideo.jpg");
        // Start camera and wait for the results.
        this.startActivityForResult(intent, REQUEST_ID_IMAGE_CAPTURE);
    }

    private void captureVideo() {
        // Implicit intent for video capture
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoUri = saveFileProcess(intent, "/jamesVideo.mp4");
        // start camera and wait for the results
        this.startActivityForResult(intent, REQUEST_ID_VIDEO_CAPTURE);
    }

    /*Permissions*/
    private void askPermissions() {
        // With Android Level >= 23, you have to ask the user
        // for permission to read/write data on the device.
        if (android.os.Build.VERSION.SDK_INT >= 23) {

            // Check if we have read/write permission
            int readPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (writePermission != PackageManager.PERMISSION_GRANTED ||
                    readPermission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_ID_READ_WRITE_PERMISSION
                );
                return;
            }
        }
    }

    /*Utility methods*/

    private Uri saveFileProcess(Intent intent, String fileName) {
        // External storage
        File dir = Environment.getExternalStorageDirectory();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // file:///storage/emulated/0/jamesVideo.mp4
        String savePath = dir.getAbsolutePath() + fileName;
        File videoFile = new File(savePath);
        Uri fileUri = Uri.fromFile(videoFile);

        // Specify where to save video files.
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        return fileUri;
    }
}
