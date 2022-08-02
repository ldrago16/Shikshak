package com.example.attend;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class adddeletesubject_AddDeleteSubjectActivity extends AppCompatActivity {

    LinearLayoutCompat addSubjectLayout, deleteSubjectLayout;
    EditText addSubjectEditText;
    Spinner operationType, courseSpinner, termSpinner, deleteSubjectSpinner;
    Button submitButton;

    adddeletesubject_AddDeleteSubjectActivity context;

    String operationTypeSpinnerSelected, courseSpinnerSelected, termSpinnerSelected, subjectSpinnerSelected;


    ArrayList<String> operationTypeSpinnerItems = new ArrayList<>();
    ArrayList<String> courseSpinnerItems = new ArrayList<>();
    ArrayList<String> termSpinnerItems = new ArrayList<>();
    ArrayList<String> subjectSpinnerItems = new ArrayList<>();

    HashMap<String, ArrayList<String>> allCourseAndTermHashMap;
    HashMap<String, ArrayList<String>> allSubjectHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adddeletesubject_activity_add_delete_subject);

        context = adddeletesubject_AddDeleteSubjectActivity.this;

        addSubjectLayout = findViewById(R.id.addSubjectLayoutContainer);
        deleteSubjectLayout = findViewById(R.id.deleteSubjectLayoutContainer);

        operationType = findViewById(R.id.subjectAddDeleteOperationTypeSpinner);
        courseSpinner = findViewById(R.id.selectCourseSpinner);
        termSpinner = findViewById(R.id.selectTermSpinner);
        addSubjectEditText = findViewById(R.id.setSubjectNameEditText);
        deleteSubjectSpinner = findViewById(R.id.selectSubjectForDeleteSpinner);

        submitButton = findViewById(R.id.addDeleteSubjectSubmitButton);

        populateOperationTypeSpinner();
        fetchAllCoursesAndTheirTermData();

    }

    void populateOperationTypeSpinner() {
        operationTypeSpinnerItems.add("Add Subject");
        operationTypeSpinnerItems.add("Delete Subject");
        operationType.setAdapter(new MySpinnerAdapter(context, operationTypeSpinnerItems));
        operationType.setOnItemSelectedListener(new operationTypeOnItemSelectListener());
    }

    void fetchAllCoursesAndTheirTermData() {
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
                                Toast.makeText(context, "Error: adddeletesubject_AddDeleteSubjectActivity_DurationData: " + e, Toast.LENGTH_LONG).show();
                                Log.d("LOOOG", "at adddeletesubject_AddDeleteSubjectActivity_DurationData onComplete:" + Log.getStackTraceString(e));
                            }
                        }
                        fetchAllSubjectsData();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Toast.makeText(context, "Error: FetchingAdminCourseDurationDataForViewAttendance: "+e, Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "Check Internet Connection Error:" + e.getCause(), Toast.LENGTH_LONG).show();
                    Log.d("LOOOG", "at adddeletesubject_AddDeleteSubjectActivity_DurationData onfaliure:" + Log.getStackTraceString(e));
                }
            });
        } else {
            fetchAllSubjectsData();
        }
    }

    public void fetchAllSubjectsData() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("SUBJECT").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allSubjectHashMap = new HashMap<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (!document.exists())
                            continue;
                        try {
                            String courseName = Objects.requireNonNull(document.get("course_name")).toString();
                            String term = Objects.requireNonNull(document.get("term")).toString();
                            String courseName_Term = courseName + term;
                            String subject = Objects.requireNonNull(document.get("subject_name")).toString();
                            ArrayList<String> subjectList = allSubjectHashMap.get(courseName_Term);

                            if (subjectList == null) {
                                ArrayList<String> temp_subject = new ArrayList<>();
                                temp_subject.add(subject);
                                allSubjectHashMap.put(courseName_Term, temp_subject);
                            } else {
                                subjectList.add(subject);
                                allSubjectHashMap.put(courseName_Term, subjectList);
                            }
                        } catch (NullPointerException e) {
                            Toast.makeText(context, "Error: adddeletesubject_AddDeleteSubjectActivitySubjectsData" + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("LOOOG", "at adddeletesubject_AddDeleteSubjectActivitySubjectsData onComplete:" + Log.getStackTraceString(e));
                        }
                    }
                    allCourseName_Duration_SubjectDataFound();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Toast.makeText(context, "Error: FetchingAdminSubjectDataForViewAttendance: "+e, Toast.LENGTH_LONG).show();
                Toast.makeText(context, "Check Internet Connection Error:" + e.getCause(), Toast.LENGTH_LONG).show();
                Log.d("LOOOG", "at adddeletesubject_AddDeleteSubjectActivitySubjectsData onFailure:" + Log.getStackTraceString(e));
            }
        });
    }

    public void allCourseName_Duration_SubjectDataFound() {
        courseSpinnerItems = new ArrayList<>(allCourseAndTermHashMap.keySet());

        if (courseSpinnerItems.isEmpty()) {
            courseSpinnerItems.add("No course Found");
        }
        Collections.sort(courseSpinnerItems);
        courseSpinner.setAdapter(new MySpinnerAdapter(context, courseSpinnerItems));
        courseSpinner.setOnItemSelectedListener(new courseOnItemSelectListener());
        termSpinner.setOnItemSelectedListener(new termOnItemSelectListener());
        deleteSubjectSpinner.setOnItemSelectedListener(new subjectOnItemSelectListener());
        submitButton.setOnClickListener(new onSubmitButtonClickListener());
    }

    class operationTypeOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                operationTypeSpinnerSelected = "add";
                addSubjectLayout.setVisibility(View.VISIBLE);
                deleteSubjectLayout.setVisibility(View.GONE);
            } else {
                operationTypeSpinnerSelected = "delete";
                deleteSubjectLayout.setVisibility(View.VISIBLE);
                addSubjectLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class courseOnItemSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

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
            try {
                subjectSpinnerItems = allSubjectHashMap.get(courseSpinnerSelected + termSpinnerSelected);
            } catch (ClassCastException | NullPointerException e) {
                subjectSpinnerItems = null;
                Toast.makeText(context, "No Subject found", Toast.LENGTH_SHORT).show();
                Log.d("LOOOG", "Exception : No Subject found " + Log.getStackTraceString(e));
            }
            if (subjectSpinnerItems == null) {
                subjectSpinnerItems = new ArrayList<>();
                subjectSpinnerItems.add("No Subject Found");
            }
            Collections.sort(subjectSpinnerItems);
            deleteSubjectSpinner.setAdapter(new MySpinnerAdapter(context, subjectSpinnerItems));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class subjectOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            subjectSpinnerSelected = subjectSpinnerItems.get(i);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class onSubmitButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                if (courseSpinnerSelected.equalsIgnoreCase("No course Found")) {
                    Toast.makeText(context, "Invalid Course Name", Toast.LENGTH_SHORT).show();
                }
                if (termSpinnerSelected.equalsIgnoreCase("No Year/Sem Found")) {
                    Toast.makeText(context, "Invalid Term Name", Toast.LENGTH_SHORT).show();
                }

                if (operationTypeSpinnerSelected.equals("add")) {

                    String subjectNameToAdd = addSubjectEditText.getText().toString().trim().toLowerCase();
                    if (subjectNameToAdd.isEmpty()) {
                        addSubjectEditText.setError("Invalid Subject Name");
                        Toast.makeText(context, "Invalid Subject Name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addSubject(courseSpinnerSelected, termSpinnerSelected, subjectNameToAdd);
                } else {
                    if (subjectSpinnerSelected.equalsIgnoreCase("No Subject found")) {
                        Toast.makeText(context, "Invalid Subject Name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setTitle("Warning")
                            .setMessage("Selected Subject is permanently deleted and not accessible in future.\nDo you want to delete")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteSubject(subjectSpinnerSelected);
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
                }
            } catch (NullPointerException e) {
                Toast.makeText(context, "Invalid Selection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void addSubject(final String courseName, final String term, final String subjectName) {
        blockScreen(true);
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("SUBJECT")
                .document(subjectName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                addSubjectEditText.setError("Subject Already Exist");
                                Toast.makeText(context, "Subject already exist", Toast.LENGTH_SHORT).show();
                                blockScreen(false);
                                return;
                            } else {
                                HashMap<String, String> addSubjectMap = new HashMap<>();
                                addSubjectMap.put("course_name", courseName);
                                addSubjectMap.put("term", term);
                                addSubjectMap.put("subject_name", subjectName);
                                firebaseFirestore.collection("SUBJECT")
                                        .document(subjectName)
                                        .set(addSubjectMap);
                                addSubjectEditText.setText("");
                                Toast.makeText(context, "Subject added successfully", Toast.LENGTH_SHORT).show();
                                fetchAllSubjectsData();
                            }
                        }
                        blockScreen(false);
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "No internet available subject addition failed", Toast.LENGTH_SHORT).show();
                        Log.d("Looog", Log.getStackTraceString(e));
                        blockScreen(false);
                    }
                });
    }

    void deleteSubject(String subjectName) {
        blockScreen(true);
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("SUBJECT")
                .document(subjectName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Subject deleted successfully", Toast.LENGTH_SHORT).show();
                        allCourseAndTermHashMap = null;
                        fetchAllCoursesAndTheirTermData();
                        blockScreen(false);
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "No internet available subject deletion failed", Toast.LENGTH_SHORT).show();
                        blockScreen(false);
                    }
                });

    }

    private void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        View inputBlockerView = findViewById(R.id.inputBlockerView);
        ProgressBar progressBar = findViewById(R.id.addDeleteSubjectProgressBar);
        if (state) {
            inputBlockerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            inputBlockerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
