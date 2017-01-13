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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Transaction attachment
 * <p>
 * Each transaction has a single attachment based on the transaction type.
 * The attachment contains the unique information for that transaction type.
 */
public abstract class Attachment {

    /** Attachment types */
    public enum AttachmentType {
        FXT_CHILDCHAIN_BLOCK(-1, 0, new ChildBlockAttachment()),
        FXT_ORDINARY_PAYMENT(-2, 0, new PaymentAttachment()),
        FXT_BALANCE_LEASING(-3, 0, new EffectiveBalanceLeasingAttachment()),
        FXT_EXCHANGE_ORDER_ISSUE(-4, 0, new CoinExchangeAttachment.OrderIssueAttachment()),
        FXT_EXCHANGE_ORDER_CANCEL(-4, 1, new CoinExchangeAttachment.OrderCancelAttachment()),
        ORDINARY_PAYMENT(0, 0, new PaymentAttachment()),
        ARBITRARY_MESSAGE(1, 0, new MessagingAttachment()),
        ASSET_ISSUANCE(2, 0, new AssetAttachment.IssuanceAttachment()),
        ASSET_TRANSFER(2, 1, new AssetAttachment.TransferAttachment()),
        ASSET_ASK_ORDER_PLACEMENT(2, 2, new AssetAttachment.AskOrderPlacementAttachment()),
        ASSET_BID_ORDER_PLACEMENT(2, 3, new AssetAttachment.BidOrderPlacementAttachment()),
        ASSET_ASK_ORDER_CANCELLATION(2, 4, new AssetAttachment.AskOrderCancellationAttachment()),
        ASSET_BID_ORDER_CANCELLATION(2, 5, new AssetAttachment.BidOrderCancellationAttachment()),
        ASSET_DIVIDEND_PAYMENT(2, 6, new AssetAttachment.DividendPaymentAttachment()),
        ASSET_DELETE(2, 7, new AssetAttachment.DeleteAttachment()),
        DIGITAL_GOODS_LISTING(3, 0, new DigitalGoodsAttachment.ListingAttachment()),
        DIGITAL_GOODS_DELISTING(3, 1, new DigitalGoodsAttachment.DelistingAttachment()),
        DIGITAL_GOODS_PRICE_CHANGE(3, 2, new DigitalGoodsAttachment.PriceChangeAttachment()),
        DIGITAL_GOODS_QUANTITY_CHANGE(3, 3, new DigitalGoodsAttachment.QuantityChangeAttachment()),
        DIGITAL_GOODS_PURCHASE(3, 4, new DigitalGoodsAttachment.PurchaseAttachment()),
        DIGITAL_GOODS_DELIVERY(3, 5, new DigitalGoodsAttachment.DeliveryAttachment()),
        DIGITAL_GOODS_FEEDBACK(3, 6, new DigitalGoodsAttachment.FeedbackAttachment()),
        DIGITAL_GOODS_REFUND(3, 7, new DigitalGoodsAttachment.RefundAttachment()),
        ACCOUNT_CONTROL_PHASING_ONLY(4, 0, new AccountAttachment.SetPhasingOnlyAttachment()),
        CURRENCY_ISSUANCE(5, 0, new CurrencyAttachment.IssuanceAttachment()),
        CURRENCY_RESERVE_INCREASE(5, 1, new CurrencyAttachment.ReserveIncreaseAttachment()),
        CURRENCY_RESERVE_CLAIM(5, 2, new CurrencyAttachment.ReserveClaimAttachment()),
        CURRENCY_TRANSFER(5, 3, new CurrencyAttachment.TransferAttachment()),
        CURRENCY_EXCHANGE_OFFER(5, 4, new CurrencyAttachment.ExchangeOfferAttachment()),
        CURRENCY_EXCHANGE_BUY(5, 5, new CurrencyAttachment.ExchangeBuyAttachment()),
        CURRENCY_EXCHANGE_SELL(5, 6, new CurrencyAttachment.ExchangeSellAttachment()),
        CURRENCY_MINTING(5, 7, new CurrencyAttachment.MintingAttachment()),
        CURRENCY_DELETION(5, 8, new CurrencyAttachment.DeletionAttachment()),
        TAGGED_DATA_UPLOAD(6, 0, new TaggedDataUploadAttachment()),
        SHUFFLING_CREATION(7, 0, new ShufflingAttachment.CreationAttachment()),
        SHUFFLING_REGISTRATION(7, 1, new ShufflingAttachment.RegistrationAttachment()),
        SHUFFLING_PROCESSING(7, 2, new ShufflingAttachment.ProcessingAttachment()),
        SHUFFLING_RECIPIENTS(7, 3, new ShufflingAttachment.RecipientsAttachment()),
        SHUFFLING_VERIFICATION(7, 4, new ShufflingAttachment.VerificationAttachment()),
        SHUFFLING_CANCELLATION(7, 5, new ShufflingAttachment.CancellationAttachment()),
        ALIAS_ASSIGNMENT(8, 0, new AliasAttachment.AssignmentAttachment()),
        ALIAS_SELL(8, 1, new AliasAttachment.SellAttachment()),
        ALIAS_BUY(8, 2, new AliasAttachment.BuyAttachment()),
        ALIAS_DELETE(8, 3, new AliasAttachment.DeleteAttachment()),
        POLL_CREATION(9, 0, new PollAttachment.CreationAttachment()),
        VOTE_CASTING(9, 1, new PollAttachment.VoteCastingAttachment()),
        PHASING_VOTE_CASTING(9, 2, new PollAttachment.PhasingVoteCastingAttachment()),
        ACCOUNT_INFO(10, 0, new AccountAttachment.AccountInfoAttachment()),
        ACCOUNT_PROPERTY_SET(10, 1, new AccountAttachment.PropertySetAttachment()),
        ACCOUNT_PROPERTY_DELETE(10, 2, new AccountAttachment.PropertyDeleteAttachment()),
        EXCHANGE_ORDER_ISSUE(11, 0, new CoinExchangeAttachment.OrderIssueAttachment()),
        EXCHANGE_ORDER_CANCEL(11, 1, new CoinExchangeAttachment.OrderCancelAttachment());

