package com.snhu.cs360project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    UserDataBaseHandler usersDatabase;
    EditText usernameText;
    EditText passwordText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Log.d(TAG, "onCreate: Main");
        usersDatabase = UserDataBaseHandler.getInstance(getApplicationContext());
        usernameText = findViewById(R.id.usernameInput);
        passwordText = findViewById(R.id.passwordInput);
    }
    public void login(View view) {
        boolean valid = usersDatabase.validUser(usernameText.getText().toString(), passwordText.getText().toString());
        if (!valid)
            Toast.makeText(this, R.string.invalid_login, Toast.LENGTH_SHORT).show();
        else
            loadDataAct();
        //Log.d(TAG, "login: " + valid);
    }
    public void register(View view) {
        long userID = usersDatabase.addUser(usernameText.getText().toString(), passwordText.getText().toString());
        if (userID == -1)
            Toast.makeText(this, R.string.bad_reg, Toast.LENGTH_SHORT).show();
        else
            loadDataAct();
        //Log.d(TAG, "login: " + userID);
    }
    private void loadDataAct() {
        Intent intent = new Intent(this, DataActivity.class);
        startActivity(intent);
    }


}