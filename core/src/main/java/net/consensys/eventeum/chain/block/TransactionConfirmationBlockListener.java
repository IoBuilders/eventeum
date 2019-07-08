package net.consensys.eventeum.chain.block;

import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.chain.service.domain.Log;
import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.integration.broadcast.blockchain.BlockchainEventBroadcaster;
import net.consensys.eventeum.service.AsyncTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransactionConfirmationBlockListener extends SelfUnregisteringBlockListener {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionConfirmationBlockListener.class);

    private TransactionDetails transactionDetails;
    private BlockchainService blockchainService;
    private BlockchainEventBroadcaster eventBroadcaster;
    private BigInteger targetBlock;
    private BigInteger blocksToWaitForMissingTx;
    private EventConfirmationConfig eventConfirmationConfig;
    private AsyncTaskService asyncTaskService;
    private BlockListener parentBlockListener;

    private AtomicBoolean isInvalidated = new AtomicBoolean(false);
    private BigInteger missingTxBlockLimit;

    public TransactionConfirmationBlockListener(TransactionDetails transactionDetails,
                                                BlockchainService blockchainService,
                                                BlockchainEventBroadcaster eventBroadcaster,
                                                EventConfirmationConfig eventConfirmationConfig,
                                                AsyncTaskService asyncTaskService,
                                                BlockListener parentBlockListener) {
        super(blockchainService);
        this.transactionDetails = transactionDetails;
        this.blockchainService = blockchainService;
        this.eventBroadcaster = eventBroadcaster;
        this.asyncTaskService = asyncTaskService;
        this.parentBlockListener = parentBlockListener;

        final BigInteger currentBlock = blockchainService.getCurrentBlockNumber();
        this.targetBlock = currentBlock.add(eventConfirmationConfig.getBlocksToWaitForConfirmation());
        this.blocksToWaitForMissingTx = eventConfirmationConfig.getBlocksToWaitForMissingTx();
    }

    @Override
    public void onBlock(BlockDetails blockDetails) {
        //Needs to be called asynchronously, otherwise websocket is blocked
        asyncTaskService.execute(() -> {
            final TransactionReceipt receipt = blockchainService.getTransactionReceipt(transactionDetails.getHash());

            if (receipt == null) {
                //Tx has disappeared...we've probably forked
                //Tx should be included in block on new fork soon
                handleMissingTransaction(blockDetails);
                return;
            }

            checkTransactionStatus(blockDetails.getNumber(), receipt);
        });
    }

    private void checkTransactionStatus(BigInteger currentBlockNumber, TransactionReceipt receipt) {
        if (isOrphanedTransaction(receipt)) {
            processInvalidatedEvent();
        } else if (currentBlockNumber.compareTo(targetBlock) >= 0) {
            LOG.debug("Target block reached for transaction: {}", transactionDetails.getHash());
            broadcastTransactionConfirmed();
            unregister();
        }
    }

    private void processInvalidatedEvent() {
        broadcastTransactionInvalidated();
        isInvalidated.set(true);
        unregister();
    }

    private boolean isOrphanedTransaction(TransactionReceipt receipt) {
        //If block hash or log index are not as expected, this means that the transaction
        //associated with the event has been included in a block on a different fork of a longer chain
        //and the original event is considered orphaned.
        String orphanReason = null;

        if (!receipt.getBlockHash().equals(transactionDetails.getBlockHash())) {
            orphanReason = "Expected blockhash " + transactionDetails.getBlockHash() + ", received " + receipt.getBlockHash();
        }

        if (orphanReason != null) {
            LOG.info("Orphan event detected: " + orphanReason);
            return true;
        }

        return false;
    }

    private void broadcastTransactionInvalidated() {
        transactionDetails.setStatus(TransactionStatus.INVALIDATED);
        broadcastEvent(transactionDetails);
    }

    private void broadcastTransactionConfirmed() {
        transactionDetails.setStatus(TransactionStatus.CONFIRMED);
        broadcastEvent(transactionDetails);

        //Unregister parent monitoring listener as we haven't forked so its no longer needed
        blockchainService.removeBlockListener(parentBlockListener);
    }

    private void broadcastEvent(TransactionDetails transactionDetails) {
        if (!isInvalidated.get()) {
            LOG.debug(String.format("Sending confirmed event for transaction: %s", transactionDetails.getHash()));
            eventBroadcaster.broadcastTransaction(transactionDetails);
        }
    }

    private void handleMissingTransaction(BlockDetails blockDetails) {
        if (missingTxBlockLimit == null) {
            missingTxBlockLimit = blockDetails.getNumber().add(blocksToWaitForMissingTx);
        } else if (blockDetails.getNumber().compareTo(missingTxBlockLimit) > 0) {
            processInvalidatedEvent();
        }
    }
}
