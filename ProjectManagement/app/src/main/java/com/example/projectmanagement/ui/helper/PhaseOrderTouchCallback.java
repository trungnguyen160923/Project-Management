package com.example.projectmanagement.ui.helper;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.data.service.PhaseService;
import com.example.projectmanagement.ui.adapter.PhaseAdapter;
import java.util.Collections;
import java.util.List;

public class PhaseOrderTouchCallback extends ItemTouchHelper.Callback {
    private final List<Phase> phases;
    private final PhaseAdapter adapter;
    private static final int SCROLL_THRESHOLD = 100; // pixels from edge to trigger scroll
    private static final int SCROLL_SPEED = 20; // pixels per scroll
    private boolean isScrolling = false;
    private RecyclerView recyclerView;
    private static final String TAG = "PhaseOrderTouchCallback";

    public PhaseOrderTouchCallback(List<Phase> phases, PhaseAdapter adapter) {
        this.phases = phases;
        this.adapter = adapter;
    }

    @Override public boolean isLongPressDragEnabled() { return true; }
    @Override public boolean isItemViewSwipeEnabled() { return false; }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        this.recyclerView = recyclerView;
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN| ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();

        // Validate indices
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= phases.size() || toPosition >= phases.size()) {
            Log.e(TAG, "Invalid move operation: fromPosition=" + fromPosition + 
                ", toPosition=" + toPosition + ", listSize=" + phases.size());
            return false;
        }

        // Notify adapter first
        adapter.notifyItemMoved(fromPosition, toPosition);

        // Update local list
        Phase movedPhase = phases.remove(fromPosition);
        phases.add(toPosition, movedPhase);

        // Update order index for all phases
        for (int i = 0; i < phases.size(); i++) {
            phases.get(i).setOrderIndex(i);
        }

        // Call API to update phase order
        PhaseService.movePhase(
            recyclerView.getContext(),
            movedPhase.getPhaseID(),
            toPosition,
            response -> {
                Log.d(TAG, "Phase order updated successfully");
            },
            error -> {
                Log.e(TAG, "Error updating phase order: " + error.getMessage());
                // Revert changes
                phases.remove(toPosition);
                phases.add(fromPosition, movedPhase);
                adapter.notifyItemMoved(toPosition, fromPosition);
                Toast.makeText(recyclerView.getContext(), 
                    "Failed to update phase order", Toast.LENGTH_SHORT).show();
            }
        );

        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Not used in this implementation
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (viewHolder == null) return;

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Add elevation to the dragged item
            if (viewHolder.itemView != null) {
                viewHolder.itemView.setElevation(8f);
                // Add scale animation
                viewHolder.itemView.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(150)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
            // Start auto-scroll check
            startAutoScroll();
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // Reset elevation and scale when drag ends
            if (viewHolder.itemView != null) {
                viewHolder.itemView.setElevation(0f);
                viewHolder.itemView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
            // Stop auto-scroll
            stopAutoScroll();
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        
        // Reset all item positions
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RecyclerView.ViewHolder child = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            child.itemView.animate()
                .translationX(0)
                .setDuration(150)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
        }
        
        // Reset elevation when drag ends
        if (viewHolder != null && viewHolder.itemView != null) {
            viewHolder.itemView.setElevation(0f);
        }
        // Stop auto-scroll
        stopAutoScroll();
    }

    private void startAutoScroll() {
        if (recyclerView == null) return;
        
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (!isScrolling || recyclerView == null) return;
                
                int[] location = new int[2];
                recyclerView.getLocationOnScreen(location);
                int recyclerViewTop = location[1];
                int recyclerViewBottom = recyclerViewTop + recyclerView.getHeight();
                
                // Get the current touch position
                RecyclerView.ViewHolder draggingViewHolder = null;
                if (recyclerView.getFocusedChild() != null) {
                    int position = recyclerView.getChildAdapterPosition(recyclerView.getFocusedChild());
                    if (position != RecyclerView.NO_POSITION) {
                        draggingViewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                    }
                }
                
                if (draggingViewHolder == null || draggingViewHolder.itemView == null) return;
                
                draggingViewHolder.itemView.getLocationOnScreen(location);
                int itemY = location[1];
                
                // Calculate scroll amount based on position
                int scrollAmount = 0;
                if (itemY < recyclerViewTop + SCROLL_THRESHOLD) {
                    // Scroll up
                    scrollAmount = -SCROLL_SPEED;
                } else if (itemY + draggingViewHolder.itemView.getHeight() > 
                          recyclerViewBottom - SCROLL_THRESHOLD) {
                    // Scroll down
                    scrollAmount = SCROLL_SPEED;
                }
                
                if (scrollAmount != 0) {
                    recyclerView.smoothScrollBy(0, scrollAmount);
                    recyclerView.postDelayed(this, 16); // ~60fps
                } else {
                    isScrolling = false;
                }
            }
        });
    }

    private void stopAutoScroll() {
        isScrolling = false;
    }

    @Override
    public void onChildDraw(@NonNull android.graphics.Canvas c, @NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                           int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        
        if (isCurrentlyActive) {
            // Get the dragged item's position
            int draggedPosition = viewHolder.getAdapterPosition();
            if (draggedPosition == RecyclerView.NO_POSITION) return;
            
            // Get the center X position of the dragged item
            float draggedItemCenterX = viewHolder.itemView.getLeft() + viewHolder.itemView.getWidth() / 2f;
            
            // Find the item that should be displaced
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                RecyclerView.ViewHolder child = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                if (child.getAdapterPosition() == draggedPosition) continue;
                
                float childCenterX = child.itemView.getLeft() + child.itemView.getWidth() / 2f;
                float distance = Math.abs(draggedItemCenterX - childCenterX);
                
                // Only displace items that are very close to the dragged item
                if (distance < viewHolder.itemView.getWidth() * 0.5f) {
                    float displacement;
                    if (draggedItemCenterX > childCenterX) {
                        // Push left with smaller displacement
                        displacement = -viewHolder.itemView.getWidth() * 0.3f;
                    } else {
                        // Push right with smaller displacement
                        displacement = viewHolder.itemView.getWidth() * 0.3f;
                    }
                    
                    // Animate the displacement with shorter duration
                    child.itemView.animate()
                        .translationX(displacement)
                        .setDuration(100)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
                } else {
                    // Reset position if items are far apart
                    child.itemView.animate()
                        .translationX(0)
                        .setDuration(100)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
                }
            }
        }
    }
}