        private static final Map<Integer, AttachmentType> typeMap = new HashMap<>();
        static {
            for (AttachmentType type : values()) {
                typeMap.put((type.type << 8) | type.subtype, type);
            }
        }

        private final int type;
        private final int subtype;
        private final Attachment attachment;

        private AttachmentType(int type, int subtype, Attachment attachment) {
            this.type = type;
            this.subtype = subtype;
            this.attachment = attachment;
        }

        public static AttachmentType get(TransactionType txType) {
            return typeMap.get((txType.getType() << 8) | txType.getSubtype());
        }

        Attachment getAttachment() {
            return attachment;
        }
    }

    /** UTF-8 character set */
    static final Charset UTF8 = Charset.forName("UTF-8");

    /** Attachment version */
    int version;

    /** Transaction type */
    TransactionType transactionType;

    /**
     * Create a dummy attachment
     */
    Attachment() {
    }

    /**
     * Create an empty attachment
     *
     * @param   transactionType             Transaction type
     */
    Attachment(TransactionType txType) {
        transactionType = txType;
        version = 0;
    }

    /**
     * Create an attachment
     *
     * @param   transactionType             Transaction type
     * @param   json                        Attachment JSON
     */
    Attachment(TransactionType txType, Response json) {
        transactionType = txType;
        version = json.getInt("version." + txType.getName());
    }

    /**
     * Create an attachment
     *
     * @param   transactionType             Transaction type
     * @param   buffer                      Attachment bytes
     */
    Attachment(TransactionType txType, ByteBuffer buffer) {
        version = buffer.get();
    }

    /**
     * Get the attachment version
     *
     * @return                              Attachment version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Get the transaction type
     *
     * @return                              Transaction type
     */
    public TransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * Return a string representation of the attachment
     *
     * @param   sb                  String builder
     * @return                      The supplied string builder
     */
    public StringBuilder toString(StringBuilder sb) {
        sb.append("Attachment:  ").append(getTransactionType().getName()).append("\n");
        return sb;
    }

    /**
     * Return a string representation of the attachment
     *
     * @return                      String representation
     */
    @Override
    public String toString() {
        return toString(new StringBuilder(64)).toString();
    }

    /**
     * Parse the attachment JSON
     *
     * @param   txType                      Transaction type
     * @param   json                        Attachment JSON
     * @return                              Attachment or null if transaction not supported
     * @throws  IdentifierException         Invalid Nxt object identifier
     * @throws  IllegalArgumentException    Response is not valid
     * @throws  NumberFormatException       Invalid numeric value
     */
    abstract protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException;

