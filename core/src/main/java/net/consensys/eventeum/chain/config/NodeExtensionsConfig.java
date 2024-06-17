package net.consensys.eventeum.chain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties
@Data
public class NodeExtensionsConfig {
    private Map<String, Object> nodeExtensions;

    public Map<String,Object> getExtension(String nodeName){
        return (Map<String, Object>) nodeExtensions.get(nodeName);
    }
}
