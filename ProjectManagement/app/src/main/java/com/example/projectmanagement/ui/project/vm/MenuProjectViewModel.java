package com.example.projectmanagement.ui.project.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MenuProjectViewModel extends ViewModel {
    // LiveData cho project hiện tại
    private final MutableLiveData<Project> projectLive = new MutableLiveData<>();

    // LiveData cho danh sách thành viên project
    private final MutableLiveData<List<ProjectMember>> memberListLive = new MutableLiveData<>();

    public MenuProjectViewModel() {
        // Tạo mock user trước
        User admin = new User();
        admin.setId(101);
        admin.setFullname("Nguyễn Văn A");
        admin.setEmail("admin@gmail.com");
        admin.setAvatar(null);

        User leader = new User();
        leader.setId(102);
        leader.setFullname("Trần Thị B");
        leader.setEmail("leader@gmail.com");
        leader.setAvatar(null);

        User member = new User();
        member.setId(103);
        member.setFullname("Lê Văn C");
        member.setEmail("member@gmail.com");
        member.setAvatar(null);

        // Tạo mock ProjectMember và gán user
        List<ProjectMember> mockMembers = new ArrayList<>();

        ProjectMember pmAdmin = new ProjectMember(1, 1, 101, new Date(), ProjectMember.Role.ADMIN);
        pmAdmin.setUser(admin);
        mockMembers.add(pmAdmin);

        ProjectMember pmLeader = new ProjectMember(1, 2, 102, new Date(), ProjectMember.Role.LEADER);
        pmLeader.setUser(leader);
        mockMembers.add(pmLeader);

        ProjectMember pmMember = new ProjectMember(1, 3, 103, new Date(), ProjectMember.Role.MEMBER);
        pmMember.setUser(member);
        mockMembers.add(pmMember);

        memberListLive.setValue(mockMembers);
    }

    public LiveData<Project> getProjectLive() {
        return projectLive;
    }

    public LiveData<List<ProjectMember>> getMemberListLive() {
        return memberListLive;
    }

    // Cập nhật project (khi mở màn, hoặc khi thay đổi)
    public void setProject(Project project) {
        projectLive.setValue(project);
    }

    // Cập nhật danh sách thành viên
    public void setMemberList(List<ProjectMember> list) {
        memberListLive.setValue(list);
    }

    // Có thể bổ sung các logic fetch data từ API ở đây, sau đó setValue/updateValue vào LiveData!
}

