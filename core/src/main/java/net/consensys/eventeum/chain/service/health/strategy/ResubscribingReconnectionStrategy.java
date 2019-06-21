package net.consensys.eventeum.chain.service.health.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.consensys.eventeum.chain.service.BlockchainService;
import net.consensys.eventeum.service.SubscriptionService;

@AllArgsConstructor
@Data
public abstract class ResubscribingReconnectionStrategy implements ReconnectionStrategy {

    private SubscriptionService subscriptionService;
    private BlockchainService blockchainService;

    @Override
    public void resubscribe() {
        subscriptionService.resubscribeToAllSubscriptions();

        blockchainService.reconnect();
    }
}
