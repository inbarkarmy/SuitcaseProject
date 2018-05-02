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

public class MainActivity extends AppCompatActivity {

    // Declare a DynamoDBMapper object- entry point to DynamoDB tables
    DynamoDBMapper dynamoDBMapper;
    UsersDO userItem = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        setContentView(R.layout.activity_main);

        // Instantiate a AmazonDynamoDBMapperClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        Button login = (Button)findViewById(R.id.loginBtn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText userName = (EditText)findViewById(R.id.username);
                EditText password = (EditText)findViewById(R.id.password);
                TextView warning = (TextView)findViewById(R.id.warning);

                String userNameStr = userName.getText().toString();
                String passwordStr = password.getText().toString();

                readUsers(userNameStr);
                if (userItem != null && userItem.getPassword().matches(passwordStr))
                    startActivity(new Intent(MainActivity.this, SuitcaseApp.class));
                else
                    warning.setText("userName or password are incorrect");
            }
        });

        Button register = (Button)findViewById(R.id.registerBtn);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Register.class));
            }
        });
    }

    public void readUsers(final String userName) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                userItem = dynamoDBMapper.load(
                        UsersDO.class,
                        userName,
                        null);
                // Item read
                Log.d("User Item:", userItem.getName().toString());
            }
        });

        t.start();
        try {
            t.join();
        } catch (Exception e) {
            Log.d("login: ", ""+ e);
            
        }
    }

}
