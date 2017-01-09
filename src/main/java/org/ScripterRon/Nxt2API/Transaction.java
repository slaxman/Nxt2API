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
import java.util.SortedMap;

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
 * identify a transaction.  The transaction identifier is still
 * provided for use as hash table index.
 */
public class Transaction {

    /** Signature offset in the transaction bytes */
    static final int SIGNATURE_OFFSET = 69;

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

    /** Transaction attachment */
    private Attachment attachment;

    /** Plain message appendix */
    private Appendix.MessageAppendix messageAppendix;

    /** Encrypted message appendix */
    private Appendix.EncryptedMessageAppendix encryptedMessageAppendix;

    /** Encrypt-to-self message appendix */
    private Appendix.EncryptToSelfMessageAppendix encryptToSelfMessageAppendix;

    /** Prunable plain message appendix */
    private Appendix.PrunablePlainMessageAppendix prunablePlainMessageAppendix;

    /** Prunable encrypted message appendix */
    private Appendix.PrunableEncryptedMessageAppendix prunableEncryptedMessageAppendix;

    /** Public key announcement appendix */
    //private Appendix.PublicKeyAnnouncementAppendix publicKeyAnnouncementAppendix;

    /** Phasing appendix */
    //private Appendix.PhasingAppendix phasingAppendix;

    /** Block identifier */
    private long blockId;

    /** Transaction height */
    private int height;

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
        ecBlockId = response.getId("ecBlockId");
        ecBlockHeight = response.getInt("ecBlockHeight");
        int type = response.getInt("type");
        int subtype = response.getInt("subtype");
        transactionType = Nxt.getTransactionType(type, subtype);
        //
        // Get the transaction attachment
        //
        Response attachmentJSON = response.getObject("attachment");
        Attachment.AttachmentType attachmentType = Attachment.AttachmentType.get(transactionType);
        if (attachmentType != null) {
            Attachment parser = attachmentType.getAttachment();
            if (parser != null) {
                attachment = parser.parseAttachment(transactionType, attachmentJSON);
            }
        }
        //
        // Get the appendices
        //
        SortedMap<Integer, Appendix.AppendixType> appendixMap = Appendix.AppendixType.getAppendixMap();
        for (Appendix.AppendixType appendixType : appendixMap.values()) {
            Appendix parser = appendixType.getAppendix();
            if (parser != null && attachmentJSON.getInt("version." + parser.getName()) > 0) {
                Appendix appendix = parser.parseAppendix(attachmentJSON);
                switch (appendix.getName()) {
                    case "Message":
                        messageAppendix = (Appendix.MessageAppendix)appendix;
                        break;
                    case "EncryptedMessage":
                        encryptedMessageAppendix = (Appendix.EncryptedMessageAppendix)appendix;
                        break;
                    case "EncryptToSelfMessage":
                        encryptToSelfMessageAppendix = (Appendix.EncryptToSelfMessageAppendix)appendix;
                        break;
                    case "PrunablePlainMessage":
                        prunablePlainMessageAppendix = (Appendix.PrunablePlainMessageAppendix)appendix;
                        break;
                    case "PrunableEncryptedMessage":
                        prunableEncryptedMessageAppendix = (Appendix.PrunableEncryptedMessageAppendix)appendix;
                        break;
                }
            }
        }
        //
        // Get the transaction height and block identifier
        //
        int txHeight = response.getInt("height");
        if (txHeight == 0 || txHeight == Integer.MAX_VALUE) {
            height = 0;
            blockId = 0;
        } else {
            height = txHeight;
            blockId = response.getLong("block");
        }
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
        int flags = buffer.getInt();
        transactionType = Nxt.getTransactionType(type, subtype);
        //
        // Get the transaction attachment
        //
        Attachment.AttachmentType attachmentType = Attachment.AttachmentType.get(transactionType);
        if (attachmentType != null) {
            Attachment parser = attachmentType.getAttachment();
            if (parser != null) {
                attachment = parser.parseAttachment(transactionType, buffer);
            }
        }
        //
        // Get the appendices
        //
        // The appendices follow the transaction attachment and ordered by the flag bits
        // (1, 2, 4, 8, etc).  The appendix map is sorted by the flag bit, so we just
        // process the map values in order.
        //
        SortedMap<Integer, Appendix.AppendixType> appendixMap = Appendix.AppendixType.getAppendixMap();
        for (Appendix.AppendixType appendixType : appendixMap.values()) {
            if ((flags & appendixType.getCode()) != 0) {
                Appendix parser = appendixType.getAppendix();
                if (parser != null) {
                    Appendix appendix = parser.parseAppendix(buffer);
                    switch (appendix.getName()) {
                        case "Message":
                            messageAppendix = (Appendix.MessageAppendix)appendix;
                            break;
                        case "EncryptedMessage":
                            encryptedMessageAppendix = (Appendix.EncryptedMessageAppendix)appendix;
                            break;
                        case "EncryptToSelfMessage":
                            encryptToSelfMessageAppendix = (Appendix.EncryptToSelfMessageAppendix)appendix;
                            break;
                        case "PrunablePlainMessage":
                            prunablePlainMessageAppendix = (Appendix.PrunablePlainMessageAppendix)appendix;
                            break;
                        case "PrunableEncryptedMessage":
                            prunableEncryptedMessageAppendix = (Appendix.PrunableEncryptedMessageAppendix)appendix;
                            break;
                    }
                }
            }
        }
        //
        // Height and block identifier are not part of the transaction bytes
        //
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
     * Get the transaction attachment
     *
     * @return                      Attachment or null if the attachment type is not supported
     */
    public Attachment getAttachment() {
        return attachment;
    }

    /**
     * Get the message appendix
     *
     * @return                      Message appendix or null if there is no appendix
     */
    public Appendix.MessageAppendix getMessageAppendix() {
        return messageAppendix;
    }

    /**
     * Get the prunable plain message appendix
     *
     * @return                      Message appendix or null if there is no appendix
     */
    public Appendix.PrunablePlainMessageAppendix getPrunablePlainMessageAppendix() {
        return prunablePlainMessageAppendix;
    }

    /**
     * Get the encrypted message appendix
     *
     * @return                      Encrypted message appendix or null if there is no appendix
     */
    public Appendix.EncryptedMessageAppendix getEncryptedMessageAppendix() {
        return encryptedMessageAppendix;
    }

    /**
     * Get the prunable encrypted message appendix
     *
     * @return                      Encrypted message appendix or null if there is no appendix
     */
    public Appendix.PrunableEncryptedMessageAppendix getPrunableEncryptedMessageAppendix() {
        return prunableEncryptedMessageAppendix;
    }

    /**
     * Get the encrypt-to-self message appendix
     *
     * @return                      Encrypt-to-self message appendix or null if there is no appendix
     */
    public Appendix.EncryptToSelfMessageAppendix getEncryptToSelfMessageAppendix() {
        return encryptToSelfMessageAppendix;
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
