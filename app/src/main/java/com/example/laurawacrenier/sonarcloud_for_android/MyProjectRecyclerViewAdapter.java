package com.example.laurawacrenier.sonarcloud_for_android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyProjectRecyclerViewAdapter extends RecyclerView.Adapter<MyProjectRecyclerViewAdapter.ViewHolder> {

    private final List<Project> mValues = new CopyOnWriteArrayList<>();

    public List<Project> getValues() {
        return mValues;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_projects_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mProjectNameView.setText(mValues.get(position).name);
        String alert_status = mValues.get(position).alert_status;
        switch (alert_status) {
            case "OK":
                holder.mProjectAlertStatusView.setBackgroundResource(R.drawable.chip_quality_gate_passed);
                holder.mProjectAlertStatusView.setText("Passed");
                break;
            case "WARN":
                holder.mProjectAlertStatusView.setBackgroundResource(R.drawable.chip_quality_gate_warning);
                holder.mProjectAlertStatusView.setText("Warning");
                break;
            case "ERROR":
                holder.mProjectAlertStatusView.setBackgroundResource(R.drawable.chip_quality_gate_failed);
                holder.mProjectAlertStatusView.setText("Failed");
                break;
            default:
                throw new IllegalStateException("Unknow status: " + alert_status);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mProjectNameView;
        public final TextView mProjectAlertStatusView;
        public Project mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mProjectNameView = (TextView) view.findViewById(R.id.ProjectName);
            mProjectAlertStatusView = (TextView) view.findViewById(R.id.QGStatus);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mProjectNameView.getText() + "'";
        }
    }
}
