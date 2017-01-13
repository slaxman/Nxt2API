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

/**
 * A ChainTransactionId contains the chain and the
 * transaction full hash for a referenced transaction
 */
public class ChainTransactionId {

    /** Chain */
    private final Chain chain;

    /** Transaction full hash */
    private final byte[] fullHash;

    /**
     * Create a new transaction reference
     *
     * @param   chainId                     Chain identifier
     * @param   fullHash                    Transaction full hash
     * @throws  IllegalArgumentException    Chain is not defined
     */
    public ChainTransactionId(int chainId, byte[] fullHash) {
        chain = Nxt.getChain(chainId);
        if (chain == null)
            throw new IllegalArgumentException("Chain '" + chainId + "' is not defined");
        this.fullHash = fullHash;
    }

    /**
     * Create a new transaction reference
     *
     * @param   json                        JSON data
     * @throws  IdentifierException         Invalid Nxt object identifier
     * @throws  IllegalArgumentException    Response is not valid
     * @throws  NumberFormatException       Invalid numeric value
     */
    public ChainTransactionId(Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
        int chainId = json.getInt("chain");
        chain = Nxt.getChain(chainId);
        if (chain == null)
            throw new IllegalArgumentException("Chain '" + chainId + "' is not defined");
        fullHash = json.getHexString("transactionFullHash");
    }

    /**
     * Create a new transaction reference
     *
     * @param   buffer                      Buffer
     * @throws  BufferUnderflowException    End-of-data reached parsing attachment
     * @throws  IllegalArgumentException    Invalid attachment
     */
    public ChainTransactionId(ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
        int chainId = buffer.getShort();
        chain = Nxt.getChain(chainId);
        if (chain == null)
            throw new IllegalArgumentException("Chain '" + chainId + "' is not defined");
        fullHash = new byte[32];
        buffer.get(fullHash);
    }

    /**
     * Get the transaction chain
     *
     * @return                      Chain
     */
    public Chain getChain() {
        return chain;
    }

    /**
     * Get the transaction hash
     *
     * @return                      Transaction hash
     */
    public byte[] getFullHash() {
        return fullHash;
    }
}
