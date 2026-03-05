
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
        "1",
        "2",
        "3",
        "4"
})
public class Streams {

    @JsonProperty("1")
    private dev.cosgy.agent.objects._1 _1;
    @JsonProperty("2")
    private dev.cosgy.agent.objects._2 _2;
    @JsonProperty("3")
    private dev.cosgy.agent.objects._3 _3;
    @JsonProperty("4")
    private dev.cosgy.agent.objects._4 _4;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("1")
    public dev.cosgy.agent.objects._1 get1() {
        return _1;
    }

    @JsonProperty("1")
    public void set1(dev.cosgy.agent.objects._1 _1) {
        this._1 = _1;
    }

    @JsonProperty("2")
    public dev.cosgy.agent.objects._2 get2() {
        return _2;
    }

    @JsonProperty("2")
    public void set2(dev.cosgy.agent.objects._2 _2) {
        this._2 = _2;
    }

    @JsonProperty("3")
    public dev.cosgy.agent.objects._3 get3() {
        return _3;
    }

    @JsonProperty("3")
    public void set3(dev.cosgy.agent.objects._3 _3) {
        this._3 = _3;
    }

    @JsonProperty("4")
    public dev.cosgy.agent.objects._4 get4() {
        return _4;
    }

    @JsonProperty("4")
    public void set4(dev.cosgy.agent.objects._4 _4) {
        this._4 = _4;
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
