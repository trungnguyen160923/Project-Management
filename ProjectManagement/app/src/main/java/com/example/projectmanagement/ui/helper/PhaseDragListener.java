package com.example.projectmanagement.ui.helper;

import android.graphics.Rect;
import android.view.DragEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
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
                
                // Cải thiện auto-scroll
                int[] boardPos = new int[2];
                boardRecyclerView.getLocationOnScreen(boardPos);
                float absX = event.getX() + boardPos[0];
                
                // Tính toán vị trí tương đối trong board
                float relativeX = absX - boardPos[0];
                float boardWidth = boardRecyclerView.getWidth();
                
                // Tăng tốc độ scroll khi gần mép
                int scrollSpeed = boardScrollAmount;
                if (relativeX < boardScrollThreshold) {
                    // Gần mép trái, tăng tốc độ scroll trái
                    float factor = 1.0f - (relativeX / boardScrollThreshold);
                    scrollSpeed = (int)(boardScrollAmount * (1 + factor));
                    boardRecyclerView.smoothScrollBy(-scrollSpeed, 0);
                    startAutoScroll();
                } else if (relativeX > boardWidth - boardScrollThreshold) {
                    // Gần mép phải, tăng tốc độ scroll phải
                    float factor = (relativeX - (boardWidth - boardScrollThreshold)) / boardScrollThreshold;
                    scrollSpeed = (int)(boardScrollAmount * (1 + factor));
                    boardRecyclerView.smoothScrollBy(scrollSpeed, 0);
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

    private void updatePlaceholders(DragEvent e) {
        DraggedTaskInfo info = (DraggedTaskInfo) e.getLocalState();
        if (info == null) return;

        // 1. Tọa độ tuyệt đối của điểm kéo
        int[] boardPos = new int[2];
        boardRecyclerView.getLocationOnScreen(boardPos);
        float absX = e.getX() + boardPos[0];
        float absY = e.getY() + boardPos[1];

        // 2. Xóa placeholder cũ
        clearAllPlaceholders();

        // 3. Tìm phase đang được hover
        int targetPhaseIdx = -1;
        float maxOverlap = 0;

        // Lấy tất cả phase positions từ adapter
        int totalPhases = phases.size();
        for (int i = 0; i < totalPhases; i++) {
            Phase phase = phases.get(i);
            RecyclerView.ViewHolder vh = boardRecyclerView.findViewHolderForAdapterPosition(i);
            if (vh == null) {
                // Nếu phase không visible, ước tính vị trí của nó
                // Lấy chiều rộng thực tế của phase từ layout
                int phaseWidth = (int) (300 * boardRecyclerView.getContext().getResources().getDisplayMetrics().density); // 300dp từ phase_item.xml
                int phaseMargin = (int) (16 * boardRecyclerView.getContext().getResources().getDisplayMetrics().density); // 8dp * 2 từ phase_item.xml
                int totalWidth = phaseWidth + phaseMargin;
                
                // Tính vị trí dựa trên orderIndex thay vì adapter position
                int estimatedLeft = phase.getOrderIndex() * totalWidth;
                int estimatedRight = estimatedLeft + phaseWidth;
                
                // Kiểm tra xem điểm kéo có nằm trong vùng ước tính không
                if (absX >= estimatedLeft && absX <= estimatedRight) {
                    targetPhaseIdx = i;
                    break;
                }
                continue;
            }

            View phaseView = vh.itemView;
            int[] phasePos = new int[2];
            phaseView.getLocationOnScreen(phasePos);

            // Tính diện tích giao nhau
            Rect phaseRect = new Rect(
                phasePos[0],
                phasePos[1],
                phasePos[0] + phaseView.getWidth(),
                phasePos[1] + phaseView.getHeight()
            );

            Rect dragRect = new Rect(
                (int)(absX - info.getTaskWidth()/2f),
                (int)(absY - info.getTaskHeight()/2f),
                (int)(absX + info.getTaskWidth()/2f),
                (int)(absY + info.getTaskHeight()/2f)
            );

            Rect intersect = new Rect();
            if (intersect.setIntersect(phaseRect, dragRect)) {
                float overlap = intersect.width() * intersect.height();
                if (overlap > maxOverlap) {
                    maxOverlap = overlap;
                    targetPhaseIdx = i;
                }
            }
        }

        // 4. Nếu tìm thấy phase đích, cập nhật placeholder
        if (targetPhaseIdx != -1) {
            RecyclerView.ViewHolder vh = boardRecyclerView.findViewHolderForAdapterPosition(targetPhaseIdx);
            if (vh != null) {
                View phaseView = vh.itemView;
                RecyclerView taskRv = phaseView.findViewById(R.id.rvTask);
                if (taskRv != null) {
                    RecyclerView.Adapter<?> raw = taskRv.getAdapter();
                    if (raw instanceof TaskAdapter) {
                        TaskAdapter adapter = (TaskAdapter) raw;
                        int[] rvPos = new int[2];
                        taskRv.getLocationOnScreen(rvPos);
                        float yRelRv = absY - rvPos[1];
                        int insertPos = calculateInsertionIndex(taskRv, yRelRv);
                        adapter.setPlaceholderPosition(
                            insertPos,
                            info.getTaskWidth(),
                            info.getTaskHeight()
                        );
                    }
                }
            }
        }
    }

    private void clearAllPlaceholders() {
        int total = phases.size();
        for (int pos = 0; pos < total; pos++) {
            RecyclerView.ViewHolder vh = boardRecyclerView.findViewHolderForAdapterPosition(pos);
            if (vh == null) continue;
            View phaseView = vh.itemView;
            RecyclerView taskRv = phaseView.findViewById(R.id.rvTask);
            if (taskRv == null) continue;
            RecyclerView.Adapter<?> raw = taskRv.getAdapter();
            if (!(raw instanceof TaskAdapter)) continue;
            ((TaskAdapter) raw).clearPlaceholder();
        }
    }

    private int calculateInsertionIndex(RecyclerView rv, float y) {
        // Lấy adapter position của item đầu tiên visible
        int firstVisible = ((LinearLayoutManager)rv.getLayoutManager()).findFirstVisibleItemPosition();
        if (firstVisible == RecyclerView.NO_POSITION) return 0;

        // Tính toán vị trí chính xác hơn
        int childCount = rv.getChildCount();
        float minDistance = Float.MAX_VALUE;
        int bestPosition = firstVisible;

        for (int i = 0; i < childCount; i++) {
            View child = rv.getChildAt(i);
            int adapterPos = rv.getChildAdapterPosition(child);
            if (adapterPos == RecyclerView.NO_POSITION) continue;

            // Tính khoảng cách từ điểm chạm đến giữa item
            float itemCenter = child.getTop() + child.getHeight() / 2f;
            float distance = Math.abs(y - itemCenter);

            if (distance < minDistance) {
                minDistance = distance;
                bestPosition = adapterPos;
            }
        }

        // Nếu điểm chạm nằm dưới item cuối cùng
        if (y > rv.getChildAt(childCount - 1).getBottom()) {
            return rv.getAdapter().getItemCount();
        }

        return bestPosition;
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

        // Lấy tất cả phase positions từ adapter
        int totalPhases = phases.size();
        for (int i = 0; i < totalPhases; i++) {
            Phase phase = phases.get(i);
            RecyclerView.ViewHolder vh = boardRecyclerView.findViewHolderForAdapterPosition(i);
            if (vh == null) {
                // Nếu phase không visible, ước tính vị trí của nó
                int phaseWidth = (int) (300 * boardRecyclerView.getContext().getResources().getDisplayMetrics().density);
                int phaseMargin = (int) (16 * boardRecyclerView.getContext().getResources().getDisplayMetrics().density);
                int totalWidth = phaseWidth + phaseMargin;
                
                // Tính vị trí dựa trên orderIndex
                int estimatedLeft = phase.getOrderIndex() * totalWidth;
                int estimatedRight = estimatedLeft + phaseWidth;
                
                // Kiểm tra xem điểm drop có nằm trong vùng ước tính không
                if (dropX >= estimatedLeft && dropX <= estimatedRight) {
                    targetPhaseIdx = i;
                    break;
                }
                continue;
            }

            View phaseView = vh.itemView;
            int[] phasePos = new int[2];
            phaseView.getLocationOnScreen(phasePos);
            Rect phaseRect = new Rect(
                    phasePos[0],
                    phasePos[1],
                    phasePos[0] + phaseView.getWidth(),
                    phasePos[1] + phaseView.getHeight()
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
            // Tính index drop trong phase đích
            Phase targetPhase = phases.get(targetPhaseIdx);
            RecyclerView.ViewHolder vh = boardRecyclerView.findViewHolderForAdapterPosition(targetPhaseIdx);
            
            if (vh != null) {
                // Nếu phase đích đang visible
                View targetPhaseView = vh.itemView;
                RecyclerView taskRv = targetPhaseView.findViewById(R.id.rvTask);
                int[] loc = new int[2];
                targetPhaseView.getLocationOnScreen(loc);
                float yRel = dropY - loc[1];
                TaskAdapter adapter = (TaskAdapter) taskRv.getAdapter();
                int dropIndex = Math.min(
                        Math.max(calculateInsertionIndex(taskRv, yRel), 0),
                        targetPhase.getTasks().size()
                );
                dropListener.onCardDropped(targetPhase, dropIndex, info);
            } else {
                // Nếu phase đích không visible, thêm task vào cuối phase
                dropListener.onCardDropped(targetPhase, targetPhase.getTasks().size(), info);
            }
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

