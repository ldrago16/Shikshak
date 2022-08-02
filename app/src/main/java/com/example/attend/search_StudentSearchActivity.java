package com.example.attend;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.itextpdf.text.pdf.StringUtils;

import org.apache.poi.util.StringUtil;

import java.util.ArrayList;
import java.util.Set;

class Student {
    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getCompletion_year() {
        return completion_year;
    }

    public void setCompletion_year(String completion_year) {
        this.completion_year = completion_year;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getRegistration_year() {
        return registration_year;
    }

    public void setRegistration_year(String registration_year) {
        this.registration_year = registration_year;
    }

    public String getStudent_email() {
        return student_email;
    }

    public void setStudent_email(String student_email) {
        this.student_email = student_email;
    }

    public String getStudent_father_mobile() {
        return student_father_mobile;
    }

    public void setStudent_father_mobile(String student_father_mobile) {
        this.student_father_mobile = student_father_mobile;
    }

    public String getStudent_father_name() {
        return student_father_name;
    }

    public void setStudent_father_name(String student_father_name) {
        this.student_father_name = student_father_name;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getStudent_mobile() {
        return student_mobile;
    }

    public void setStudent_mobile(String student_mobile) {
        this.student_mobile = student_mobile;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    private String student_name, batch, completion_year, course_name, registration_year, student_email, student_father_mobile, student_father_name, student_id, student_mobile, term;
}

public class search_StudentSearchActivity extends AppCompatActivity {

    Button fetchButton;
    AutoCompleteTextView studentNameAutoCompTextView;
    RecyclerView studDetailRecyclerView;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    static search_StudentSearchActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_student_search);

        context = search_StudentSearchActivity.this;
        studentNameAutoCompTextView = findViewById(R.id.studentNameIdAutoCompTextView);
        studDetailRecyclerView = findViewById(R.id.studentDetailRecyclerView);
        fetchButton = findViewById(R.id.searchStudentFetchButton);

//        try {
//            PackageManager manager = context.getPackageManager();
//            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
//            Log.d("LOOOG", "ver "+info.versionName);
//        }catch (PackageManager.NameNotFoundException e){}


        setSuggestionsOnAutoCompleteTextView();
        fetchButton.setOnClickListener(new onFetchButtonClick());

    }

    class onFetchButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String studentNameOrID = studentNameAutoCompTextView.getText().toString().toLowerCase().trim();
            String searchByIdCheckRegex = "\\d+";
            String searchByNameCheckRegex = "(^[a-zA-Z\\s]+)";
            String searchByCourseTerm = "^(\\w+)(\\s+)(\\d{1}+)$";
            String searchByCourseRegistrationYear = "^(\\w+)(\\s+)(\\d{4}+)$";
            if (!studentNameOrID.isEmpty()) {
                blockScreen(true);

                try {
                    InputMethodManager hideKeyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    hideKeyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                } catch (NullPointerException e) {
                }

                if (studentNameOrID.matches(searchByIdCheckRegex)) {
                    fetchStudentByID(studentNameOrID);
                } else if(studentNameOrID.matches(searchByNameCheckRegex)) {
                    fetchStudentByName(studentNameOrID);
                }
                else if(studentNameOrID.matches(searchByCourseTerm)){
                    String[]courseAt0TermAt1 = studentNameOrID.split("\\s+");
                    String course = courseAt0TermAt1[0];
                    String term = courseAt0TermAt1[1];
                    fetchStudentByCourseTerm(course, term);
                }
                else if(studentNameOrID.matches(searchByCourseRegistrationYear)){
                    String[]courseAt0RegYearAt1 = studentNameOrID.split("\\s+");
                    String course = courseAt0RegYearAt1[0];
                    String regYear = courseAt0RegYearAt1[1];
                    fetchStudentByCourseRegistrationYear(course, regYear);
                }
                else {
                    blockScreen(false);
                    studentNameAutoCompTextView.setError("Invalid value");
                }
            } else {
                studentNameAutoCompTextView.setError("Empty Field");
            }
        }
    }

    void fetchStudentByName(String name) {
        final ArrayList<Student> studentsList = new ArrayList<>();
        firebaseFirestore.collection("STUDENT")
                .whereEqualTo("student_name", name)
                .orderBy("registration_year", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        blockScreen(false);
                        if (task.isSuccessful() && task.getResult().size()>0) {
                            for (QueryDocumentSnapshot snap : task.getResult()) {
                                Student student = snap.toObject(Student.class);
                                studentsList.add(student);
                            }

                            setStudentRecyclerView(studentsList);
                        } else {
                            Toast.makeText(context, "no data found", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        blockScreen(false);
                        Log.d("LOOOG", Log.getStackTraceString(e));
                        Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
                    }
                });

    }

    void fetchStudentByID(String ID) {
        final ArrayList<Student> studentsList = new ArrayList<>();
        firebaseFirestore.collection("STUDENT")
                .document("arya"+ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        blockScreen(false);
                        if (task.isSuccessful() && task.getResult().exists()) {
                            studentsList.add(task.getResult().toObject(Student.class));
                            setStudentRecyclerView(studentsList);
                        }else {
                            Toast.makeText(context, "no data found", Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                blockScreen(false);
                Log.d("LOOOG", Log.getStackTraceString(e));
                Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
            }
        });
    }

    void fetchStudentByCourseTerm(String course, String term){
        final ArrayList<Student> studentsList = new ArrayList<>();
        firebaseFirestore.collection("STUDENT")
                .whereEqualTo("course_name", course)
                .whereEqualTo("term", term)
                .whereEqualTo("completion_year", "pursuing")
                .orderBy("student_name")
                .orderBy("student_father_name")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        blockScreen(false);
                        if (task.isSuccessful() && task.getResult().size()>0) {
                            for (QueryDocumentSnapshot snap : task.getResult()) {
                                Student student = snap.toObject(Student.class);
                                studentsList.add(student);
                            }

                            setStudentRecyclerView(studentsList);
                        } else {
                            Toast.makeText(context, "no data found", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        blockScreen(false);
                        Log.d("LOOOG", Log.getStackTraceString(e));
                        Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
                    }
                });
    }

    void fetchStudentByCourseRegistrationYear(String course, String regYear) {
        final ArrayList<Student> studentsList = new ArrayList<>();
        firebaseFirestore.collection("STUDENT")
                .whereEqualTo("course_name", course)
                .whereEqualTo("registration_year", regYear)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        blockScreen(false);
                        if (task.isSuccessful() && task.getResult().size()>0) {
                            for (QueryDocumentSnapshot snap : task.getResult()) {
                                Student student = snap.toObject(Student.class);
                                studentsList.add(student);
                            }

                            setStudentRecyclerView(studentsList);
                        } else {
                            Toast.makeText(context, "no data found", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        blockScreen(false);
                        Log.d("LOOOG", Log.getStackTraceString(e));
                        Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
                    }
                });

    }

    void setStudentRecyclerView(ArrayList<Student> studentsList) {
        SearchStudentProgrammingAdapter programmingAdapter = new SearchStudentProgrammingAdapter(context, studentsList);
        studDetailRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        studDetailRecyclerView.setAdapter(programmingAdapter);
        blockScreen(false);
    }

    void setSuggestionsOnAutoCompleteTextView() {
        firebaseFirestore.collection("STUDENT")
                .document("StudentNameData")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Set<String> idSet = task.getResult().getData().keySet();
                            ArrayList<String> list = new ArrayList<>(idSet);

                            studentNameAutoCompTextView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, list));
                        } else {
                            Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
                        Log.d("LOOOG", Log.getStackTraceString(e));
                    }
                });
    }

    static void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        RelativeLayout inputBlocker = context.findViewById(R.id.inputBlocker);
        if (state) {
            inputBlocker.setVisibility(View.VISIBLE);
        } else {
            inputBlocker.setVisibility(View.GONE);
        }
    }
}

