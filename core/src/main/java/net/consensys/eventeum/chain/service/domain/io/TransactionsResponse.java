package net.consensys.eventeum.chain.service.domain.io;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class TransactionsResponse {

    private List<TransactionResponse> transactions;

    private Map<String, String> links;

}
