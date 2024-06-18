package net.consensys.eventeum.chain.service.domain.io;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
public class StakingRewardTransferResponse {

    String account;

    BigInteger amount;

}
