package com.neatherbench.quencher;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private String TAG = "LogDebug";

    private static String server_address;

    EditText _nameText;
    EditText _emailText;
    EditText _passwordText;
    Button _signupButton;
    Button _skipButton;
    TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //ButterKnife.inject(this);
        _nameText = findViewById(R.id.input_name);
        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _signupButton = findViewById(R.id.btn_signup);
        _skipButton = findViewById(R.id.signup_btn_skip);
        _loginLink = findViewById(R.id.link_login);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final SharedPreferences.Editor editor = prefs.edit();

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        _skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putString("username", "skipped");
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @SafeVarargs
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }


    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed("Не удалось войти");
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Создание аккаунта...");
        progressDialog.show();

        final String name = _nameText.getText().toString();
        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();

        // TODO: Implement your own signup logic here.

        List<NameValuePair> login_data = new ArrayList<>();
        login_data.add(new BasicNameValuePair("email", email));
        login_data.add(new BasicNameValuePair("username", name));
        login_data.add(new BasicNameValuePair("password", password));
        final String new_account_server_address = "https://178.162.41.115/add_account.php";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final SharedPreferences.Editor editor = prefs.edit();
        executeAsyncTask(new SignupTask(new_account_server_address, login_data, SignupActivity.this.getApplicationContext(),
                new SignupTask.AsyncResponse() {


                    @Override
                    public void processFinish(String output) {
                        if(output.equals("4")){
                            onSignupFailed("Пользователь с таким именем уже существует");
                        progressDialog.dismiss();}
                        else if(output.equals("1")){
                            onSignupFailed("Отсутствует Email");
                        progressDialog.dismiss();}
                        else if(output.equals("2")){
                            onSignupFailed("Отсутствует имя пользователя");
                        progressDialog.dismiss();}
                        else if(output.equals("5")){
                            onSignupFailed("Отсутствует пароль");
                        progressDialog.dismiss();}
                        else if(output.equals("6")){
                            editor.putString("username", name);
                            editor.putString("email", email);
                            editor.putInt("reputation", 0);
                            editor.apply();
                            onSignupSuccess();
                        progressDialog.dismiss();}
                        else if(output.equals("3")){
                            onSignupFailed("Пользователь с таким email уже существует");
                        progressDialog.dismiss();}
                        else{
                            onSignupFailed("Произошла ошибка, повторите позже");
                            progressDialog.dismiss();}
                    }
                }));

        /*new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);*/
    }


    public void onSignupSuccess() {
        Toast.makeText(getBaseContext(), "Аккаунт успешно создан", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed(String message) {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("минимум 3 символа");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Введите верный email");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("от 4 до 15 символов");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}