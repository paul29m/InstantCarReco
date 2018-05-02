

package com.example.paulinho.instantcarreco.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paulinho.instantcarreco.R;
import com.example.paulinho.instantcarreco.model.Car;
import com.example.paulinho.instantcarreco.model.Recognition;
import com.example.paulinho.instantcarreco.utils.Classifier;
import com.example.paulinho.instantcarreco.utils.TensorFlowImageClassifier;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by paulinho on 4/15/2018.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject, btnToggleCamera, btnSignOut;
    private ImageView imageViewResult;
    private CameraView cameraView;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        cameraView = (CameraView) findViewById(R.id.cameraView);
        imageViewResult = (ImageView) findViewById(R.id.imageViewResult);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewResult.setMovementMethod(new ScrollingMovementMethod());

        btnToggleCamera = (Button) findViewById(R.id.btnToggleCamera);
        btnDetectObject = (Button) findViewById(R.id.btnDetectObject);
        btnSignOut = (Button) findViewById(R.id.btnSignOut);

        firebaseAuth = FirebaseAuth.getInstance();
        checkIfLoggedIn();

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                imageViewResult.setImageBitmap(bitmap);
                final List<Recognition> results = classifier.recognizeImage(bitmap);
                List<Car> resultCarList = convertToCarList(results,bitmap);
                textViewResult.setText(resultCarList.toString());

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnToggleCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.toggleFacing();
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

    }

    private List<Car> convertToCarList(List<Recognition> results, Bitmap bitmap) {
        Log.i(TAG,"Converting the result");
        List<Car> result =new ArrayList<>();
        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray = byteBuffer.array();
        for (Recognition car : results) {
            List<String> elements = new ArrayList<>(Arrays.asList(car.getTitle().split(" ")));
            StringBuilder model = new StringBuilder();
            for(int i=1;i<elements.size()-1;i++) {
                model.append(elements.get(i)).append(" ");
            }
            Car foundCar = new Car(elements.get(0), elements.get(elements.size()-1), "", model.toString(), byteArray);
            result.add(foundCar);
        }
        return result;
    }

    private void logOut() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void checkIfLoggedIn() {
        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        else{
            user = firebaseAuth.getCurrentUser();
            if (user.getEmail() != null) {
                Toast.makeText(this,"Welcome "+user.getEmail(),Toast.LENGTH_LONG).show();
            }
            initTensorFlowAndLoadModel();
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
