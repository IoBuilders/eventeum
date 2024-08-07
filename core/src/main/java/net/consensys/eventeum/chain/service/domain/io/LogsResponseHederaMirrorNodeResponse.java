
package net.consensys.eventeum.chain.service.domain.io;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "logs",
    "links"
})
@Generated("jsonschema2pojo")
public class LogsResponseHederaMirrorNodeResponse {

    @JsonProperty("logs")
    private List<LogHederaMirrorNodeResponse> logHederaMirrorNodeResponses;
    @JsonProperty("links")
    private LinksResponse linksResponse;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("logs")
    public List<LogHederaMirrorNodeResponse> getLogs() {
        return logHederaMirrorNodeResponses;
    }

    @JsonProperty("logs")
    public void setLogs(List<LogHederaMirrorNodeResponse> logHederaMirrorNodeResponses) {
        this.logHederaMirrorNodeResponses = logHederaMirrorNodeResponses;
    }

    @JsonProperty("links")
    public LinksResponse getLinks() {
        return linksResponse;
    }

    @JsonProperty("links")
    public void setLinks(LinksResponse linksResponse) {
        this.linksResponse = linksResponse;
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
