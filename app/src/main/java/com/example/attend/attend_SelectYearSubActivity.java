package com.example.attend;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class attend_SelectYearSubActivity extends AppCompatActivity {

    //    firebase database reference====
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    //    store given regular and admin teacher database information for take attendance====
    DocumentSnapshot teacherTakeAttendanceDocRef;

    //    current activity object====
    attend_SelectYearSubActivity context;

    //    stores which item is selected from attendance, course, term, subject, batch, year, month  and date spinners====
    String attendanceTypeSpinnerSelected, courseSpinnerSelected, termSpinnerSelected, subjectSpinnerSelected, batchSpinnerSelected;
    Integer yearSpinnerSelected, monthSpinnerSelected, dateSpinnerSelected;

    //    stores present date according to the phone systemdate====
    Integer calenderTodayYear, calenderTodayMonth, calenderTodayDay;

    //    array list which store items used to populate respective spinner====
    ArrayList<String> attendanceTypeSpinnerItems = new ArrayList<>();
    ArrayList<String> courseSpinnerItems = new ArrayList<>();
    ArrayList<String> termSpinnerItems = new ArrayList<>();
    ArrayList<String> subjectSpinnerItems = new ArrayList<>();
    ArrayList<String> batchSpinnerItems = new ArrayList<>();
    ArrayList<String> yearSpinnerItems = new ArrayList<>();
    ArrayList<String> monthSpinnerItems = new ArrayList<>();
    ArrayList<String> dateSpinnerItems = new ArrayList<>();

    //    Container used to set visibility on the basis of attendance type spinner selection====
    LinearLayoutCompat dateContainer, monthContainer, yearContainer, dateMonthYearContainer;

    //    All Spinner and Submit Button Reference====
    Spinner attendanceTypeSpinner, courseSpinner, termSpinner, subjectSpinner, batchSpinner;
    Spinner yearSpinner, calenderMonthSpinner, calenderDateSpinner;
    Button submitYearSubjectButton;

    //    store teacher id and reference come from intent used to fetch respective teacher data====
//    String TeacherId = "aniket000";
//    String TeacherRight = "admin";
//    String TeacherName = "aniket";
    TeacherDetailHolder teacherDetailHolder = TeacherDetailHolder.getTeacherDetailHolderReference();

    //    store course duration and subject information for admin when he wants to view attendance====
    HashMap<String, ArrayList<String>> adminCourseAndDurationMapForViewAttend = new HashMap<>();
    HashMap<String, ArrayList<String>> adminSubjectMapForViewAttend = new HashMap<>();

    //    used to store the last submit fetchCancelButton state====
    boolean submitButtonState = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attend_activity_select_year_sub);

//        Intent intent = getIntent();
//        String TeacherId = intent.getStringExtra("TeacherId");
//        String TeacherRight = intent.getStringExtra("Rights");
//        String TeacherName = intent.getStringExtra("Name");

