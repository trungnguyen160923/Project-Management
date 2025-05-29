package com.example.projectmanagement.utils;

import android.graphics.Rect;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.ui.adapter.PhaseAdapter;
import com.example.projectmanagement.ui.adapter.TaskAdapter;

import java.util.List;

public class PhaseDragListener implements View.OnDragListener {

    private RecyclerView boardRecyclerView;
    private List<Phase> phases;
    private PhaseAdapter phaseAdapter;
    private int boardScrollThreshold;
    private int boardScrollAmount;

    // Dùng để tự động cuộn với tốc độ cố định
    private Runnable autoScrollRunnable;
    private boolean isAutoScrolling = false;
    // Lưu trữ tọa độ X (relative với boardRecyclerView) từ drag event
    private float lastLocalX = 0;

    public PhaseDragListener(RecyclerView boardRecyclerView, List<Phase> phases,
                             PhaseAdapter phaseAdapter, int boardScrollThreshold, int boardScrollAmount) {
        this.boardRecyclerView = boardRecyclerView;
        this.phases = phases;
        this.phaseAdapter = phaseAdapter;
        this.boardScrollThreshold = boardScrollThreshold;
        this.boardScrollAmount = boardScrollAmount;
        initAutoScrollRunnable();
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_LOCATION:
                Log.d("BoardDragListener", "ACTION_DRAG_LOCATION");
                handleDragLocation(event);
                break;
            case DragEvent.ACTION_DROP:
                Log.d("BoardDragListener", "ACTION_DROP");
                handleDrop(event);
                stopAutoScroll();
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                Log.d("BoardDragListener", "ACTION_DRAG_ENDED");
                stopAutoScroll();
                clearPlaceholders();
                break;
        }
        return true;
    }

    private void handleDragLocation(DragEvent event) {
        // Lấy vị trí của boardRecyclerView trên màn hình
        int[] boardLocation = new int[2];
        boardRecyclerView.getLocationOnScreen(boardLocation);

        // event.getX() và event.getY() trả về tọa độ tương đối của boardRecyclerView
        float localX = event.getX();
        float localY = event.getY();
        // Tính tọa độ tuyệt đối (để so sánh với vị trí của list card)
        float rawX = localX + boardLocation[0];
        float rawY = localY + boardLocation[1];

        // Cập nhật lastLocalX (sử dụng tọa độ relative)
        lastLocalX = localX;

        // Nếu vị trí con trỏ nằm gần mép trái hoặc phải, bắt đầu auto-scroll nếu chưa chạy
        int boardWidth = boardRecyclerView.getWidth();
        if ((localX < boardScrollThreshold || localX > boardWidth - boardScrollThreshold) && !isAutoScrolling) {
            isAutoScrolling = true;
            boardRecyclerView.post(autoScrollRunnable);
        } else if (localX >= boardScrollThreshold && localX <= boardWidth - boardScrollThreshold) {
            stopAutoScroll();
        }

//        // Cập nhật placeholder cho từng list card dựa trên tọa độ tuyệt đối
//        for (int i = 0; i < boardRecyclerView.getChildCount(); i++) {
//            View PhaseView = boardRecyclerView.getChildAt(i);
//            int[] location = new int[2];
//            PhaseView.getLocationOnScreen(location);
//            Rect listRect = new Rect(location[0], location[1],
//                    location[0] + PhaseView.getWidth(), location[1] + PhaseView.getHeight());
//            RecyclerView cardRecyclerView = PhaseView.findViewById(R.id.cardRecyclerView);
//            CardAdapter cardAdapter = (CardAdapter) cardRecyclerView.getAdapter();
//            if (listRect.contains((int) rawX, (int) rawY)) {
//                float yRelative = rawY - listRect.top;
//                int index = calculateInsertionIndex(cardRecyclerView, yRelative, cardAdapter);
//                cardAdapter.setPlaceholderPosition(index);
//            } else {
//                cardAdapter.clearPlaceholder();
//            }
//        }
    }

    private void handleDrop(DragEvent event) {
        // Lấy tọa độ của boardRecyclerView trên màn hình
        int[] boardLocation = new int[2];
        boardRecyclerView.getLocationOnScreen(boardLocation);

        float localX = event.getX();
        float localY = event.getY();
        float dropX = localX + boardLocation[0];
        float dropY = localY + boardLocation[1];

        DraggedTaskInfo info = (DraggedTaskInfo) event.getLocalState();
        if (info == null) return;

        // Tính toán vị trí của Card (giả sử điểm drop là tâm của card)
        float cardLeft = dropX - info.getTaskWidth() / 2f;
        float cardRight = dropX + info.getTaskWidth() / 2f;
        float cardTop = dropY - info.getTaskHeight() / 2f;
        float cardBottom = dropY + info.getTaskHeight() / 2f;
        Rect cardRect = new Rect((int) cardLeft, (int) cardTop, (int) cardRight, (int) cardBottom);

        float maxIntersection = 0;
        int targetIndex = -1;
        // Duyệt qua các list card để tìm list giao nhau lớn nhất với card
        Log.d("MyTag","ChildCount: " + boardRecyclerView.getChildCount());
        for (int i = 0; i < boardRecyclerView.getChildCount(); i++) {
            View PhaseView = boardRecyclerView.getChildAt(i);
            int[] loc = new int[2];
            PhaseView.getLocationOnScreen(loc);
            Rect listRect = new Rect(loc[0], loc[1],
                    loc[0] + PhaseView.getWidth(), loc[1] + PhaseView.getHeight());
            Rect intersect = new Rect();
            if (intersect.setIntersect(cardRect, listRect)) {
                int area = intersect.width() * intersect.height();
                if (area > maxIntersection) {
                    maxIntersection = area;
                    targetIndex = i;
                }
            }
        }
        if (targetIndex != -1) {
            View targetPhaseView = boardRecyclerView.getChildAt(targetIndex);
            RecyclerView cardRecyclerView = targetPhaseView.findViewById(R.id.rvTask);
            int[] loc = new int[2];
            targetPhaseView.getLocationOnScreen(loc);
            float yRelative = dropY - loc[1];
            TaskAdapter taskAdapter = (TaskAdapter) cardRecyclerView.getAdapter();
            int dropIndex = calculateInsertionIndex(cardRecyclerView, yRelative, taskAdapter);
            Phase targetList = phases.get(targetIndex);
            if (dropIndex > targetList.getTasks().size()) {
                dropIndex = targetList.getTasks().size();
            } else if (dropIndex < 0) {
                dropIndex = 0;
            }
            if (phaseAdapter.getOnCardDropListener() != null) {
                Log.d("MyTag", "Target index:"  + targetIndex);
                Log.d("MyTag", "Drop index:"  + dropIndex);
                phaseAdapter.getOnCardDropListener().onCardDropped(targetList, dropIndex, info);
            }
        } else {
            Log.d("MyTag", "No target list found; restoring card to original list.");
            info.getPhase().getTasks().add(info.getOriginalPosition(), info.getTask());
            phaseAdapter.notifyDataSetChanged();
        }
    }

    private void clearPlaceholders() {
        for (int i = 0; i < boardRecyclerView.getChildCount(); i++) {
            View PhaseView = boardRecyclerView.getChildAt(i);
            RecyclerView cardRecyclerView = PhaseView.findViewById(R.id.rvTask);
            TaskAdapter taskAdapter = (TaskAdapter) cardRecyclerView.getAdapter();
            taskAdapter.clearPlaceholder();
        }
    }

    private int calculateInsertionIndex(RecyclerView recyclerView, float y, TaskAdapter adapter) {
        int count = recyclerView.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = recyclerView.getChildAt(i);
            if (y <= child.getBottom() && y >= child.getTop()) {
                return i;
            }
        }
        return adapter.getItemCount();
    }

    private void initAutoScrollRunnable() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                int boardWidth = boardRecyclerView.getWidth();
                // Sử dụng tốc độ cuộn cố định: boardScrollAmount
                if (lastLocalX < boardScrollThreshold) {
                    boardRecyclerView.smoothScrollBy(-boardScrollAmount, 0);
                } else if (lastLocalX > boardWidth - boardScrollThreshold) {
                    boardRecyclerView.smoothScrollBy(boardScrollAmount, 0);
                }
                // Nếu vẫn còn trong vùng kích hoạt, tiếp tục post runnable sau 50ms
                if (lastLocalX < boardScrollThreshold || lastLocalX > boardWidth - boardScrollThreshold) {
                    boardRecyclerView.postDelayed(this, 50);
                }
            }
        };
    }

    private void stopAutoScroll() {
        boardRecyclerView.removeCallbacks(autoScrollRunnable);
        isAutoScrolling = false;
    }
}
