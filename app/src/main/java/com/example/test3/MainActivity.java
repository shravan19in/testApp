package com.example.test3;

import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton btnAdd;
    RecyclerView recyclerView;
    TextView taskCountText;
    ArrayList<String> taskList;
    TaskAdapter adapter;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    new AlertDialog.Builder(this)
                            .setTitle("Notifications Disabled")
                            .setMessage("You won't receive task reminders unless notifications are enabled.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerTasks);
        btnAdd = findViewById(R.id.btnAdd);
        taskCountText = findViewById(R.id.taskCountText);

        taskList = loadTasks();
        updateTaskCount();

        adapter = new TaskAdapter(this, taskList, () -> {
            saveTasks();
            updateTaskCount();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddTaskDialog());
        requestNotificationPermission();
    }

    private void updateTaskCount() {
        int count = taskList.size();
        taskCountText.setText(count == 1 ? "1 task" : count + " tasks");
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    public void saveTasks() {
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

    private void scheduleNotification(String taskName, Calendar triggerTime) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TaskReminderReceiver.class);
        intent.putExtra(TaskReminderReceiver.EXTRA_TASK_NAME, taskName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                taskName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime.getTimeInMillis(), pendingIntent);
    }

    private void showAddTaskDialog() {
        View view = getLayoutInflater().inflate(R.layout.date, null);

        EditText taskInput = view.findViewById(R.id.dialogTaskInput);
        Button btnDate = view.findViewById(R.id.btnPickDate);
        Button btnTime = view.findViewById(R.id.btnPickTime);
        Button btnSave = view.findViewById(R.id.btnSave);

        Calendar calendar = Calendar.getInstance();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        btnDate.setOnClickListener(v -> new DatePickerDialog(this,
                (dateView, year, month, day) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    btnDate.setText("📅  " + day + "/" + (month + 1) + "/" + year);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());

        btnTime.setOnClickListener(v -> new TimePickerDialog(this,
                (timeView, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    btnTime.setText(String.format("🕐  %02d:%02d", hour, minute));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true).show());

        btnSave.setOnClickListener(v -> {
            String taskText = taskInput.getText().toString().trim();
            if (!taskText.isEmpty()) {
                String fullTask = taskText + "\n" +
                        android.text.format.DateFormat.format("MMM dd, yyyy - HH:mm", calendar);
                taskList.add(fullTask);
                adapter.notifyItemInserted(taskList.size() - 1);
                saveTasks();
                updateTaskCount();
                scheduleNotification(taskText, calendar);
                dialog.dismiss();
            } else {
                taskInput.setError("Enter a task name");
            }
        });

        dialog.show();
    }
}