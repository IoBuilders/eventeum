package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.settings.Node;

public interface BlockchainServiceFactory {

    BlockchainService create(Node node);
}
