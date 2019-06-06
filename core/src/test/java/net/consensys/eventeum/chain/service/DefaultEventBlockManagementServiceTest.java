package net.consensys.eventeum.chain.service;

import net.consensys.eventeum.chain.service.container.ChainServicesContainer;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.service.EventStoreService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultEventBlockManagementServiceTest {

    private static final String EVENT_SPEC_HASH = "0x4d44a3f7bbdc3ab16cf28ad5234f38b7464ff912da473754ab39f0f97692eded";

    private static final ContractEventSpecification EVENT_SPEC;

    private static final ContractEventFilter EVENT_FILTER;

    private DefaultEventBlockManagementService underTest;

    private BlockchainService mockBlockchainService;

    private EventStoreService mockEventStoreService;

    private NodeServices mockNodeServices;

    private ChainServicesContainer mockChainServicesContainer;

    static {
        EVENT_SPEC = new ContractEventSpecification();
        EVENT_SPEC.setEventName("AnEvent");
        EVENT_SPEC.setIndexedParameterDefinitions(Arrays.asList(
                 new ParameterDefinition(0, ParameterType.ADDRESS),
                 new ParameterDefinition(1, ParameterType.UINT256)));

        EVENT_FILTER = new ContractEventFilter();
        EVENT_FILTER.setNode(ContractEventFilter.DEFAULT_NODE_NAME);
        EVENT_FILTER.setEventSpecification(EVENT_SPEC);

        EVENT_SPEC.setNonIndexedParameterDefinitions(
                Arrays.asList(new ParameterDefinition(2, ParameterType.BYTES32)));
    }

    @Before
    public void init() {
        mockNodeServices = mock(NodeServices.class);
        mockChainServicesContainer = mock(ChainServicesContainer.class);
        mockBlockchainService = mock(BlockchainService.class);
        mockEventStoreService = mock(EventStoreService.class);

        when(mockChainServicesContainer.getNodeServices(ContractEventFilter.DEFAULT_NODE_NAME))
                .thenReturn(mockNodeServices);
        when(mockNodeServices.getBlockchainService()).thenReturn(mockBlockchainService);

        underTest = new DefaultEventBlockManagementService(mockChainServicesContainer, mockEventStoreService);
    }

    @Test
    public void testUpdateAndGetNoMatch() {
        underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.TEN);
        final BigInteger result = underTest.getBlockNumberToScanEvent(EVENT_FILTER);

        assertEquals(BigInteger.valueOf(11), result);
    }

    @Test
    public void testUpdateAndGetLowerMatch() {
        underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.ONE);
        underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.TEN);
        final BigInteger result = underTest.getBlockNumberToScanEvent(EVENT_FILTER);

        assertEquals(BigInteger.valueOf(11), result);
    }

    @Test
    public void testUpdateAndGetHigherMatch() {
        underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.TEN);
        underTest.updateLatestBlock(EVENT_SPEC_HASH, BigInteger.ONE);
        final BigInteger result = underTest.getBlockNumberToScanEvent(EVENT_FILTER);

        assertEquals(BigInteger.valueOf(11), result);
    }

    @Test
    public void testGetNoLocalMatchButHitInEventStore() {
        final ContractEventDetails mockEventDetails = mock(ContractEventDetails.class);
        when(mockEventDetails.getBlockNumber()).thenReturn(BigInteger.ONE);
        when(mockEventStoreService.getLatestContractEvent(EVENT_SPEC_HASH)).thenReturn(mockEventDetails);

        final BigInteger result = underTest.getBlockNumberToScanEvent(EVENT_FILTER);

        assertEquals(BigInteger.valueOf(2), result);
    }

    @Test
    public void testGetNoLocalMatchAndNoHitInEventStore() {
        when(mockEventStoreService.getLatestContractEvent(EVENT_SPEC_HASH)).thenReturn(null);
        when(mockBlockchainService.getCurrentBlockNumber()).thenReturn(BigInteger.valueOf(20));

        final BigInteger result = underTest.getBlockNumberToScanEvent(EVENT_FILTER);

        assertEquals(BigInteger.valueOf(20), result);
    }
}