    /**
     * Parse the attachment bytes
     *
     * @param   txType                      Transaction type
     * @param   buffer                      Attachment bytes
     * @return                              Attachment or null if transaction not supported
     * @throws  BufferUnderflowException    End-of-data reached parsing attachment
     * @throws  IllegalArgumentException    Invalid attachment
     */
    abstract protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException;

    /**
     * Get a string
     *
     * @param   length                      String length
     * @param   buffer                      Byte buffer
     * @return                              String
     * @throws  BufferUnderflowException    End-of-data reached parsing attachment
     * @throws  IllegalArgumentException    Invalid attachment
     */
    protected String readString(int length, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
        if (length == 0)
            return "";
        byte[] stringBytes = new byte[length];
        buffer.get(stringBytes);
        return new String(stringBytes, UTF8);
    }

    /**
     * Child Block attachment
     */
    public static class ChildBlockAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new ChildBlockAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new ChildBlockAttachment(txType, buffer);
        }

        private Chain chain;
        private List<byte[]> fullHashes;
        private byte[] hash;

        ChildBlockAttachment() {
        }

        ChildBlockAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            int chainId = json.getInt("chain");
            chain = Nxt.getChain(chainId);
            if (chain == null)
                throw new IllegalArgumentException("Chain '" + chainId + "' is not defined");
            List<String> hashList = json.getStringList("childTransactionFullHashes");
            if (!hashList.isEmpty()) {
                fullHashes = new ArrayList<>(hashList.size());
                for (String hashString : hashList) {
                    fullHashes.add(Utils.parseHexString(hashString));
                }
            } else {
                hash = json.getHexString("hash");
            }
        }

        ChildBlockAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            int flags = buffer.get();
            int chainId = buffer.getInt();
            chain = Nxt.getChain(chainId);
            if (chain == null)
                throw new IllegalArgumentException("Chain '" + chainId + "' is not defined");
            if ((flags & 1) != 0) {
                int count = buffer.getShort();
                fullHashes = new ArrayList<>(count);
                for (int i=0; i<count; i++) {
                    byte[] txHash = new byte[32];
                    buffer.get(txHash);
                    fullHashes.add(txHash);
                }
            } else {
                hash = new byte[32];
                buffer.get(hash);
            }
        }

        /**
         * Get the child chain
         *
         * @return                  Child chain
         */
        public Chain getChain() {
            return chain;
        }

        /**
         * Get the transaction full hashes
         *
         * @return                  List of transaction hashes or null if pruned
         */
        public List<byte[]> getTransactionFullHashes() {
            return fullHashes;
        }

        /**
         * Get the attachment hash
         *
         * @return                  Attachment hash
         */
        public byte[] getHash() {
            if (hash == null) {
                hash = Crypto.singleDigest(fullHashes);
            }
            return hash;
        }

        /**
         * Return a string representation of this attachment
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            if (fullHashes != null) {
                sb.append("  Chain:  ").append(chain.getName()).append("\n");
                if (!getTransactionFullHashes().isEmpty()) {
                    getTransactionFullHashes().forEach(txHash ->
                        sb.append("  Tx Hash:  ").append(Utils.toHexString(txHash)).append("\n"));
                }
            }
            sb.append("  Hash:  ").append(Utils.toHexString(getHash())).append("\n");
            return sb;
        }
    }

    /**
     * Effective Balance Leasing attachment
     */
    public static class EffectiveBalanceLeasingAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new EffectiveBalanceLeasingAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new EffectiveBalanceLeasingAttachment(txType, buffer);
        }

        private int period;

        EffectiveBalanceLeasingAttachment() {
        }

        EffectiveBalanceLeasingAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            period = json.getInt("period");
        }

        EffectiveBalanceLeasingAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            period = buffer.getShort();
        }

        /**
         * Get the lease period
         *
         * @return                  Lease period in blocks
         */
        public int getPeriod() {
            return period;
        }

        /**
         * Return a string representation of this attachment
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Period:  ").append(getPeriod()).append("\n");
            return sb;
        }
    }

    /**
     * Tagged Data Upload attachment
     */
    public static class TaggedDataUploadAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new TaggedDataUploadAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new TaggedDataUploadAttachment(txType, buffer);
        }

        private boolean isText;
        private String name;
        private String description;
        private String tags;
        private String type;
        private String channel;
        private String filename;
        private byte[] data;
        private byte[] hash;

        TaggedDataUploadAttachment() {
        }

        TaggedDataUploadAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            String dataString = json.getString("data");
            if (dataString.length() > 0) {
                isText = json.getBoolean("isText");
                name = json.getString("name");
                description = json.getString("description");
                tags = json.getString("tags");
                type = json.getString("type");
                channel = json.getString("channel");
                filename = json.getString("filename");
                data = (isText ? dataString.getBytes(UTF8) : Utils.parseHexString(dataString));
            } else {
                hash = json.getHexString("hash");
            }
        }

        TaggedDataUploadAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            int flags = buffer.get();
            if ((flags & 1) != 0) {
                isText = ((flags & 2) != 0);
                name = readString(buffer.getShort(), buffer);
                description = readString(buffer.getShort(), buffer);
                tags = readString(buffer.getShort(), buffer);
                type = readString(buffer.getShort(), buffer);
                channel = readString(buffer.getShort(), buffer);
                filename = readString(buffer.getShort(), buffer);
                int length = buffer.getInt();
                data = new byte[length];
                if (length > 0)
                    buffer.get(data);
            } else {
                hash = new byte[32];
                buffer.get(hash);
            }
        }

        /**
         * Get the name
         *
         * @return              Name (null if the data has been pruned)
         */
        public String getName() {
            return name;
        }

        /**
         * Get the description
         *
         * @return              Description (null if the data has been pruned)
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get the tags
         *
         * @return              Tags (null if the data has been pruned)
         */
        public String getTags() {
            return tags;
        }

        /**
         * Get the data type
         *
         * @return              Type (null if the data has been pruned)
         */
        public String getType() {
            return type;
        }

        /**
         * Get the channel
         *
         * @return              Channel (null if the data has been pruned)
         */
        public String getChannel() {
            return channel;
        }

        /**
         * Get the filename
         *
         * @return              Filename (null if the data has been pruned)
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Get the data
         * <p>
         * Text data is returned as a text string, otherwise the data is returned as a hexadecimal string.
         *
         * @return              Data (null if the data has been pruned)
         */
        public String getData() {
            return (data != null ?
                    (isText ? new String(data, UTF8) : Utils.toHexString(data)) : null);
        }

        /**
         * Get the data bytes
         *
         * @return              Data (null if the data has been pruned)
         */
        public byte[] getDataBytes() {
            return data;
        }

        /**
         * Get the data hash
         *
         * @return              Data hash
         */
        public byte[] getHash() {
            if (hash == null) {
                byte[] flagByte = new byte[1];
                flagByte[0] = (isText ? (byte)1 : (byte)0);
                hash = Crypto.singleDigest(
                        name.getBytes(UTF8),
                        description.getBytes(UTF8),
                        tags.getBytes(UTF8),
                        type.getBytes(UTF8),
                        channel.getBytes(UTF8),
                        flagByte,
                        filename.getBytes(UTF8),
                        data);
            }
            return hash;
        }

        /**
         * Return a string representation of this attachment
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Name:  ").append(name).append("\n")
                    .append("  Description:  ").append(description).append("\n")
                    .append("  Tags:  ").append(tags).append("\n")
                    .append("  Type:  ").append(type).append("\n")
                    .append("  Channel:  ").append(channel).append("\n")
                    .append("  IsText:  ").append(isText).append("\n")
                    .append("  Filename:  ").append(filename).append("\n");
            return sb;
        }
    }

    /**
     * Payment attachment
     *
     * Payment information is part of the base transaction, so the
     * payment attachment is an empty attachment
     */
    public static class PaymentAttachment extends Attachment {
        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json) {
            return new PaymentAttachment(txType);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer) {
            return new PaymentAttachment(txType);
        }

        PaymentAttachment() {
        }

        PaymentAttachment(TransactionType txType) {
            super(txType);
        }
    }

    /**
     * Messaging attachment
     *
     * Messages are carried in appendices, so the messaging attachment
     * is an empty attachment
     */
    public static class MessagingAttachment extends Attachment {
        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json) {
            return new MessagingAttachment(txType);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer) {
            return new MessagingAttachment(txType);
        }

        MessagingAttachment() {
        }

        MessagingAttachment(TransactionType txType) {
            super(txType);
        }
    }
}
