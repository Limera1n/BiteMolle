package dev.cosgy.niconicoSearchAPI;

import java.util.Arrays;

public class nicoVideoInfo {
    //video info
    private String videoId;
    private String title;
    private String[] tags;
    private String watchUrl;
    private String thumbnailUrl;
    private String description;
    private String lengthFormatted;

    //counts
    private int viewCount;
    private int mylistCount;
    private int commentCount;

    //Uploader info
    private int uploadUserId;
    private String uploadUserName;
    private String uploadUserIconUrl;

    public nicoVideoInfo(String videoId, String title, String[] tags, String watchUrl, String thumbnailUrl, String description, String lengthFormatted,
                         int viewCount, int mylistCount, int commentCount,
                         int uploadUserId, String uploadUserName, String uploadUserIconUrl) {

        this.videoId = videoId;
        this.title = title;
        this.tags = tags;
        this.watchUrl = watchUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.lengthFormatted = lengthFormatted;

        this.viewCount = viewCount;
        this.mylistCount = mylistCount;
        this.commentCount = commentCount;

        this.uploadUserId = uploadUserId;
        this.uploadUserName = uploadUserName;
        this.uploadUserIconUrl = uploadUserIconUrl;
    }

    public nicoVideoInfo() {
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public nicoVideoInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public String[] getTags() {
        return tags;
    }

    public nicoVideoInfo setTags(String[] tags) {
        this.tags = tags;
        return this;
    }

    public String getWatchUrl() {
        return watchUrl;
    }

    public nicoVideoInfo setWatchUrl(String watchUrl) {
        this.watchUrl = watchUrl;
        return this;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public nicoVideoInfo setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public nicoVideoInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLengthFormatted() {
        return lengthFormatted;
    }

    public nicoVideoInfo setLengthFormatted(String lengthFormatted) {
        this.lengthFormatted = lengthFormatted;
        return this;
    }

    public int getViewCount() {
        return viewCount;
    }

    public nicoVideoInfo setViewCount(int viewCount) {
        this.viewCount = viewCount;
        return this;
    }

    public int getMylistCount() {
        return mylistCount;
    }

    public nicoVideoInfo setMylistCount(int mylistCount) {
        this.mylistCount = mylistCount;
        return this;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public nicoVideoInfo setCommentCount(int commentCount) {
        this.commentCount = commentCount;
        return this;
    }

    public int getUploadUserId() {
        return uploadUserId;
    }

    public nicoVideoInfo setUploadUserId(int uploadUserId) {
        this.uploadUserId = uploadUserId;
        return this;
    }

    public String getUploadUserName() {
        return uploadUserName;
    }

    public nicoVideoInfo setUploadUserName(String uploadUserName) {
        this.uploadUserName = uploadUserName;
        return this;
    }

    public String getUploadUserIconUrl() {
        return uploadUserIconUrl;
    }

    public nicoVideoInfo setUploadUserIconUrl(String uploadUserIconUrl) {
        this.uploadUserIconUrl = uploadUserIconUrl;
        return this;
    }

    public String toString() {
        return "videoId=" + videoId +
                ", title=" + title +
                ", tags=" + Arrays.toString(tags) +
                ", watchUrl=" + watchUrl +
                ", thumbnailUrl=" + thumbnailUrl +
                ", description=" + description +
                ", lengthFormatted=" + lengthFormatted +
                ", viewCount=" + viewCount +
                ", mylistCount=" + mylistCount +
                ", commentCount=" + commentCount +
                ", uploadUserId=" + uploadUserId +
                ", uploadUserName=" + uploadUserName +
                ", uploadUserIconUrl=" + uploadUserIconUrl;
    }
}
