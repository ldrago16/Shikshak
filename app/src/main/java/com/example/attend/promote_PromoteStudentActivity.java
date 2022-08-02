package com.example.attend;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class promote_PromoteStudentActivity extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    PromoteStudentProgrammingAdapter programmingAdapter;
    RecyclerView recyclerView;

    static TextView totalPromoteTextView, totalDeleteTextView;

    TextView promoteStudentTextView, deleteStudentTextView;

    ArrayList<String> courseArrayList = new ArrayList<>();
    ArrayList<String> termArrayList = new ArrayList<>();

    ArrayList<String> studentNameList = new ArrayList<>();
    ArrayList<String> studentIdList = new ArrayList<>();
    ArrayList<String> studentFatherNameList = new ArrayList<>();
    ArrayList<String> studentMobileNoList = new ArrayList<>();

    HashMap<String, ArrayList<String>> courseTermMap = new HashMap<>();
    String courseSpinnerSelected, termSpinnerSelected;
    Spinner courseSpinner, termSpinner;
    static promote_PromoteStudentActivity context;
    Button fetchCancelButton, submitPromoteDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promote_activity_promote_student);
        context = promote_PromoteStudentActivity.this;

//        { // student name update in studentNameData document
//            firebaseFirestore.collection("STUDENT").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                    HashMap<String, String> data = new HashMap<>();
//                    for (QueryDocumentSnapshot snap : task.getResult()) {
//                        try {
//                            String studName = snap.get("student_name").toString();
//                            data.put(studName, "");
//                        } catch (NullPointerException e) {
//
//                        }
//                    }
//                    firebaseFirestore.collection("STUDENT").document("StudentNameData").set(data);
//                }
//            });
//        }


        fetchCourseAndTermData();

        fetchCancelButton = findViewById(R.id.promoteStudentFetchSubmitButton);
        fetchCancelButton.setOnClickListener(new onFetchCancelButtonClick());

        promoteStudentTextView = findViewById(R.id.promoteStudentTextView);
        promoteStudentTextView.setOnClickListener(new onPromoteOrDeleteTextViewClick());
        deleteStudentTextView = findViewById(R.id.deleteStudentTextView);
        deleteStudentTextView.setOnClickListener(new onPromoteOrDeleteTextViewClick());

        totalPromoteTextView = context.findViewById(R.id.totalPromoteCheckedTextView);
        totalDeleteTextView = context.findViewById(R.id.totalDeleteCheckedTextView);

        submitPromoteDeleteButton = findViewById(R.id.promoteDeleteStudentButton);
        submitPromoteDeleteButton.setOnClickListener(new onSubmitPromoteDeleteButtonClick());
    }

    void fetchCourseAndTermData() {
        blockScreen(true);
        firebaseFirestore.collection("COURSE")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshots : task.getResult()) {
                                if (!snapshots.exists())
                                    continue;

                                String courseName = snapshots.get("course_name").toString();
                                Integer duration = Integer.parseInt(snapshots.get("duration").toString());

                                ArrayList<String> tempTerm = new ArrayList<>();
                                for (int i = 1; i <= duration; i++) {
                                    tempTerm.add(String.valueOf(i));
                                }
                                courseTermMap.put(courseName, tempTerm);
                            }
                            courseTermDataFetched();
                        }
                    }
                });

    }

    void courseTermDataFetched() {
        blockScreen(false);

        courseSpinner = findViewById(R.id.selectCourseSpinner);
        termSpinner = findViewById(R.id.selectTermSpinner);

        if (courseTermMap.isEmpty()) {
            courseArrayList.add("no course found");
        } else {
            courseSpinner.setOnItemSelectedListener(new onCourseSpinnerSelected());
            termSpinner.setOnItemSelectedListener(new onTermSpinnerSelected());
            courseArrayList.addAll(courseTermMap.keySet());
        }


        courseSpinner.setAdapter(new MySpinnerAdapter(context, courseArrayList));

    }

    class onCourseSpinnerSelected implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            courseSpinnerSelected = courseArrayList.get(position);
            termArrayList = courseTermMap.get(courseSpinnerSelected);
            termSpinner.setAdapter(new MySpinnerAdapter(context, termArrayList));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class onTermSpinnerSelected implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            termSpinnerSelected = termArrayList.get(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    class onFetchCancelButtonClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (fetchCancelButton.getText().toString().equals("fetch")) {
                if (courseSpinnerSelected==null && termSpinnerSelected==null) {
                    Toast.makeText(context, "Invalid selection", Toast.LENGTH_LONG).show();
                } else {
                    blockScreen(true);
                    fetchStudentData(courseSpinnerSelected, termSpinnerSelected);
                }

            } else {
                AlertDialog.Builder cancelAlert = new AlertDialog.Builder(context)
                        .setTitle("Confirm Cancellation")
                        .setMessage("Do you really want to cancel");

                cancelAlert
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearRecycleViewAndShowCourseTermSpinner();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();

            }
        }
    }

    void clearRecycleViewAndShowCourseTermSpinner() {
        clearRecyclerView();
        submitPromoteDeleteButton.setEnabled(false);
        setTotalPromoteDeleteCount(0, 0);
        setCourseTermSpinnerContainerVisibilityAndFetchButtonText(View.VISIBLE);
    }

    class onPromoteOrDeleteTextViewClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.promoteStudentTextView) {
                programmingAdapter.setPromoteOrDeleteAllCheck("promote");
            } else {
                programmingAdapter.setPromoteOrDeleteAllCheck("delete");
            }
        }
    }

    class onSubmitPromoteDeleteButtonClick implements View.OnClickListener {

        CollectionReference studCollectionRef = firebaseFirestore.collection("STUDENT");
        CollectionReference attendCollectionRef = firebaseFirestore.collection("ATTENDANCE");

        @Override
        public void onClick(View v) {

            if (!isNetworkConnected()) {
                Toast.makeText(context, "Check Internet Connection", Toast.LENGTH_LONG).show();
                return;
            }
            AlertDialog.Builder promoteConfirmAlert = new AlertDialog.Builder(context)
                    .setTitle("WARNING")
                    .setMessage("changes cannot be revert back.\n")
                    .setPositiveButton("conform", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String studTerm = termSpinnerSelected;
                            String maxDuration = String.valueOf(termArrayList.size());
                            String numOfPromoteStudent = totalPromoteTextView.getText().toString();
                            Log.d("LOOOG", "NUM " + numOfPromoteStudent);
                            if (studTerm.equals(maxDuration) || numOfPromoteStudent.equals("0")) {
                                finalUpdate();
                            } else {  // check if student are in the next year else current student data mix with next year student data
                                final String justNextTerm = String.valueOf(Integer.parseInt(studTerm) + 1);
                                blockScreen(true);

                                studCollectionRef
                                        .whereEqualTo("course_name", courseSpinnerSelected)
                                        .whereEqualTo("term", justNextTerm)
                                        .get(Source.SERVER)
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                Log.d("LOOOG", "ookk 1");
                                                if (task.isSuccessful()) {
                                                    Log.d("LOOOG", "ookk 2");
                                                    int numberOfStudInNextTerm = task.getResult().size();
                                                    if (numberOfStudInNextTerm > 0) {
                                                        Log.d("LOOOG", "ookk 3 " + numberOfStudInNextTerm);
                                                        AlertDialog.Builder alreadyStudentAlert = new AlertDialog.Builder(context)
                                                                .setTitle("WARNING")
                                                                .setMessage("Already " + numberOfStudInNextTerm + " Student in " + courseSpinnerSelected + " " + justNextTerm + ".\nIf you continue both year or semester student will be merged.\nDo you still want to promote.")
                                                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        finalUpdate();
                                                                    }
                                                                })
                                                                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.cancel();
                                                                    }
                                                                });

                                                        alreadyStudentAlert.create().show();
                                                    } else {
                                                        finalUpdate();
                                                    }
                                                }
                                                blockScreen(false);
                                            }
                                        })
                                        .addOnFailureListener(context, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Looog", Log.getStackTraceString(e));
                                                blockScreen(false);
                                                Toast.makeText(context, "NoInternetConnection", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }

                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            promoteConfirmAlert.create().show();
        }

        void setCompletionYearAndDeleteTermFieldFromCollection(String studentId, String year) {

            {// updates in StudentCollection
                DocumentReference studDocumentRef = studCollectionRef.document(studentId);

                studDocumentRef
                        .update("completion_year", year);
                studDocumentRef
                        .update("term", FieldValue.delete());
            }

            {// updates in AttendanceCollection
                DocumentReference attendDocumentRef = attendCollectionRef.document(studentId);
                attendDocumentRef
                        .update("completion_year", year);

                attendDocumentRef
                        .update("term", FieldValue.delete());

            }
        }

        void finalUpdate() {
            Toast.makeText(context, "uploading...", Toast.LENGTH_SHORT).show();
            blockScreen(true);
            String registrationYear = getPresentYear();
            if (registrationYear == null) {
                Toast.makeText(context, "TimeFetchingError: NoInternetConnection", Toast.LENGTH_LONG).show();
                return;
            }

            String duration = termSpinnerSelected;
            String maxDuration = String.valueOf(termArrayList.size());
            HashMap<Integer, Integer> checkBoxMap = programmingAdapter.getCheckedCheckBoxMap();
            ArrayList<String> idList = programmingAdapter.getStudentIdList();

            for (int i = 0; i < idList.size(); i++) {
                try {
                    int checkBoxId = checkBoxMap.get(i);
                    String studentId = idList.get(i);

                    if (checkBoxId == R.id.promoteStudentCheckBox) {
                        if (duration.equals(maxDuration)) {   //student is belong to final year and his completion year is to be set and delete term field;
                            setCompletionYearAndDeleteTermFieldFromCollection(studentId, registrationYear);
                        } else {  //student is not in final year so just increment the term
                            studCollectionRef.document(studentId)
                                    .update("term", String.valueOf(Integer.parseInt(termSpinnerSelected) + 1));
                        }
                    } else if (checkBoxId == R.id.deleteStudentCheckBox) {
                        setCompletionYearAndDeleteTermFieldFromCollection(studentId, registrationYear);
                    }
                } catch (NullPointerException e) {
                }
            }

            blockScreen(false);
            Toast.makeText(context, "Data Uploaded", Toast.LENGTH_SHORT).show();
            clearRecycleViewAndShowCourseTermSpinner();
        }
    }

    private boolean isNetworkConnected() {

        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED | connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        }
        return connected;
