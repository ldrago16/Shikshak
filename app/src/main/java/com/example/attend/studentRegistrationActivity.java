package com.example.attend;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class studentRegistrationActivity extends AppCompatActivity {
    Spinner registrationStatusSpinner, courseSpinner, termSpinner, batchSpinner;
    EditText registrationPasswordEditText;
    Button submitStudentRegistrationButton;

    String registrationStatusSpinnerSelected, courseSpinnerSelected, termSpinnerSelected, batchSpinnerSelected;
    String registrationYear;

    ArrayList<String> registrationStatusSpinnerItems = new ArrayList<>();
    ArrayList<String> courseSpinnerItems = new ArrayList<>();
    ArrayList<String> termSpinnerItems = new ArrayList<>();
    ArrayList<String> batchSpinnerItems = new ArrayList<>();

    HashMap<String, ArrayList<String>> allCourseAndTermHashMap;

    studentRegistrationActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        context = studentRegistrationActivity.this;

        registrationStatusSpinner = findViewById(R.id.registrationOpenCloseSpinner);
        courseSpinner = findViewById(R.id.courseSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        batchSpinner = findViewById(R.id.batchSpinner);
        registrationPasswordEditText = findViewById(R.id.setRegistrationPasswordEditText);
        submitStudentRegistrationButton = findViewById(R.id.registrationOpenCloseSubmitButton);

        populateOperationTypeSpinner();
    }

    void populateOperationTypeSpinner() {
        registrationStatusSpinnerItems.add("open");
        registrationStatusSpinnerItems.add("close");
        registrationStatusSpinner.setAdapter(new MySpinnerAdapter(context, registrationStatusSpinnerItems));
        registrationStatusSpinner.setOnItemSelectedListener(new registrationStatusOnItemSelectListener());
    }

    public void allCourseName_DurationDataFound() {
        courseSpinnerItems = new ArrayList<>(allCourseAndTermHashMap.keySet());
        if (courseSpinnerItems.isEmpty()) {
            courseSpinnerItems.add("No course Found");
        }
        Collections.sort(courseSpinnerItems);
        courseSpinner.setAdapter(new MySpinnerAdapter(context, courseSpinnerItems));

        courseSpinner.setOnItemSelectedListener(new courseOnItemSelectListener());
        termSpinner.setOnItemSelectedListener(new termOnItemSelectListener());
        batchSpinner.setOnItemSelectedListener(new batchOnItemSelectListener());
        submitStudentRegistrationButton.setOnClickListener(new submitButtonOnClickListener());
    }


    class registrationStatusOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            registrationStatusSpinnerSelected = registrationStatusSpinnerItems.get(position);
            if (registrationStatusSpinnerSelected.equals(registrationStatusSpinnerItems.get(0))) {
                setItemEnable(true);
                fetchAllCoursesAndTheirTermData();
            } else {
                setItemEnable(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public void setItemEnable(boolean status) {
//        courseSpinner.setEnabled(status);
//        termSpinner.setEnabled(status);
//        batchSpinner.setEnabled(status);
        registrationPasswordEditText.setEnabled(status);

    }

    private void blockScreen(boolean state){        //if true screen set To block otherwise unblock
        View inputBlockerView = findViewById(R.id.inputBlockerView);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if(state) {
            inputBlockerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            inputBlockerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    class courseOnItemSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            //blockSubmitButton(true);
            courseSpinnerSelected = courseSpinnerItems.get(i);
            try {
                termSpinnerItems = allCourseAndTermHashMap.get(courseSpinnerSelected);

            } catch (IndexOutOfBoundsException | NullPointerException e) {
                termSpinnerItems = null;
                Log.d("LOOOG", "at courseOnItemSelectListener:" + Log.getStackTraceString(e));
            }
            if (termSpinnerItems == null) {
                termSpinnerItems = new ArrayList<>();
                termSpinnerItems.add("No Year/Sem Found");
            }
            Collections.sort(termSpinnerItems);
            termSpinner.setAdapter(new MySpinnerAdapter(context, termSpinnerItems));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class termOnItemSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
            //blockSubmitButton(true);
            termSpinnerSelected = termSpinnerItems.get(i);
            batchSpinnerItems.clear();
            batchSpinnerItems.add("1");
            batchSpinnerItems.add("2");
            Collections.sort(batchSpinnerItems);
            batchSpinner.setAdapter(new MySpinnerAdapter(context, batchSpinnerItems));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class batchOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
            //blockSubmitButton(true);
            batchSpinnerSelected = batchSpinnerItems.get(i);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class submitButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                InputMethodManager hideKeyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                hideKeyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
            catch (NullPointerException e){}

            if(!isNetworkConnected()){
                Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show();
                return;
            }

            if(courseSpinnerSelected.equals("No course Found") && termSpinnerSelected.equals("No Year/Sem Found")){
                Toast.makeText(context, "Invalid Course / term", Toast.LENGTH_LONG).show();
                return;
            }

            if (registrationStatusSpinnerSelected.equalsIgnoreCase("open")) {
                EditText registrationPasswordEditText = findViewById(R.id.setRegistrationPasswordEditText);
                final String registrationPassword = "" + registrationPasswordEditText.getText();
                if (registrationPassword.isEmpty()) {
                    Toast.makeText(studentRegistrationActivity.this, "Give a password", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    AlertDialog.Builder dialogBuild = new AlertDialog.Builder(studentRegistrationActivity.this)
                            .setTitle("Confirm")
                            .setMessage("Open Registration For " + courseSpinnerSelected.toUpperCase() + " " + termSpinnerSelected + " Year " + batchSpinnerSelected + " Batch");
                    dialogBuild.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            blockScreen(true);
                            if(setRegistrationYear()){
                                Toast.makeText(studentRegistrationActivity.this, "timeFetchError:checkInternetConnection", Toast.LENGTH_SHORT).show();
                                blockScreen(false);
                                return;
                            }

                            HashMap<String, String> registrationData = new HashMap<>();
                            registrationData.put("status", registrationStatusSpinnerSelected);
                            registrationData.put("course", courseSpinnerSelected);
                            registrationData.put("term", termSpinnerSelected);
                            registrationData.put("batch", batchSpinnerSelected);
                            registrationData.put("password", registrationPassword);
                            registrationData.put("registration_year", registrationYear);

                            FirebaseFirestore.getInstance().collection("STUDENT_REGISTRAR").document("detail").set(registrationData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(studentRegistrationActivity.this, "Registration open", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(studentRegistrationActivity.this, "Registration not open", Toast.LENGTH_SHORT).show();
                                            }
                                            blockScreen(false);
                                        }
                                    });

                        }
                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            } else {
                AlertDialog.Builder dialogBuild = new AlertDialog.Builder(studentRegistrationActivity.this)
                        .setTitle("Confirm")
                        .setMessage("Close Registration");
                dialogBuild.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blockScreen(true);
                        HashMap<String, String> registrationData = new HashMap<>();
                        registrationData.put("status", registrationStatusSpinnerSelected);
                        registrationData.put("course", null);
                        registrationData.put("term", null);
                        registrationData.put("batch", null);
                        registrationData.put("password", null);

                        FirebaseFirestore.getInstance().collection("STUDENT_REGISTRAR").document("detail").set(registrationData)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(studentRegistrationActivity.this, "Registration close", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(studentRegistrationActivity.this, "Registration not closed", Toast.LENGTH_SHORT).show();
                                        }
                                        blockScreen(false);
                                    }
                                });


                    }
                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }



    public void fetchAllCoursesAndTheirTermData() {
        if (allCourseAndTermHashMap == null) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("COURSE").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        allCourseAndTermHashMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String courseName = Objects.requireNonNull(document.get("course_name")).toString();
                                Integer duration = Integer.parseInt(Objects.requireNonNull(document.get("duration")).toString());
                                ArrayList<String> termList = new ArrayList<>();
                                for (Integer i = 1; i <= duration; i++) {
                                    termList.add(i.toString());
                                }
                                allCourseAndTermHashMap.put(courseName, termList);
                            } catch (NullPointerException e) {
                                Toast.makeText(context, "Error: fetchAdminViewAttendanceCourseName_DurationData: " + e, Toast.LENGTH_LONG).show();
                                Log.d("LOOOG", "at fetchAdminViewAttendanceCourseName_DurationData onComplete:" + Log.getStackTraceString(e));
                            }
                        }
                        allCourseName_DurationDataFound();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Toast.makeText(context, "Error: FetchingAdminCourseDurationDataForViewAttendance: "+e, Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "Check Internet Connection Error:" + e.getCause(), Toast.LENGTH_LONG).show();
                    Log.d("LOOOG", "at fetchAdminViewAttendanceCourseName_DurationData onfaliure:" + Log.getStackTraceString(e));
                }
            });
        }
    }

    private boolean isNetworkConnected() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED | connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }

        return connected;

//            final String command = "ping -c 1 google.com";
//            try {
//                return Runtime.getRuntime().exec(command).waitFor() == 0;
//            } catch (InterruptedException | IOException e) {
//                return false;
//            }
    }

    private boolean setRegistrationYear() {
        MyCalendar myCalendar = new MyCalendar();
        registrationYear = null;
        try {
            myCalendar.start();
            myCalendar.join();
            registrationYear = myCalendar.getYear();
        }catch (Exception e){
            return false;
        }
        return registrationYear==null;
    }
}
