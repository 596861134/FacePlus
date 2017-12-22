package com.face.plus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SplashActivity extends AppCompatActivity implements View.OnClickListener{

    Button button;
    Button button2;
    Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(SplashActivity.this,ImageActivity.class);
        switch (view.getId()){
            case R.id.button:
                intent.putExtra("msg","1");
                break;
            case R.id.button2:
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                break;
            case R.id.button3:
                intent.putExtra("msg","2");
                break;

        }
        startActivity(intent);
    }
}
