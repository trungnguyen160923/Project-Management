package com.example.projectmanagement.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.data.model.DraggedTaskInfo;
import com.example.projectmanagement.data.model.Phase;
import com.example.projectmanagement.utils.DetailPhaseDialogUtil;
import com.example.projectmanagement.ui.helper.PhaseTouchHelperCallback;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class PhaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements PhaseTouchHelperCallback.ItemTouchHelperAdapter {

    RecyclerView rvBoard;
    private static final int TYPE_LIST = 0;
    private static final int TYPE_ADD = 1;
    private final List<Phase> phases;
    private final OnAddPhaseListener addPhaseListener;
    private final OnAddTaskListener addTaskListener;
    private final OnTaskActionListener taskListener;
    private final OnCardDropListener cardDropListener;

    // For edit mode
    private int editingPosition = -1;
    private String newTaskName = "";

    public interface OnAddPhaseListener {
        void onAddPhase();
    }
    public interface OnAddTaskListener {
        void onAddTask(int phasePosition);
    }
    public interface OnTaskActionListener {
        void onTaskAddRequested(int phasePosition);
        void onTaskAddConfirmed(int phasePosition, String taskName);
        void onTaskAddCanceled();
        void onTaskNameChanged(int phasePosition, String newName);
    }
    public interface OnCardDropListener {
        void onCardDropped(Phase targetList, int dropIndex, DraggedTaskInfo draggedInfo);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void startEditing(int pos) {
        editingPosition = pos;
        newTaskName = "";
        notifyDataSetChanged();
    }
    /** Hủy mode input */
    @SuppressLint("NotifyDataSetChanged")
    public void cancelEditing() {
        editingPosition = -1;
        newTaskName = "";
        notifyDataSetChanged();
    }
    /** Lấy text đang nhập */
    public String getNewTaskName() {
        return newTaskName.trim();
    }

    /**
     * Primary constructor with drag-and-drop support
     */
    public PhaseAdapter(
            List<Phase> phases,
            OnAddPhaseListener addPhaseListener,
            OnAddTaskListener addTaskListener,
            OnTaskActionListener taskListener,
            OnCardDropListener cardDropListener,
            RecyclerView board
    ) {
        this.phases = phases != null ? phases : new ArrayList<>();
        this.addPhaseListener = addPhaseListener;
        this.addTaskListener = addTaskListener;
        this.taskListener = taskListener;
        this.cardDropListener = cardDropListener;
        this.rvBoard = board;
    }

    /**
     * Convenience constructor without drag-and-drop
     */

    @Override
    public int getItemViewType(int position) {
        if (position < phases.size()) {
            return TYPE_LIST;
        } else {
            return TYPE_ADD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LIST) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.phase_item, parent, false);
            return new PhaseViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_phase_item, parent, false);
            return new AddPhaseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PhaseViewHolder) {
            PhaseViewHolder vh = (PhaseViewHolder) holder;
            int adapterPos = vh.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Phase phase = phases.get(adapterPos);
            vh.tvPhaseTitle.setText(phase.getPhaseName());

            // Task list
            TaskAdapter taskAdapter = new TaskAdapter(phase.getTasks(), phase);
            vh.rvTask.setLayoutManager(new LinearLayoutManager(vh.itemView.getContext()));
            vh.rvTask.setAdapter(taskAdapter);

            // Sự kiện cho nút ba chấm
            vh.btnPhaseMenu.setOnClickListener(v -> showPhaseMenu(v, position));


            PhaseTouchHelperCallback callback = new PhaseTouchHelperCallback(
                    taskAdapter::onItemMove,     // giả sử TaskAdapter có method onItemMove(int, int)
                    rvBoard                      // biến rvBoard bạn truyền từ Activity xuống Adapter
            );
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(vh.rvTask);

            // Edit mode
            if (adapterPos == editingPosition) {
                vh.etNew.setVisibility(View.VISIBLE);
                vh.etNew.setText(newTaskName);
                vh.tvAddTask.setVisibility(View.GONE);
                vh.etNew.requestFocus();

                InputMethodManager imm = (InputMethodManager)
                        vh.itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(vh.etNew, 0);

                vh.etNew.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                    @Override
                    public void onTextChanged(CharSequence s, int st, int b, int c) {
                        newTaskName = s.toString();
                        int pos = vh.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION && taskListener != null) {
                            taskListener.onTaskNameChanged(pos, newTaskName);
                        }
                    }
                    @Override public void afterTextChanged(Editable e) {}
                });
            } else {
                vh.etNew.setVisibility(View.GONE);
                vh.tvAddTask.setVisibility(View.VISIBLE);
                vh.etNew.clearFocus();
            }

            vh.tvAddTask.setOnClickListener(v -> {
                int pos = vh.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && taskListener != null) {
                    taskListener.onTaskAddRequested(pos);
                }
            });

        } else if (holder instanceof AddPhaseViewHolder) {
            ((AddPhaseViewHolder) holder).tvAddPhase.setOnClickListener(v -> {
                if (addPhaseListener != null) addPhaseListener.onAddPhase();
            });
        }
    }
    @Override
    public int getItemCount() {
        // +1 cho nút "+ Thêm phase"
        return (phases != null ? phases.size() : 0) + 1;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        // Chỉ hoán đổi giữa các item phase (không tính nút thêm)
        if (fromPosition < phases.size() && toPosition < phases.size()) {
            Collections.swap(phases, fromPosition, toPosition);
            // Cập nhật orderIndex cho tất cả phase
            for (int i = 0; i < phases.size(); i++) {
                phases.get(i).setOrderIndex(i);
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }
        return false;
    }

    // Thêm method mới để thêm phase
    @SuppressLint("NotifyDataSetChanged")
    public void addPhase(Phase phase) {
        phase.setOrderIndex(phases.size()); // Set orderIndex là index mới
        phases.add(phase);
        notifyDataSetChanged();
    }

    private void showPhaseMenu(View anchor, int phasePosition) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_phase, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_about_phase) {
                DetailPhaseDialogUtil.showPhaseDetailDialog(anchor.getContext(), phases.get(phasePosition), new DetailPhaseDialogUtil.PhaseDialogListener() {
                    @Override
                    public void onPhaseUpdated(Phase updatedPhase) {
                        // xử lý cập nhật
                    }
                    @Override
                    public void onPhaseDeleted(Phase phaseToDelete) {
                        // xử lý xóa
                    }
                });
                return true;
            }
            if (id == R.id.action_sort) {
                showSortMenu(anchor, phasePosition);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showSortMenu(View anchor, int phasePosition) {
        PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_sort_phase, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_sort_due_newest) {
                sortTasksByDueDate(phasePosition, true);
                return true;
            }
            if (id == R.id.action_sort_due_oldest) {
                sortTasksByDueDate(phasePosition, false);
                return true;
            }
            if (id == R.id.action_sort_created_newest) {
                sortTasksByCreatedDate(phasePosition, true);
                return true;
            }
            if (id == R.id.action_sort_created_oldest) {
                sortTasksByCreatedDate(phasePosition, false);
                return true;
            }
            if (id == R.id.action_sort_name_az) {
                sortTasksByName(phasePosition, true);
                return true;
            }
            if (id == R.id.action_sort_name_za) {
                sortTasksByName(phasePosition, false);
                return true;
            }
            if (id == R.id.action_back) {
                showPhaseMenu(anchor, phasePosition);
                return true;
            }
            return false;
        });
        popup.show();
    }
    private void sortTasksByDueDate(int phasePosition, boolean newestFirst) {
        Phase phase = phases.get(phasePosition);
        if (newestFirst) {
            Collections.sort(phase.getTasks(), (t1, t2) -> t2.getDueDate().compareTo(t1.getDueDate()));
        } else {
            Collections.sort(phase.getTasks(), (t1, t2) -> t1.getDueDate().compareTo(t2.getDueDate()));
        }
        notifyItemChanged(phasePosition);
    }

    private void sortTasksByCreatedDate(int phasePosition, boolean newestFirst) {
        Phase phase = phases.get(phasePosition);
        if (newestFirst) {
            Collections.sort(phase.getTasks(), (t1, t2) -> t2.getCreateAt().compareTo(t1.getCreateAt()));
        } else {
            Collections.sort(phase.getTasks(), (t1, t2) -> t1.getCreateAt().compareTo(t2.getCreateAt()));
        }
        notifyItemChanged(phasePosition);
    }

    private void sortTasksByName(int phasePosition, boolean az) {
        Phase phase = phases.get(phasePosition);
        if (az) {
            Collections.sort(phase.getTasks(), (t1, t2) -> t1.getTaskName().compareToIgnoreCase(t2.getTaskName()));
        } else {
            Collections.sort(phase.getTasks(), (t1, t2) -> t2.getTaskName().compareToIgnoreCase(t1.getTaskName()));
        }
        notifyItemChanged(phasePosition);
    }



    class PhaseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvPhaseTitle, tvAddTask;
        final RecyclerView rvTask;
        final EditText etNew;
        final View btnPhaseMenu;
        PhaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPhaseTitle = itemView.findViewById(R.id.tvPhaseTitle);
            rvTask       = itemView.findViewById(R.id.rvTask);
            tvAddTask    = itemView.findViewById(R.id.tvAddTask);
            etNew        = itemView.findViewById(R.id.etNewTaskName);
            btnPhaseMenu = itemView.findViewById(R.id.btnPhaseMenu);
        }
    }

    static class AddPhaseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAddPhase;
        AddPhaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddPhase = itemView.findViewById(R.id.tvAddPhase);
        }
    }
}