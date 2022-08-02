package com.example.attend;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class teacherTakeSubject_TeacherSelectSubjectActivity extends AppCompatActivity {
    Spinner operationType, courseSpinner, termSpinner, subjectSpinner, batchSpinner;
    Button submitSelectedSubjectButton;
    DocumentSnapshot teacherAlreadySelectedDocRef;

    TeacherDetailHolder teacherDetailHolder = TeacherDetailHolder.getTeacherDetailHolderReference();

    String operationTypeSpinnerSelected, courseSpinnerSelected, termSpinnerSelected, subjectSpinnerSelected, batchSpinnerSelected;


    ArrayList<String> operationTypeSpinnerItems = new ArrayList<>();
    ArrayList<String> courseSpinnerItems = new ArrayList<>();
    ArrayList<String> termSpinnerItems = new ArrayList<>();
    ArrayList<String> subjectSpinnerItems = new ArrayList<>();
    ArrayList<String> batchSpinnerItems = new ArrayList<>();

    HashMap<String, ArrayList<String>> allCourseAndTermHashMap;
    HashMap<String, ArrayList<String>> allSubjectHashMap;

    static String TeacherId, TeacherRight, TeacherName;

    static teacherTakeSubject_TeacherSelectSubjectActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_select_subject);
        context = teacherTakeSubject_TeacherSelectSubjectActivity.this;
