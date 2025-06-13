package com.example.projectmanagement.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class User implements Parcelable {
    private int id;
    private String username;
    private String email;
    private String password;
    private String fullname;
    private Date birthday;
    private String gender;
    private String social_links;
    private String avatar;
    private String bio;
    private Date created_at;
    private Date last_updated;
    private Boolean email_verified;

    public User() {
    }

    public User(int id, String username, String email, String avatar) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
    }

    public User(int id,
                String username,
                String password,
                String email,
                String fullname,
                String gender,
                Date birthday,
                String social_links,
                String avatar,
                String bio,
                Date created_at,
                Date last_updated,
                Boolean email_verified) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullname = fullname;
        this.gender = gender;
        this.birthday = birthday;
        this.social_links = social_links;
        this.avatar = avatar;
        this.bio = bio;
        this.created_at = created_at;
        this.last_updated = last_updated;
        this.email_verified = email_verified;
    }

    protected User(Parcel in) {
        id = in.readInt();
        username = in.readString();
        email = in.readString();
        password = in.readString();
        fullname = in.readString();

        long bd = in.readLong();
        birthday = bd == -1 ? null : new Date(bd);

        gender = in.readString();
        social_links = in.readString();
        avatar = in.readString();
        bio = in.readString();

        long ca = in.readLong();
        created_at = ca == -1 ? null : new Date(ca);

        long lu = in.readLong();
        last_updated = lu == -1 ? null : new Date(lu);

        email_verified = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(fullname);
        dest.writeLong(birthday != null ? birthday.getTime() : -1);
        dest.writeString(gender);
        dest.writeString(social_links);
        dest.writeString(avatar);
        dest.writeString(bio);
        dest.writeLong(created_at != null ? created_at.getTime() : -1);
        dest.writeLong(last_updated != null ? last_updated.getTime() : -1);
        dest.writeByte((byte) (email_verified != null && email_verified ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // ----------- getters & setters ------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSocial_links() {
        return social_links;
    }

    public void setSocial_links(String social_links) {
        this.social_links = social_links;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(Date last_updated) {
        this.last_updated = last_updated;
    }

    public Boolean getEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(Boolean email_verified) {
        this.email_verified = email_verified;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", birthday=" + birthday +
                ", gender='" + gender + '\'' +
                ", social_links='" + social_links + '\'' +
                ", avatar='" + avatar + '\'' +
                ", bio='" + bio + '\'' +
                ", created_at=" + created_at +
                ", last_updated=" + last_updated +
                ", email_verified=" + email_verified +
                '}';
    }
}
