package com.example.projectmanagement.utils;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class PhaseTouchHelperCallback extends ItemTouchHelper.Callback {

    public interface ItemTouchHelperAdapter {
        boolean onItemMove(int fromPosition, int toPosition);
    }

    public interface TaskMoveListener {
        boolean  onTaskMove(int fromPosition, int toPosition);
    }

//    private final ItemTouchHelperAdapter adapter;
    private final TaskMoveListener moveListener;
    private final RecyclerView rvBoard;      // RecyclerView ngang (board)
    private static final int EDGE_THRESHOLD = 100; // px
    private static final int SCROLL_STEP     = 50;  // px mỗi lần cuộn

    public PhaseTouchHelperCallback(TaskMoveListener moveListener, RecyclerView rvBoard) {
        this.moveListener = moveListener;
        this.rvBoard = rvBoard;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true; // Kích hoạt kéo bằng long press
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false; // Không cho phép swipe xóa
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Cho phép kéo sang trái và phải
        int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT |ItemTouchHelper.DOWN | ItemTouchHelper.UP;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder source,
                          @NonNull RecyclerView.ViewHolder target) {
//        return adapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return moveListener.onTaskMove(
                source.getAdapterPosition(),
                target.getAdapterPosition()
        );
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Không xử lý swipe
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c,
                                @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY,
                                int actionState,
                                boolean isCurrentlyActive) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY,
                actionState, isCurrentlyActive);

        // Khi đang drag và user vẫn giữ ngón tay
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && isCurrentlyActive) {
            View itemView = viewHolder.itemView;

            // Tọa độ của rvBoard trên màn hình
            int[] boardPos = new int[2];
            rvBoard.getLocationOnScreen(boardPos);
            int boardLeft  = boardPos[0];
            int boardRight = boardLeft + rvBoard.getWidth();

            // Tọa độ của itemView trên màn hình
            int[] itemPos = new int[2];
            itemView.getLocationOnScreen(itemPos);
            int itemLeft  = itemPos[0];
            int itemRight = itemLeft + itemView.getWidth();

            // Nếu item gần mép phải rvBoard => cuộn phải
            if (itemRight > boardRight - EDGE_THRESHOLD) {
                rvBoard.scrollBy(SCROLL_STEP, 0);
            }
            // Nếu item gần mép trái rvBoard => cuộn trái
            else if (itemLeft < boardLeft + EDGE_THRESHOLD) {
                rvBoard.scrollBy(-SCROLL_STEP, 0);
            }
        }
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        // (Tuỳ chọn) reset trạng thái nếu cần
    }
}
