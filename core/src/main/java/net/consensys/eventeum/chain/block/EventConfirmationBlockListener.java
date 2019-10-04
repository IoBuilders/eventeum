package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.service.domain.Log;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.service.AsyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventConfirmationBlockListener extends SelfUnregisteringBlockListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfirmationBlockListener.class);

    private ContractEventDetails contractEvent;
    private BlockchainService blockchainService;
    private BlockchainEventBroadcaster eventBroadcaster;
    private BigInteger targetBlock;
    private BigInteger blocksToWaitForMissingTx;
    private BigInteger blocksToWait;
    private EventConfirmationConfig eventConfirmationConfig;
    private AsyncTaskService asyncTaskService;

    private AtomicBoolean isInvalidated = new AtomicBoolean(false);
    private BigInteger missingTxBlockLimit;
    private BigInteger blockOrphanLimit;

    public EventConfirmationBlockListener(ContractEventDetails contractEvent,
                                          BlockchainService blockchainService,
                                          BlockchainEventBroadcaster eventBroadcaster,
                                          EventConfirmationConfig eventConfirmationConfig,
                                          AsyncTaskService asyncTaskService) {
        super(blockchainService);
        this.contractEvent = contractEvent;
        this.blockchainService = blockchainService;
        this.eventBroadcaster = eventBroadcaster;
        this.asyncTaskService = asyncTaskService;

        final BigInteger currentBlock = blockchainService.getCurrentBlockNumber();
        this.targetBlock = currentBlock.add(eventConfirmationConfig.getBlocksToWaitForConfirmation());
        this.blocksToWaitForMissingTx = eventConfirmationConfig.getBlocksToWaitForMissingTx();
        this.blocksToWait = eventConfirmationConfig.getBlocksToWaitToInvalidateEvent();
    }

    @Override
    public void onBlock(BlockDetails blockDetails) {
        //Needs to be called asynchronously, otherwise websocket is blocked
        asyncTaskService.execute(() -> {
            LOG.debug(String.format("Receiving block: %s", blockDetails.getNumber()));
            final TransactionReceipt receipt = blockchainService.getTransactionReceipt(contractEvent.getTransactionHash());

            if (receipt == null) {
                //Tx has disappeared...we've probably forked
                //Tx should be included in block on new fork soon
                handleMissingTransaction(blockDetails);
                return;
            }

            final Optional<Log> log = getCorrespondingLog(receipt);

            if (log.isPresent()) {
                checkEventStatus(blockDetails, log.get());
            } else {
                processInvalidatedEvent(blockDetails);
            }
        });
    }

    private void checkEventStatus(BlockDetails blockDetails, Log log) {
        if (isEventAnOrphan(log)) {
            processInvalidatedEvent(blockDetails);
        } else if (blockDetails.getNumber().compareTo(targetBlock) >= 0) {
            LOG.debug("Target block reached for event: {}", contractEvent.getId());
            broadcastEventConfirmed();
            unregister();
        }
    }

    private boolean isEventAnOrphan(Log log) {
        //If log is flagged as removed then event has obviously been orphaned.
        //If block hash or log index are not as expected, this means that the transaction
        //associated with the event has been included in a block on a different fork of a longer chain
        //and the original event is considered orphaned.
        String orphanReason = null;

        if (log.isRemoved()) {
            orphanReason = "isRemoved == true";
        } else if (!log.getBlockHash().equals(contractEvent.getBlockHash())) {
            orphanReason = "Expected blockhash " + contractEvent.getBlockHash() + ", received " + log.getBlockHash();
        }

        if (orphanReason != null) {
            LOG.info("Orphan event detected: " + orphanReason);
            return true;
        }

        return false;
    }

    private void broadcastEventInvalidated() {
        contractEvent.setStatus(ContractEventStatus.INVALIDATED);
        broadcastEvent(contractEvent);
    }

    private void broadcastEventConfirmed() {
        contractEvent.setStatus(ContractEventStatus.CONFIRMED);
        broadcastEvent(contractEvent);
    }

    private void broadcastEvent(ContractEventDetails contractEvent) {
        if (!isInvalidated.get()) {
            LOG.debug(String.format("Sending confirmed event for contract event: %s", contractEvent.getId()));
            eventBroadcaster.broadcastContractEvent(contractEvent);
        }
    }

    private Optional<Log> getCorrespondingLog(TransactionReceipt receipt) {
        return receipt.getLogs()
                .stream()
                .filter((log) -> log.getLogIndex().equals(contractEvent.getLogIndex()))
                .findFirst();
    }

    private void processInvalidatedEvent(BlockDetails blockDetails) {
        if (blockOrphanLimit == null) {
            blockOrphanLimit = blockDetails.getNumber().add(blocksToWait);
        } else if (blockDetails.getNumber().compareTo(blockOrphanLimit) > 0) {
            LOG.debug(String.format("Unregistering because the block %s was invalidated", blockDetails.getNumber()));
            broadcastEventInvalidated();
            isInvalidated.set(true);
            unregister();
        }
    }

    private void handleMissingTransaction(BlockDetails blockDetails) {
        if (missingTxBlockLimit == null) {
            missingTxBlockLimit = blockDetails.getNumber().add(blocksToWaitForMissingTx);
        } else if (blockDetails.getNumber().compareTo(missingTxBlockLimit) > 0) {
            processInvalidatedEvent(blockDetails);
        }
    }
}
