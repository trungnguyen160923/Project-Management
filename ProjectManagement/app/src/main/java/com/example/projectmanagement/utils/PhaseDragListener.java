package com.example.projectmanagement.utils;

import android.graphics.Rect;
import android.view.DragEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectmanagement.R;
import com.example.projectmanagement.ui.adapter.TaskAdapter;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import java.util.List;

public class PhaseDragListener implements View.OnDragListener {
    private final RecyclerView boardRecyclerView;
    private final List<Phase> phases;
    private final OnCardDropListener dropListener;
    private final int boardScrollThreshold;
    private final int boardScrollAmount;

    private boolean isAutoScrolling = false;
    private float lastLocalX;
    private Runnable autoScrollRunnable = null;

    public PhaseDragListener(@NonNull RecyclerView boardRecyclerView,
                             @NonNull List<Phase> phases,
                             @NonNull OnCardDropListener dropListener,
                             int boardScrollThreshold,
                             int boardScrollAmount) {
        this.boardRecyclerView    = boardRecyclerView;
        this.phases               = phases;
        this.dropListener         = dropListener;
        this.boardScrollThreshold = boardScrollThreshold;
        this.boardScrollAmount    = boardScrollAmount;

        // Khởi tạo auto-scroll runnable
        autoScrollRunnable = () -> {
            if (lastLocalX < boardScrollThreshold) {
                boardRecyclerView.smoothScrollBy(-boardScrollAmount, 0);
            } else if (lastLocalX > boardRecyclerView.getWidth() - boardScrollThreshold) {
                boardRecyclerView.smoothScrollBy(boardScrollAmount, 0);
            }
            // Nếu vẫn đang ở vùng kích hoạt, tiếp tục post
            if (isAutoScrolling) {
                boardRecyclerView.postDelayed(autoScrollRunnable, 50);
            }
        };
    }

