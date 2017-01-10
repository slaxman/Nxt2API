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
 * Asset Exchange attachments
 */
public abstract class AssetAttachment {

    /**
     * Asset Issuance attachment
     */
    public static class AssetIssuanceAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new AssetIssuanceAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new AssetIssuanceAttachment(txType, buffer);
        }

        private String name;
        private String description;
        private long quantity;
        private int decimals;

        AssetIssuanceAttachment() {
        }

        AssetIssuanceAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            name = json.getString("name");
            description = json.getString("description");
            quantity = json.getLong("quantityQNT");
            decimals = json.getInt("decimals");
        }

        AssetIssuanceAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            int length = buffer.get();
            byte[] bytes = new byte[length];
            buffer.get(bytes);
            name = new String(bytes, UTF8);
            length = buffer.getShort();
            bytes = new byte[length];
            buffer.get(bytes);
            description = new String(bytes, UTF8);
            quantity = buffer.getLong();
            decimals = buffer.get();
        }

        /**
         * Get the asset name
         *
         * @return                  Asset name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the asset description
         *
         * @return                  Asset description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get the asset quantity
         * <p>
         * The asset quantity has an implicit decimal point determined by the 'decimals' property
         *
         * @return                  Asset quantity
         */
        public long getQuantity() {
            return quantity;
        }

        /**
         * Get the number of decimal places
         *
         * @return                  Decimal places
         */
        public int getDecimals() {
            return decimals;
        }
    }

    /**
     * Asset Transfer attachment
     */
    public static class AssetTransferAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new AssetTransferAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new AssetTransferAttachment(txType, buffer);
        }

        private long assetId;
        private long quantity;

        AssetTransferAttachment() {
        }

        AssetTransferAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            assetId = json.getId("asset");
            quantity = json.getLong("quantityQNT");
        }

        AssetTransferAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            assetId = buffer.getLong();
            quantity = buffer.getLong();
        }

        /**
         * Get the asset identifier
         *
         * @return                  Asset identifier
         */
        public long getId() {
            return assetId;
        }

        /**
         * Get the asset quantity
         * <p>
         * The asset quantity has an implicit decimal point determined by the 'decimals' property
         *
         * @return                  Asset quantity
         */
        public long getQuantity() {
            return quantity;
        }
    }

}
