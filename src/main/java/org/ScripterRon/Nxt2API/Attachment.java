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
import java.util.HashMap;
import java.util.Map;

/**
 * Transaction attachment
 */
public abstract class Attachment {

    /** Attachment types */
    public enum AttachmentType {
        FXT_CHILDCHAIN_BLOCK(-1, 0, null),
        FXT_ORDINARY_PAYMENT(-2, 0, new PaymentAttachment()),
        FXT_BALANCE_LEASING(-3, 0, null),
        FXT_EXCHANGE_ORDER_ISSUE(-4, 0, new ExchangeOrderIssueAttachment()),
        FXT_EXCHANGE_ORDER_CANCEL(-4, 1, null),
        ORDINARY_PAYMENT(0, 0, new PaymentAttachment()),
        ARBITRARY_MESSAGE(1, 0, new MessagingAttachment()),
        ASSET_ISSUANCE(2, 0, null),
        ASSET_TRANSFER(2, 1, null),
        ASSET_ASK_ORDER_PLACEMENT(2, 2, null),
        ASSET_BID_ORDER_PLACEMENT(2, 3, null),
        ASSET_ASK_ORDER_CANCELLATION(2, 4, null),
        ASSET_BID_ORDER_CANCELLATION(2, 5, null),
        ASSET_DIVIDEND_PAYMENT(2, 6, null),
        ASSET_DELETE(2, 7, null),
        DIGITAL_GOODS_LISTING(3, 0, null),
        DIGITAL_GOODS_DELISTING(3, 1, null),
        DIGITAL_GOODS_PRICE_CHANGE(3, 2, null),
        DIGITAL_GOODS_QUANTITY_CHANGE(3, 3, null),
        DIGITAL_GOODS_PURCHASE(3, 4, null),
        DIGITAL_GOODS_DELIVERY(3, 5, null),
        DIGITAL_GOODS_FEEDBACK(3, 6, null),
        DIGITAL_GOODS_REFUND(3, 7, null),
        ACCOUNT_CONTROL_PHASING_ONLY(4, 0, null),
        CURRENCY_ISSUANCE(5, 0, null),
        CURRENCY_RESERVE_INCREASE(5, 1, null),
        CURRENCY_RESERVE_CLAIM(5, 2, null),
        CURRENCY_TRANSFER(5, 3, null),
        CURRENCY_EXCHANGE_OFFER(5, 4, null),
        CURRENCY_EXCHANGE_BUY(5, 5, null),
        CURRENCY_EXCHANGE_SELL(5, 6, null),
        CURRENCY_MINTING(5, 7, null),
        CURRENCY_DELETION(5, 8, null),
        TAGGED_DATA_UPLOAD(6, 0, null),
        SHUFFLING_CREATION(7, 0, null),
        SHUFFLING_REGISTRATION(7, 1, null),
        SHUFFLING_PROCESSING(7, 2, null),
        SHUFFLING_RECIPIENTS(7, 3, null),
        SHUFFLING_VERIFICATION(7, 4, null),
        SHUFFLING_CANCELLATION(7, 5, null),
        ALIAS_ASSIGNMENT(8, 0, null),
        ALIAS_SELL(8, 1, null),
        ALIAS_BUY(8, 2, null),
        ALIAS_DELETE(8, 3, null),
        POLL_CREATION(9, 0, null),
        VOTE_CASTING(9, 1, null),
        PHASING_VOTE_CASTING(9, 2, null),
        ACCOUNT_INFO(10, 0, null),
        ACCOUNT_PROPERTY_SET(10, 1, null),
        ACCOUNT_PROPERTY_DELETE(10, 2, null),
        EXCHANGE_ORDER_ISSUE(11, 0, new ExchangeOrderIssueAttachment()),
        EXCHANGE_ORDER_CANCEL(1, 1, null);

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

        public Attachment getAttachment() {
            return attachment;
        }
    }

