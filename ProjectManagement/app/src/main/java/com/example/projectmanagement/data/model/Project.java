package com.example.projectmanagement.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.projectmanagement.utils.ParseDateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Project implements Parcelable {
    private int projectID;
    private String projectName;
    private String projectDescription;
    private Date deadline;
    private String status;
    private Date createAt;
    private Date startDate;
    private String accessLevel;
    private String inviteLinkToken;
    private String backgroundImg;
    private Date updateAt;
    private User user;
    private List<Phase> phases;

    public Project() {
        // no-arg constructor
    }

    public Project(String projectName, String projectDescription, String date, String backgroundImg) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.deadline = ParseDateUtil.parseDate(date);
        this.backgroundImg = backgroundImg;
        this.phases = new ArrayList<>();
    }

    public Project(int projectID,
                   String projectName,
                   String projectDescription,
                   Date deadline,
                   String status,
                   Date createAt,
                   Date startDate,
                   String accessLevel,
                   String inviteLinkToken,
                   String backgroundImg,
                   Date updateAt,
                   User user,
                   List<Phase> phases) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.deadline = deadline;
        this.status = status;
        this.createAt = createAt;
        this.startDate = startDate;
        this.accessLevel = accessLevel;
        this.inviteLinkToken = inviteLinkToken;
        this.backgroundImg = backgroundImg;
        this.updateAt = updateAt;
        this.user = user;
        this.startDate = startDate;
        this.phases = phases != null ? phases : new ArrayList<>();
    }

    protected Project(Parcel in) {
        projectID         = in.readInt();
        projectName       = in.readString();
        projectDescription= in.readString();
        long dl           = in.readLong();
        deadline          = dl == -1 ? null : new Date(dl);
        status            = in.readString();
        long ca           = in.readLong();
        createAt          = ca == -1 ? null : new Date(ca);
        long sd           = in.readLong();
        startDate         = sd == -1 ? null : new Date(sd);
        accessLevel       = in.readString();
        inviteLinkToken   = in.readString();
        backgroundImg     = in.readString();
        long ua           = in.readLong();
        updateAt          = ua == -1 ? null : new Date(ua);
        user              = in.readParcelable(User.class.getClassLoader());
        phases = new ArrayList<>();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(projectID);
        dest.writeString(projectName);
        dest.writeString(projectDescription);
        dest.writeLong(deadline != null ? deadline.getTime() : -1);
        dest.writeString(status);
        dest.writeLong(createAt != null ? createAt.getTime() : -1);
        dest.writeLong(startDate != null ? startDate.getTime() : -1);
        dest.writeString(accessLevel);
        dest.writeString(inviteLinkToken);
        dest.writeString(backgroundImg);
        dest.writeLong(updateAt != null ? updateAt.getTime() : -1);
        dest.writeParcelable(user, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    // ===== Getters & Setters =====

    public int getProjectID() { return projectID; }
    public void setProjectID(int projectID) { this.projectID = projectID; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreateAt() { return createAt; }
    public void setCreateAt(Date createAt) { this.createAt = createAt; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public String getInviteLinkToken() { return inviteLinkToken; }
    public void setInviteLinkToken(String inviteLinkToken) { this.inviteLinkToken = inviteLinkToken; }

    public String getBackgroundImg() { return backgroundImg; }
    public void setBackgroundImg(String backgroundImg) { this.backgroundImg = backgroundImg; }

    public Date getUpdateAt() { return updateAt; }
    public void setUpdateAt(Date updateAt) { this.updateAt = updateAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Phase> getPhases() { return phases; }
    public void setPhases(List<Phase> phases) { this.phases = phases; }
}
