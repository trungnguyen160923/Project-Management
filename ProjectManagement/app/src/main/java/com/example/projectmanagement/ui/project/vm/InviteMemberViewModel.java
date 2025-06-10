package com.example.projectmanagement.ui.project.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.projectmanagement.data.model.ProjectMember;
import com.example.projectmanagement.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InviteMemberViewModel extends ViewModel {
    private final MutableLiveData<List<ProjectMember>> memberListLive = new MutableLiveData<>();

    public InviteMemberViewModel() {
        // Mock users
        User admin = new User();
        admin.setId(1);
        admin.setFullname("Nguyễn Thành Trung");
        admin.setEmail("2imtrung160923@gmail.com");
        admin.setUsername("2imtrung160923");
        admin.setAvatar(null);

        User leader = new User();
        leader.setId(2);
        leader.setFullname("Nguyễn Văn B");
        leader.setEmail("leader@gmail.com");
        leader.setUsername("nguyenvanb");
        leader.setAvatar(null);

        User member = new User();
        member.setId(3);
        member.setFullname("Nguyễn Văn C");
        member.setEmail("member@gmail.com");
        member.setUsername("nguyenvanc");
        member.setAvatar(null);

        List<ProjectMember> mockMembers = new ArrayList<>();
        ProjectMember pmAdmin = new ProjectMember(1, 1, 1, new Date(), ProjectMember.Role.ADMIN);
        pmAdmin.setUser(admin);
        mockMembers.add(pmAdmin);

        ProjectMember pmLeader = new ProjectMember(1, 2, 2, new Date(), ProjectMember.Role.LEADER);
        pmLeader.setUser(leader);
        mockMembers.add(pmLeader);

        ProjectMember pmMember = new ProjectMember(1, 3, 3, new Date(), ProjectMember.Role.MEMBER);
        pmMember.setUser(member);
        mockMembers.add(pmMember);

        memberListLive.setValue(mockMembers);
    }

    public LiveData<List<ProjectMember>> getMemberListLive() {
        return memberListLive;
    }
}
