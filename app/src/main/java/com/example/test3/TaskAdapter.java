package com.example.test3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<String> tasks;
    private Context context;
    private Runnable onTaskChanged; // FIX: callback to save after delete

    public TaskAdapter(Context context, ArrayList<String> tasks, Runnable onTaskChanged) {
        this.context = context;
        this.tasks = tasks;
        this.onTaskChanged = onTaskChanged;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.textTask.setText(tasks.get(position));

        holder.btnDelete.setOnClickListener(v -> {
            // FIX: use getAdapterPosition() to avoid stale position
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) {
                tasks.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, tasks.size());
                onTaskChanged.run(); // FIX: save after delete
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textTask;
        ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textTask = itemView.findViewById(R.id.textTask);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}