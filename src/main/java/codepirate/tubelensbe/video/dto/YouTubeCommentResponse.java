package codepirate.tubelensbe.video.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;

@Data
public class YouTubeCommentResponse {
    private String kind;
    private String etag;
    private String nextPageToken;
    private PageInfo pageInfo;
    private ArrayList<Item> items;

    @Data
    public static class PageInfo {
        private int totalResults;
        private int resultsPerPage;
    }

    @Data
    public static class Item {
        private String kind;
        private String etag;
        private String id;
        private Snippet snippet;
    }

    @Data
    public static class Snippet {
        private String channelId;
        private String videoId;
        private TopLevelComment topLevelComment;
        private boolean canReply;
        private int totalReplyCount;
        private boolean isPublic;
        private String textDisplay;
        private String textOriginal;
        private String authorDisplayName;
        private String authorProfileImageUrl;
        private String authorChannelUrl;
        private AuthorChannelId authorChannelId;
        private boolean canRate;
        private String viewerRating;
        private int likeCount;
        private Date publishedAt;
        private Date updatedAt;
    }

    @Data
    public static class TopLevelComment {
        private String kind;
        private String etag;
        private String id;
        private Snippet snippet;
    }

    @Data
    public static class AuthorChannelId {
        private String value;
    }
}
