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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Nxt2 transaction
 * <p>
 * A Nxt transaction is associated with a chain.  An FXT
 * transaction is included in a block as a block transaction.
 * A child chain transaction is bundled with other transactions
 * on the same child chain and the bundled transactions are included
 * in a block as a single child block transaction.
 * <p>
 * The transaction identifier is not unique, even within the same
 * child chain.  As a result, the transaction full hash is used to
 * identify a transaction.  The transaction identifier is provided as
 * a means to look up a transaction in a hash table.
 */
public class Transaction {

    /** Transaction length */
    private static final int BASE_LENGTH = 149;

    /** Signature offset in the transaction bytes */
    private static final int SIGNATURE_OFFSET = 69;

    /** Zero signature */
    private static final byte[] ZERO_SIGNATURE = new byte[64];

    /** Chain containing the transaction */
    private final Chain chain;

    /** Transaction version */
    private final int version;

    /** Transaction identifier */
    private final long id;

    /** Transaction full hash */
    private final byte[] fullHash;

    /** Transaction amount */
    private final long amount;

    /** Transaction fee */
    private final long fee;

    /** Transaction timestamp */
    private final Date timestamp;

    /** Transaction sender */
    private final long senderId;

    /** Transaction recipient */
    private final long recipientId;

    /** Transaction deadline */
    private final int deadline;

    /** Transaction EC block height */
    private final int ecBlockHeight;

    /**Transaction EC block identifier */
    private final long ecBlockId;

    /** Transaction type */
    private final TransactionType transactionType;

    /** Block identifier */
    private long blockId;

    /** Transaction height */
    private int height;

    /** Transaction attachment bytes */
    private final byte[] attachmentBytes;

    /** Transaction attachment JSON */
    private final Response attachmentJSON;

    /**
     * Process a transaction list
     *
     * @param   transactionList         JSON transaction list
     * @return                          Transaction list
     * @throws  IdentifierException     Invalid Nxt object identifier
     * @throws  NumberFormatException   Invalid numeric value
     */
    public static List<Transaction> processTransactions(List<Response> transactionList)
                                        throws IdentifierException, NumberFormatException {
        List<Transaction> txList = new ArrayList<>(transactionList.size());
        for (Response tx : transactionList) {
            txList.add(new Transaction(tx));
        }
        return txList;
    }

    /**
     * Create a new transaction from the transaction JSON.
     * <p>
     * The transaction identifier and full hash will be zero for an unsigned transaction.
     * The height and block identifier will be zero for an unconfirmed transaction.
     *
     * @param   response                    Nxt transaction response
     * @throws  IdentifierException         Invalid Nxt object identifier
     * @throws  NumberFormatException       Invalid numeric value
     */
    public Transaction(Response response) throws IdentifierException, NumberFormatException {
        version = response.getByte("version");
        fullHash = Utils.parseHexString(response.getString("fullHash"));
        if (fullHash.length > 0) {
            id = Utils.fullHashToId(fullHash);
        } else {
            id = 0;
        }
        amount = response.getLong("amountNQT");
        fee = response.getLong("feeNQT");
        timestamp = new Date((response.getLong("timestamp")) * 1000L + Nxt.getEpoch());
        senderId = response.getLong("sender");
        recipientId = response.getLong("recipient");
        deadline = response.getInt("deadline");
        int chainId = response.getInt("chain");
        chain = Nxt.getChain(chainId);
        if (chain == null)
            throw new IdentifierException("Nxt chain '" + chainId +"' is not defined");
        int txHeight = response.getInt("height");
        if (txHeight == 0 || txHeight == Integer.MAX_VALUE) {
            height = 0;
            blockId = 0;
        } else {
            height = txHeight;
            blockId = response.getLong("block");
        }
        ecBlockId = response.getId("ecBlockId");
        ecBlockHeight = response.getInt("ecBlockHeight");
        attachmentBytes = null;
        attachmentJSON = response.getObject("attachment");
        int type = response.getInt("type");
        int subtype = response.getInt("subtype");
        transactionType = Nxt.getTransactionType(type, subtype);
    }

