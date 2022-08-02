package com.example.attend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class option_AllOptionContainerActivity extends AppCompatActivity {

    option_AllOptionContainerActivity context;

    LinearLayout adminOptions;

    TeacherDetailHolder teacherDetailHolder = TeacherDetailHolder.getTeacherDetailHolderReference();

    Toolbar toolbar;

    Button profile, takeViewAttend, uploadDeleteNote, takeRevokeSubject, searchEditStudent, promoteDeleteStudent, studentRegistration, addDeleteCourse, addDeleteSubject, addDeleteTeacher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option_activity_all_option_container);
        setToolBar();

        TeacherDetailHolder teacherDetailHolder = TeacherDetailHolder.getTeacherDetailHolderReference();
//        String TeacherId = "aniket000";
//        String TeacherRight = "admin";
//        String TeacherName = "aniket";
//        teacherDetailHolder.setTeacherId(TeacherId);
//        teacherDetailHolder.setTeacherRight(TeacherRight);
//        teacherDetailHolder.setTeacherName(TeacherName);
        adminOptions = findViewById(R.id.adminOptions);
        if(teacherDetailHolder.getTeacherRight().equals("admin")){
            adminOptions.setEnabled(true);
            adminOptions.setVisibility(View.VISIBLE);
        }

        context = this;
        profile = findViewById(R.id.profileButton);
        takeViewAttend = findViewById(R.id.takeViewAttendanceButton);
        uploadDeleteNote = findViewById(R.id.uploadDeleteNoteButton);
        takeRevokeSubject = findViewById(R.id.takeRevokeSubjectButton);
        searchEditStudent = findViewById(R.id.searchEditStudentButton);
        promoteDeleteStudent = findViewById(R.id.promoteDeleteStudentButton);
        studentRegistration = findViewById(R.id.studentRegistrationButton);
        addDeleteCourse = findViewById(R.id.addDeleteCourseButton);
        addDeleteSubject = findViewById(R.id.addDeleteSubjectButton);
        addDeleteTeacher = findViewById(R.id.addDeleteTeacherButton);

        OnOptionButtonClickListener buttonClickListener = new OnOptionButtonClickListener();
        profile.setOnClickListener(buttonClickListener);
        takeViewAttend.setOnClickListener(buttonClickListener);
        uploadDeleteNote.setOnClickListener(buttonClickListener);
        takeRevokeSubject.setOnClickListener(buttonClickListener);
        searchEditStudent.setOnClickListener(buttonClickListener);
        promoteDeleteStudent.setOnClickListener(buttonClickListener);
        studentRegistration.setOnClickListener(buttonClickListener);
        addDeleteCourse.setOnClickListener(buttonClickListener);
        addDeleteSubject.setOnClickListener(buttonClickListener);
        addDeleteTeacher.setOnClickListener(buttonClickListener);

    }

    void setToolBar() {
        toolbar = findViewById(R.id.dot_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dot_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.resetPassword) {
            //add code for LogOut
            //resetPassword();
        }
        if (item.getItemId() == R.id.about) {

        }
        if (item.getItemId() == R.id.logout) {
            //add code for LogOut
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    void logout(){
        SharedPrefLoginInfo sharedPrefLoginInfo = new SharedPrefLoginInfo(getApplicationContext());
        sharedPrefLoginInfo.clearUserIdNameDetail();

        Intent transferToLogin = new Intent(context, login_TeacherLoginActivity.class);
        startActivity(transferToLogin);
        TeacherDetailHolder.clearTeacherDetail();
        finish();
    }

    class OnOptionButtonClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            int clickedButtonId = v.getId();
            switch (clickedButtonId){
                case R.id.profileButton:
                    
                    break;
                case R.id.takeViewAttendanceButton:
                    intent = new Intent(context, attend_SelectYearSubActivity.class);
                    break;
                case R.id.uploadDeleteNoteButton:
                    intent = new Intent(context, note_UploadNoteActivity.class);
                    break;
                case R.id.takeRevokeSubjectButton:
                    intent = new Intent(context, teacherTakeSubject_TeacherSelectSubjectActivity.class);
                    break;
                case R.id.searchEditStudentButton:
                    intent = new Intent(context, search_StudentSearchActivity.class);
                    break;
                case R.id.promoteDeleteStudentButton:
                    intent = new Intent(context, promote_PromoteStudentActivity.class);
                    break;
                case R.id.studentRegistrationButton:
                    intent = new Intent(context, studentRegistrationActivity.class);
                    break;
                case R.id.addDeleteCourseButton:
                    intent = new Intent(context, adddeletecourse_AddDeleteCourseActivity.class);
                    break;
                case R.id.addDeleteSubjectButton:
                    intent = new Intent(context, adddeletesubject_AddDeleteSubjectActivity.class);
                    break;
                case R.id.addDeleteTeacherButton:

                    break;
            }

            context.startActivity(intent);
        }
    }
}
