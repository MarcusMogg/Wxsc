package test.com.fg.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.fg.activity.R;

public class DetailsActivity extends AppCompatActivity {

    private TextView foodName;
    private TextView briefIntroduction;
    private Intent mIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        intiView();
    }

    private void intiView() {
        mIntent = this.getIntent();
        foodName = findViewById(R.id.foodNameInDetails);
        foodName.setText(mIntent.getStringExtra("foodName"));
    }

}
