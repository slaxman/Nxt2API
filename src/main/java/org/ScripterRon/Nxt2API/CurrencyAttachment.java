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
 * Monetary System attachments
 */
public abstract class CurrencyAttachment {

    /**
     * CurrencyMinting attachment
     */
    public static class CurrencyMintingAttachment extends Attachment {

        private long nonce;
        private long currencyId;
        private long units;
        private long counter;

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new CurrencyMintingAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new CurrencyMintingAttachment(txType, buffer);
        }

        CurrencyMintingAttachment() {
        }

        CurrencyMintingAttachment(TransactionType txType, Response response)
                    throws IdentifierException, NumberFormatException {
            super(txType, response);
            nonce = response.getLong("nonce");
            currencyId = response.getId("currency");
            units = response.getLong("units");
            counter = response.getLong("counter");
        }

        CurrencyMintingAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            this.nonce = buffer.getLong();
            this.currencyId = buffer.getLong();
            this.units = buffer.getLong();
            this.counter = buffer.getLong();
        }

        /**
         * Return the currency identifier
         *
         * @return                      Currency identifier
         */
        public long getCurrencyId() {
            return currencyId;
        }

        /**
         * Return the nonce
         *
         * @return                      Nonce
         */
        public long getNonce() {
            return nonce;
        }

        /**
         * Return the minting units
         * <p>
         * The minting units has an implicit decimal point determined by the currency 'decimals' property
         *
         * @return                      Minting units
         */
        public long getUnits() {
            return units;
        }

        /**
         * Return the minting counter
         *
         * @return                      Minting counter
         */
        public long getCounter() {
            return counter;
        }
    }
}
