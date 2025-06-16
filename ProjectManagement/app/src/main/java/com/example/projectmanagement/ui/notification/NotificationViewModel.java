package com.example.projectmanagement.ui.notification;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.VolleyError;
import com.example.projectmanagement.data.convertor.NotificationConvertor;
import com.example.projectmanagement.data.model.Notification;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.repository.NotificationRepository;
import com.example.projectmanagement.data.service.NotificationService;
import com.example.projectmanagement.utils.CustomCallback;
import com.example.projectmanagement.utils.Helpers;
import com.example.projectmanagement.utils.ParseDateUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * ViewModel cho NotificationActivity, expose LiveData để Activity observe.
 */
public class NotificationViewModel extends ViewModel {

    private static final String TAG = "NotificationViewModel";
    private Context context;

    public enum Filter {ALL, READ, UNREAD}

    private NotificationRepository repository;
    private final MutableLiveData<List<Notification>> _allNotifications = new MutableLiveData<>();
    private final MutableLiveData<Filter> _filter = new MutableLiveData<>(Filter.ALL);

    // LiveData cho UI quan sát — đã lọc theo filter mode
    private final MediatorLiveData<List<Notification>> _filteredNotifications = new MediatorLiveData<>();
    public LiveData<List<Notification>> filteredNotifications = _filteredNotifications;

    public NotificationViewModel() {
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        repository = new NotificationRepository(context);
        loadNotifications();
        // Mỗi khi dữ liệu gốc hoặc filter thay đổi, recalc
        _filteredNotifications.addSource(_allNotifications, list -> applyFilter());
        _filteredNotifications.addSource(_filter, f -> applyFilter());
    }

    public void markAsRead(Long notificationId) {
        List<Notification> current = _allNotifications.getValue();
        if (current == null) return;

        NotificationService.markAsRead(context, notificationId, res -> {
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
        }, err -> {
            String errMsg = "lỗi không thể đánh dấu tất cả là đã đọc";
            try {
                errMsg = Helpers.parseError(err);
            } catch (Exception e) {
            }
            Log.d(TAG, ">>> error to mark all as read: " + errMsg);
            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
        });
    }

    public void markAllRead() {
        List<Notification> current = _allNotifications.getValue();
        if (current == null) return;

        NotificationService.markAllAsRead(context, res -> {
            List<Notification> updated = new ArrayList<>();
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
        }, err -> {
            String errMsg = "lỗi không thể đánh dấu tất cả là đã đọc";
            try {
                errMsg = Helpers.parseError(err);
            } catch (Exception e) {
            }
            Log.d(TAG, ">>> error to mark all as read: " + errMsg);
            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
        });
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
                if (f == Filter.READ && Boolean.TRUE.equals(n.getIsRead()) ||
                        f == Filter.UNREAD && Boolean.FALSE.equals(n.getIsRead())) {
                    tmp.add(n);
                }
            }
            _filteredNotifications.setValue(tmp);
        }
    }

    public void refreshNotifications() {
        // Load lại danh sách thông báo
        loadNotifications();
    }

    private void loadNotifications() {
        repository.fetchNotifications(new CustomCallback<JSONObject, VolleyError>() {
            @Override
            public void onSuccess(JSONObject result) {
                List<Notification> notifications = new ArrayList<>();
                JSONArray data = result.optJSONArray("data");
                Log.d(TAG, ">>> data from fetch notifications recursively: " + data);
                try {
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject notificationJson = data.getJSONObject(i);
                            Notification notification = NotificationConvertor.fromJson(notificationJson);
                            Log.d(TAG, ">>> converted noti: " + notification);
                            notifications.add(notification);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                _allNotifications.setValue(notifications);
            }

            @Override
            public void onError(VolleyError volleyError) {
                String errMsg = "lỗi không thể lấy thông báo";
                try {
                    errMsg = Helpers.parseError(volleyError);
                } catch (Exception e) {
                }
                Log.d(TAG, ">>> error to fetch notifications: " + errMsg);
                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
