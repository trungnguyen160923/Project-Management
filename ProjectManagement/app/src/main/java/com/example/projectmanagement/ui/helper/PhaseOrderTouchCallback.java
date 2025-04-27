package com.example.projectmanagement.ui.helper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.ui.adapter.PhaseAdapter;
import java.util.Collections;
import java.util.List;

public class PhaseOrderTouchCallback extends ItemTouchHelper.Callback {
    private final List<Phase> phases;
    private final PhaseAdapter adapter;

    public PhaseOrderTouchCallback(List<Phase> phases, PhaseAdapter adapter) {
        this.phases = phases;
        this.adapter = adapter;
    }

    @Override public boolean isLongPressDragEnabled() { return true; }
    @Override public boolean isItemViewSwipeEnabled() { return false; }

    @Override public int getMovementFlags(@NonNull RecyclerView rv,
                                          @NonNull RecyclerView.ViewHolder vh) {
        return makeMovementFlags(ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT,0);
    }

    @Override public boolean onMove(@NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder src,
                                    @NonNull RecyclerView.ViewHolder tgt) {
        int f=src.getAdapterPosition(), t=tgt.getAdapterPosition();
        Collections.swap(phases,f,t);
        for(int i=0;i<phases.size();i++) phases.get(i).setOrderIndex(i);
        adapter.notifyItemMoved(f,t);
        return true;
    }

    @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh,int dir){}
}

