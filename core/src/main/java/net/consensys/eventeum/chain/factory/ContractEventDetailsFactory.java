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

package net.consensys.eventeum.chain.factory;

import net.consensys.eventeum.chain.service.domain.TransactionReceipt;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;

/**
 * A factory interface for creating ContractEventDetails objects from the event filter plus the
 * Web3J log.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public interface ContractEventDetailsFactory {
    ContractEventDetails createEventDetails(ContractEventFilter eventFilter, Log log, EthBlock ethBlock, TransactionReceipt transactionReceipt);
    ContractEventDetails createEventDetails(ContractEventFilter eventFilter, Log log, BigInteger blockTimestamp, String fromTransactionReceipt);
}
