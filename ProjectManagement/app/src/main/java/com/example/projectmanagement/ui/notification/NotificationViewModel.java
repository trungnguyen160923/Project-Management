package com.example.projectmanagement.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Notification;
import com.example.projectmanagement.data.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ViewModel cho NotificationActivity, expose LiveData để Activity observe.
 */
public class NotificationViewModel extends ViewModel {

    public enum Filter { ALL, READ, UNREAD }

    private final NotificationRepository repository;
    private final MutableLiveData<List<Notification>> _allNotifications = new MutableLiveData<>();
    private final MutableLiveData<Filter> _filter = new MutableLiveData<>(Filter.ALL);

    // LiveData cho UI quan sát — đã lọc theo filter mode
    private final MediatorLiveData<List<Notification>> _filteredNotifications = new MediatorLiveData<>();
    public LiveData<List<Notification>> filteredNotifications = _filteredNotifications;

    public NotificationViewModel() {
        repository = new NotificationRepository();
        _allNotifications.setValue(repository.getMockNotifications());

        // Mỗi khi dữ liệu gốc hoặc filter thay đổi, recalc
        _filteredNotifications.addSource(_allNotifications, list -> applyFilter());
        _filteredNotifications.addSource(_filter, f -> applyFilter());
    }

    public void markAsRead(Long notificationId) {
        List<Notification> current = _allNotifications.getValue();
        if (current == null) return;

        List<Notification> updated = new ArrayList<>(current.size());
        for (Notification n : current) {
            if (n.getNotificationId().equals(notificationId)) {
                // tạo một instance mới với isRead = true
                updated.add(new Notification(
                        n.getNotificationId(),
                        n.getAction(),
                        n.getCreatedAt(),
                        true,                       // đã đọc
                        n.getMessage(),
                        n.getType(),
                        n.getProjectId(),
                        n.getSenderId(),
                        n.getUserId()
                ));
            } else {
                // giữ nguyên object cũ
                updated.add(n);
            }
        }
        _allNotifications.setValue(updated);
    }
    public void markAllRead() {
        List<Notification> current = _allNotifications.getValue();
        if (current == null) return;

        List<Notification> updated = new ArrayList<>(current.size());
        for (Notification n : current) {
            // tạo instance mới với isRead = true
            updated.add(new Notification(
                    n.getNotificationId(),
                    n.getAction(),
                    n.getCreatedAt(),
                    true,               // set true cho tất cả
                    n.getMessage(),
                    n.getType(),
                    n.getProjectId(),
                    n.getSenderId(),
                    n.getUserId()
            ));
        }
        _allNotifications.setValue(updated);
    }
    public void setFilter(Filter filter) {
        _filter.setValue(filter);
    }

    private void applyFilter() {
        List<Notification> all = _allNotifications.getValue();
        Filter f = _filter.getValue();
        if (all == null || f == null) {
            _filteredNotifications.setValue(Collections.emptyList());
            return;
        }
        if (f == Filter.ALL) {
            _filteredNotifications.setValue(all);
        } else {
            List<Notification> tmp = new ArrayList<>();
            for (Notification n : all) {
                if (f == Filter.READ    && Boolean.TRUE.equals(n.getIsRead()) ||
                        f == Filter.UNREAD  && Boolean.FALSE.equals(n.getIsRead())) {
                    tmp.add(n);
                }
            }
            _filteredNotifications.setValue(tmp);
        }
    }
}
