package com.example.inbar.loggin;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class Register extends AppCompatActivity {

    // Declare a DynamoDBMapper object- entry point to DynamoDB tables
    DynamoDBMapper dynamoDBMapper;
    UsersDO userItemRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        setContentView(R.layout.activity_register);

        // Instantiate a AmazonDynamoDBMapperClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        Button registerBtn = (Button)findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText userName = (EditText)findViewById(R.id.username);
                EditText password = (EditText)findViewById(R.id.password);
                EditText email = (EditText)findViewById(R.id.email);
                EditText address = (EditText)findViewById(R.id.address);
                EditText phone = (EditText)findViewById(R.id.phone);
                boolean check = true;

                String userNameStr = userName.getText().toString();
                String addressStr = address.getText().toString();
                String passwordStr = password.getText().toString();
                String emailStr = email.getText().toString();
                String phoneStr = phone.getText().toString();

                check = checkFields(userNameStr,passwordStr, addressStr, phoneStr, emailStr);
                if (check) {
                    Double phoneDbl = Double.parseDouble(phoneStr);
                    createUser(userNameStr, passwordStr, addressStr, phoneDbl, emailStr);
                }
            }
        });
    }

    public boolean checkFields (final String userName, String password,
                                String address, String phone, String email) {
        TextView warning = (TextView)findViewById(R.id.warning);

        if (userName.matches("") || address.matches("") || email.matches("") ||
            password.matches("") || phone.matches("")) {
            Log.d("Register", "missing information- please fill all fields above!");
            warning.setText("missing information- please fill all fields above!");
            return false;
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                userItemRead = dynamoDBMapper.load(UsersDO.class, userName, null);
                // Item read
            }
        });

        t.start();
        try {
            t.join();
        } catch (Exception e){
            Log.d("join exception: ", ""+ e);
        }

        if(userItemRead != null) {
            Log.d("Register", "Username already exist");
            warning.setText("Username already exist!");
            return false;
        }

        return true;
    }

    public void createUser(String userName, String password,
                           String address, double phone, String email) {
        final UsersDO userItem = new UsersDO();

        userItem.setName(userName);
        userItem.setAddress(address);
        userItem.setPhone(phone);
        userItem.setEmail(email);
        userItem.setPassword(password);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                        dynamoDBMapper.save(userItem);
                        startActivity(new Intent(Register.this, MainActivity.class));
                    } catch (Exception e){
                    Log.d("save to database", "the exception: "+ e);
                }
                // Item saved
            }
        }).start();
    }

}
