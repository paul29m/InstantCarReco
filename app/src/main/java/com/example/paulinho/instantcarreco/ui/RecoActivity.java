

package com.example.paulinho.instantcarreco.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paulinho.instantcarreco.R;
import com.example.paulinho.instantcarreco.model.Car;
import com.example.paulinho.instantcarreco.model.Owner;
import com.example.paulinho.instantcarreco.model.Recognition;
import com.example.paulinho.instantcarreco.utils.AppUtils;
import com.example.paulinho.instantcarreco.utils.Classifier;
import com.example.paulinho.instantcarreco.utils.TensorFlowImageClassifier;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by paulinho on 4/15/2018.
 */

public class RecoActivity extends AppCompatActivity {

    private static final String TAG = "RecoActivity";
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";
    private static final int REQUEST_CODE_GALLERY = 999;

    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnList, btnSignOut, btnChoose;
    private ImageView imageViewResult;
    private CameraView cameraView;

    DatabaseReference databaseCar;
    DatabaseReference databaseOwner;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reco);
        cameraView = (CameraView) findViewById(R.id.cameraView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        btnList = (Button) findViewById(R.id.btnList);
        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);
        btnChoose = (Button) findViewById(R.id.btnChoose); 

        firebaseAuth = FirebaseAuth.getInstance();
        checkIfLoggedIn();
        databaseCar = FirebaseDatabase.getInstance().getReference("cars");
        databaseCar.keepSynced(true);
        databaseOwner = FirebaseDatabase.getInstance().getReference("Owners");
        databaseOwner.keepSynced(true);

        cameraView.setCropOutput(true);
        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                recognizeImg(cameraKitImage.getBitmap());

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //TODO
            }
        });

        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               logOut();
            }
        });
        
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(
                        RecoActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });

    }

    private void recognizeImg(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        imageViewResult.setImageBitmap(bitmap);
        final List<Recognition> results = classifier.recognizeImage(bitmap);
        List<Car> resultCarList = convertToCarList(results,bitmap);
        addCarsToDB(resultCarList);
        textViewResult.setText(resultCarList.toString());
    }

    private void addCarsToDB(List<Car> resultCarList) {
        for(Car car:resultCarList){
            String carId = databaseCar.push().getKey();
            car.setId(carId);
            String Id = databaseOwner.push().getKey();
            databaseCar.child(carId).setValue(car);
            databaseOwner.child(Id).setValue(new Owner(Id,user.getUid(),carId));
        }
        Toast.makeText(this,"Car added",Toast.LENGTH_LONG).show();
    }


    private void logOut() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void checkIfLoggedIn() {
        try{
            user = firebaseAuth.getCurrentUser();
            if (user.getEmail() != null) {
                Toast.makeText(this,"Welcome "+user.getEmail(),Toast.LENGTH_LONG).show();
            }
            initTensorFlowAndLoadModel();
        }catch (NullPointerException e){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                recognizeImg(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Car> convertToCarList(List<Recognition> results, Bitmap bitmap) {
        List<Car> result =new ArrayList<>();
        String image = AppUtils.encodeToBase64(bitmap);
        for (Recognition car : results) {
            List<String> elements = new ArrayList<>(Arrays.asList(car.getTitle().split(" ")));
            Car foundCar = new Car(elements.get(0), elements.get(elements.size()-1), "No comments yet...",AppUtils.decodeModel(elements),image);
            result.add(foundCar);
        }
        return result;
    }
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }
}
