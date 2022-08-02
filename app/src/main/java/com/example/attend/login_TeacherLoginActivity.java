package com.example.attend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

public class login_TeacherLoginActivity extends AppCompatActivity {

    EditText idEditText, passwordEditText;
    Button submitButton;
    TextView registerStudent;

    login_TeacherLoginActivity context;

    SharedPrefLoginInfo sharedPrefLog;

    @Override
    protected void onResume() {
        super.onResume();
        checkUserAlreadyLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_teacher_login);

        idEditText = findViewById(R.id.idEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        submitButton = findViewById(R.id.submitIdPassword);
        registerStudent = findViewById(R.id.teacherRegisterText);

        context = this;

        sharedPrefLog = new SharedPrefLoginInfo(getApplicationContext());
        class OnClickSubmitButton implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                String studId = idEditText.getText().toString().trim();
                String studPassword = passwordEditText.getText().toString().trim();
                checkStudIdPasswordInDataBase(studId, studPassword);
            }
        }
        submitButton.setOnClickListener(new OnClickSubmitButton());

        // FUTURE FEATURE FOR STUDENT REGISTRATION
        class OnRegisterButtonClick implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                /*checkRegistrationOpenThenOpenActivity();*/

                LinearLayoutCompat linearLayout = new LinearLayoutCompat(context);
                linearLayout.setOrientation(LinearLayoutCompat.VERTICAL);

                final EditText dialogPhoneEditText = new EditText(context);
                dialogPhoneEditText.setMaxLines(1);
                dialogPhoneEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                dialogPhoneEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                dialogPhoneEditText.setHint("Enter phone no.");

                final EditText dialogPasswordEditText = new EditText(context);
                dialogPasswordEditText.setMaxLines(1);
                dialogPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                dialogPasswordEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                dialogPasswordEditText.setHint("Enter password");

                linearLayout.addView(dialogPhoneEditText, 0);
                linearLayout.addView(dialogPasswordEditText, 1);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context)
                        .setView(linearLayout)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String studentEnteredPassword = dialogPasswordEditText.getText().toString();
                                if (studentEnteredPassword.isEmpty()) {
                                    Toast.makeText(getBaseContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                //checkRegistrationPassword(studentEnteredPassword);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertBuilder.create().show();
            }
        }
        registerStudent.setOnClickListener(new OnRegisterButtonClick());
    }

    void checkUserAlreadyLogin() {

        if(sharedPrefLog.readLoginStatus()){
            String studId = sharedPrefLog.getUserId();
            String studPassword = sharedPrefLog.getUserPassword();
            Log.d("Looog", studId + " " + studPassword);
            checkStudIdPasswordInDataBase(studId, studPassword);    //check if login id or name is in database or not
        }

        /*

        //filePath location used to store student login id and name to make student login until not logout
        filePath = new File(getExternalCacheDir(), "credential.sys");
        //check if login filePath exist or not if exist student directly login without entering login id or login name
        if (filePath.exists()) {
            try (FileInputStream fis = new FileInputStream(filePath)) {
                Scanner sn = new Scanner(fis);
                String studId = sn.nextLine();      //fetch login student id
                String studName = sn.nextLine();    //fetch login student name
                Log.d("Looog", studId + " " + studName);
                checkStudIdPasswordInDataBase(studId, studName);    //check if login id or name is in database or not
            } catch (Exception e) {
                Log.d("Looog", Log.getStackTraceString(e));
            }
        }

        */
    }

    private void checkStudIdPasswordInDataBase(final String id, final String password) {
//        if(!NetworkConnectivityChecker.isNetworkConnected(context))
//        {
//            Toast.makeText(context, "Check Network Connectivity", Toast.LENGTH_SHORT).show();
//            return;
//        }
        blockScreen(true);
        Log.d("Looog", "Check");
        try {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("TEACHERS")
                    .document(id)
                    .get(Source.SERVER)
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            try {
                                if (!documentSnapshot.exists()) {       //if giving is is not exist in databases
                                    Log.d("Looog", "Invalid ID");
                                    idEditText.setError("Invalid ID");
                                    Toast.makeText(context, "Invalid ID", Toast.LENGTH_SHORT).show();
                                } else if (!password.equals(documentSnapshot.get("teacher_password").toString())) {     //if given password is not match with given id
                                    Log.d("Looog", "Incorrect Password");
                                    passwordEditText.setError("Incorrect Password");
                                    Toast.makeText(context, "Incorrect Password", Toast.LENGTH_SHORT).show();
                                } else {    //if id and password match and student data found
                                    Log.d("Looog", "Student FOUND " + documentSnapshot.get("teacher_name"));

                                    TeacherDetailHolder teacherDetailHolder = TeacherDetailHolder.getTeacherDetailHolderReference();
                                    teacherDetailHolder.setTeacherName(documentSnapshot.getString("teacher_name"));
                                    teacherDetailHolder.setTeacherId(documentSnapshot.getId());
                                    teacherDetailHolder.setTeacherRight(documentSnapshot.getString("teacher_right"));

                                    sharedPrefLog.setUserIdPassword(id, password);

                                    Intent intent = new Intent(context, option_AllOptionContainerActivity.class);  //jump on studentPanel
                                    startActivity(intent);
                                    finish();
                                }
                            }catch (NullPointerException e){
                                Toast.makeText(context, "error:Some fields missing "+e.toString(),Toast.LENGTH_LONG).show();
                                Log.d("LOOOG",Log.getStackTraceString(e));
                            }
                            blockScreen(false);
                        }
                    })
                    .addOnFailureListener(context, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder noAlertBuilder = new AlertDialog.Builder(context);
                            AlertDialog alertDialog = noAlertBuilder
                                    .setMessage("Turn on mobile net or connect to wifi")
                                    .setCancelable(false)
                                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        @Override
                                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                                dialog.dismiss();
                                                context.finish();
                                            }
                                            return true;
                                        }
                                    })
                                    .setPositiveButton("Refresh", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            context.checkUserAlreadyLogin();
                                            dialog.dismiss();
                                        }
                                    }).create();
                            alertDialog.show();


                            Log.d("Looog", "Fail " + Log.getStackTraceString(e));
                            blockScreen(false);
                        }
                    });
        } catch (Exception e) {
            idEditText.setError("Invalid ID");
            passwordEditText.setError("Invalid Password");
            Log.d("Looog", "Exception in checkStudIdPasswordInDataBase " + Log.getStackTraceString(e));
            blockScreen(false);
        }
    }

    private void blockScreen(boolean state) {        //if true screen set To block otherwise unblock
        View inputBlockerView = findViewById(R.id.inputBlockerView);
        ProgressBar progressBar = findViewById(R.id.submitProgressBar);

        if (state) {
            inputBlockerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            inputBlockerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }
}
