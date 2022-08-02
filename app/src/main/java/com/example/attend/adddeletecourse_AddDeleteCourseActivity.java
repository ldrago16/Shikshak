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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class adddeletecourse_AddDeleteCourseActivity extends AppCompatActivity {

    String operationType;
    ArrayList<String> allCourseList;
    LinearLayoutCompat addCourseLayout, deleteCourseLayout;
    Spinner deleteCourseSpinner;
    String deleteCourseSpinnerSelected;
    EditText courseNameEditText, courseDurationEditText;
    Spinner courseAddDeleteOperationTypeSpinner;
    Button submitAddCourseDetailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adddeletecourse_activity_add_course);

        addCourseLayout = findViewById(R.id.addCourseLayoutContainer);
        deleteCourseLayout = findViewById(R.id.deleteCourseLayoutContainer);

        courseNameEditText = findViewById(R.id.addCourseNameEditText);
        courseDurationEditText = findViewById(R.id.setCourseDurationEditText);
        submitAddCourseDetailButton = findViewById(R.id.addCourseSubmitButton);
        courseAddDeleteOperationTypeSpinner = findViewById(R.id.courseAddDeleteOperationTypeSpinner);

        deleteCourseSpinner = findViewById(R.id.deleteCourseSpinner);
        populateCourseAddDeleteOperationTypeSpinner();
    }

    void populateCourseAddDeleteOperationTypeSpinner() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Add Course");
        arrayList.add("Remove Course");
        MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter(adddeletecourse_AddDeleteCourseActivity.this, arrayList);
        courseAddDeleteOperationTypeSpinner.setAdapter(spinnerAdapter);
        courseAddDeleteOperationTypeSpinner.setOnItemSelectedListener(new CourseAddDeleteOperationSpinnerOnSelect());
    }

    class CourseAddDeleteOperationSpinnerOnSelect implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                operationType = "add";
                addCourseLayout.setVisibility(View.VISIBLE);
                deleteCourseLayout.setVisibility(View.GONE);
            } else {
                operationType = "delete";
                fetchAllCourses();
                deleteCourseSpinner.setOnItemSelectedListener(new DeleteCourseSpinnerOnSelect());
                deleteCourseLayout.setVisibility(View.VISIBLE);
                addCourseLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class DeleteCourseSpinnerOnSelect implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            deleteCourseSpinnerSelected = allCourseList.get(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public void fetchAllCourses() {
        blockScreen(true);
        if (allCourseList == null) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("COURSE").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        allCourseList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String courseName = document.get("course_name").toString();
                                allCourseList.add(courseName);
                            } catch (NullPointerException e) {
                                Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "Error: AddDeleteCourseActivityCourseName_DurationData: " + e, Toast.LENGTH_LONG).show();
                                Log.d("LOOOG", "at AddDeleteCourseActivityCourseName_DurationData onComplete:" + Log.getStackTraceString(e));
                            }
                        }

                        if (allCourseList.isEmpty()) {
                            allCourseList.add("No Course Found");
                        }

                        submitAddCourseDetailButton.setOnClickListener(new addDeleteCourseSubmitButtonOnClick()); // set listener on button only when all course and their data successfully found
                        deleteCourseSpinner.setAdapter(new MySpinnerAdapter(adddeletecourse_AddDeleteCourseActivity.this, allCourseList));
                        blockScreen(false);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Toast.makeText(context, "Error: FetchingAdminCourseDurationDataForViewAttendance: "+e, Toast.LENGTH_LONG).show();
                    Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "Check Internet Connection Error:" + e.getCause(), Toast.LENGTH_LONG).show();
                    Log.d("LOOOG", "at AddDeleteCourseActivityCourseName_DurationData onfaliure:" + Log.getStackTraceString(e));
                }
            });
        } else {
            deleteCourseSpinner.setAdapter(new MySpinnerAdapter(adddeletecourse_AddDeleteCourseActivity.this, allCourseList));
            blockScreen(false);
        }
    }

    class addDeleteCourseSubmitButtonOnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if(!isNetworkConnected()){
                Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                return;
            }

            if (operationType.equals("add")) {
                final String addCourseName = courseNameEditText.getText().toString().trim().toLowerCase();
                final String addcourseDuration = courseDurationEditText.getText().toString().trim();
                if (addCourseName.isEmpty()) {
                    courseNameEditText.setError("Invalid Course Name");
                    return;
                }
                if (addcourseDuration.isEmpty()) {
                    courseDurationEditText.setError("Invalid Course Duration");
                    return;
                }
                if (!addcourseDuration.matches("[1-9]{1}")) {
                    courseDurationEditText.setError("Invalid Course Duration");
                    return;
                }

                try {
                    InputMethodManager hideKeyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    hideKeyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (NullPointerException e) {
                }
                addCourse(addCourseName, addcourseDuration);
            } else {
                final String deleteCourseName = deleteCourseSpinnerSelected;

                if (!deleteCourseName.equalsIgnoreCase("No Course Found")) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(adddeletecourse_AddDeleteCourseActivity.this);
                    alertBuilder.setTitle("Warning")
                            .setMessage("Selected Course is permanently deleted and not accessible in future.\nDo you want to delete")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteCourse(deleteCourseName);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog deleteAlert = alertBuilder.create();
                    deleteAlert.show();
                } else {
                    Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "Select valid course name", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    void addCourse(final String courseName, final String courseDuration) {
        blockScreen(true);
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("COURSE")
                .document(courseName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                courseNameEditText.setError("Course Already Exist");
                                Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "Course already exist", Toast.LENGTH_SHORT).show();
                                blockScreen(false);
                                return;
                            } else {
                                HashMap<String, String> courseDurationMap = new HashMap<>();
                                courseDurationMap.put("course_name", courseName);
                                courseDurationMap.put("duration", courseDuration);
                                firebaseFirestore.collection("COURSE")
                                        .document(courseName)
                                        .set(courseDurationMap);
                                courseNameEditText.setText("");
                                courseDurationEditText.setText("");
                                Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "Course added successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                        blockScreen(false);
                    }
                })
                .addOnFailureListener(adddeletecourse_AddDeleteCourseActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "No internet available course addition failed", Toast.LENGTH_SHORT).show();
                        Log.d("Looog", Log.getStackTraceString(e));
                        blockScreen(false);
                    }
                });

    }

    void deleteCourse(final String courseName) {
        blockScreen(true);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("COURSE")
                .document(courseName)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        blockScreen(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                            fetchAllCourses();
                        } else {
                            Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "Course deletion failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(adddeletecourse_AddDeleteCourseActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(adddeletecourse_AddDeleteCourseActivity.this, "No internet available course deletion failed", Toast.LENGTH_SHORT).show();
                        blockScreen(false);
                    }
                });
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

    private void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        View inputBlockerView = findViewById(R.id.inputBlockerView);
        ProgressBar progressBar = findViewById(R.id.addDeleteCourseProgressBar);
        if (state) {
            inputBlockerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            inputBlockerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
