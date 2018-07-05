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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.wonderkiln.camerakit.CameraKit;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    private CarListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_list_activity);

        gridView = (GridView) findViewById(R.id.gridView);
        carList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        checkIfLoggedIn();
        databaseCar = FirebaseDatabase.getInstance().getReference("Cars");
        databaseCar.keepSynced(true);
        databaseOwner = FirebaseDatabase.getInstance().getReference("Owners");
        databaseOwner.keepSynced(true);

        updateCarList();

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                CharSequence[] items = {"Show details","Update and comment", "Delete"};
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
        if (!car.getComment().equals("No comments yet..."))
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
                            car.getDate(),
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
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);
        // set width for dialog
        int width = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.95);
        // set height for dialog
        int height = (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.8);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
        databaseCar.child(car.getId()).setValue(car);
        updateCarList();
    }

    private void updateCarList(){
        Query ownerQuery = databaseOwner.orderByChild("userId").equalTo(user.getUid());
        carList.clear();
        ownerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Owner owner = postSnapshot.getValue(Owner.class);
                        getCarFromDB(owner.getCarId());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCarFromDB(String carId) {
        Query carQuery = databaseCar.orderByKey().equalTo(carId);
        carQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Car car = postSnapshot.getValue(Car.class);
                    addCarToList(car);
                    adapter = new CarListAdapter(CarListActivity.this, R.layout.car_items, carList);
                    adapter.notifyDataSetChanged();
                    gridView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addCarToList(Car car) {
        for (Car car1 : carList)
            if (car1.getId().equals(car.getId()))
                return;
        carList.add(car);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_menu, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.date_order:
                orderByDate();
                break;

            case R.id.rating_order:
                orderByRating();
                break;

            case R.id.man_order:
                orderByManufacture();
                break;

            case R.id.year_order:
                orderByYear();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void orderByYear() {
        Collections.sort(carList,new Comparator<Car>() {
            @Override
            public int compare(Car car1, Car car2) {
                return car2.getYear().compareTo(car1.getYear());
            }
        });
        adapter = new CarListAdapter(CarListActivity.this, R.layout.car_items, carList);
        gridView.invalidateViews();
        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void orderByManufacture() {
        Collections.sort(carList,new Comparator<Car>() {
            @Override
            public int compare(Car car1, Car car2) {
                return car1.getManufacture().compareToIgnoreCase(car2.getManufacture());
            }
        });
        adapter = new CarListAdapter(CarListActivity.this, R.layout.car_items, carList);
        gridView.invalidateViews();
        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void orderByRating() {
        Collections.sort(carList,new Comparator<Car>() {
            @Override
            public int compare(Car car1, Car car2) {
                return Float.toString(car2.getRating()).compareTo(Float.toString(car1.getRating()));
            }
        });

        adapter = new CarListAdapter(CarListActivity.this, R.layout.car_items, carList);
        gridView.invalidateViews();
        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void orderByDate() {
        Collections.sort(carList,new Comparator<Car>() {
            @Override
            public int compare(Car car1, Car car2) {
                return car1.getDate().compareTo(car2.getDate());
            }});
        adapter = new CarListAdapter(CarListActivity.this, R.layout.car_items, carList);
        gridView.invalidateViews();
        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}