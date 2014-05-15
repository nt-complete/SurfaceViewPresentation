package com.nerdery.MapConcept.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.nerdery.MapConcept.R;
import com.nerdery.MapConcept.views.MapSurfaceView;

public class MainActivity extends Activity {

    private TextView mDialogView;
    private MapSurfaceView mMapSurfaceView;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDialogView = (TextView) findViewById(R.id.main_coord_textview);
        mDialogView.buildDrawingCache();
        mDialogView.setDrawingCacheEnabled(true);


        final EditText editText = (EditText) findViewById(R.id.main_edittext);
        mDialogView.setText(editText.getText());


        mMapSurfaceView = (MapSurfaceView) findViewById(R.id.main_customsurfaceview);
        mMapSurfaceView.setDialogBitmap(mDialogView.getDrawingCache());


        (findViewById(R.id.main_update_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialogView.setText(editText.getText());
                mMapSurfaceView.setDialogBitmap(mDialogView.getDrawingCache());
            }
        });

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.main_toggle);
        toggleButton.setChecked(true);
        mMapSurfaceView.setDisplayDialog(toggleButton.isChecked());

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mMapSurfaceView.setDisplayDialog(b);
            }
        });
    }
}
