

package com.example.paulinho.instantcarreco.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.wonderkiln.camerakit.CameraKit;
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
    private static boolean SAVE = true;
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
    private Bitmap carBitmap;

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
        databaseCar = FirebaseDatabase.getInstance().getReference("Cars");
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
                carBitmap = cameraKitImage.getBitmap();
                recognizeImg();

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecoActivity.this, CarListActivity.class));
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
               showDialogDelete();
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

                carBitmap= BitmapFactory.decodeStream(inputStream);
                recognizeImg();

            } catch (FileNotFoundException e) {
                Log.w(TAG, "Choose image failed", e);
            }
        }
    }

    private void recognizeImg() {
        imageViewResult.setImageBitmap(carBitmap);
        Bitmap bitmap = Bitmap.createScaledBitmap(carBitmap, INPUT_SIZE, INPUT_SIZE, false);
        final List<Recognition> results = classifier.recognizeImage(bitmap);
        int nh = (int) ( carBitmap.getHeight() * (512.0 / carBitmap.getWidth()) );
        carBitmap = Bitmap.createScaledBitmap(carBitmap, 512, nh, true);
        List<Car> resultCarList = convertToCarList(results,carBitmap);
        textViewResult.setText(showResult(resultCarList));
        addCarsToDB(resultCarList);
    }

    private String showResult(List<Car> resultCarList) {
        StringBuilder result = new StringBuilder("Best match: \n");
        for (Car car : resultCarList) result.append(car.toString()+"\n");
        return result.toString();
    }

    private void addCarsToDB(List<Car> resultCarList) {
        if (SAVE) {
            for (Car car : resultCarList) {
                String carId = databaseCar.push().getKey();
                car.setId(carId);
                String Id = databaseOwner.push().getKey();
                databaseCar.child(carId).setValue(car);
                databaseOwner.child(Id).setValue(new Owner(Id, user.getUid(), carId));
            }
            Toast.makeText(this, "Result added to your list", Toast.LENGTH_LONG).show();
        }
        else {
            String carId = databaseCar.push().getKey();
            Car car = resultCarList.get(0);
            car.setId(carId);
            String Id = databaseOwner.push().getKey();
            databaseCar.child(carId).setValue(car);
            databaseOwner.child(Id).setValue(new Owner(Id, user.getUid(), carId));
            Toast.makeText(this, "Best confidence car added to your list", Toast.LENGTH_LONG).show();
        }
    }


    private void checkIfLoggedIn() {
        try{
            user = firebaseAuth.getCurrentUser();
            if (user.getEmail() != null && checkParent()) {
                Toast.makeText(this,"Welcome "+user.getEmail(),Toast.LENGTH_LONG).show();
            }
            initTensorFlowAndLoadModel();
        }catch (NullPointerException e){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void showDialogDelete(){
        final AlertDialog.Builder dialogDelete = new AlertDialog.Builder(RecoActivity.this);
        dialogDelete.setTitle("Warning:");
        dialogDelete.setMessage("If no internet connection is available you may not be able to use the app! Are you sure you want to logout?");
        dialogDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logOut();
            }
        });
        dialogDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogDelete.show();
    }

    private void logOut() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    private boolean checkParent() {
        try {
            Intent intent = this.getIntent();
            String parentActivity = (String) intent.getExtras().get("PARENT");
            return parentActivity.equals("parent");
        }catch (NullPointerException e ){
            return false;
        }
    }

    public static List<Car> convertToCarList(List<Recognition> results, Bitmap bitmap) {
        List<Car> result =new ArrayList<>();
        String image = AppUtils.encodeToBase64(bitmap);
        for (Recognition car : results) {
            List<String> elements = new ArrayList<>(Arrays.asList(car.getTitle().split(" ")));
            Car foundCar = new Car(elements.get(0), elements.get(elements.size()-1),AppUtils.decodeModel(elements), car.getConfidence(), image);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(item.isChecked())
            item.setChecked(false);
        else
            item.setChecked(true);

        switch (id){
            case R.id.checkable_flash:
                if (item.isChecked())
                    cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                else
                    cameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                break;

            case R.id.checkable_save:
                if (item.isChecked())
                    SAVE = true;
                else
                    SAVE = false;
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
