package com.example.pantausehat.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;

import java.util.Objects;

public class MedicationAdapter extends ListAdapter<Medication, MedicationAdapter.MedViewHolder> {

    public MedicationAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Medication> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Medication>() {
                @Override
                public boolean areItemsTheSame(@NonNull Medication oldItem, @NonNull Medication newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Medication oldItem, @NonNull Medication newItem) {
                    return oldItem.name.equals(newItem.name)
                            && oldItem.dosage.equals(newItem.dosage)
                            && oldItem.frequency.equals(newItem.frequency)
                            && oldItem.hour == newItem.hour
                            && oldItem.minute == newItem.minute;
                }
            };

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medication med = getItem(position);
        holder.tvName.setText(med.name != null ? med.name : "");
        holder.tvDosage.setText(med.dosage != null ? med.dosage : "");
        // format time as hh:mm AM/PM
        int hour = med.hour;
        boolean pm = hour >= 12;
        int displayHour = (hour % 12 == 0) ? 12 : hour % 12;
        String minute = String.format("%02d", med.minute);
        String ampm = pm ? "PM" : "AM";
        holder.tvTime.setText(displayHour + ":" + minute + " " + ampm);
        holder.cbTaken.setChecked(false);  // you can later hook this to your log
    }

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTime;
        CheckBox cbTaken;

        MedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime   = itemView.findViewById(R.id.tvTime);
            cbTaken  = itemView.findViewById(R.id.cbTaken);
        }
    }
}
