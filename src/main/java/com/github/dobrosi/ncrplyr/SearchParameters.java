package com.github.dobrosi.ncrplyr;

import com.github.dobrosi.ncrplyr.controller.VideoType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SearchParameters {
    public String title;
    public String imdbid;
    public VideoType type;

    public SearchParameters(String title, String imdbid, VideoType type) {
        this.title = title;
        this.imdbid = imdbid;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        SearchParameters other = (SearchParameters) obj;
        return new EqualsBuilder().append(other.imdbid, imdbid).append(other.type, type).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).
            append(imdbid).
            append(true).
            toHashCode();
    }

    @Override
    public String toString() {
        return imdbid + (type != null ? "/" + type + (title != null ? "/" + title : "") : "");
    }
}
