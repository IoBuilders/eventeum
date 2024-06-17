package net.consensys.eventeum.dto.message;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@Document
@NoArgsConstructor
public class MessageDetails {

    @Id
    @GeneratedValue
    private String id;

    private String nodeName;

    private String topicId;

    private String message;

    private Long timestamp;

    private Long sequenceNumber;

    private byte[] runningHash;

    public MessageDetails(String nodeName, String topicId, String message, Long timestamp, Long sequenceNumber, byte[] runningHash) {
        this.nodeName = nodeName;
        this.topicId = topicId;
        this.message = message;
        this.timestamp = timestamp;
        this.sequenceNumber = sequenceNumber;
        this.runningHash = runningHash;
    }
}
