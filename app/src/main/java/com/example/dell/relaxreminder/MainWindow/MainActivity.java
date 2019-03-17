package com.example.dell.relaxreminder.MainWindow;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.dell.relaxreminder.Database.RelaxDataSource;
import com.example.dell.relaxreminder.R;
import com.example.dell.relaxreminder.Settings.PrefsHelper;
import com.github.lzyzsd.circleprogress.DonutProgress;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private Button addRelax;
    public static DonutProgress circleProgress;
    public static TextView choosenAmountTv;
    private RelaxDataSource db;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleProgress = (DonutProgress) findViewById(R.id.donut_progress);
        choosenAmountTv = (TextView)findViewById(R.id.choosen_relax_text) ;
        db= new RelaxDataSource(this);
        db.open();
        context=getApplicationContext();




        addRelax = findViewById(R.id.add_relax_buttons);

        addRelax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int perValue= db.getConsumedPercentage();
                circleProgress.setProgress(perValue);
                choosenAmountTv.setText(String.valueOf(db.geConsumedRelaxForToadyDateLog()+" out of "+
                        PrefsHelper.getRelaxNeedPrefs(getApplicationContext())+" times"));
            }
        });

    }
}
