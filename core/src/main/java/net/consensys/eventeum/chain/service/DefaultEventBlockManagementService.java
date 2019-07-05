package net.consensys.eventeum.chain.service;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.service.EventStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default implementation of an EventBlockManagementService, which "Manages the latest block
 * that has been seen to a specific event specification."
 *
 * This implementation stores the latest blocks for each event filter in memory, but delegates to
 * the event store if an entry is not found in memory.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
@Slf4j
public class DefaultEventBlockManagementService implements EventBlockManagementService {

    private AbstractMap<String, BigInteger> latestBlocks = new ConcurrentHashMap<>();

    private ChainServicesContainer chainServicesContainer;

    private EventStoreService eventStoreService;

    @Autowired
    public DefaultEventBlockManagementService(@Lazy ChainServicesContainer chainServicesContainer,
                                              EventStoreService eventStoreService) {
        this.chainServicesContainer = chainServicesContainer;
        this.eventStoreService = eventStoreService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLatestBlock(String eventSpecHash, BigInteger blockNumber) {
        final BigInteger currentLatest = latestBlocks.get(eventSpecHash);

        if (currentLatest == null || blockNumber.compareTo(currentLatest) > 0) {
            latestBlocks.put(eventSpecHash, blockNumber);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger getBlockNumberToScanEvent(ContractEventFilter eventFilter) {
        final String eventSignature = Web3jUtil.getSignature(eventFilter.getEventSpecification());
        final BigInteger latestBlockNumber = latestBlocks.get(eventSignature);

        if (latestBlockNumber != null) {
            log.debug("latestBlockNumber {} found in memory, starting at blockNumber: {}", eventFilter.getId(), latestBlockNumber.add(BigInteger.ONE));

            return latestBlockNumber.add(BigInteger.ONE);
        }

        final ContractEventDetails contractEvent = eventStoreService.getLatestContractEvent(eventSignature, eventFilter.getNode());

        if (contractEvent != null) {
            log.debug("contractEvent {} found in the database, starting at blockNumber: {}", eventFilter.getId(), contractEvent.getBlockNumber().add(BigInteger.ONE));


            return contractEvent.getBlockNumber().add(BigInteger.ONE);
        }

        final BlockchainService blockchainService =
                chainServicesContainer.getNodeServices(eventFilter.getNode()).getBlockchainService();

        BigInteger currentBlockNumber =  blockchainService.getCurrentBlockNumber();

        log.debug("Event {} not found in memory or database, starting at blockNumber: {}", eventFilter.getId(), currentBlockNumber);


        return currentBlockNumber;
    }
}