//        String TeacherId = "aniket000";
//        String TeacherRight = "admin";
//        String TeacherName = "aniket";
//
//        teacherDetailHolder.setTeacherId(TeacherId);
//        teacherDetailHolder.setTeacherRight(TeacherRight);
//        teacherDetailHolder.setTeacherName(TeacherName);

        attendanceTypeSpinner = findViewById(R.id.attandanceTypeSpinner);
        courseSpinner = findViewById(R.id.courseSpinner);
        termSpinner = findViewById(R.id.termSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        batchSpinner = findViewById(R.id.batchSpinner);
        submitYearSubjectButton = findViewById(R.id.attendYearSubSubmitButton);

        dateContainer = findViewById(R.id.dateLayoutContainer);
        monthContainer = findViewById(R.id.monthLayoutContainer);
        yearContainer = findViewById(R.id.yearLayoutContainer);
        dateMonthYearContainer = findViewById(R.id.dateMonthYearSelectLayout);

        context = attend_SelectYearSubActivity.this;
        setDataFor_AttendanceType_Spinner();
        attendanceTypeSpinner.setOnItemSelectedListener(new attendanceTypeOnItemSelectListener());


//        request for external storage access permission to user
//        if (ActivityCompat.checkSelfPermission(attend_SelectYearSubActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(attend_SelectYearSubActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }

//        set fetchCancelButton State to false until initial data is not fetched from database
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDataFor_AttendanceType_Spinner();
        Log.d("Looog",""+submitButtonState);
        blockSubmitButton(submitButtonState);
    }

    //    this method called when user give response of application request for access external storage access permission if permission is not given then this activity finished
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission((Activity) attend_SelectYearSubActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    //    set list option on attendance type spinner and set adapter on it
    public void setDataFor_AttendanceType_Spinner() {
        if(attendanceTypeSpinnerItems == null | attendanceTypeSpinnerItems.isEmpty()) {
            attendanceTypeSpinnerItems = new ArrayList<>();
            attendanceTypeSpinnerItems.add("take attendance");
            attendanceTypeSpinnerItems.add("view attendance by date");
            attendanceTypeSpinnerItems.add("view attendance by month");
            attendanceTypeSpinner.setAdapter(new MySpinnerAdapter(context, attendanceTypeSpinnerItems));
        }
    }

    //   this method is called when user select the take attendance option in Attendance type spinner and it
//   is used to fetch Admin and regular teacher data for take attendance eg. for which course teacher teach, in which year which subject is taught by teacher etc
//   when data found it calls to teacherSelectedCourseTermSubjectDataFound() method
    public void fetchTeacherTakeAttendanceData() {
        courseSpinnerItems.clear();

        firebaseFirestore.collection("TEACHERS").document(teacherDetailHolder.getTeacherId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Toast.makeText(attend_SelectYearSubActivity.this, "TEACHER ID FOUND", Toast.LENGTH_SHORT).show();
                    teacherTakeAttendanceDocRef = documentSnapshot;
                    teacherTakeAttendanceDataFound();
                } else {
                    Toast.makeText(context, "NO TEACHER ID FOUND", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    //  this method is invoked by fetchTeacherSelectedCourseTermSubjectData() when Teacher Data is found for take Attendance
    @SuppressWarnings("unchecked")
    public void teacherTakeAttendanceDataFound() {
//        fetch a array list of course for which teacher teach
        courseSpinnerItems = (ArrayList<String>) teacherTakeAttendanceDocRef.get("course_name");
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

    //  called when teacher is admin and it fetch all courses and duration according to course
    public void fetchAdminViewAttendanceCourseName_DurationData() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ATTENDANCE")
                .document("CourseSubjectData")
                .collection("COURSE")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    adminCourseAndDurationMapForViewAttend = new HashMap<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            String courseName = Objects.requireNonNull(document.get("course_name")).toString();
                            Integer courseDuration = Integer.parseInt(Objects.requireNonNull(document.get("duration")).toString());
                            ArrayList<String> termList = new ArrayList<>();
                            for (Integer i = 1; i <= courseDuration; i++) {
                                termList.add(i.toString());
                            }
                            adminCourseAndDurationMapForViewAttend.put(courseName, termList);
                        } catch (NullPointerException e) {
                            Toast.makeText(context, "Error: fetchAdminViewAttendanceCourseName_DurationData: " + e, Toast.LENGTH_LONG).show();
                            Log.d("LOOOG", "at fetchAdminViewAttendanceCourseName_DurationData onComplete:" + Log.getStackTraceString(e));
                        }
                    }
                    fetchAdminViewAttendanceSubjectsData();
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

    //    called by fetchAdminViewAttendanceCourseName_DurationData() and this method fetch all the subject list in each course and term
    public void fetchAdminViewAttendanceSubjectsData() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("ATTENDANCE")
                .document("CourseSubjectData")
                .collection("SUBJECT")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    adminSubjectMapForViewAttend = new HashMap<>();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        try {
                            String courseName = Objects.requireNonNull(document.get("course_name")).toString();
                            String term = Objects.requireNonNull(document.get("term")).toString();
                            String courseName_Term = courseName + term;
                            String subject = Objects.requireNonNull(document.get("subject_name")).toString();
                            ArrayList<String> subjectList = adminSubjectMapForViewAttend.get(courseName_Term);

                            if (subjectList == null) {
                                ArrayList<String> temp_subject = new ArrayList<>();
                                temp_subject.add(subject);
                                adminSubjectMapForViewAttend.put(courseName_Term, temp_subject);
                            } else {
                                subjectList.add(subject);
                                adminSubjectMapForViewAttend.put(courseName_Term, subjectList);
                            }
                        } catch (NullPointerException e) {
                            Toast.makeText(context, "Error: fetchAdminViewAttendanceSubjectsData" + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("LOOOG", "at fetchAdminViewAttendanceSubjectsData onComplete:" + Log.getStackTraceString(e));
                        }
                    }
                    adminViewAttendanceCourseName_Duration_SubjectDataFound();
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

    //    called by allCourseName_Duration_SubjectDataFound() when all subject according to the course is found
    public void adminViewAttendanceCourseName_Duration_SubjectDataFound() {
        courseSpinnerItems = new ArrayList<>(adminCourseAndDurationMapForViewAttend.keySet());
        if (courseSpinnerItems.isEmpty()) {
            courseSpinnerItems.add("No course Found");
        }
        Collections.sort(courseSpinnerItems);
        courseSpinner.setAdapter(new MySpinnerAdapter(context, courseSpinnerItems));
    }

    public void blockSubmitButton(boolean buttonstate) {
        submitYearSubjectButton.setEnabled(!buttonstate);
        submitButtonState = buttonstate;
    }

    public void setDateMonthYearContainerVisibility(final int dateVisibility, final int monthVisibility, final int yearVisibility) {

        if (dateVisibility == View.INVISIBLE && monthVisibility == View.INVISIBLE && yearVisibility == View.INVISIBLE) {
            dateMonthYearContainer.animate().alpha(0).setDuration(50).scaleY(0).setDuration(300).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    dateMonthYearContainer.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else if (dateVisibility == View.VISIBLE && monthVisibility == View.VISIBLE && yearVisibility == View.VISIBLE) {
            dateMonthYearContainer.animate().alpha(1).setDuration(50).scaleY(1).setDuration(200).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    dateContainer.setVisibility(View.VISIBLE);
                    dateMonthYearContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else if (dateVisibility == View.INVISIBLE && monthVisibility == View.VISIBLE && yearVisibility == View.VISIBLE) {
            dateMonthYearContainer.animate().alpha(1).setDuration(50).scaleY(1).setDuration(200).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    dateContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    dateMonthYearContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

        }
    }

    public void populateYearSpinnerItem() {
        yearSpinner = findViewById(R.id.yearSpin);
        yearSpinnerItems.clear();
        calenderTodayYear = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.ENGLISH).format(Calendar.getInstance().getTime()));
        for (Integer i = 2019; i <= calenderTodayYear; i++) {
            yearSpinnerItems.add(i.toString());
        }
        yearSpinner.setAdapter(new MySpinnerAdapter(context, yearSpinnerItems));
        yearSpinner.setSelection(yearSpinnerItems.size() - 1);
        yearSpinner.setOnItemSelectedListener(new yearOnItemSelectListener());
    }

    class attendanceTypeOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            courseSpinner.setOnItemSelectedListener(new courseOnItemSelectListener());
            termSpinner.setOnItemSelectedListener(new termOnItemSelectListener());
            batchSpinner.setOnItemSelectedListener(new batchOnItemSelectListener());
            subjectSpinner.setOnItemSelectedListener(new subjectOnItemSelectListener());
            submitYearSubjectButton.setOnClickListener(new OnClick());

            blockSubmitButton(true);
            attendanceTypeSpinnerSelected = attendanceTypeSpinnerItems.get(i);
            courseSpinnerItems.clear();
            termSpinnerItems.clear();
            subjectSpinnerItems.clear();
            batchSpinnerItems.clear();
            courseSpinner.setAdapter(new MySpinnerAdapter(context, courseSpinnerItems));
            termSpinner.setAdapter(new MySpinnerAdapter(context, termSpinnerItems));
            subjectSpinner.setAdapter(new MySpinnerAdapter(context, termSpinnerItems));
            batchSpinner.setAdapter(new MySpinnerAdapter(context, termSpinnerItems));
            switch (i) {
                case 0:
                    attend_SelectYearSubActivity.this.fetchTeacherTakeAttendanceData();
                    attend_SelectYearSubActivity.this.setDateMonthYearContainerVisibility(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
                    break;
                case 1:
                    attend_SelectYearSubActivity.this.populateYearSpinnerItem();
                    if (teacherDetailHolder.getTeacherRight().equals("admin")) {
                        attend_SelectYearSubActivity.this.fetchAdminViewAttendanceCourseName_DurationData();
                    } else {
                        attend_SelectYearSubActivity.this.fetchTeacherTakeAttendanceData();
                    }
                    attend_SelectYearSubActivity.this.setDateMonthYearContainerVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE);
//                    request for external storage access permission to user
                    if (ActivityCompat.checkSelfPermission(attend_SelectYearSubActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(attend_SelectYearSubActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                    break;
                case 2:
                    attend_SelectYearSubActivity.this.populateYearSpinnerItem();
                    if (teacherDetailHolder.getTeacherRight().equals("admin")) {
                        attend_SelectYearSubActivity.this.fetchAdminViewAttendanceCourseName_DurationData();
                    } else {
                        attend_SelectYearSubActivity.this.fetchTeacherTakeAttendanceData();
                    }
                    attend_SelectYearSubActivity.this.setDateMonthYearContainerVisibility(View.INVISIBLE, View.VISIBLE, View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class courseOnItemSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            blockSubmitButton(true);
            try {
                courseSpinnerSelected = courseSpinnerItems.get(i);
                if (teacherDetailHolder.getTeacherRight().equals("regular") || teacherDetailHolder.getTeacherRight().equals("admin") && attendanceTypeSpinnerSelected.equals(attendanceTypeSpinnerItems.get(0))) {
                    termSpinnerItems = ((ArrayList<String>) teacherTakeAttendanceDocRef.get(courseSpinnerSelected));
                } else {
                    try {
                        termSpinnerItems = adminCourseAndDurationMapForViewAttend.get(courseSpinnerSelected);
                    } catch (NullPointerException e) {
                        Log.d("LOOOG", "at courseOnItemSelectListener:" + Log.getStackTraceString(e));
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                termSpinnerItems = null;
                Log.d("LOOOG", "at courseOnItemSelectListener:" + Log.getStackTraceString(e));
            }
            if (termSpinnerItems == null) {
                termSpinnerItems = new ArrayList<>();
                termSpinnerItems.add("No Year/Sem. Found");
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
            blockSubmitButton(true);
            termSpinnerSelected = termSpinnerItems.get(i);
            if (teacherDetailHolder.getTeacherRight().equals("regular") || teacherDetailHolder.getTeacherRight().equals("admin") && attendanceTypeSpinnerSelected.equals(attendanceTypeSpinnerItems.get(0))) {
                try {
                    batchSpinnerItems = (ArrayList<String>) teacherTakeAttendanceDocRef.get(courseSpinnerSelected + "" + termSpinnerSelected);
                } catch (IllegalArgumentException | ClassCastException e) {
                    batchSpinnerItems = null;
                    Toast.makeText(context, "No Batch found", Toast.LENGTH_SHORT).show();
                    Log.d("LOOOG", "Exception : No Batch found " + Log.getStackTraceString(e));
                }
            } else {  //if teacher is admin so it is assume that there are atmost 2 batch in each course term
                batchSpinnerItems.clear();
                batchSpinnerItems.add("1");
                batchSpinnerItems.add("2");
            }
            if (batchSpinnerItems == null) {
                batchSpinnerItems = new ArrayList<>();
                batchSpinnerItems.add("No Batch Found");
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
            blockSubmitButton(true);
            batchSpinnerSelected = batchSpinnerItems.get(i);
            if (teacherDetailHolder.getTeacherRight().equals("regular") || teacherDetailHolder.getTeacherRight().equals("admin") && attendanceTypeSpinnerSelected.equals(attendanceTypeSpinnerItems.get(0))) {
                try {
                    subjectSpinnerItems = (ArrayList<String>) teacherTakeAttendanceDocRef.get(courseSpinnerSelected + "" + termSpinnerSelected + "_" + batchSpinnerSelected);
                } catch (IllegalArgumentException | ClassCastException e) {
                    subjectSpinnerItems = null;
                    Toast.makeText(context, "No Subject found", Toast.LENGTH_SHORT).show();
                    Log.d("LOOOG", "Exception : No Subject found " + Log.getStackTraceString(e));
                }
            } else {
                subjectSpinnerItems = adminSubjectMapForViewAttend.get(courseSpinnerSelected + termSpinnerSelected);
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
//    class termOnItemSelectListener implements AdapterView.OnItemSelectedListener {
//        @Override
//        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//            onTermSpinnerSelected = termSpinnerItems.get(i);
//            if (TeacherRight.equals("regular") || TeacherRight.equals("admin") && attendanceTypeSpinnerSelected.equals(attendanceTypeSpinnerItems.get(0))) {
//                try {
//                    subjectSpinnerItems = (ArrayList<String>) TeacherDocRef.get(onCourseSpinnerSelected + "" + onTermSpinnerSelected);
//                } catch (ClassCastException e) {
//                    subjectSpinnerItems = null;
//                    Toast.makeText(context, "No Subject found", Toast.LENGTH_SHORT).show();
//                    Log.d("LOOOG", "Exception : No Subject found " + Log.getStackTraceString(e));
//                }
//            } else {
//                subjectSpinnerItems = adminSubjectMapForViewAttend.get(onCourseSpinnerSelected + onTermSpinnerSelected);
//            }
//            batchSpinnerItems.clear();
//            if (subjectSpinnerItems == null) {
//                batchSpinnerItems.add("No Batch Found");
//                subjectSpinnerItems = new ArrayList<>();
//                subjectSpinnerItems.add("No Subject Found");
//                blockSubmitButton(false);
//
//            } else {
//                batchSpinnerItems.add("1");
//                batchSpinnerItems.add("2");
//                blockSubmitButton(true);
//            }
//            batchSpinner.setAdapter(new MySpinnerAdapter(context, batchSpinnerItems));
//            Collections.sort(subjectSpinnerItems);
//            subjectSpinner.setAdapter(new MySpinnerAdapter(context, subjectSpinnerItems));
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> adapterView) {
//
//        }
//    }
//
//    class batchOnItemSelectListener implements AdapterView.OnItemSelectedListener {
//
//        @Override
//        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//            batchSpinnerSelected = batchSpinnerItems.get(i);
//
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> adapterView) {
//
//        }
//    }

    class subjectOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            subjectSpinnerSelected = subjectSpinnerItems.get(i);
            if (!subjectSpinnerSelected.equalsIgnoreCase("No Subject Found")) {
                blockSubmitButton(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class yearOnItemSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            calenderMonthSpinner = findViewById(R.id.monthSpin);
            yearSpinnerSelected = Integer.parseInt(yearSpinnerItems.get(i));
            monthSpinnerItems.clear();
            if (yearSpinnerSelected.equals(calenderTodayYear)) {
                calenderTodayMonth = Integer.parseInt(new SimpleDateFormat("M", Locale.ENGLISH).format(Calendar.getInstance().getTime()));
                for (Integer j = 1; j <= calenderTodayMonth; j++) {
                    monthSpinnerItems.add(j.toString());
                }
                calenderMonthSpinner.setAdapter(new MySpinnerAdapter(context, monthSpinnerItems));
                calenderMonthSpinner.setSelection(monthSpinnerItems.size() - 1);
            } else {
                for (Integer j = 1; j <= 12; j++) {
                    monthSpinnerItems.add(j.toString());
                }
                calenderMonthSpinner.setAdapter(new MySpinnerAdapter(context, monthSpinnerItems));
            }

            calenderMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    calenderDateSpinner = findViewById(R.id.dateSpin);
                    monthSpinnerSelected = Integer.parseInt(monthSpinnerItems.get(i));
                    dateSpinnerItems.clear();

                    if (monthSpinnerSelected.equals(calenderTodayMonth) && yearSpinnerSelected.equals(calenderTodayYear)) {
                        calenderTodayDay = Integer.parseInt(new SimpleDateFormat("d", Locale.ENGLISH).format(Calendar.getInstance().getTime()));
                        for (Integer j = 1; j <= calenderTodayDay; j++) {
                            dateSpinnerItems.add(j.toString());
                        }
                        calenderDateSpinner.setAdapter(new MySpinnerAdapter(context, dateSpinnerItems));
                        calenderDateSpinner.setSelection(dateSpinnerItems.size() - 1);
                    } else {
                        Integer dayInMonth = new GregorianCalendar(yearSpinnerSelected, monthSpinnerSelected - 1, 1).getActualMaximum(Calendar.DAY_OF_MONTH);
                        // YearMonth yearMonth = YearMonth.of(yearSpinnerSelected , monthSpinnerSelected);
                        for (Integer j = 1; j <= dayInMonth; j++) {
                            dateSpinnerItems.add(j.toString());
                        }
                        calenderDateSpinner.setAdapter(new MySpinnerAdapter(context, dateSpinnerItems));
                    }

                    calenderDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            dateSpinnerSelected = Integer.parseInt(dateSpinnerItems.get(i));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class OnClick implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View view) {
            if (!isNetworkConnected()) {
                Toast.makeText(context, "Check Internet Connection", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!subjectSpinnerItems.contains(subjectSpinnerSelected)){
                Toast.makeText(context, "Subject not selected", Toast.LENGTH_SHORT).show();
                return;
            }

            final ProgressBar progressBar = findViewById(R.id.yearSubSubmitProgressBar);


            if (attendanceTypeSpinnerSelected.equals(attendanceTypeSpinnerItems.get(0))) {
                blockSubmitButton(true);      //resist user to click on fetchCancelButton two time
                progressBar.setVisibility(View.VISIBLE);
                blockScreen(true);


                Intent AttandActivityIntent = new Intent(attend_SelectYearSubActivity.this, attend_TakeAttandanceActivity.class);
                AttandActivityIntent.putExtra("course", courseSpinnerSelected);
                AttandActivityIntent.putExtra("term", termSpinnerSelected);
                AttandActivityIntent.putExtra("batch", batchSpinnerSelected);
                AttandActivityIntent.putExtra("subject", subjectSpinnerSelected);
                attend_SelectYearSubActivity.this.startActivity(AttandActivityIntent);
                progressBar.setVisibility(View.GONE);
                blockScreen(false);
                submitButtonState = false; //make submit fetchCancelButton clickable when user come back after take attendance without this submit fetchCancelButton remains disable
                //finish();
            } else if (attendanceTypeSpinnerSelected.equals(attendanceTypeSpinnerItems.get(1))) {
                final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

                class WantExcelTypeDocument implements DialogInterface.OnClickListener {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blockSubmitButton(true);      //resist user to click on fetchCancelButton two time
                        progressBar.setVisibility(View.VISIBLE);
                        blockScreen(true);

                        final String fileName = courseSpinnerSelected + "_" + termSpinnerSelected + "_" + batchSpinnerSelected + "_" + subjectSpinnerSelected + ".xls";
                        String path = "ATTENDANCE/" + yearSpinnerSelected + "/" + monthSpinnerSelected + "/" + dateSpinnerSelected + "/excel/" + fileName;
                        StorageReference storageReference = firebaseStorage.getReference(path);
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    final Uri excelUri = task.getResult();
                                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                    DownloadManager.Request request = new DownloadManager.Request(excelUri);
                                    request.setTitle(fileName);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    final String fileName = courseSpinnerSelected + "_" + termSpinnerSelected + "_" + batchSpinnerSelected + "_" + subjectSpinnerSelected + "_" + System.currentTimeMillis() + ".xls";
                                    // request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS ,fileName);
                                    final File excelDestination = new File(attend_SelectYearSubActivity.this.getExternalFilesDir("Excel") + "/" + dateSpinnerSelected + "_" + monthSpinnerSelected + "_" + yearSpinnerSelected, fileName);
                                    request.setDestinationUri(Uri.fromFile(excelDestination));
                                    downloadManager.enqueue(request);

                                    BroadcastReceiver onComplete = new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
//                                            File file = new File(attend_SelectYearSubActivity.this.getFilesDir().toString());
//                                            File newFile = new File(file, fileName);
                                            Uri uri = FileProvider.getUriForFile(attend_SelectYearSubActivity.this.context, context.getApplicationContext().getPackageName() + ".fileprovider", excelDestination);
                                            Intent intentt = new Intent();
                                            intentt.setAction(Intent.ACTION_VIEW);
                                            intentt.setDataAndType(uri, "text/xml");
                                            intentt.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            startActivityForResult(intentt, -1);
                                            progressBar.setVisibility(View.GONE);
                                            blockScreen(false);
                                            //Log.d("LOOOG", file.getPath());
                                            blockSubmitButton(false);
                                        }
                                    };
                                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)); //register method call after download complete
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    blockScreen(false);
                                    blockSubmitButton(false);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                blockScreen(false);
                                blockSubmitButton(false);
                                Toast.makeText(context, "No Attendance Sheet Found", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                class WantPdfTypeDocument implements DialogInterface.OnClickListener {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        blockSubmitButton(true);      //resist user to click on fetchCancelButton two time
                        progressBar.setVisibility(View.VISIBLE);
                        blockScreen(true);
                        final String fileName = courseSpinnerSelected + "_" + termSpinnerSelected + "_" + batchSpinnerSelected + "_" + subjectSpinnerSelected + ".pdf";
                        String path = "ATTENDANCE/" + yearSpinnerSelected + "/" + monthSpinnerSelected + "/" + dateSpinnerSelected + "/pdf/" + fileName;
                        StorageReference storageReference = firebaseStorage.getReference(path);

                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    final Uri imageUri = task.getResult();
                                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                    DownloadManager.Request request = new DownloadManager.Request(imageUri);
                                    request.setTitle(fileName);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    final String fileName = courseSpinnerSelected + "_" + termSpinnerSelected + "_" + batchSpinnerSelected + "_" + subjectSpinnerSelected + "_" + System.currentTimeMillis() + ".pdf";
                                    // request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS ,fileName);
                                    final File ref = new File(attend_SelectYearSubActivity.this.getExternalFilesDir("Pdf") + "/" + dateSpinnerSelected + "_" + monthSpinnerSelected + "_" + yearSpinnerSelected, fileName);
                                    request.setDestinationUri(Uri.fromFile(ref));
                                    downloadManager.enqueue(request);

                                    BroadcastReceiver onComplete = new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            File file = new File(attend_SelectYearSubActivity.this.getFilesDir().toString());
                                            //File newFile = new File(file, fileName);
                                            Uri uri = FileProvider.getUriForFile(attend_SelectYearSubActivity.this.context, context.getApplicationContext().getPackageName() + ".fileprovider", ref);
                                            Intent intentt = new Intent();
                                            intentt.setAction(Intent.ACTION_VIEW);
                                            intentt.setDataAndType(uri, "application/pdf");
                                            intentt.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            startActivityForResult(intentt, -1);
                                            progressBar.setVisibility(View.GONE);
                                            blockScreen(false);
                                            //Log.d("LOOOG", file.getPath());
                                            blockSubmitButton(false);
                                        }
                                    };
                                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    blockScreen(false);
                                    blockSubmitButton(false);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                blockScreen(false);
                                blockSubmitButton(false);
                                Toast.makeText(context, "No Attendance Sheet Found", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                AlertDialog.Builder documentTypeAlert = new AlertDialog.Builder(attend_SelectYearSubActivity.this)
                        .setMessage("Which type of document you want?")
                        .setCancelable(true)
                        .setNegativeButton("Pdf", new WantPdfTypeDocument())
                        .setPositiveButton("Excel", new WantExcelTypeDocument());

                AlertDialog askForDocumentType = documentTypeAlert.create();
                askForDocumentType.setTitle("Document Type");
                askForDocumentType.show();
            } else if (attendanceTypeSpinnerSelected.equals(attendanceTypeSpinnerItems.get(2))) {
                blockSubmitButton(true);      //resist user to click on fetchCancelButton two time
                progressBar.setVisibility(View.VISIBLE);
                blockScreen(true);

                Intent monthTotalAttendActivityIntent = new Intent(attend_SelectYearSubActivity.this, attend_ShowMonthTotalAttendActivity.class);
                monthTotalAttendActivityIntent.putExtra("course", courseSpinnerSelected);
                monthTotalAttendActivityIntent.putExtra("term", termSpinnerSelected);
                monthTotalAttendActivityIntent.putExtra("batch", batchSpinnerSelected);
                monthTotalAttendActivityIntent.putExtra("subject", subjectSpinnerSelected);
                monthTotalAttendActivityIntent.putExtra("month", monthSpinnerSelected.toString());
                monthTotalAttendActivityIntent.putExtra("year", yearSpinnerSelected.toString());
                startActivity(monthTotalAttendActivityIntent);
                progressBar.setVisibility(View.GONE);
                blockScreen(false);
                blockSubmitButton(false);
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

        private void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
            View inputBlockerView = findViewById(R.id.inputBlockerView);

            if (state) {
                inputBlockerView.setVisibility(View.VISIBLE);
            } else {
                inputBlockerView.setVisibility(View.GONE);
            }
        }
    }
}


////class AdminDaySummaryAttendance {
////    ArrayList<String>subjects = new ArrayList<>();
////    public void get(String day, String month, String year, String course, String term, String batch, final Context context){
////        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
////        firebaseFirestore.collection("SUBJECT")
////                .whereEqualTo("course_name", course)
////                .whereEqualTo("term",term )
////                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
////            @Override
////            public void onComplete(@NonNull Task<QuerySnapshot> task) {
////                if(task.isSuccessful()){
////                    for(DocumentSnapshot documentSnapshot : task.getResult()){
////                        if(documentSnapshot!=null){
////                            subjects.add(documentSnapshot.get("subject_name").toString());
////                        }
////                    }
////                }
////            }
////        });
////        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
////        StorageReference reference = firebaseStorage.getReference("ATTENDANCE/"+ 2020 +"/"+ 1 +"/"+ 8 +"/excel/");
////    }
//}


