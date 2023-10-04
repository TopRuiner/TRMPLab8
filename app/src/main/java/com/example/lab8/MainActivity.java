package com.example.lab8;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;
import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {

    SurfaceView sv;
    SurfaceHolder holder;
    HolderCallback holderCallback;
    static Camera camera;

    Button mButton;

    Button mButton2;
    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = true;
    String currentPhotoPath;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int PERMISSIONS_REQUEST_CAPTURE_IMAGE = 1;
    public static final int RequestPermissionCode = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);


        setContentView(R.layout.activity_main);
        sv = (SurfaceView) findViewById(R.id.surfaceView);
        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);

        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                int degrees = 0;
                switch (rotation) {
                    case Surface.ROTATION_0: degrees = 0; break; //Natural orientation
                    case Surface.ROTATION_90: degrees = 90; break; //Landscape left
                    case Surface.ROTATION_180: degrees = 180; break;//Upside down
                    case Surface.ROTATION_270: degrees = 270; break;//Landscape right
                }
                int rotate = (info.orientation - degrees + 360) % 360;

//STEP #2: Set the 'rotation' parameter
                Camera.Parameters params = camera.getParameters();
                params.setRotation(rotate);
                camera.setParameters(params);
                camera.takePicture(null, null, mPicture);
//
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent,100);
////                startActivityForResult(intent,100);
////                dispatchTakePictureIntent();
            }
        });
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                camera.stopPreview();
                camera.startPreview();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static void checkCameraPermissions(Context context){
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Permission is not granted
            Log.d("checkCameraPermissions", "No Camera Permissions");
            ActivityCompat.requestPermissions((Activity) context,
                    new String[] { android.Manifest.permission.CAMERA },
                    100);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Camera open");
            camera = Camera.open(CAMERA_ID);

        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(parameters);


            holderCallback.setPreviewSize(FULL_SCREEN);

//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
        }
        camera = null;
    }

    public class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(holder);

                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(CAMERA_ID);
            try {

                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }

        public void setPreviewSize(boolean fullscreen) {
            Display display = getWindowManager().getDefaultDisplay();
            boolean widthIsMax = display.getWidth() > display.getHeight();

            Camera.Size size = camera.getParameters().getPreviewSize();

            RectF rectDisplay = new RectF();
            RectF rectPreview = new RectF();

            rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

            if (widthIsMax) {
                rectPreview.set(0, 0, size.width, size.height);
//                rectPreview.set(0, 0, display.getWidth(), display.getHeight());
            }
            else {
                rectPreview.set(0,0,size.height, size.width);
//                rectPreview.set(0,0,display.getHeight(), display.getWidth());
            }
            Matrix matrix = new Matrix();

            if (!fullscreen) {
                matrix.setRectToRect(rectPreview, rectDisplay, Matrix.ScaleToFit.START);
            } else {
                matrix.setRectToRect(rectDisplay, rectPreview, Matrix.ScaleToFit.START);
                matrix.invert(matrix);
            }
            matrix.mapRect(rectPreview);
            sv.getLayoutParams().height = (int) (rectPreview.bottom);
            sv.getLayoutParams().width = (int) (rectPreview.right);
        }

        public void setCameraDisplayOrientation(int cameraId) {
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }
            int result = 0;
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                result = ((360 - degrees) + info.orientation);

            } else
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    result = ((360 - degrees) - info.orientation);
                    result += 360;
                }
                result = result % 360;
                camera.setDisplayOrientation(result);

        }

    }

}
