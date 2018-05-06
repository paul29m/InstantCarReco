package com.example.paulinho.instantcarreco.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paulinho.instantcarreco.R;
import com.example.paulinho.instantcarreco.model.Car;
import com.example.paulinho.instantcarreco.model.Owner;
import com.example.paulinho.instantcarreco.utils.AppUtils;
import com.example.paulinho.instantcarreco.utils.CarListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by paulinho on 11/24/2017.
 */

public class CarListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    DatabaseReference databaseCar;
    DatabaseReference databaseOwner;
    private FirebaseUser user;
    private ImageView imageViewCar;
    protected GridView gridView;
    private ArrayList<Car> carList;
    private List<String> carId;
    private CarListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_list_activity);

        gridView = (GridView) findViewById(R.id.gridView);
        carList = new ArrayList<>();


        mAuth = FirebaseAuth.getInstance();
        checkIfLoggedIn();
        databaseCar = FirebaseDatabase.getInstance().getReference("cars");
        databaseCar.keepSynced(true);
        databaseOwner = FirebaseDatabase.getInstance().getReference("Owners");
        databaseOwner.keepSynced(true);
        carId = new ArrayList<>();
        carList = new ArrayList<>();
        updateCarList();

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                CharSequence[] items = {"Show details","Update", "Delete"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(CarListActivity.this);

                dialog.setTitle("Choose an action");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        Car car = carList.get(position);
                        if (item == 0) {
                            showDialogDetails(CarListActivity.this, car);
                        }
                        else
                            if (item == 1) {
                            showDialogUpdate(CarListActivity.this, car);

                        } else
                            {
                                showDialogDelete(car);
                        }
                    }
                });
                dialog.show();
                return true;
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDialogDetails(CarListActivity.this, carList.get(i));
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 888){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 888);
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

        if(requestCode == 888 && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageViewCar.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showDialogUpdate(Activity activity, final Car car){

        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.update_car_activity);
        dialog.setTitle("Update");
        imageViewCar = (ImageView) dialog.findViewById(R.id.imageViewCar);
        imageViewCar.setImageBitmap(AppUtils.decodeBase64(car.getImage()));
        final EditText edtMan = (EditText) dialog.findViewById(R.id.edtManufacture);
        edtMan.setText(car.getManufacture());
        final EditText edtYear = (EditText) dialog.findViewById(R.id.edtYear);
        edtYear.setText(car.getYear());
        final EditText edtCom = (EditText) dialog.findViewById(R.id.edtCom);
        edtCom.setText(car.getComment());
        final EditText edtModel = (EditText) dialog.findViewById(R.id.edtModel);
        edtModel.setText(car.getModel());
        Button btnUpdate = (Button) dialog.findViewById(R.id.btnUpdate);
        Button btnBack = (Button) dialog.findViewById(R.id.btnBack);
        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.9);
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        imageViewCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // request photo library
                ActivityCompat.requestPermissions(
                        CarListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        888
                );
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Car updatedCar = new Car(
                            car.getId(),
                            edtMan.getText().toString().trim(),
                            edtYear.getText().toString().trim(),
                            edtModel.getText().toString().trim(),
                            edtCom.getText().toString().trim(),
                            car.getConfidence(),
                            car.getRating(),
                            AppUtils.encodeToBase64(((BitmapDrawable)imageViewCar.getDrawable()).getBitmap()));
                    databaseCar.child(car.getId()).setValue(updatedCar);
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Update successfully", Toast.LENGTH_SHORT).show();
                }
                catch (Exception error) {
                    Log.e("Update error", error.getMessage());
                    Toast.makeText(getApplicationContext(), "Please fill the spaces with good data", Toast.LENGTH_SHORT).show();
                }
                updateCarList();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    private void showDialogDetails(Activity activity , final Car car){final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.show_car_activity);
        dialog.setTitle("Details");
        imageViewCar = (ImageView) dialog.findViewById(R.id.imageViewCar);
        imageViewCar.setImageBitmap(AppUtils.decodeBase64(car.getImage()));
        final TextView textMan = (TextView) dialog.findViewById(R.id.edtManufacture);
        textMan.setText(car.toString());
        final TextView textCom = (TextView) dialog.findViewById(R.id.edtCom);
        textCom.setText("Comment: "+car.getComment());
        final RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.ratingBar);
        ratingBar.setRating(car.getRating());
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                ratingBar.setRating(rating);
                car.setRating(rating);
                updateRating(car);
            }
        });
        Button btnShare = (Button) dialog.findViewById(R.id.btnShare);
        // set width for dialog
        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.95);
        // set height for dialog
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.8);
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(car);
            }
        });
    }

    private void showDialogDelete(final Car car){
        final AlertDialog.Builder dialogDelete = new AlertDialog.Builder(CarListActivity.this);

        dialogDelete.setTitle("Warning:");
        dialogDelete.setMessage("Are you sure you want to delete this car?");
        dialogDelete.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeCar(car.getId());
                updateCarList();
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

    private void removeCar(String id) {
        databaseCar.child(id).removeValue();
        Query query =databaseOwner.orderByChild("carId").equalTo(id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot owner : dataSnapshot.getChildren()) {
                        String id = owner.getKey();
                        databaseOwner.child(id).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateRating(Car car) {
        Car updatedCar = new Car(
                car.getId(),
                car.getManufacture(),
                car.getYear(),
                car.getModel(),
                car.getComment(),
                car.getConfidence(),
                car.getRating(),
                AppUtils.encodeToBase64(((BitmapDrawable)imageViewCar.getDrawable()).getBitmap()));
        databaseCar.child(car.getId()).setValue(updatedCar);
        updateCarList();
    }

    private void updateCarList(){
        databaseOwner.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                Owner owner = postSnapshot.getValue(Owner.class);
                if(user.getUid().equals(owner.getUserId()))
                    carId.add(owner.getCarId());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        databaseCar.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                carList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Car car = postSnapshot.getValue(Car.class);
                    if (carId.contains(car.getId())) {
                        carList.add(car);
                    }
                    adapter = new CarListAdapter(CarListActivity.this, R.layout.car_items, carList);
                    gridView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkIfLoggedIn() {
        try{
            user = mAuth.getCurrentUser();
            if (user.getEmail() != null);

        }catch (NullPointerException e){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void sendEmail(Car car){

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Found this awesome car"+car.getManufacture());
        emailIntent.putExtra(Intent.EXTRA_TEXT, "I found it on Wanted Cars app:\n "+car.toString());
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(CarListActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}