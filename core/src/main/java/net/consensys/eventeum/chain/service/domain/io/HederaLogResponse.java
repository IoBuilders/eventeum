package net.consensys.eventeum.chain.service.domain.io;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HederaLogResponse {

    private String address;
    private String bloom;
    private String contract_id;
    private String data;
    private String index;
    private List<String> topics;

}
