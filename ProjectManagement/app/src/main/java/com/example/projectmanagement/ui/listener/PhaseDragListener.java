package com.example.projectmanagement.ui.listener;

import android.graphics.Rect;
import android.view.DragEvent;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.ui.adapter.TaskAdapter;
import java.util.List;

public class PhaseDragListener implements View.OnDragListener{


        private RecyclerView boardRv = null;
        private final List<Phase> phases;
        private final TaskAdapter.OnTaskDropListener dropListener;
        private int threshold = 0;
    private int amount = 0;
        private boolean isAuto;
        private float lastX;
    private final Runnable autoScroll = new Runnable() {
        @Override
        public void run() {
            if (lastX < threshold) {
                boardRv.smoothScrollBy(-amount, 0);
            } else if (lastX > boardRv.getWidth() - threshold) {
                boardRv.smoothScrollBy(amount, 0);
            }
            if (isAuto) {
                // 'this' là Runnable hiện tại
                boardRv.postDelayed(this, 50);
            }
        }
    };

        public PhaseDragListener(RecyclerView rv, List<Phase> phases,
                                 TaskAdapter.OnTaskDropListener listener,
                                 int threshold, int amount) {
            this.boardRv = rv;
            this.phases = phases;
            this.dropListener = listener;
            this.threshold = threshold;
            this.amount = amount;
        }

        @Override public boolean onDrag(View v, DragEvent e) {
            switch (e.getAction()) {
                case DragEvent.ACTION_DRAG_LOCATION:
                    lastX = e.getX();
                    if (!isAuto && (lastX < threshold || lastX > boardRv.getWidth() - threshold)) {
                        isAuto = true;
                        boardRv.post(autoScroll);
                    } else if (isAuto && lastX >= threshold && lastX <= boardRv.getWidth() - threshold) {
                        stopAuto();
                    }
                    break;
                case DragEvent.ACTION_DROP:
                case DragEvent.ACTION_DRAG_ENDED:
                    stopAuto();
                    DraggedTaskInfo info = (DraggedTaskInfo) e.getLocalState();
                    if (info == null) return true;
                    int[] loc = new int[2];
                    boardRv.getLocationOnScreen(loc);
                    float rawX = e.getX() + loc[0];
                    float rawY = e.getY() + loc[1];
                    Rect cardRect = new Rect(
                            (int)(rawX - info.viewWidth/2f),
                            (int)(rawY - info.viewHeight/2f),
                            (int)(rawX + info.viewWidth/2f),
                            (int)(rawY + info.viewHeight/2f)
                    );
                    int best=-1,maxA=0;
                    for (int i=0;i<boardRv.getChildCount();i++){
                        View ch=boardRv.getChildAt(i);
                        ch.getLocationOnScreen(loc);
                        Rect lr=new Rect(loc[0],loc[1],loc[0]+ch.getWidth(),loc[1]+ch.getHeight());
                        Rect in=new Rect();
                        if(in.setIntersect(cardRect,lr)){
                            int a=in.width()*in.height();
                            if(a>maxA){maxA=a;best=i;}
                        }
                    }
                    if(best!=-1){
                        Phase tgt=phases.get(best);
                        tgt.getTask().add(info.task);
                        for(int i=0;i<tgt.getTask().size();i++)
                            tgt.getTask().get(i).setOrderIndex(i);
                        dropListener.onTaskDropped(tgt,tgt.getTask().size()-1,info);
                    } else {
                        info.sourcePhase.getTask().add(info.originalPosition,info.task);
                        for(int i=0;i<info.sourcePhase.getTask().size();i++)
                            info.sourcePhase.getTask().get(i).setOrderIndex(i);
                    }
                    break;
            }
            return true;
        }

        private void stopAuto(){isAuto=false;boardRv.removeCallbacks(autoScroll);}
}
