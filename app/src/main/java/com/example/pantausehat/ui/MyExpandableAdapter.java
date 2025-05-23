package com.example.pantausehat.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;

import com.example.pantausehat.R;
import com.example.pantausehat.data.Medication;

import java.util.List;

public class MyExpandableAdapter extends BaseExpandableListAdapter {
    public interface OnChildDeleteListener {
        void onDeleteChild(Medication med);
    }
    public interface OnGroupDeleteListener {
        void onDeleteGroup(long groupId);
    }

    private final Context ctx;
    private final List<Long> groupIds;
    private final List<String> titles;
    private final List<List<Medication>> children;
    private final OnChildDeleteListener childDeleteListener;
    private final OnGroupDeleteListener groupDeleteListener;

    public MyExpandableAdapter(Context ctx,
                               List<Long> groupIds,
                               List<String> titles,
                               List<List<Medication>> children,
                               OnChildDeleteListener childDeleteListener,
                               OnGroupDeleteListener groupDeleteListener) {
        this.ctx                   = ctx;
        this.groupIds              = groupIds;
        this.titles                = titles;
        this.children              = children;
        this.childDeleteListener   = childDeleteListener;
        this.groupDeleteListener   = groupDeleteListener;
    }

    @Override public int getGroupCount()            { return titles.size(); }
    @Override public int getChildrenCount(int g)   { return children.get(g).size(); }
    @Override public Object getGroup(int g)        { return titles.get(g); }
    @Override public Object getChild(int g, int c) { return children.get(g).get(c); }
    @Override public long getGroupId(int g)        { return groupIds.get(g); }
    @Override public long getChildId(int g, int c) { return children.get(g).get(c).id; }
    @Override public boolean hasStableIds()        { return true; }

    @Override
    public View getGroupView(int groupPos, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_medication_group, parent, false);
        }
        TextView tv       = convertView.findViewById(R.id.tvGroupTitle);
        ImageView ivMenu  = convertView.findViewById(R.id.ivGroupMenu);

        tv.setText(titles.get(groupPos));
        // rotate your custom chevron

        ivMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(ctx, ivMenu);
            menu.getMenu().add("Hapus semua");
            menu.setOnMenuItemClickListener(item -> {
                if ("Hapus semua".equals(item.getTitle())) {
                    long groupId = groupIds.get(groupPos);
                    groupDeleteListener.onDeleteGroup(groupId);
                    return true;
                }
                return false;
            });
            menu.show();
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPos, int childPos,
                             boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_medication, parent, false);
        }
        Medication m    = children.get(groupPos).get(childPos);
        TextView tvName   = convertView.findViewById(R.id.tvMedName);
        TextView tvDosage = convertView.findViewById(R.id.tvDosage);
        TextView tvTime   = convertView.findViewById(R.id.tvTime);
        ImageView ivMenu  = convertView.findViewById(R.id.ivMenu);

        tvName.setText(m.name);
        tvDosage.setText(m.dosage);
        boolean pm = m.hour >= 12;
        int h12 = (m.hour % 12 == 0) ? 12 : (m.hour % 12);
        tvTime.setText(String.format("%d:%02d %s", h12, m.minute, pm ? "PM" : "AM"));

        ivMenu.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(ctx, ivMenu);
            menu.getMenu().add("Hapus");
            menu.setOnMenuItemClickListener(item -> {
                if ("Hapus".equals(item.getTitle())) {
                    childDeleteListener.onDeleteChild(m);
                    return true;
                }
                return false;
            });
            menu.show();
        });

        return convertView;
    }

    @Override public boolean isChildSelectable(int g, int c) { return true; }
}

