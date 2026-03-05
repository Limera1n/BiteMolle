
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
        "LASTUPDATE",
        "SERVERS",
        "STATUS",
        "LISTENERS",
        "STREAMS",
        "MODE"
})

public class Serverinfo {

    @JsonProperty("LASTUPDATE")
    private Integer lastupdate;
    @JsonProperty("SERVERS")
    private Integer servers;
    @JsonProperty("STATUS")
    private String status;
    @JsonProperty("LISTENERS")
    private Integer listeners;
    @JsonProperty("STREAMS")
    private Streams streams;
    @JsonProperty("MODE")
    private String mode;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("LASTUPDATE")
    public Integer getLastupdate() {
        return lastupdate;
    }

    @JsonProperty("LASTUPDATE")
    public void setLastupdate(Integer lastupdate) {
        this.lastupdate = lastupdate;
    }

    @JsonProperty("SERVERS")
    public Integer getServers() {
        return servers;
    }

    @JsonProperty("SERVERS")
    public void setServers(Integer servers) {
        this.servers = servers;
    }

    @JsonProperty("STATUS")
    public String getStatus() {
        return status;
    }

    @JsonProperty("STATUS")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("LISTENERS")
    public Integer getListeners() {
        return listeners;
    }

    @JsonProperty("LISTENERS")
    public void setListeners(Integer listeners) {
        this.listeners = listeners;
    }

    @JsonProperty("STREAMS")
    public Streams getStreams() {
        return streams;
    }

    @JsonProperty("STREAMS")
    public void setStreams(Streams streams) {
        this.streams = streams;
    }

    @JsonProperty("MODE")
    public String getMode() {
        return mode;
    }

    @JsonProperty("MODE")
    public void setMode(String mode) {
        this.mode = mode;
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