    private void startAutoScroll() {
        if (!isAutoScrolling) {
            isAutoScrolling = true;
            boardRecyclerView.post(autoScrollRunnable);
        }
    }
    private void stopAutoScroll() {
        isAutoScrolling = false;
        boardRecyclerView.removeCallbacks(autoScrollRunnable);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_LOCATION:
                // Lấy tọa độ relative trong board
                lastLocalX = event.getX();
                // Auto-scroll nếu gần mép
                if (lastLocalX < boardScrollThreshold
                        || lastLocalX > boardRecyclerView.getWidth() - boardScrollThreshold) {
                    startAutoScroll();
                } else {
                    stopAutoScroll();
                }
                // Cập nhật placeholder cho từng phase
                updatePlaceholders(event);
                break;

            case DragEvent.ACTION_DRAG_EXITED:
            case DragEvent.ACTION_DRAG_ENDED:
                // Xoá hết placeholder và dừng auto-scroll
                clearAllPlaceholders();
                stopAutoScroll();
                break;

            case DragEvent.ACTION_DROP:
                // Thực thi drop
                handleDrop(event);
                // Xoá placeholder và dừng auto-scroll
                clearAllPlaceholders();
                stopAutoScroll();
                break;
        }
        return true;
    }

    private void updatePlaceholders(DragEvent event) {
        // Tọa độ tuyệt đối của điểm drag trên màn hình
        int[] boardLoc = new int[2];
        boardRecyclerView.getLocationOnScreen(boardLoc);
        float dropX = event.getX() + boardLoc[0];
        float dropY = event.getY() + boardLoc[1];

        // Duyệt qua mỗi phase để tìm phase chứa điểm drop
        for (int i = 0; i < boardRecyclerView.getChildCount(); i++) {
            View phaseView = boardRecyclerView.getChildAt(i);
            int[] loc = new int[2];
            phaseView.getLocationOnScreen(loc);
            Rect phaseRect = new Rect(
                    loc[0], loc[1],
                    loc[0] + phaseView.getWidth(),
                    loc[1] + phaseView.getHeight()
            );

            RecyclerView taskRv = phaseView.findViewById(R.id.rvTask);
            TaskAdapter adapter = (TaskAdapter) taskRv.getAdapter();

            if (phaseRect.contains((int)dropX, (int)dropY)) {
                // Tính vị trí sẽ insert placeholder
                float yRel = dropY - loc[1];
                int placeholderIndex = calculateInsertionIndex(taskRv, yRel);
                adapter.setPlaceholderPosition(placeholderIndex);  // hiển thị placeholder :contentReference[oaicite:0]{index=0}
            } else {
                adapter.clearPlaceholder();                        // xoá placeholder :contentReference[oaicite:1]{index=1}
            }
        }
    }

    private void clearAllPlaceholders() {
        for (int i = 0; i < boardRecyclerView.getChildCount(); i++) {
            View phaseView = boardRecyclerView.getChildAt(i);
            RecyclerView taskRv = phaseView.findViewById(R.id.rvTask);
            TaskAdapter adapter = (TaskAdapter) taskRv.getAdapter();
            adapter.clearPlaceholder();                            // xoá placeholder :contentReference[oaicite:2]{index=2}
        }
    }

    private int calculateInsertionIndex(RecyclerView rv, float y) {
        int childCount = rv.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = rv.getChildAt(i);
            if (y < child.getTop() + child.getHeight() / 2f) {
                return i;
            }
        }
        // Nếu vượt hết, placeholder nằm ở cuối
        TaskAdapter adapter = (TaskAdapter) rv.getAdapter();
        return adapter.getItemCount();
    }

    private void handleDrop(DragEvent event) {
        DraggedTaskInfo info = (DraggedTaskInfo) event.getLocalState();
        if (info == null) return;

        // Tọa độ drop tuyệt đối
        int[] boardLoc = new int[2];
        boardRecyclerView.getLocationOnScreen(boardLoc);
        float dropX = event.getX() + boardLoc[0];
        float dropY = event.getY() + boardLoc[1];

        // Tìm phase có diện tích giao nhau lớn nhất
        float maxArea = 0;
        int targetPhaseIdx = -1;
        Rect cardRect = new Rect(
                (int)(dropX - info.getTaskWidth()/2f),
                (int)(dropY - info.getTaskHeight()/2f),
                (int)(dropX + info.getTaskWidth()/2f),
                (int)(dropY + info.getTaskHeight()/2f)
        );

        for (int i = 0; i < boardRecyclerView.getChildCount(); i++) {
            View pv = boardRecyclerView.getChildAt(i);
            int[] loc = new int[2];
            pv.getLocationOnScreen(loc);
            Rect phaseRect = new Rect(
                    loc[0], loc[1],
                    loc[0] + pv.getWidth(),
                    loc[1] + pv.getHeight()
            );
            Rect intersect = new Rect();
            if (intersect.setIntersect(cardRect, phaseRect)) {
                float area = intersect.width() * intersect.height();
                if (area > maxArea) {
                    maxArea = area;
                    targetPhaseIdx = i;
                }
            }
        }

        if (targetPhaseIdx != -1) {
            // Tính index drop trong phase đích và gọi listener
            View targetPhaseView = boardRecyclerView.getChildAt(targetPhaseIdx);
            RecyclerView taskRv = targetPhaseView.findViewById(R.id.rvTask);
            int[] loc = new int[2];
            targetPhaseView.getLocationOnScreen(loc);
            float yRel = dropY - loc[1];
            TaskAdapter adapter = (TaskAdapter) taskRv.getAdapter();
            int dropIndex = Math.min(
                    Math.max(calculateInsertionIndex(taskRv, yRel), 0),
                    phases.get(targetPhaseIdx).getTasks().size()
            );
            dropListener.onCardDropped(
                    phases.get(targetPhaseIdx),
                    dropIndex,
                    info
            );
        } else {
            // Không tìm được phase đích: hoàn tác
            info.getPhase().getTasks().add(info.getOriginalPosition(), info.getTask());
            dropListener.onCardDropped(info.getPhase(), info.getOriginalPosition(), info);
        }
    }

    /**
     * Interface để ProjectActivity/PhaseAdapter bắt event khi drop xong
     */
    public interface OnCardDropListener {
        void onCardDropped(Phase targetPhase, int position, DraggedTaskInfo info);
    }
}