//            //ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            final String command = "ping -c 1 google.com";
//            try {
//                return Runtime.getRuntime().exec(command).waitFor() == 0;
//            } catch (InterruptedException e) {
//                return false;
//            } catch (IOException e) {
//                return false;
//            }
    }

    private String getPresentYear() {
        MyCalendar myCalendar = new MyCalendar();
        String registrationYear = null;
        try {
            myCalendar.start();
            myCalendar.join();
            registrationYear = myCalendar.getYear();
        } catch (Exception e) {
            return null;
        }
        return registrationYear;
    }

    void fetchStudentData(final String course, String term) {
        firebaseFirestore.collection("STUDENT")
                .whereEqualTo("course_name", course)
                .whereEqualTo("term", term)
                .whereEqualTo("completion_year", "pursuing")
                .orderBy("student_name")
                .orderBy("student_father_name")
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        try {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                LinkedHashMap<String, String> tempSortedStudentId_NameMap = new LinkedHashMap<>();
                                ArrayList<String> tempStudentFatherNameList = new ArrayList<>();
                                ArrayList<String> tempStudentMobileNoList = new ArrayList<>();

                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                                    String id = documentSnapshot.getId();
                                    String name = documentSnapshot.get("student_name").toString();
                                    String fatherName = documentSnapshot.get("student_father_name").toString();
                                    String mobileNo = documentSnapshot.get("student_mobile").toString();

                                    tempStudentFatherNameList.add(fatherName);
                                    tempStudentMobileNoList.add(mobileNo);

                                    tempSortedStudentId_NameMap.put(id, name);
                                }

                                ArrayList<String> studentiddata = new ArrayList<>(tempSortedStudentId_NameMap.keySet());
                                ArrayList<String> studentnamedata = new ArrayList<>(tempSortedStudentId_NameMap.values());

                                context.studentIdList = studentiddata;
                                context.studentNameList = studentnamedata;
                                context.studentFatherNameList = tempStudentFatherNameList;
                                context.studentMobileNoList = tempStudentMobileNoList;
                                context.populateRecyclerView();

                            } else {
                                Toast.makeText(context, "No Student Found", Toast.LENGTH_LONG).show();
                            }
                        } catch (NullPointerException e) {
                            Toast.makeText(context, "Error: in fetching student in success", Toast.LENGTH_LONG).show();
                            Log.d("LOOOG", "at fetchStudentData :" + Log.getStackTraceString(e));

                        }
                        blockScreen(false);
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("LOOOG", "failed" + Log.getStackTraceString(e));
                    }
                });
    }

    void populateRecyclerView() {
        setTotalPromoteDeleteCount(0, 0);
        submitPromoteDeleteButton.setEnabled(true);
        setCourseTermSpinnerContainerVisibilityAndFetchButtonText(View.GONE);
        recyclerView = findViewById(R.id.promoteDeleteStudentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        programmingAdapter = new PromoteStudentProgrammingAdapter(context, studentNameList, studentFatherNameList, studentMobileNoList, studentIdList);
        recyclerView.setAdapter(programmingAdapter);
    }

    void clearRecyclerView() {
        recyclerView.setAdapter(null);
    }

    static void setTotalPromoteDeleteCount(int promoteCount, int deleteCount) {

        totalPromoteTextView.setText(String.valueOf(promoteCount));
        totalDeleteTextView.setText(String.valueOf(deleteCount));
    }

    void setCourseTermSpinnerContainerVisibilityAndFetchButtonText(final int visibility) {
        final LinearLayoutCompat courseTermSpinnerContainer = findViewById(R.id.promoteStudentSelectCourseTermContainer);
        int alpha = visibility == View.VISIBLE ? 1 : 0;
        int translation = visibility == View.VISIBLE ? 0 : -500;
        int duration = visibility == View.VISIBLE ? 0 : 250;
        courseTermSpinnerContainer.animate().translationY(translation).setDuration(duration).alpha(alpha).setDuration(duration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                courseTermSpinnerContainer.setVisibility(visibility);
                fetchCancelButton.setText(visibility == View.GONE ? "cancel" : "fetch");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

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

class PromoteStudentProgrammingAdapter extends RecyclerView.Adapter<PromoteStudentProgrammingAdapter.PromoteStudentProgViewHolder> {
    Context context;
    private ArrayList<String> studentNameList, studentFatherNameList, studentMobileNoList, studentIdList;   //store students names
    private HashMap<Integer, Integer> checkedCheckBoxMap = new HashMap<>();
    android.os.Handler handler = new Handler(Looper.myLooper());
    CheckCount checkCount = new CheckCount();
    private boolean promoteAllCheck = false, deleteAllCheck = false;

    PromoteStudentProgrammingAdapter(Context context, ArrayList<String> tempStudentNameList, ArrayList<String> tempStudentFatherNameList, ArrayList<String> tempStudentMobileNoList, ArrayList<String> tempStudentIdList) {
        this.context = context;
        this.studentNameList = tempStudentNameList;
        this.studentFatherNameList = tempStudentFatherNameList;
        this.studentMobileNoList = tempStudentMobileNoList;
        this.studentIdList = tempStudentIdList;

    }

    public HashMap<Integer, Integer> getCheckedCheckBoxMap() {
        return checkedCheckBoxMap;
    }

    public ArrayList<String> getStudentIdList() {
        return studentIdList;
    }

    @NonNull
    @Override
    public PromoteStudentProgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.promote_student_promote_adapter_layout, parent, false);
        return new PromoteStudentProgViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return studentIdList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final PromoteStudentProgViewHolder holder, final int position) {
        holder.srNo.setText("" + (position + 1));
        final String stud_Name = studentNameList.get(position);
        final String stud_Father = studentFatherNameList.get(position);
        final String stud_Id = studentIdList.get(position);

        holder.studentName.setText(wordCaseConvert(stud_Name));

        setCheckBoxCheckedState(holder, position);

        holder.promoteCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.promoteCheck.isChecked()) {
                    holder.deleteCheck.setChecked(false);
                }
                setCheckedCheckBoxMapInfo(position, holder.promoteCheck);
            }
        });

        holder.deleteCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.deleteCheck.isChecked()) {
                    holder.promoteCheck.setChecked(false);
                }
                setCheckedCheckBoxMapInfo(position, holder.deleteCheck);
            }
        });

        holder.studentName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(holder.studentName.getContext());
                android.app.AlertDialog alert = alertDialogBuilder.create();
                alert.setCancelable(true);
                alert.setIcon(android.R.drawable.ic_menu_info_details);
                alert.setTitle("Student Information.");
                alert.setMessage("Student Id = " + stud_Id + "\nStudent name = " + stud_Name + "\nFather's name = " + stud_Father + "\nMobile no. = " + studentMobileNoList.get(position));
                alert.show();
            }
        });

    }

    void setPromoteOrDeleteAllCheck(String allCheckItemName) {
        Integer id = null;
        if (allCheckItemName.equals("promote")) {
            if (promoteAllCheck) {
                id = null;
                promoteAllCheck = false;
            } else {
                id = R.id.promoteStudentCheckBox;
                promoteAllCheck = true;
                deleteAllCheck = false;
            }
        } else if (allCheckItemName.equals("delete")) {
            if (deleteAllCheck) {
                id = null;
                deleteAllCheck = false;
            } else {
                id = R.id.deleteStudentCheckBox;
                deleteAllCheck = true;
                promoteAllCheck = false;
            }
        }

        for (int i = 0; i < getItemCount(); i++) {
            checkedCheckBoxMap.put(i, id);
        }
        notifyDataSetChanged();
        runCheckBoxCountThread();
    }

    void setCheckedCheckBoxMapInfo(int position, CheckBox checkBox) {


        if (checkBox.isChecked()) {
            checkedCheckBoxMap.put(position, checkBox.getId());
        } else {
            checkedCheckBoxMap.put(position, null);
        }
        runCheckBoxCountThread();
//        handler.postDelayed(new CheckCount(), 500);

    }

    void runCheckBoxCountThread() {
        checkCount.cancel = true;
        checkCount = new CheckCount();
        checkCount.start();
    }

    void setCheckBoxCheckedState(PromoteStudentProgViewHolder holder, int position) {

        try {
            if (holder.promoteCheck.getId() == checkedCheckBoxMap.get(position)) {
                holder.promoteCheck.setChecked(true);
                holder.deleteCheck.setChecked(false);
            } else if (holder.deleteCheck.getId() == checkedCheckBoxMap.get(position)) {
                holder.promoteCheck.setChecked(false);
                holder.deleteCheck.setChecked(true);
            }
        } catch (NullPointerException e) {
            holder.promoteCheck.setChecked(false);
            holder.deleteCheck.setChecked(false);
        }
    }


    class PromoteStudentProgViewHolder extends RecyclerView.ViewHolder {
        TextView srNo, studentName;
        CheckBox promoteCheck, deleteCheck;

        public PromoteStudentProgViewHolder(@NonNull View itemView) {
            super(itemView);
            srNo = itemView.findViewById(R.id.studentSrNo);
            studentName = itemView.findViewById(R.id.promoteStudentNameInfoTextView);
            promoteCheck = itemView.findViewById(R.id.promoteStudentCheckBox);
            deleteCheck = itemView.findViewById(R.id.deleteStudentCheckBox);
        }
    }

    public String wordCaseConvert(String tempName) {
        String[] nameArray = tempName.split(" ");
        String realNameWordCaseConverted = "";
        for (int i = 0; i < nameArray.length; i++) {
            nameArray[i] = (nameArray[i].substring(0, 1).toUpperCase()) + nameArray[i].substring(1);
            realNameWordCaseConverted = realNameWordCaseConverted + nameArray[i] + " ";
        }
        return realNameWordCaseConverted.trim();
    }

    class CheckCount extends Thread {
        boolean cancel = false;

        @Override
        public void run() {
            int promoteCount = 0;
            int deleteCount = 0;

            for (int positions : checkedCheckBoxMap.keySet()) {
                if (cancel)
                    return;
                try {
                    if (checkedCheckBoxMap.get(positions) == R.id.promoteStudentCheckBox) {
                        promoteCount++;
                    } else {
                        deleteCount++;
                    }
                } catch (NullPointerException e) {
                }
            }

//            for (int i = 0; i < checkedCheckBoxMap.size(); i++) {
//                if(cancel)
//                    return;
//                try {
//                    if (checkedCheckBoxMap.get(i) == R.id.promoteStudentCheckBox) {
//                        promoteCount++;
//                    } else {
//                        deleteCount++;
//                    }
//                } catch (NullPointerException e) {
//                }
//            }
            promote_PromoteStudentActivity.setTotalPromoteDeleteCount(promoteCount, deleteCount);
        }
    }
}
