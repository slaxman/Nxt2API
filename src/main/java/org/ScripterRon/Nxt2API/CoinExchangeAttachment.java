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
 * Coin Exchange attachments
 */
public abstract class CoinExchangeAttachment {

    /**
     * Order Issue attachment
     */
    public static class OrderIssueAttachment extends Attachment {

        private Chain chain;
        private Chain exchangeChain;
        private long amount;
        private long price;

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new OrderIssueAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new OrderIssueAttachment(txType, buffer);
        }

        OrderIssueAttachment() {
        }

        OrderIssueAttachment(TransactionType txType, Response response)
                throws IllegalArgumentException, NumberFormatException {
            super(txType, response);
            int chainId = response.getInt("chain");
            chain = Nxt.getChain(chainId);
            if (chain == null)
                throw new IllegalArgumentException("Chain " + chainId + " is not defined");
            chainId = response.getInt("exchangeChain");
            exchangeChain = Nxt.getChain(chainId);
            if (exchangeChain == null)
                throw new IllegalArgumentException("Exchange chain " + chainId + " is not defined");
            amount = response.getLong("quantityQNT");
            price = response.getLong("priceNQT");
        }

        OrderIssueAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            int chainId = buffer.getInt();
            chain = Nxt.getChain(chainId);
            if (chain == null)
                throw new IllegalArgumentException("Chain " + chainId + " is not defined");
            chainId = buffer.getInt();
            exchangeChain = Nxt.getChain(chainId);
            if (exchangeChain == null)
                throw new IllegalArgumentException("Exchange chain " + chainId + " is not defined");
            amount = buffer.getLong();
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
         * Get the amount
         * <p>
         * The amount has an implied decimal point determined by the chain 'decimals' property
         *
         * @return                      Quantity
         */
        public long getAmount() {
            return amount;
        }

        /**
         * Get the price
         * <p>
         * The price has an implied decimal point determined by the chain 'decimals' property
         *
         * @return                      Price
         */
        public long getPrice() {
            return price;
        }

        /**
         * Return a string representation of the attachment
         *
         * @param   sb                  String builder
         * @return                      The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Chain:  ").append(chain.getName()).append("\n")
                    .append("  Exchange Chain:  ").append(exchangeChain.getName()).append("\n")
                    .append("  Amount:  ").append(Utils.nqtToString(amount, exchangeChain.getDecimals())).append("\n")
                    .append("  Price:  ").append(Utils.nqtToString(price, chain.getDecimals())).append("\n");
            return sb;
        }
    }

    /**
     * Order Cancel attachment
     */
    public static class OrderCancelAttachment extends Attachment {

        private long orderId;
        private byte[] orderHash;

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new OrderCancelAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new OrderCancelAttachment(txType, buffer);
        }

        OrderCancelAttachment() {
        }

        OrderCancelAttachment(TransactionType txType, Response response)
                throws IdentifierException, NumberFormatException {
            super(txType, response);
            orderHash = response.getHexString("orderHash");
            orderId = Utils.fullHashToId(orderHash);
        }

        OrderCancelAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            orderHash = new byte[32];
            buffer.get(orderHash);
            orderId = Utils.fullHashToId(orderHash);
        }

        /**
         * Get the order identifier
         *
         * @return                      Order identifier
         */
        public long getOrderId() {
            return orderId;
        }

        /**
         * Get the order hash
         *
         * @return                      Order hash
         */
        public byte[] getOrderHash() {
            return orderHash;
        }

        /**
         * Return a string representation of the attachment
         *
         * @param   sb                  String builder
         * @return                      The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Order:  ").append(Utils.idToString(orderId)).append("\n");
            return sb;
        }
    }
}
