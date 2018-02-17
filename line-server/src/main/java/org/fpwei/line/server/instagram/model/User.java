package org.fpwei.line.server.instagram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private String biography;

    @JsonProperty("blocked_by_viewer")
    private boolean blockedByViewer;

    @JsonProperty("country_block")
    private boolean countryBlock;

    @JsonProperty("external_url")
    private String externalUrl;

    @JsonProperty("external_url_linkshimmed")
    private String externalUrlLinkshimmed;

    @JsonProperty("edge_follow")
    private EdgeFollow edgeFollow;

    @JsonProperty("edge_followed_by")
    private EdgeFollowedBy edgeFollowedBy;

    @JsonProperty("followed_by")
    private FollowedBy followedBy;

    @JsonProperty("followed_by_viewer")
    private boolean followedByViewer;

    @JsonProperty("follows")
    private Follows follows;

    @JsonProperty("follows_viewer")
    private boolean followsViewer;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("has_blocked_viewer")
    private boolean hasBlockedViewer;

    @JsonProperty("has_requested_viewer")
    private boolean hasRequestedViewer;

    private int id;

    @JsonProperty("is_private")
    private boolean isPrivate;

    @JsonProperty("is_verified")
    private boolean isVerified;

    @JsonProperty("profile_pic_url")
    private String profilePicUrl;

    @JsonProperty("profile_pic_url_hd")
    private String profilePicUrlHd;

    @JsonProperty("requested_by_viewer")
    private boolean requestedByViewer;

    private String username;

    @JsonProperty("connected_fb_page")
    private String connectedFbPage;

    private Media media;

    @JsonProperty("saved_media")
    private SavedMedia savedMedia;

    @JsonProperty("media_collections")
    private MediaCollections mediaCollections;

    @JsonProperty("edge_owner_to_timeline_media")
    private EdgeOwnerToTimelineMedia edgeOwnerToTimelineMedia;

    @JsonProperty("edge_saved_media")
    private EdgeSavedMedia edgeSavedMedia;

    @JsonProperty("edge_media_collections")
    private EdgeMediaCollections edgeMediaCollections;

    @JsonProperty("mutual_followers")
    private String mutualFollowers;
}
