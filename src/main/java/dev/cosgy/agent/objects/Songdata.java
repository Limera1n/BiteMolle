
/*
 *  Copyright 2022 Cosgy Dev (info@cosgy.dev).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package dev.cosgy.agent.objects;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "SONGID",
        "ALBUMID",
        "RATING",
        "TIMESRATED"
})

public class Songdata {

    @JsonProperty("SONGID")
    private String songid;
    @JsonProperty("ALBUMID")
    private String albumid;
    @JsonProperty("RATING")
    private String rating;
    @JsonProperty("TIMESRATED")
    private Integer timesrated;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("SONGID")
    public String getSongid() {
        return songid;
    }

    @JsonProperty("SONGID")
    public void setSongid(String songid) {
        this.songid = songid;
    }

    @JsonProperty("ALBUMID")
    public String getAlbumid() {
        return albumid;
    }

    @JsonProperty("ALBUMID")
    public void setAlbumid(String albumid) {
        this.albumid = albumid;
    }

    @JsonProperty("RATING")
    public String getRating() {
        return rating;
    }

    @JsonProperty("RATING")
    public void setRating(String rating) {
        this.rating = rating;
    }

    @JsonProperty("TIMESRATED")
    public Integer getTimesrated() {
        return timesrated;
    }

    @JsonProperty("TIMESRATED")
    public void setTimesrated(Integer timesrated) {
        this.timesrated = timesrated;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
