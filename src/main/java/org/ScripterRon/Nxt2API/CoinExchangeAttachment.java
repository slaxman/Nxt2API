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
     * Coin Exchange order issue attachment
     */
    public static class ExchangeOrderIssueAttachment extends Attachment {

        private Chain chain;
        private Chain exchangeChain;
        private long quantity;
        private long price;

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IllegalArgumentException, NumberFormatException {
            return new ExchangeOrderIssueAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new ExchangeOrderIssueAttachment(txType, buffer);
        }

        ExchangeOrderIssueAttachment() {
        }

        ExchangeOrderIssueAttachment(TransactionType txType, Response response)
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
            quantity = response.getLong("quantityQNT");
            price = response.getLong("priceNQT");
        }

        ExchangeOrderIssueAttachment(TransactionType txType, ByteBuffer buffer)
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
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new ExchangeOrderCancelAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new ExchangeOrderCancelAttachment(txType, buffer);
        }

        ExchangeOrderCancelAttachment() {
        }

        ExchangeOrderCancelAttachment(TransactionType txType, Response response)
                throws IdentifierException, NumberFormatException {
            super(txType, response);
            orderId = response.getId("order");
        }

        ExchangeOrderCancelAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
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
