package com.example.projectmanagement.data.repository;

import com.example.projectmanagement.data.model.Notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationRepository {

    /**
     * Trả về danh sách Notification mẫu để demo.
     */
    public List<Notification> getMockNotifications() {
        List<Notification> list = new ArrayList<>();

        list.add(new Notification(
                1L,
                "TASK_ASSIGNED",
                new Date(),
                false,
                "Bạn vừa được giao task \u201CThiết kế UI\u201D",
                "TASK",
                101L,
                10L,
                5L
        ));

        list.add(new Notification(
                2L,
                "COMMENT_ADDED",
                new Date(),
                true,
                "Alice đã bình luận vào task \u201CBackend API\u201D",
                "COMMENT",
                101L,
                3L,
                5L
        ));

        list.add(new Notification(
                3L,
                "PROJECT_CREATED",
                new Date(),
                false,
                "Bạn đã tạo dự án \u201CMobile App\u201D thành công",
                "PROJECT",
                102L,
                5L,
                5L
        ));

        return list;
    }
}