class SearchStudentProgrammingAdapter extends RecyclerView.Adapter<SearchStudentProgrammingAdapter.SearchStudentViewHolder> {

    search_StudentSearchActivity context;
    ArrayList<Student> studentsList;

    SearchStudentProgrammingAdapter(search_StudentSearchActivity context, ArrayList<Student> studentsList) {
        this.context = context;
        this.studentsList = studentsList;
    }

    @NonNull
    @Override
    public SearchStudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.search_student_search_adapter_layout, parent, false);
        return new SearchStudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchStudentViewHolder holder, final int position) {
        final Student student = studentsList.get(position);
        holder.srNo.setText(String.valueOf(position+1));
        holder.name.setText(wordCaseConvert(student.getStudent_name()));
        holder.fatherName.setText(wordCaseConvert(student.getStudent_father_name()));
        holder.id.setText(student.getStudent_id());
        holder.studPhone.setText(student.getStudent_mobile());
        holder.studEmail.setText(student.getStudent_email());
        holder.fatherPhone.setText(student.getStudent_father_mobile());
        holder.course.setText(student.getCourse_name().toUpperCase() + " " + (student.getTerm() == null ? "completed " : student.getTerm()));
        holder.batch.setText(student.getBatch());
        holder.registrationYear.setText(student.getRegistration_year());
        holder.completionYear.setText(student.getCompletion_year());
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.moreButton);
                popupMenu.inflate(R.menu.search_search_student_more_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        MenuItemAction menuAction = new MenuItemAction();
                        switch (item.getItemId()) {
                            case R.id.editMenuItem:
                                menuAction.editStudentDetailMenuItemAction(position);
                                return true;
                            case R.id.deleteMenuItem:
                                menuAction.permanentlyDeleteStudentMenuItemAction(student.getStudent_id(), position);
                                return true;
                            case R.id.resetPassMenuItem:
                                menuAction.resetStudentPasswordMenuItemAction(student.getStudent_id(), student.getStudent_mobile());
                                return true;
                            default:
                                return false;

                        }
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentsList.size();
    }

    class MenuItemAction {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        void editStudentDetailMenuItemAction(final int positionInArray) {
            LayoutInflater inflater = LayoutInflater.from(context);
            final ViewGroup parent = context.findViewById(android.R.id.content);
            View editStudentView = inflater.inflate(R.layout.search_edit_student_details_layout, parent, false);


            EditText id = editStudentView.findViewById(R.id.studentId);
            EditText name = editStudentView.findViewById(R.id.studentName);
            EditText fatherName = editStudentView.findViewById(R.id.studentFatherName);
            final EditText email = editStudentView.findViewById(R.id.studentEmail);
            final EditText phone = editStudentView.findViewById(R.id.studentPhoneno);
            final EditText fatherPhone = editStudentView.findViewById(R.id.fatherPhoneno);
            EditText course = editStudentView.findViewById(R.id.studentCourse);
            EditText term = editStudentView.findViewById(R.id.studentTerm);
            EditText batch = editStudentView.findViewById(R.id.studentBatch);
            Button submit = editStudentView.findViewById(R.id.studentEditSubmit);
            final Button cancel = editStudentView.findViewById(R.id.studentEditCancel);


            final Student student = studentsList.get(positionInArray);

            id.setText(student.getStudent_id());
            name.setText(student.getStudent_name());
            fatherName.setText(student.getStudent_father_name());
            email.setText(student.getStudent_email());
            phone.setText(student.getStudent_mobile());
            fatherPhone.setText(student.getStudent_father_mobile());
            course.setText(student.getCourse_name());
            term.setText(student.getTerm());
            batch.setText(student.getBatch());

            final Dialog studentDetailEditViewDialog = new Dialog(context);
            studentDetailEditViewDialog.setCancelable(false);
            studentDetailEditViewDialog.setContentView(editStudentView);
            studentDetailEditViewDialog.show();

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder studentDetailEditFinalAlert = new AlertDialog.Builder(context)
                            .setTitle("Warning!")
                            .setMessage("Do you really want to update data.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    String studPhoneString = phone.getText().toString().trim();
                                    String studEmailString = email.getText().toString().trim();
                                    String fatherPhoneString = fatherPhone.getText().toString().trim();
                                    {
                                        if (studPhoneString.isEmpty() || studEmailString.isEmpty() || fatherPhoneString.isEmpty()){
                                            Toast.makeText(context, "Fields Empty", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        if(!studPhoneString.matches("(0/91)?[6-9][0-9]{9}")){
                                            phone.setError("Invalid Phone Number");
                                            return;
                                        }
                                        if(!fatherPhoneString.matches("(0/91)?[6-9][0-9]{9}")){
                                            fatherPhone.setError("Invalid Phone Number");
                                            return;
                                        }
                                    }

                                    student.setStudent_mobile(studPhoneString);
                                    student.setStudent_father_mobile(fatherPhoneString);
                                    student.setStudent_email(studEmailString);

                                    dialog.dismiss();
                                    studentDetailEditViewDialog.dismiss();
                                    try {
                                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
                                    }catch (NullPointerException e){}
                                    search_StudentSearchActivity.blockScreen(true);


                                    firebaseFirestore.collection("STUDENT")
                                            .document(student.getStudent_id())
                                            .set(student, SetOptions.merge())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    search_StudentSearchActivity.blockScreen(false);
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(context, "student data updated", Toast.LENGTH_LONG).show();
                                                        dialog.cancel();
                                                        notifyDataSetChanged();
                                                    }
                                                    else {
                                                        Toast.makeText(context, "data update failed!", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(context, new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    search_StudentSearchActivity.blockScreen(false);
                                                    Toast.makeText(context, "data update failed!", Toast.LENGTH_LONG).show();
                                                    Log.d("LOOOG",Log.getStackTraceString(e));
                                                }
                                            });
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    studentDetailEditFinalAlert.create().show();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    studentDetailEditViewDialog.cancel();
                }
            });


        }

        void permanentlyDeleteStudentMenuItemAction(final String studentId, final int positionInArray) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context)
                    .setTitle("WARNING")
                    .setCancelable(false)
                    .setMessage("Student account will permanently deleted")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            firebaseFirestore.collection("STUDENT")
                                    .document(studentId)
                                    .delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Student Deleted", Toast.LENGTH_LONG).show();
                                            studentsList.remove(positionInArray);
                                            notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(context, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
                                            Log.d("LOOOG", Log.getStackTraceString(e));
                                        }
                                    });

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertBuilder.create().show();
        }

        void resetStudentPasswordMenuItemAction(final String studentId, final String studentMobile) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context)
                    .setTitle("INFORMATION")
                    .setCancelable(false)
                    .setMessage("Student mobile number set as new password")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            firebaseFirestore.collection("STUDENT")
                                    .document(studentId)
                                    .update("student_password", studentMobile)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Password Reset", Toast.LENGTH_LONG).show();
                                            notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(context, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
                                        }
                                    });

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertBuilder.create().show();

        }
    }


    class SearchStudentViewHolder extends RecyclerView.ViewHolder {
        TextView srNo, name, fatherName, id, course, batch, registrationYear, completionYear, studPhone, studEmail, fatherPhone;
        Button moreButton;

        public SearchStudentViewHolder(@NonNull View itemView) {
            super(itemView);

            srNo = itemView.findViewById(R.id.studentSrNo);
            name = itemView.findViewById(R.id.studentName);
            fatherName = itemView.findViewById(R.id.fatherName);
            id = itemView.findViewById(R.id.studentId);
            studPhone = itemView.findViewById(R.id.studentPhone);
            studEmail = itemView.findViewById(R.id.studentEmail);
            fatherPhone = itemView.findViewById(R.id.fatherPhone);
            course = itemView.findViewById(R.id.course);
            batch = itemView.findViewById(R.id.batch);
            registrationYear = itemView.findViewById(R.id.registrationYear);
            completionYear = itemView.findViewById(R.id.completionYear);
            moreButton = itemView.findViewById(R.id.moreButton);
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
}
