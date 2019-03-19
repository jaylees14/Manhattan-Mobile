package com.manhattan.blueprint.Controller;

import android.app.Notification;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.DAO.Consumer;
import com.manhattan.blueprint.Model.Session;
import com.manhattan.blueprint.R;

public class SettingsActivity extends AppCompatActivity {

    public int toggleOnColor;
    public int toggleOffColor;
    Button toggleHololens;
    EditText hololensIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.toggleHololens = findViewById(R.id.toggle_hololens);
        this.hololensIP = findViewById(R.id.settings_hololens_ip);
        this.toggleOffColor = getResources().getColor(R.color.red);
        this.toggleOnColor  = getResources().getColor(R.color.lime_green);

        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.getSession().ifPresent(session -> {
            hololensIP.setText(session.hololensIP);
            if (session.isHololensConnected()) {
                toggleHololens.setTextColor(toggleOnColor);
            } else {
                toggleHololens.setTextColor(toggleOffColor);
            }
        });

        toggleHololens.setOnClickListener(this::onToggleClickListener);
        findViewById(R.id.settings_save).setOnClickListener(this::onSaveClickListener);
    }

    private void onSaveClickListener(View view) {
        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.getSession().ifPresent(session -> {
            dao.setSession(new Session(session.getUsername(),
                    session.getAccountType(),
                    hololensIP.getText().toString(),
                    session.isHololensConnected()));
            this.runOnUiThread(this::finish);
        });
    }

    private void onToggleClickListener(View view) {
        BlueprintDAO dao = BlueprintDAO.getInstance(this);
        dao.getSession().ifPresent(session -> {
            dao.setSession(new Session(session.getUsername(),
                               session.getAccountType(),
                               hololensIP.getText().toString(),
                               !session.isHololensConnected()));
            if (dao.getSession().get().isHololensConnected()) {
                toggleHololens.setTextColor(toggleOnColor);
            } else {
                toggleHololens.setTextColor(toggleOffColor);
            }
        });
    }
}