    /**
     * Create an attachment for the supplied transaction
     *
     * @param   txType                      Transaction type
     * @param   txJSON                      Transaction JSON
     * @return                              Attachment or null if transaction not supported
     * @throws  IdentifierException         Invalid Nxt object identifier
     * @throws  IllegalArgumentException    Response is not valid
     * @throws  NumberFormatException       Invalid numeric value
     */
    static Attachment getAttachment(TransactionType txType, Response txJSON)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
        AttachmentType type = AttachmentType.get(txType);
        if (type == null) {
            return null;
        }
        Attachment attachment = type.getAttachment();
        if (attachment == null) {
            return null;
        }
        return attachment.parseAttachment(txJSON);
    }

    /**
     * Create an attachment for the supplied transaction
     *
     * @param   txType                      Transaction type
     * @param   txBytes                     Transaction bytes
     * @return                              Attachment or null if transaction not supported
     * @throws  BufferUnderflowException    End-of-data reached parsing attachment
     * @throws  IllegalArgumentException    Invalid attachment
     */
    static Attachment getAttachment(TransactionType txType, byte[] txBytes)
                throws BufferUnderflowException, IllegalArgumentException {
        AttachmentType type = AttachmentType.get(txType);
        if (type == null) {
            return null;
        }
        Attachment attachment = type.getAttachment();
        if (attachment == null) {
            return null;
        }
        return attachment.parseAttachment(txBytes);
    }

    /**
     * Parse the attachment JSON
     *
     * @param   txJSON                      Transaction JSON
     * @return                              Attachment or null if transaction not supported
     * @throws  IdentifierException         Invalid Nxt object identifier
     * @throws  IllegalArgumentException    Response is not valid
     * @throws  NumberFormatException       Invalid numeric value
     */
    abstract protected Attachment parseAttachment(Response txJSON)
                throws IdentifierException, IllegalArgumentException, NumberFormatException;
    /**
     * Parse the attachment bytes
     *
     * @param   txBytes                     Transaction bytes
     * @return                              Attachment or null if transaction not supported
     * @throws  BufferUnderflowException    End-of-data reached parsing attachment
     * @throws  IllegalArgumentException    Invalid attachment
     */
    abstract protected Attachment parseAttachment(byte[] txBytes)
                throws BufferUnderflowException, IllegalArgumentException;

    /**
     * Payment attachment
     *
     * Payment information is part of the base transaction, so the
     * payment attachment is an empty attachment
     */
    public static class PaymentAttachment extends Attachment {
        @Override
        protected Attachment parseAttachment(Response txJSON) {
            return new PaymentAttachment();
        }

        @Override
        protected Attachment parseAttachment(byte[] txBytes) {
            return new PaymentAttachment();
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
        protected Attachment parseAttachment(Response txJSON) {
            return new MessagingAttachment();
        }

        @Override
        protected Attachment parseAttachment(byte[] txBytes) {
            return new MessagingAttachment();
        }
    }

    /**
     * Coin Exchange order issue attachment
     */
    public static class ExchangeOrderIssueAttachment extends Attachment {

        private Chain chain;
        private Chain exchangeChain;
        private long quantity;
        private long price;

        @Override
        protected Attachment parseAttachment(Response txJSON)
                throws IllegalArgumentException, NumberFormatException {
            return new ExchangeOrderIssueAttachment(txJSON);
        }

        @Override
        protected Attachment parseAttachment(byte[] txBytes)
                throws IllegalArgumentException, NumberFormatException {
            return new ExchangeOrderIssueAttachment(txBytes);
        }

        private ExchangeOrderIssueAttachment() {
        }

        private ExchangeOrderIssueAttachment(Response response)
                throws IllegalArgumentException, NumberFormatException {
            int chainId = response.getInt("chain");
            chain = Nxt.getChain(chainId);
            if (chain == null)
                throw new IllegalArgumentException("Chain " + chainId + " is not defined");
            chainId = response.getInt("exchangeChain");
            exchangeChain = Nxt.getChain(chainId);
            if (exchangeChain == null)
                throw new IllegalArgumentException("Exchange chain " + chainId + " is not defined");
            quantity = response.getLong("quantityQNT");
            price = response.getLong("priceNQT");
        }

        private ExchangeOrderIssueAttachment(byte[] transactionBytes)
                throws BufferUnderflowException, IllegalArgumentException {
            ByteBuffer buffer = ByteBuffer.wrap(transactionBytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(Transaction.BASE_LENGTH);
            int version = buffer.get();
            if (version != 1)
                throw new IllegalArgumentException("Attachment version " + version + " is not supported");
            int chainId = buffer.getInt();
            chain = Nxt.getChain(chainId);
            if (chain == null)
                throw new IllegalArgumentException("Chain " + chainId + " is not defined");
            chainId = buffer.getInt();
            exchangeChain = Nxt.getChain(chainId);
            if (exchangeChain == null)
                throw new IllegalArgumentException("Exchange chain " + chainId + " is not defined");
            quantity = buffer.getLong();
            price = buffer.getLong();
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
         * Get the exchange chain
         *
         * @return                      Exchange chain
         */
        public Chain getExchangeChain() {
            return exchangeChain;
        }

        /**
         * Get the quantity
         *
         * @return                      Quantity
         */
        public long getQuantity() {
            return quantity;
        }

        /**
         * Get the price
         *
         * @return                      Price
         */
        public long getPrice() {
            return price;
        }
    }

    /**
     * Coin Exchange order cancel attachment
     */
    public static class ExchangeOrderCancelAttachment extends Attachment {

        private long orderId;

        @Override
        protected Attachment parseAttachment(Response txJSON)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new ExchangeOrderCancelAttachment(txJSON);
        }

        @Override
        protected Attachment parseAttachment(byte[] txBytes)
                throws BufferUnderflowException, IllegalArgumentException {
            return new ExchangeOrderCancelAttachment(txBytes);
        }

        private ExchangeOrderCancelAttachment() {
        }

        private ExchangeOrderCancelAttachment(Response response)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            orderId = response.getId("order");
        }

        private ExchangeOrderCancelAttachment(byte[] transactionBytes)
                throws BufferUnderflowException, IllegalArgumentException {
            ByteBuffer buffer = ByteBuffer.wrap(transactionBytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(Transaction.BASE_LENGTH);
            int version = buffer.get();
            if (version != 1)
                throw new IllegalArgumentException("Attachment version " + version + " is not supported");
            orderId = buffer.getLong();
        }

        /**
         * Get the order identifier
         *
         * @return                      Order identifier
         */
        public long getOrderId() {
            return orderId;
        }
    }
}