//        Intent intent = getIntent();
//        String TeacherId = intent.getStringExtra("TeacherId");
//        String TeacherRight = intent.getStringExtra("Rights");
//        String TeacherName = intent.getStringExtra("Name");

        TeacherId = teacherDetailHolder.getTeacherId();
        TeacherRight = teacherDetailHolder.getTeacherRight();
        TeacherName = teacherDetailHolder.getTeacherName();

        operationType = findViewById(R.id.selectOrDeselectSubjectOperationTypeSpinner);
        courseSpinner = findViewById(R.id.courseSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        batchSpinner = findViewById(R.id.batchSpinner);
        submitSelectedSubjectButton = findViewById(R.id.selectSubjectSubmitButton);

        populateOperationTypeSpinner();
    }

    void populateOperationTypeSpinner() {
        operationTypeSpinnerItems.add("Select Subject");
        operationTypeSpinnerItems.add("Deselect Subject");
        operationType.setAdapter(new MySpinnerAdapter(context, operationTypeSpinnerItems));
        operationType.setOnItemSelectedListener(new operationTypeOnItemSelectListener());
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
                        teacherTakeSubject_TeacherSelectSubjectActivity.this.fetchAllSubjectsData();
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
        } else {
            teacherTakeSubject_TeacherSelectSubjectActivity.this.fetchAllSubjectsData();
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
                            Toast.makeText(context, "Error: fetchAdminViewAttendanceSubjectsData" + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("LOOOG", "at fetchAdminViewAttendanceSubjectsData onComplete:" + Log.getStackTraceString(e));
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
                Log.d("LOOOG", "at fetchAdminViewAttendanceSubjectsData onFailure:" + Log.getStackTraceString(e));
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
        batchSpinner.setOnItemSelectedListener(new batchOnItemSelectListener());
        subjectSpinner.setOnItemSelectedListener(new subjectOnItemSelectListener());
    }

    class operationTypeOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            operationTypeSpinnerSelected = operationTypeSpinnerItems.get(position);
            if (operationTypeSpinnerSelected.equals(operationTypeSpinnerItems.get(0))) {
                fetchAllCoursesAndTheirTermData();
            } else {
                fetchTeacherAlreadySelectedCourseTermSubjectData();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public void fetchTeacherAlreadySelectedCourseTermSubjectData() {
        courseSpinnerItems.clear();
        if (teacherAlreadySelectedDocRef == null) {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("TEACHERS").document(TeacherId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // Toast.makeText(attend_SelectYearSubActivity.this, "TEACHER ID FOUND", Toast.LENGTH_SHORT).show();
                        teacherAlreadySelectedDocRef = documentSnapshot;
                        teacherAlreadySelectedCourseTermSubjectDataFound();
                    } else {
                        Toast.makeText(context, "NO TEACHER ID FOUND", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            teacherAlreadySelectedCourseTermSubjectDataFound();
        }
    }

    public void teacherAlreadySelectedCourseTermSubjectDataFound() {
//        fetch a array list of course for which teacher teach
        courseSpinnerItems = (ArrayList<String>) teacherAlreadySelectedDocRef.get("course_name");
        if (courseSpinnerItems == null) {
            courseSpinnerItems = new ArrayList<>();
            courseSpinnerItems.add("No Courses Found");
        }
        courseSpinner.setOnItemSelectedListener(new courseOnItemSelectListener());
        Collections.sort(courseSpinnerItems);
        courseSpinner.setAdapter(new MySpinnerAdapter(context, courseSpinnerItems));

        termSpinner.setOnItemSelectedListener(new termOnItemSelectListener());
        batchSpinner.setOnItemSelectedListener(new batchOnItemSelectListener());
        subjectSpinner.setOnItemSelectedListener(new subjectOnItemSelectListener());
    }

    class courseOnItemSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            //blockSubmitButton(true);
            courseSpinnerSelected = courseSpinnerItems.get(i);
            try {
                if (operationTypeSpinnerSelected.equals(operationTypeSpinnerItems.get(0))) {
                    termSpinnerItems = allCourseAndTermHashMap.get(courseSpinnerSelected);
                } else {
                    termSpinnerItems = ((ArrayList<String>) teacherAlreadySelectedDocRef.get(courseSpinnerSelected));
                }

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
            if (operationTypeSpinnerSelected.equals(operationTypeSpinnerItems.get(0))) {
                batchSpinnerItems.clear();
                batchSpinnerItems.add("1");
                batchSpinnerItems.add("2");
            } else {
                try {
                    batchSpinnerItems = (ArrayList<String>) teacherAlreadySelectedDocRef.get(courseSpinnerSelected + "" + termSpinnerSelected);
                } catch (IllegalArgumentException | ClassCastException | NullPointerException e) {
                    batchSpinnerItems = null;
                    Toast.makeText(context, "No Batch found", Toast.LENGTH_SHORT).show();
                    Log.d("LOOOG", "Exception : No Batch found " + Log.getStackTraceString(e));
                }
                if (batchSpinnerItems == null) {
                    batchSpinnerItems = new ArrayList<>();
                    batchSpinnerItems.add("No Batch Found");
                }
            }
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
            try {
                if (operationTypeSpinnerSelected.equals(operationTypeSpinnerItems.get(0))) {
                    subjectSpinnerItems = allSubjectHashMap.get(courseSpinnerSelected + termSpinnerSelected);
                } else {
                    subjectSpinnerItems = (ArrayList<String>) teacherAlreadySelectedDocRef.get(courseSpinnerSelected + "" + termSpinnerSelected + "_" + batchSpinnerSelected);
                }
            } catch (IllegalArgumentException | ClassCastException | NullPointerException e) {
                subjectSpinnerItems = null;
                Toast.makeText(context, "No Subject found", Toast.LENGTH_SHORT).show();
                Log.d("LOOOG", "Exception : No Subject found " + Log.getStackTraceString(e));
            }
            if (subjectSpinnerItems == null) {
                subjectSpinnerItems = new ArrayList<>();
                subjectSpinnerItems.add("No Subject Found");
            }
            Collections.sort(subjectSpinnerItems);
            subjectSpinner.setAdapter(new MySpinnerAdapter(context, subjectSpinnerItems));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class subjectOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            subjectSpinnerSelected = subjectSpinnerItems.get(i);
            if (!subjectSpinnerSelected.equalsIgnoreCase("No Subject Found")) {
                submitSelectedSubjectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!isNetworkConnected()){
                            Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (operationTypeSpinnerSelected.equals(operationTypeSpinnerItems.get(0))) {
                            String field = courseSpinnerSelected + termSpinnerSelected + "_" + batchSpinnerSelected;
                            teacherTakeSubject_TeacherSelectSubjectActivity.this.checkSubjectAlreadyTakenIfNotGive(field, courseSpinnerSelected, termSpinnerSelected, batchSpinnerSelected, subjectSpinnerSelected);
                        } else {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(teacherTakeSubject_TeacherSelectSubjectActivity.context);
                            AlertDialog subjectDeleteAlert = alertBuilder.setTitle("WARNING")
                                    .setMessage("Selected subject will permanently deleted from your subject list\nConfirm Delete")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteSubjectFromTeacherSubjectList(courseSpinnerSelected, termSpinnerSelected, batchSpinnerSelected, subjectSpinnerSelected);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create();
                            subjectDeleteAlert.show();
                        }
                    }
                });
            } else {
                submitSelectedSubjectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.this, "Invalid subject selected", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public void checkSubjectAlreadyTakenIfNotGive(final String field, final String course, final String term, final String batch, final String subject) {
        blockScreen(true);
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("TEACHERS")
                .whereArrayContains(field, subject)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d("LOOOG", "Not Taken");
                            allotSubjectToTeacher(course, term, batch, subject);
                        } else {
                            Log.d("LOOOG", "Already Taken");
                            String name = queryDocumentSnapshots.getDocuments().get(0).get("teacher_name").toString();
                            blockScreen(false);
                            Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "Subject already taken by " + name, Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(teacherTakeSubject_TeacherSelectSubjectActivity.context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("LOOOG", "Exception: Subject Checking Failed " + Log.getStackTraceString(e));
                Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "No internet connection", Toast.LENGTH_LONG).show();
                blockScreen(false);
            }
        });
    }

    public void allotSubjectToTeacher(String course, String term, String batch, String subject) {
//        teacherDocRef.collection("TEACHERS")
//                .document(teacherTakeSubject_TeacherSelectSubjectActivity.TeacherId)
//                .update("course_name", FieldValue.arrayUnion(course));
//
//        teacherDocRef.collection("TEACHERS")
//                .document(teacherTakeSubject_TeacherSelectSubjectActivity.TeacherId)
//                .update(course, FieldValue.arrayUnion(term));
//
//        teacherDocRef.collection("TEACHERS")
//                .document(teacherTakeSubject_TeacherSelectSubjectActivity.TeacherId)
//                .update(course+term, FieldValue.arrayUnion(batch));
//
//        teacherDocRef.collection("TEACHERS")
//                .document(teacherTakeSubject_TeacherSelectSubjectActivity.TeacherId)
//                .update(course+term+"_"+batch, FieldValue.arrayUnion(subject));
        class UploadTeacherSelectedSubjectDataInSequence {
            private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            private String course, term, batch, subject;

            UploadTeacherSelectedSubjectDataInSequence(String tempCourse, String tempTerm, String tempBatch, String tempSubject) {
                course = tempCourse;
                term = tempTerm;
                batch = tempBatch;
                subject = tempSubject;
            }

            void start() {

                //Set course_name array which contain array of selected course
                firebaseFirestore.collection("TEACHERS")
                        .document(teacherTakeSubject_TeacherSelectSubjectActivity.TeacherId)
                        .update("course_name", FieldValue.arrayUnion(course))
                        .continueWith(new Continuation<Void, Object>() {
                            @Override
                            public Object then(@NonNull Task<Void> task) throws Exception {

                                //Set course field which contain array of selected term in that course
                                return firebaseFirestore.collection("TEACHERS")
                                        .document(teacherTakeSubject_TeacherSelectSubjectActivity.TeacherId)
                                        .update(course, FieldValue.arrayUnion(term))
                                        .continueWith(new Continuation<Void, Object>() {
                                            @Override
                                            public Object then(@NonNull Task<Void> task) throws Exception {

                                                //Set course+term field which contain array of selected batch in that course and in that term
                                                return firebaseFirestore.collection("TEACHERS")
                                                        .document(teacherTakeSubject_TeacherSelectSubjectActivity.TeacherId)
                                                        .update(course + term, FieldValue.arrayUnion(batch))
                                                        .continueWith(new Continuation<Void, Object>() {
                                                            @Override
                                                            public Object then(@NonNull Task<Void> task) throws Exception {

                                                                //Set course+term+"_"+batch field which contain array of subject teacher taught in that particular courses term and batch
                                                                firebaseFirestore.collection("TEACHERS")
                                                                        .document(teacherTakeSubject_TeacherSelectSubjectActivity.TeacherId)
                                                                        .update(course + term + "_" + batch, FieldValue.arrayUnion(subject));
                                                                teacherAlreadySelectedDocRef = null;
                                                                return null;
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Object>() {
                    @Override
                    public void onComplete(@NonNull Task<Object> task) {
                        if (task.isSuccessful()) {
                            Log.d("LOOOG", "Subject Added Successfully");
                            Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "Subject added in your subject list", Toast.LENGTH_LONG).show();
                        } else {
                            Log.d("LOOOG", "Error: Subject Addition Failed");
                            Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "Subject not added", Toast.LENGTH_LONG).show();
                        }
                        blockScreen(false);
                    }
                })
                        .addOnFailureListener(teacherTakeSubject_TeacherSelectSubjectActivity.context, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("LOOOG", "Exception: Subject Addition Failed " + Log.getStackTraceString(e));
                                Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "No Internet Connection", Toast.LENGTH_LONG).show();
                                blockScreen(false);
                            }
                        });
            }
        }
        UploadTeacherSelectedSubjectDataInSequence uploadTeacherSubjectData = new UploadTeacherSelectedSubjectDataInSequence(course, term, batch, subject);
        uploadTeacherSubjectData.start();
    }

    public void deleteSubjectFromTeacherSubjectList(String course, String term, String batch, String subject) {
        blockScreen(true);
        class DeleteTeacherSelectedSubjectDataInSequence {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            private DocumentReference teacherDocRef = firebaseFirestore.collection("TEACHERS").document(TeacherId);

            private String course, term, batch, subject;
            ArrayList<String[]> fieldList = new ArrayList<>();
            int documentCleanCounter = 0;

            DeleteTeacherSelectedSubjectDataInSequence(String tempCourse, String tempTerm, String tempBatch, String tempSubject) {
                course = tempCourse;
                term = tempTerm;
                batch = tempBatch;
                subject = tempSubject;
            }

            void start() {
                startTeacherDocumentCleaningProcess();
            }

            void startTeacherDocumentCleaningProcess() {
                fieldList.add(new String[]{course + term + "_" + batch, subject});
                fieldList.add(new String[]{course + term, batch});
                fieldList.add(new String[]{course, term});
                fieldList.add(new String[]{"course_name", course});

                doClean();
//                teacherDocRef.update(course + term + "_" + batch, FieldValue.arrayRemove(subject))
//                        .continueWith(new Continuation<Void, Object>() {
//                            @Override
//                            public Object then(@NonNull Task<Void> task) throws Exception {
//                                teacherDocRef.get().continueWith(new Continuation<DocumentSnapshot, Object>() {
//                                    @Override
//                                    public Object then(@NonNull Task<DocumentSnapshot> task) throws Exception {
//                                        if (task.isSuccessful()) {
//                                            DocumentSnapshot docSnap = task.getResult();
//                                            try {
//                                                if (((ArrayList<String>) docSnap.get(course + term + "_" + batch)).isEmpty()) {
//                                                    teacherDocRef.update(course + term + "_" + batch, FieldValue.delete())
//                                                            .continueWith(new Continuation<Void, Object>() {
//                                                                @Override
//                                                                public Object then(@NonNull Task<Void> task) throws Exception {
//                                                                    doClean();
//                                                                    return null;
//                                                                }
//                                                            });
//                                                }
//                                            } catch (Exception e) {
//                                                Log.d("LOOOG", Log.getStackTraceString(e));
//                                            }
//                                        }
//                                        return null;
//                                    }
//                                });
//                                return null;
//                            }
//                        });
            }

            void doClean() {
                Log.d("Looog", "Subject Deleted");
                if (documentCleanCounter < fieldList.size()) {
                    final String[] fieldName = fieldList.get(documentCleanCounter);
                    teacherDocRef.update(fieldName[0], FieldValue.arrayRemove(fieldName[1])).continueWith(new Continuation<Void, Object>() {
                        @Override
                        public Object then(@NonNull Task<Void> task) throws Exception {
                            if (task.isSuccessful()) {
                                teacherDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        try {
                                            if (((ArrayList<String>) (documentSnapshot.get(fieldName[0]))).isEmpty()) {
                                                teacherDocRef.update(fieldName[0], FieldValue.delete()).continueWith(new Continuation<Void, Object>() {
                                                    @Override
                                                    public Object then(@NonNull Task<Void> task) throws Exception {
                                                        if (task.isSuccessful()) {
                                                            documentCleanCounter++;
                                                            doClean();
                                                        }
                                                        return null;
                                                    }
                                                });
                                            }
                                            else{
                                                teacherAlreadySelectedDocRef = null;
                                                fetchTeacherAlreadySelectedCourseTermSubjectData();
                                                Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "Subject deleted from your subject list", Toast.LENGTH_LONG).show();
                                                blockScreen(false);
                                            }
                                        } catch (Exception e) {
                                            Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "Subject deletion failed check network connectivity", Toast.LENGTH_LONG).show();
                                            blockScreen(false);
                                            Log.d("LOOOG", Log.getStackTraceString(e));
                                        }
                                    }
                                });
                            }
                            return null;
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Object>() {
                        @Override
                        public void onComplete(@NonNull Task<Object> task) {
                            if(!task.isSuccessful()) {
                                Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "Subject deletion failed, you may see", Toast.LENGTH_LONG).show();
                                blockScreen(false);
                            }

                        }
                    }).addOnFailureListener(teacherTakeSubject_TeacherSelectSubjectActivity.context, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "Subject deletion failed check network connectivity", Toast.LENGTH_LONG).show();
                            Log.d("LOOOG", Log.getStackTraceString(e));
                            blockScreen(false);
                        }
                    });
                }
                else {
                    teacherAlreadySelectedDocRef = null;
                    fetchTeacherAlreadySelectedCourseTermSubjectData();
                    Toast.makeText(teacherTakeSubject_TeacherSelectSubjectActivity.context, "Subject deleted from your subject list", Toast.LENGTH_LONG).show();
                    blockScreen(false);
                }
            }
        }
        DeleteTeacherSelectedSubjectDataInSequence uploadTeacherSubjectData = new DeleteTeacherSelectedSubjectDataInSequence(course, term, batch, subject);
        uploadTeacherSubjectData.start();
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
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (state) {
            inputBlockerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            inputBlockerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
