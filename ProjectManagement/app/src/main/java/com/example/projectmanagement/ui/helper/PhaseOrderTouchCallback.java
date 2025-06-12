package com.example.projectmanagement.ui.helper;

import android.annotation.SuppressLint;
import android.util.Log;
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
    @Override public boolean onMove(@NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder src,
                                    @NonNull RecyclerView.ViewHolder target) {
        int oldIndex = src.getAdapterPosition();
        int newIndex = target.getAdapterPosition();

        // Check if list is empty or indices are invalid
        if (phases == null || phases.isEmpty() || oldIndex < 0 || newIndex < 0 || 
            oldIndex >= phases.size() || newIndex >= phases.size()) {
            Log.e("PhaseOrderTouchCallback", "Invalid move operation: oldIndex=" + oldIndex + 
                ", newIndex=" + newIndex + ", listSize=" + (phases != null ? phases.size() : 0));
            return false;
        }

        // Notify adapter about the move first to trigger animation
        adapter.notifyItemMoved(oldIndex, newIndex);

        // Swap phases in the list
        Collections.swap(phases, oldIndex, newIndex);

        // Update order index for all phases
        for (int i = 0; i < phases.size(); i++) {
            phases.get(i).setOrderIndex(i);
        }

        // Call API to update order on server
        if (oldIndex != newIndex) {
            // Create final variables for lambda
            final int fromIndex = oldIndex;
            final int toIndex = newIndex;
            final Phase phase = phases.get(toIndex);

            PhaseService.movePhase(
                    rv.getContext(),
                    phase.getPhaseID(),
                    toIndex,
                    response -> {
                        Log.d("PhaseOrderTouchCallback",
                                "Phase " + phase.getPhaseID() + " moved to position " + toIndex);
                    },
                    error -> {
                        Log.e("PhaseOrderTouchCallback", "Error moving phase: " + error.getMessage());
                        // Revert the change if API call fails
                        if (phases != null && !phases.isEmpty() && 
                            fromIndex < phases.size() && toIndex < phases.size()) {
                            Collections.swap(phases, toIndex, fromIndex);
                            for (int j = 0; j < phases.size(); j++) {
                                phases.get(j).setOrderIndex(j);
                            }
                            // Notify adapter about the revert with animation
                            adapter.notifyItemMoved(toIndex, fromIndex);
                        }
                    });
        }

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
            isScrolling = true;
            startAutoScroll();
            
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
                
                // If items are close enough, animate the displacement
                if (distance < viewHolder.itemView.getWidth() * 1.5f) {
                    float displacement;
                    if (draggedItemCenterX > childCenterX) {
                        // Push left
                        displacement = -viewHolder.itemView.getWidth();
                    } else {
                        // Push right
                        displacement = viewHolder.itemView.getWidth();
                    }
                    
                    // Animate the displacement
                    child.itemView.animate()
                        .translationX(displacement)
                        .setDuration(150)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
                } else {
                    // Reset position if items are far apart
                    child.itemView.animate()
                        .translationX(0)
                        .setDuration(150)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
                }
            }
        }
    }
}

