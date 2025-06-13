package com.example.projectmanagement.data.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectMemberHolder {
    private static ProjectMemberHolder instance;
    private List<ProjectMember> members;

    private ProjectMemberHolder() {
        members = new ArrayList<>();
        // Add test data
        addTestData();
    }

    private void addTestData() {
        // Create test users
        User user1 = new User(1, "nguyenvana", "nguyenvana@gmail.com", null);
        user1.setFullname("Nguyễn Văn A");
        user1.setGender("Nam");
        user1.setBirthday(new Date());
        user1.setBio("Developer with 5 years experience");
        user1.setCreated_at(new Date());
        user1.setEmail_verified(true);

        User user2 = new User(2, "tranthib", "tranthib@gmail.com", null);
        user2.setFullname("Trần Thị B");
        user2.setGender("Nữ");
        user2.setBirthday(new Date());
        user2.setBio("UI/UX Designer");
        user2.setCreated_at(new Date());
        user2.setEmail_verified(true);

        User user3 = new User(3, "levanc", "levanc@gmail.com", null);
        user3.setFullname("Lê Văn C");
        user3.setGender("Nam");
        user3.setBirthday(new Date());
        user3.setBio("Project Manager");
        user3.setCreated_at(new Date());
        user3.setEmail_verified(true);

        User user4 = new User(4, "phamthid", "phamthid@gmail.com", null);
        user4.setFullname("Phạm Thị D");
        user4.setGender("Nữ");
        user4.setBirthday(new Date());
        user4.setBio("QA Engineer");
        user4.setCreated_at(new Date());
        user4.setEmail_verified(true);


        User user5 = new User(5, "phamthid", "phamthid@gmail.com", null);
        user5.setFullname("Phạm Thị D");
        user5.setGender("Nữ");
        user5.setBirthday(new Date());
        user5.setBio("QA Engineer");
        user5.setCreated_at(new Date());
        user5.setEmail_verified(true);

        // Create project members
        ProjectMember member1 = new ProjectMember(1, 1, 1, new Date(), ProjectMember.Role.Member);
        member1.setUser(user1);

        ProjectMember member2 = new ProjectMember(1, 2, 2, new Date(), ProjectMember.Role.Member);
        member2.setUser(user2);

        ProjectMember member3 = new ProjectMember(1, 3, 3, new Date(), ProjectMember.Role.Leader);
        member3.setUser(user3);

        ProjectMember member4 = new ProjectMember(1, 4, 4, new Date(), ProjectMember.Role.Member);
        member4.setUser(user4);

        ProjectMember member5 = new ProjectMember(1, 5, 5, new Date(), ProjectMember.Role.Leader);
        member5.setUser(user5);

        // Add members to list
        members.add(member1);
        members.add(member2);
        members.add(member3);
        members.add(member4);
        members.add(member5);
    }

    public static ProjectMemberHolder get() {
        if (instance == null) {
            instance = new ProjectMemberHolder();
        }
        return instance;
    }

    public void setMembers(List<ProjectMember> members) {
        this.members = members;
    }

    public List<ProjectMember> getMembers() {
        return members;
    }

    public void clear() {
        members.clear();
    }
} 