package jceex1.example.com.camandrate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    // declaring views
    Button doneB;
    EditText sizeED;
    TextView exampletv;
    String tempsizeString;
    float tempsizeFloat = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // initializing views
        exampletv = (TextView) findViewById(R.id.TVexample);
        sizeED = (EditText) findViewById(R.id.EDsize);
        doneB = (Button) findViewById(R.id.Bdone);

        // if listener will check if the user has pressed the return done button of the keyboard
        sizeED.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    if (!sizeED.getText().toString().isEmpty()) {
                        // the size entered must be between 10 and 25, otherwise it will not display properly
                        tempsizeString = sizeED.getText().toString();
                        tempsizeFloat = Float.parseFloat(tempsizeString);
                        if (tempsizeFloat < 10) {
                            tempsizeFloat = 10;
                            Toast.makeText(getApplicationContext(), " Size between 10 - 25", Toast.LENGTH_SHORT).show();
                        } else if (tempsizeFloat > 25) {
                            tempsizeFloat = 25;
                            Toast.makeText(getApplicationContext(), " Size between 10 - 25", Toast.LENGTH_SHORT).show();
                        }
                        sizeED.setText("");
                        exampletv.setTextSize(tempsizeFloat);
                        return false;
                    }
                }
                return true;
            }
        });

        // when the button done is clicked then it will set the size in main activity and finish this activity
        doneB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tempsizeFloat != 0.0f) {
                    MainActivity mainActivity = new MainActivity();
                    mainActivity.textSize = tempsizeFloat;
                    //mainActivity.setTextSize(tempsizeFloat);
                }
                finish();
            }
        });
    }
}
