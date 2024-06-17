package net.consensys.eventeum.config;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.micrometer.core.instrument.util.StringUtils;
import net.consensys.eventeum.integration.HederaSettings;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "hedera.account.id")
public class HederaConfiguration {

    @Bean
    public Client hederaClient(HederaSettings hederaSettings) throws BeanCreationException {
        AccountId operatorId = AccountId.fromString(hederaSettings.getAccount().getId());
        Client client = hederaSettings.isTestnet() ? Client.forTestnet() : Client.forMainnet();
        if (StringUtils.isEmpty(hederaSettings.getAccount().getPrivateKey())) {
            throw new BeanCreationException("Invalid Hedera account configuration. Provide a valid private key.");
        }

        PrivateKey operatorKey = PrivateKey.fromString(hederaSettings.getAccount().getPrivateKey());
        client.setOperator(operatorId, operatorKey);

        return client;
    }

}
