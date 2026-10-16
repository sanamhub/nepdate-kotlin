package io.github.sanamhub.nepdate.sample.size;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/** The baseline flavor: no NepDate code, so this APK is the size floor. */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText("no NepDate");
        setContentView(view);
    }
}
