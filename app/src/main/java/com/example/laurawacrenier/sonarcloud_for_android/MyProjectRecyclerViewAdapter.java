package com.example.laurawacrenier.sonarcloud_for_android;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
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
        Project project = mValues.get(position);
        holder.mItem = project;
        holder.mProjectNameView.setText(project.name);
        String alert_status = project.measures.get("alert_status");
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
        holder.mBugs.setText(project.measures.get("bugs"));
        holder.mVulnerabilities.setText(project.measures.get("vulnerabilities"));
        holder.mCodeSmells.setText(project.measures.get("codeSmells"));

        String reliability_rating = project.measures.get("reliability_rating");
        holder.mReliabilityRating.setText(getRating(reliability_rating));
        holder.mReliabilityRating.setBackgroundResource(getBackGroundRating(reliability_rating));

        String security_rating = project.measures.get("security_rating");
        holder.mSecurityRating.setText(getRating(security_rating));
        holder.mSecurityRating.setBackgroundResource(getBackGroundRating(security_rating));

        String sqale_rating = project.measures.get("sqale_rating");
        holder.mMaintainabilityRating.setText(getRating(sqale_rating));
        holder.mMaintainabilityRating.setBackgroundResource(getBackGroundRating(sqale_rating));

        String coverage = project.measures.get("coverage");
        holder.mCoverageNumber.setText(coverage != null ? coverage + "%" : "");
        String dupl = project.measures.get("duplicated_lines_density");
        holder.mDuplicationsNumber.setText(dupl != null ? dupl + "%" : "");
        String ncloc = project.measures.get("ncloc");
        if (ncloc != null && ncloc.length() > 0) {
            holder.mLoCNumber.setText(format(Integer.parseInt(ncloc)));
            holder.mSizeBadge.setText(getNclocSize(Integer.parseInt(ncloc)));
            holder.mSizeBadge.setVisibility(View.VISIBLE);
        } else {
            holder.mLoCNumber.setText("");
            holder.mSizeBadge.setVisibility(View.INVISIBLE);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private static String[] suffix = new String[]{"","k", "m", "b", "t"};
    private static int MAX_LENGTH = 4;

    private static String format(double number) {
        String r = new DecimalFormat("##0E0").format(number);
        r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
        while(r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")){
            r = r.substring(0, r.length()-2) + r.substring(r.length() - 1);
        }
        return r;
    }

    private String getNclocSize(int i) {
        if (i < 1000) {
            return "XS";
        }
        if (i < 10000) {
            return "S";
        }
        if (i < 100_000) {
            return "M";
        }
        if (i < 500_000) {
            return "L";
        }
        return "XL";
    }

    private int getBackGroundRating(String rating) {
        switch (rating) {
            case "1.0":
                return R.drawable.chip_rating_badge_a;
            case "2.0":
                return R.drawable.chip_rating_badge_b;
            case "3.0":
                return R.drawable.chip_rating_badge_c;
            case "4.0":
                return R.drawable.chip_rating_badge_d;
            case "5.0":
                return R.drawable.chip_rating_badge_e;
            default:
                throw new IllegalStateException("Unknow rating: " + rating);
        }
    }

    private String getRating(String rating) {
        switch (rating) {
            case "1.0":
                return "A";
            case "2.0":
                return "B";
            case "3.0":
                return "C";
            case "4.0":
                return "D";
            case "5.0":
                return "E";
            default:
                throw new IllegalStateException("Unknow rating: " + rating);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mProjectNameView;
        public final TextView mProjectAlertStatusView;
        public final TextView mBugs;
        public final TextView mVulnerabilities;
        public final TextView mCodeSmells;
        public final TextView mReliabilityRating;
        public final TextView mSecurityRating;
        public final TextView mMaintainabilityRating;
        public final TextView mCoverageNumber;
        public final TextView mDuplicationsNumber;
        public final TextView mLoCNumber;
        public final TextView mSizeBadge;
        public Project mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mProjectNameView = (TextView) view.findViewById(R.id.ProjectName);
            mProjectAlertStatusView = (TextView) view.findViewById(R.id.QGStatus);
            mBugs = (TextView) view.findViewById(R.id.BugsNumber);
            mVulnerabilities = (TextView) view.findViewById(R.id.VulnNumber);
            mCodeSmells = (TextView) view.findViewById(R.id.CodeSmellNumber);
            mReliabilityRating = (TextView) view.findViewById(R.id.ReliabilityRating);
            mSecurityRating = (TextView) view.findViewById(R.id.SecurityRating);
            mMaintainabilityRating = (TextView) view.findViewById(R.id.MaintainabilityRating);
            mCoverageNumber = (TextView) view.findViewById(R.id.CoverageNumber);
            mDuplicationsNumber = (TextView) view.findViewById(R.id.DuplicationsNumber);
            mLoCNumber = (TextView) view.findViewById(R.id.LoCNumber);
            mSizeBadge = (TextView) view.findViewById(R.id.SizeBadge);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mProjectNameView.getText() + "'";
        }
    }
}
