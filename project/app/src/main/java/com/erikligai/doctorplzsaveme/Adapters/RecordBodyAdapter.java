package com.erikligai.doctorplzsaveme.Adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.erikligai.doctorplzsaveme.Activities.EditRecordActivity;
import com.erikligai.doctorplzsaveme.Models.Record;
import com.erikligai.doctorplzsaveme.R;
import com.erikligai.doctorplzsaveme.backend.Backend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.support.constraint.Constraints.TAG;

public class RecordBodyAdapter extends RecyclerView.Adapter<RecordBodyAdapter.MyViewHolder> implements Filterable {
    private final ArrayList<Record> mDataset;
    private ArrayList<Record> mRecordsCopy;

    private int problem_index;

//    MainRecordActivity parent_activity;

//    public void setParentActivity(MainRecordActivity a) { parent_activity = a; }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        final TextView title;
        final TextView date;
        final TextView comment;
        final ImageButton option;

        MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);

            title = v.findViewById(R.id.record_item_title);
            date = v.findViewById(R.id.record_item_date);
            comment = v.findViewById(R.id.record_item_comment);
            option = v.findViewById(R.id.record_item_options);
        }

        @Override
        public void onClick(View v) {
            if (mOnEntryClickListener != null) {
                mOnEntryClickListener.onEntryClick(getAdapterPosition());
            }
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecordBodyAdapter(ArrayList<Record> myDataset, int problem_index) {
        this.mDataset = myDataset;
        this.mRecordsCopy = new ArrayList<>(mDataset);
        this.problem_index = problem_index;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public RecordBodyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record, parent, false);
        return new RecordBodyAdapter.MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull final RecordBodyAdapter.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Record record = mDataset.get(position);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
        Date date = record.getDate();
        String f_date = df.format(date);
        String title_data = record.getTitle();
        String comment_data = record.getComment();
        if (title_data.equals("")){
            holder.title.setText("<no title>");
        } else {
            holder.title.setText(title_data);
        }
        if (comment_data.equals("")){
            holder.comment.setText("<no comment>");
        } else {
            holder.comment.setText(comment_data);
        }
        holder.date.setText(f_date);
        holder.option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //creating a popup menu
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                //inflating menu from xml resource
                popup.inflate(R.menu.menu_record_options);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.record_menu1:
                                //handle menu1 click
                                ClickMenuOne();
                                return true;

                            case R.id.record_menu2:
                                //handle menu2 click
                                ClickMenuTwo();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();

            }

            void ClickMenuOne() {
                Log.d("rview", "view/edit");
                Log.d("rview", Integer.toString(holder.getAdapterPosition()));
                Intent intent = new Intent(holder.itemView.getContext(), EditRecordActivity.class);
                intent.putExtra("R_Pos", holder.getAdapterPosition());
                intent.putExtra("P_Pos", problem_index);
                Log.d("rview", "edit");
                holder.itemView.getContext().startActivity(intent);
            }

            void ClickMenuTwo() {
                Log.d("rview", "delete");
                Backend.getInstance().deletePatientRecord(problem_index, holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private RecordBodyAdapter.OnEntryClickListener mOnEntryClickListener;

    public interface OnEntryClickListener {
        void onEntryClick(int position);
    }

    public void setOnEntryClickListener(RecordBodyAdapter.OnEntryClickListener onEntryClickListener) {
        mOnEntryClickListener = onEntryClickListener;
    }

    @Override
    public Filter getFilter() {
        return patientProblemFilter;
    }

    private Filter patientProblemFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Record> filteredRecords = new ArrayList<>();

            Log.d(TAG, "search: " + constraint);

            if (constraint == null || constraint.length() == 0) {
                filteredRecords.addAll(mRecordsCopy);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                Log.d(TAG, "filterPattern:  " + filterPattern);


                for (Record record : mRecordsCopy) {
                    Log.d(TAG, "problemTitle: " + record.getTitle().toLowerCase());

                    // here photoid of record is accessed
                    String myPhotoID = record.getPhotoid();

                    // parallel arrays
                    ArrayList<String> current_patient_ids = Backend.getInstance().getPatientProfile().getPhotoIds();
                    ArrayList<String> current_patient_labels = Backend.getInstance().getPatientProfile().getPhotoLabels();

                    int counter = 0;

                    for (String photo_id : current_patient_ids) {
                        if (myPhotoID.equals(photo_id)) {
                            if (current_patient_labels.get(counter).toLowerCase().contains(filterPattern)) {
                                filteredRecords.add(record);
                            }
                        }
                        counter++;
                    }


//                    if (record.getTitle().toLowerCase().contains(filterPattern)) {
//                        filteredRecords.add(record);
//                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredRecords;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mDataset.clear();
            mDataset.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };
}
