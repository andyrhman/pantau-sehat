package com.example.pantausehat.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;

import java.util.Objects;

public class MedicationAdapter
        extends ListAdapter<Medication, MedicationAdapter.MedViewHolder> {

    public interface OnItemActionListener {
        void onDelete(Medication med);
    }
    private final OnItemActionListener actionListener;

    public MedicationAdapter(OnItemActionListener listener) {
        super(DIFF_CALLBACK);
        this.actionListener = listener;
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
    public void onBindViewHolder(@NonNull MedViewHolder h, int pos) {
        Medication med = getItem(pos);
        h.tvName.setText(med.name);
        h.tvDosage.setText(med.dosage);
        // format time as hh:mm AM/PM
        int hour = med.hour;
        boolean pm = hour >= 12;
        int displayHour = (hour % 12 == 0) ? 12 : hour % 12;
        String minute = String.format("%02d", med.minute);
        String ampm = pm ? "PM" : "AM";
        h.tvTime.setText(displayHour + ":" + minute + " " + ampm);
        h.ivMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            menu.getMenu().add("Delete");
            menu.setOnMenuItemClickListener(item -> {
                if ("Delete".equals(item.getTitle())) {
                    actionListener.onDelete(med);
                    return true;
                }
                return false;
            });
            menu.show();
        });
    }

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvTime;
        ImageView ivMenu;
        MedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime   = itemView.findViewById(R.id.tvTime);
            ivMenu   = itemView.findViewById(R.id.ivMenu);
        }
    }
}
