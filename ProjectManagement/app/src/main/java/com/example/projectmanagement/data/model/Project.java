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

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    private List<Phase> phases;

    public Project() { }

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
                   String accessLevel,
                   String inviteLinkToken,
                   String backgroundImg,
                   List<Phase> phases) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.deadline = deadline;
        this.status = status;
        this.createAt = createAt;
        this.accessLevel = accessLevel;
        this.inviteLinkToken = inviteLinkToken;
        this.backgroundImg = backgroundImg;
        this.phases = phases != null ? phases : new ArrayList<>();
    }

    public Date getStartDate() {
        return startDate;
    }

    public Project(int projectID, String projectName, String projectDescription, Date deadline, String status, Date createAt, String accessLevel, Date startDate, String inviteLinkToken, String backgroundImg, List<Phase> phases) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.deadline = deadline;
        this.status = status;
        this.createAt = createAt;
        this.accessLevel = accessLevel;
        this.startDate = startDate;
        this.inviteLinkToken = inviteLinkToken;
        this.backgroundImg = backgroundImg;
        this.phases = phases;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
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
        accessLevel       = in.readString();
        inviteLinkToken   = in.readString();
        backgroundImg     = in.readString();
        phases            = in.createTypedArrayList(Phase.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(projectID);
        dest.writeString(projectName);
        dest.writeString(projectDescription);
        dest.writeLong(deadline != null ? deadline.getTime() : -1);
        dest.writeString(status);
        dest.writeLong(createAt != null ? createAt.getTime() : -1);
        dest.writeString(accessLevel);
        dest.writeString(inviteLinkToken);
        dest.writeString(backgroundImg);
        dest.writeTypedList(phases);
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

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public String getInviteLinkToken() { return inviteLinkToken; }
    public void setInviteLinkToken(String inviteLinkToken) { this.inviteLinkToken = inviteLinkToken; }

    public String getBackgroundImg() { return backgroundImg; }
    public void setBackgroundImg(String backgroundImg) { this.backgroundImg = backgroundImg; }

    public List<Phase> getPhases() { return phases; }
    public void setPhases(List<Phase> phases) { this.phases = phases; }
}
