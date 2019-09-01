package com.jqk.pictureselector;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button single, more;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        single = (Button) findViewById(R.id.single);
        more = (Button) findViewById(R.id.more);

        single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SingleActivity.class);
                startActivity(intent);
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MoreActivity.class);
                startActivity(intent);

//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, EditPictureActivity.class);
//                startActivity(intent);
            }
        });
    }
}
