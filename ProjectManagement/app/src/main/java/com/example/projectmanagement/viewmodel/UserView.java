package com.example.projectmanagement.viewmodel;

import java.util.Date;

public class UserView {
    private int id;
    private String username;
    private String email;
    private String fullname;
    private Date birthday;
    private String gender;
    private String social_links;
    private String avatar;
    private String bio;
    private Date created_at;
    private Date last_updated;
    private Boolean email_verified;

    public UserView(String fullname) {
        this.fullname = fullname;
    }

    public UserView() {
    }

    public UserView(int id, String username, String fullname, String email, Date birthday,
                    String gender, String social_links, String avatar, String bio, Date created_at,
                    Date last_updated, Boolean email_verified) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
        this.social_links = social_links;
        this.avatar = avatar;
        this.bio = bio;
        this.created_at = created_at;
        this.last_updated = last_updated;
        this.email_verified = email_verified;
    }

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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
}