    /**
     * Create a new transaction from the transaction bytes.
     * <p>
     * The transaction identifier and full hash will be zero for an unsigned transaction.
     * The height and block identifier will be zero since that information
     * is not available as part of the transaction bytes.
     *
     * @param   transactionBytes            Transaction bytes
     * @throws  BufferUnderflowException    Transaction bytes buffer is too short
     * @throws  IdentifierException         A Nxt object identifier is not valid
     * @throws  NumberFormatException       Invalid hexadecimal format
     */
    public Transaction(byte[] transactionBytes)
                throws IdentifierException, BufferUnderflowException, NumberFormatException {
        ByteBuffer buffer = ByteBuffer.wrap(transactionBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int chainId = buffer.getInt();
        chain = Nxt.getChain(chainId);
        if (chain == null)
            throw new IdentifierException("Nxt chain '" + chainId + "' is not defined");
        int type = buffer.get();
        int subtype = buffer.get();
        version = buffer.get();
        timestamp = new Date(((long)buffer.getInt() * 1000L) + Nxt.getEpoch());
        deadline = buffer.getShort();
        byte[] publicKey = new byte[32];
        buffer.get(publicKey);
        senderId = Utils.getAccountId(publicKey);
        recipientId = buffer.getLong();
        amount = buffer.getLong();
        fee = buffer.getLong();
        byte[] signature = new byte[64];
        buffer.get(signature);
        ecBlockHeight = buffer.getInt();
        ecBlockId = buffer.getLong();
        transactionType = Nxt.getTransactionType(type, subtype);
        attachmentBytes = Arrays.copyOfRange(transactionBytes, BASE_LENGTH, transactionBytes.length);
        attachmentJSON = null;
        height = 0;
        blockId = 0;
        //
        // Generate the full hash and transaction identifier if the transaction
        // has been signed.  Otherwise, the hash and idenifier are zero.
        //
        if (Arrays.equals(signature, ZERO_SIGNATURE)) {
            id = 0;
            fullHash = new byte[0];
        } else {
            byte[] data = Arrays.copyOf(transactionBytes, transactionBytes.length);
            Arrays.fill(data, SIGNATURE_OFFSET, SIGNATURE_OFFSET+64, (byte)0);
            byte[] signatureHash = Crypto.singleDigest(signature);
            fullHash = Crypto.singleDigest(data, signatureHash);
            id = Utils.fullHashToId(fullHash);
        }
    }

    /**
     * Get the transaction type
     *
     * @return                      Transaction type
     */
    public TransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * Get the chain
     *
     * @return                      Chain
     */
    public Chain getChain() {
        return chain;
    }

    /**
     * Get the transaction version
     *
     * @return                      Transaction version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Get the transaction identifier
     *
     * @return                      Transaction identifier (0 if transaction not signed)
     */
    public long getId() {
        return id;
    }

    /**
     * Get the transaction full hash
     *
     * @return                      Full hash (zero-length array if transaction not signed)
     */
    public byte[] getFullHash() {
        return fullHash;
    }

    /**
     * Get the sender
     *
     * @return                      Sender
     */
    public long getSenderId() {
        return senderId;
    }

    /**
     * Get the recipient
     *
     * @return                      Recipient or 0 if no recipient
     */
    public long getRecipientId() {
        return recipientId;
    }

    /**
     * Get the transaction amount with an implicit decimal point as determined
     * by the chain
     *
     * @return                      Amount
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Get the transaction fee with an implicit decimal point as determined
     * by the chain
     *
     * @return                      Fee
     */
    public long getFee() {
        return fee;
    }

    /**
     * Get the transaction timestamp
     *
     * @return                      Timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Get the transaction deadline
     *
     * @return                      Transaction deadline in minutes
     */
    public int getDeadline() {
        return deadline;
    }

    /**
     * Get the EC block height
     *
     * @return                      EC block height
     */
    public int getEcBlockHeight() {
        return ecBlockHeight;
    }

    /**
     * Get the EC block identifier
     *
     * @return                      EC block identifier
     */
    public long getEcBlockIdentifier() {
        return ecBlockId;
    }

    /**
     * Get the block identifier
     *
     * @return                      Block identifier (0 if transaction is unconfirmed)
     */
    public long getBlockId() {
        return blockId;
    }

    /**
     * Set the block identifier
     *
     * @param   blockId             New block identifier
     */
    public void setBlockId(long blockId) {
        this.blockId = blockId;
    }

    /**
     * Get the transaction height
     *
     * @return                      Height (0 if transaction unconfirmed)
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set the transaction height
     *
     * @param   height              New transaction height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the attachment JSON
     * <p>
     * The attachment JSON is available only for a transaction created from a JSON
     * response.
     *
     * @return                      JSON attachment response or null
     */
    public Response getAttachmentJSON() {
        return attachmentJSON;
    }

    /**
     * Get the attachment bytes
     * <p>
     * The attachment bytes are available only for a transaction created from a
     * byte stream.
     *
     * @return                      Attachment bytes or null
     */
    public byte[] getAttachmentBytes() {
        return attachmentBytes;
    }

    /**
     * Get the hash code
     *
     * @return                      Hash code
     */
    @Override
    public int hashCode() {
        return (fullHash.length > 0 ? Arrays.hashCode(fullHash) :
                timestamp.hashCode() ^ Long.hashCode(senderId) ^ Long.hashCode(recipientId) ^
                Long.hashCode(amount) ^ transactionType.hashCode());
    }

    /**
     * Check if two transactions are equal
     *
     * @param   obj                 Comparison transaction
     * @return                      TRUE if the transactions are the same
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Transaction))
            return false;
        Transaction tx = (Transaction)obj;
        if (fullHash.length > 0)
            return Arrays.equals(fullHash, tx.fullHash);
        return timestamp.equals(tx.timestamp) && senderId == tx.senderId &&
                recipientId == tx.recipientId && amount == tx.amount &&
                transactionType.equals(tx.transactionType);
    }
}