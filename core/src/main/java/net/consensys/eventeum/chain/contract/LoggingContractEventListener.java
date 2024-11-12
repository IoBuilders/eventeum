/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeum.chain.contract;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.integration.eventstore.EventStore;
import org.springframework.stereotype.Component;

/**
 * A contract event listener that logs the contract event details.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Slf4j
@Component
@AllArgsConstructor
public class LoggingContractEventListener implements ContractEventListener {

    private EventStore eventStore;

    @Override
    public void onEvent(ContractEventDetails eventDetails) {
        if (!isExistingEvent(eventDetails)) {
            log.info("Contract event fired: {}", eventDetails.getName());
        }
    }

    private boolean isExistingEvent(ContractEventDetails eventDetails) {
        return eventStore.getContractEvent(
                eventDetails.getEventSpecificationSignature(),
                eventDetails.getAddress(),
                eventDetails.getBlockHash(),
                eventDetails.getTransactionHash(),
                eventDetails.getLogIndex()
        ).isPresent();
    }

}
