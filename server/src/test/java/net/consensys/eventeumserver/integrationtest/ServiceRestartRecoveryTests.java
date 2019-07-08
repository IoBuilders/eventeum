package net.consensys.eventeumserver.integrationtest;

import junit.framework.TestCase;
import net.consensys.eventeum.annotation.EnableEventeum;
import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.transaction.TransactionDetails;
import net.consensys.eventeum.dto.transaction.TransactionStatus;
import net.consensys.eventeum.integration.eventstore.db.repository.LatestBlockRepository;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.repository.TransactionMonitoringSpecRepository;
import net.consensys.eventeumserver.Application;
import net.consensys.eventeumserver.integrationtest.utils.ExcludeEmbeddedMongoApplication;
import net.consensys.eventeumserver.integrationtest.utils.RestartingSpringRunner;
import net.consensys.eventeumserver.integrationtest.utils.SpringRestarter;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.CacheAwareContextLoaderDelegate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(RestartingSpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@TestPropertySource(properties=
        {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration"})
public abstract class ServiceRestartRecoveryTests extends BaseKafkaIntegrationTest {
    private static final int MONGO_PORT = 27017;

    private static FixedHostPortGenericContainer mongoContainer;

    @Autowired
    private TransactionMonitoringSpecRepository txRepo;

    @BeforeClass
    public static void startMongo() {
        mongoContainer = new FixedHostPortGenericContainer("mongo:3.5.5");
        mongoContainer.waitingFor(Wait.forListeningPort());
        mongoContainer.withFixedExposedPort(MONGO_PORT, MONGO_PORT);
        mongoContainer.start();
    }

    @AfterClass
    public static void stopMongo() {
        if (mongoContainer != null) {
            mongoContainer.stop();
        }
    }

    protected void doBroadcastMissedBlocksOnStartupAfterFailureTest() throws Exception {

        triggerBlocks(5);

        waitForBlockMessages(5);

        //Depending on timing, the genesis block is sometimes broadcast,
        //So wait another few seconds for the last block if this is the case
        waitForBroadcast();

        List<BlockDetails> broadcastBlocks = getBroadcastBlockMessages();

        final BigInteger lastBlockNumber = broadcastBlocks.get(broadcastBlocks.size() - 1).getNumber();

        getBroadcastBlockMessages().clear();

        restartEventeum(() -> {
            try {
                triggerBlocks(4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        triggerBlocks(2);

        waitForBlockMessages(6);

        //Eventeum will rebroadcast the last seen block after restart in case block
        //wasn't fully processed
        assertEquals(lastBlockNumber, getBroadcastBlockMessages().get(0).getNumber());

        //Assert incremental blocks
        for(int i = 0; i < getBroadcastBlockMessages().size(); i++) {
            final BigInteger expectedNumber = BigInteger.valueOf(i + lastBlockNumber.intValue());

            assertEquals(expectedNumber, getBroadcastBlockMessages().get(i).getNumber());
        }
    }

    public void doBroadcastUnconfirmedEventAfterFailureTest() throws Exception {

        final EventEmitter emitter = deployEventEmitterContract();

        final ContractEventFilter registeredFilter = registerDummyEventFilter(emitter.getContractAddress());

        restartEventeum(() -> {
            try {
                emitter.emitEvent(stringToBytes("BytesValue"), BigInteger.TEN, "StringValue").send();
                waitForBroadcast();
            } catch (Exception e) {
                e.printStackTrace();
                TestCase.fail("Unable to emit event");
            }
        });

        waitForContractEventMessages(1);

        assertEquals(1, getBroadcastContractEvents().size());

        final ContractEventDetails eventDetails = getBroadcastContractEvents().get(0);
        verifyDummyEventDetails(registeredFilter, eventDetails, ContractEventStatus.UNCONFIRMED);
    }

    protected void doBroadcastTransactionUnconfirmedAfterFailureTest() throws Exception {

        triggerBlocks(1);

        waitForBlockMessages(1);

        final String signedHex = createRawSignedTransactionHex();

        final String txHash = Hash.sha3(signedHex);

        monitorTransaction(txHash);

        txRepo.findAll();

        restartEventeum(() -> {
            try {
                final String actualTxHash = sendRawTransaction(signedHex);
                assertEquals(txHash, actualTxHash);
                waitForBroadcast();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        txRepo.findAll();

        waitForTransactionMessages(1);

        assertEquals(1, getBroadcastTransactionMessages().size());

        final TransactionDetails txDetails = getBroadcastTransactionMessages().get(0);
        assertEquals(txHash, txDetails.getHash());
        assertEquals(TransactionStatus.UNCONFIRMED, txDetails.getStatus());
    }
}
