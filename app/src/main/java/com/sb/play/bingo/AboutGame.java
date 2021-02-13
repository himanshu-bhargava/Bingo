package com.sb.play.bingo;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.sb.play.adaptor.AboutAdaptor;
import com.sb.play.bingo.models.About;
import com.sb.play.util.BingoUtil;

import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AboutGame extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_game);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("About");
        readQuesAns();
        ((TextView) findViewById(R.id.versionView)).setText(getVersion());
        RecyclerView recyclerView = findViewById(R.id.aboutListHolderLayout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new AboutAdaptor(this, readQuesAns()));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1,
                GridLayoutManager.VERTICAL, false));
    }

    private List<About> readQuesAns() {
        try {
            return BingoUtil.readQuesAnswer(this);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open about!", Toast.LENGTH_SHORT).show();
            Log.e("Error in about", "readQuesAns: Could not read the about", e);
            finish();
        }
        return null;
    }

    private String getVersion() {
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
            Log.e("Error reading version", "getVersion: ", e);
        }
        return "Unknown";
    }
}