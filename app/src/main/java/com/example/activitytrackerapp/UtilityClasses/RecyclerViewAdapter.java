package com.example.activitytrackerapp.UtilityClasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activitytrackerapp.R;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    List<Project> project_list;

    public RecyclerViewAdapter(Context context, List<Project> project_list) {
        this.context = context;
        this.project_list = project_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        Project project = project_list.get(position);
        holder.projectName.setText("Project: "+project.getProjectName());
        holder.Description.setText("Description: "+project.getDescription());
        holder.startDate.setText("Start Date:- "+project.getStartTime());
        holder.submitDate.setText("Submit Date:- "+project.getSubmitTime());
        holder.woNumber.setText("W.O Number: "+project.getWoNumber());
        holder.runningTime.setText("Running Time:- "+project.getRunningTime());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.description.getVisibility() == View.VISIBLE) {
                    holder.description.setVisibility(View.GONE);
                }
                else {
                    holder.description.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return project_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView projectName, woNumber, startDate, submitDate, Description, runningTime;
        public ConstraintLayout description;

        public ViewHolder(View itemView) {
            super(itemView);
            description = (ConstraintLayout) itemView.findViewById(R.id.description);
            projectName = (TextView) itemView.findViewById(R.id.proj_name);
            woNumber = (TextView) itemView.findViewById(R.id.proj_woNumber);
            startDate = (TextView) itemView.findViewById(R.id.proj_start);
            submitDate = (TextView) itemView.findViewById(R.id.proj_submit);
            Description = (TextView) itemView.findViewById(R.id.proj_desc);
            runningTime = (TextView) itemView.findViewById(R.id.proj_running);
        }
    }
}
