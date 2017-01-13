/*
 * Copyright 2017 Ronald W Hoffman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ScripterRon.Nxt2API;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Phasing parameters
 */
public class PhasingParameters {

    private final int votingModel;
    private final long quorum;
    private final long minBalance;
    private final List<Long> whitelistAccounts;
    private final long holdingId;
    private final int minBalanceModel;

    /**
     * Create phasing parameters
     *
     * @param   json                        Phasing parameters JSON
     * @throws  IdentifierException         Invalid Nxt object identifier
     * @throws  IllegalArgumentException    Response is not valid
     * @throws  NumberFormatException       Invalid numeric value
     */
    public PhasingParameters(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
        votingModel = json.getInt("phasingVotingModel");
        quorum = json.getLong("phasingQuorum");
        minBalance = json.getLong("phasingMinBalance");
        whitelistAccounts = json.getIdList("phasingWhitelist");
        holdingId = json.getId("phasingHolding");
        minBalanceModel = json.getInt("phasingMinBalanceModel");
    }

    /**
     * Create phasing parameters
     *
     * @param   buffer                      Phasing parameters buffer
     * @throws  BufferUnderflowException    End-of-data reached parsing attachment
     * @throws  IllegalArgumentException    Invalid attachment
     */
    public PhasingParameters(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
        votingModel = buffer.get();
        quorum = buffer.getLong();
        minBalance = buffer.getLong();
        int count = buffer.get();
        whitelistAccounts = new ArrayList<>();
        for (int i=0; i<count; i++) {
            whitelistAccounts.add(buffer.getLong());
        }
        holdingId = buffer.getLong();
        minBalanceModel = buffer.get();
    }

    /**
     * Get the voting model
     *
     * @return                  Voting model identifier
     */
    public int getVotingModel() {
        return votingModel;
    }

    /**
     * Get the quorum
     *
     * @return                  Quorum
     */
    public long getQuorum() {
        return quorum;
    }

    /**
     * Get the minimum balance model
     *
     * @return                  Minimum balance model identifier
     */
    public int getMinBalanceModel() {
        return minBalanceModel;
    }

    /**
     * Get the minimum balance
     *
     * @return                  Minimum balance
     */
    public long getMinBalance() {
        return minBalance;
    }

    /**
     * Get the whitelist accounts
     *
     * @return                  Whitelist account identifiers
     */
    public List<Long> getWhitelistAccounts() {
        return whitelistAccounts;
    }

    /**
     * Get the holding identifier
     *
     * @return                  Holding identifier
     */
    public long getHoldingId() {
        return holdingId;
    }

    /**
    * Return a string representation of the phasing parameters
    *
    * @param   sb              String builder
    * @return                  The supplied string builder
    */
    public StringBuilder toString(StringBuilder sb) {
        sb.append("  Voting Model:  ").append(Nxt.getVotingModel(votingModel)).append("\n")
                .append("  Quorum:  ").append(quorum).append("\n");
        if (minBalance != 0) {
            sb.append("  Minimum Balance Model:  ").append(Nxt.getVotingModel(minBalanceModel)).append("\n");
            if (minBalanceModel == Nxt.getVotingModel("COIN")) {
                Chain chain = Nxt.getChain((int)holdingId);
                if (chain != null) {
                    sb.append("  Chain:  ").append(chain.getName()).append("\n")
                            .append("  Minimum Balance:  ")
                            .append(Utils.nqtToString(minBalance, chain.getDecimals()))
                            .append("\n");
                }
            } else if (holdingId != 0) {
                sb.append("  Holding:  ").append(Utils.idToString(holdingId)).append("\n")
                        .append("  Minimum Balance:  ").append(String.format("%,d", minBalance)).append("\n");
            }
        }
        if (!getWhitelistAccounts().isEmpty()) {
            getWhitelistAccounts().forEach(account -> sb.append("  Whitelist:  ")
                    .append(Utils.getAccountRsId(account)).append("\n"));
        }
        return sb;
    }

    /**
     * Return a string representation of the phasing parameters
     *
     * @return                      String representation
     */
    @Override
    public String toString() {
        return toString(new StringBuilder(64)).toString();
    }
}
