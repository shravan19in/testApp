package com.example.test3;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    EditText editTask;
    //Button btnAdd;
    FloatingActionButton btnAdd;
    RecyclerView recyclerView;
    ArrayList<String> taskList;
    TaskAdapter adapter;
    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTask = findViewById(R.id.editTask);

        recyclerView = findViewById(R.id.recyclerTasks);
        btnAdd = findViewById(R.id.btnAdd);

        taskList = loadTasks();

        adapter = new TaskAdapter(this, taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddTaskDialog());
        btnAdd.setOnClickListener(v -> {
            String task = editTask.getText().toString().trim();
            if (!task.isEmpty()) {
                taskList.add(task);
                adapter.notifyItemInserted(taskList.size() - 1);
                editTask.setText("");
                saveTasks();
            }
        });

    }


    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("size", taskList.size());
        for (int i = 0; i < taskList.size(); i++) {
            editor.putString("task_" + i, taskList.get(i));
        }
        editor.apply();
    }

    private ArrayList<String> loadTasks() {
        SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
        int size = prefs.getInt("size", 0);
        ArrayList<String> tasks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            tasks.add(prefs.getString("task_" + i, null));
        }
        return tasks;
    }
    private void showAddTaskDialog() {

        View view = getLayoutInflater().inflate(R.layout.date, null);

        EditText taskInput = view.findViewById(R.id.dialogTaskInput);
        Button btnDate = view.findViewById(R.id.btnPickDate);
        Button btnTime = view.findViewById(R.id.btnPickTime);
        Button btnSave = view.findViewById(R.id.btnSave);

        Calendar calendar = Calendar.getInstance();

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        }
        btnDate.setOnClickListener(view -> {

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (dateView, year, month, day) -> {

                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);

                    btnDate.setText(day + "/" + (month + 1) + "/" + year);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    });

    }