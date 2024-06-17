package net.consensys.eventeum.chain.service.domain.io;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class BlocksResponse {
    List<BlockResponse> blocks;
    Map<String, String> links;
}
