package com.ihsan.sketch.collab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView projs;
    OnlineProjsAdapter adapter;
    ArrayList<OnlineProject> projslist = new ArrayList<OnlineProject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.GRAY);
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("projects");

        projs = findViewById(R.id.online_projects);
        adapter = new OnlineProjsAdapter(projslist, this);
        projs.setLayoutManager(new LinearLayoutManager(this));
        projs.setAdapter(adapter);

        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        projslist.clear();
                        for (DataSnapshot data: snapshot.getChildren()) {
                            OnlineProject project = new OnlineProject(
                                    data.child("name").getValue(String.class),
                                    "v" + String.valueOf(data.child("version").getValue(double.class)),
                                    data.child("author").getValue(String.class),
                                    data.child("open").getValue(boolean.class),
                                    false);
                            project.setDescription(data.child("desc").getValue(String.class));
                            projslist.add(project);
                        }
                        findViewById(R.id.progressMain).setVisibility(View.GONE);
                        adapter.updateView(projslist);
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(findViewById(R.id.parent_main), "ERROR: ".concat(error.getMessage()), Snackbar.LENGTH_LONG)
                                .setBackgroundTint(Color.RED)
                                .show();
                    }
                });
            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                projslist.clear();
                for (DataSnapshot data: snapshot.getChildren()) {
                    OnlineProject project = new OnlineProject(
                            data.child("name").getValue(String.class),
                            "v" + String.valueOf(data.child("version").getValue(double.class)),
                            data.child("author").getValue(String.class),
                            data.child("open").getValue(boolean.class),
                            false);
                    projslist.add(project);
                }
                findViewById(R.id.progressMain).setVisibility(View.GONE);
                adapter.updateView(projslist);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(findViewById(R.id.parent_main), "ERROR: ".concat(error.getMessage()), Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.RED)
                .show();
            }
        });
    }
}