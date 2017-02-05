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
    public static class IssuanceAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new IssuanceAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new IssuanceAttachment(txType, buffer);
        }

        private String name;
        private String description;
        private long quantity;
        private int decimals;

        IssuanceAttachment() {
        }

        IssuanceAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            name = json.getString("name");
            description = json.getString("description");
            quantity = json.getLong("quantityQNT");
            decimals = json.getInt("decimals");
        }

        IssuanceAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.get(), buffer);
            description = readString(buffer.getShort(), buffer);
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
         * The asset quantity has an implied decimal point determined by the 'decimals' property
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
                    .append("  Quantity:  ").append(Utils.nqtToString(quantity, decimals)).append("\n")
                    .append("  Decimals:  ").append(decimals).append("\n");
            return sb;
        }
    }

    /**
     * Asset Transfer attachment
     */
    public static class TransferAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new TransferAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new TransferAttachment(txType, buffer);
        }

        private long assetId;
        private long quantity;

        TransferAttachment() {
        }

        TransferAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            assetId = json.getId("asset");
            quantity = json.getLong("quantityQNT");
        }

        TransferAttachment(TransactionType txType, ByteBuffer buffer)
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
        public long getAssetId() {
            return assetId;
        }

        /**
         * Get the asset quantity
         * <p>
         * The asset quantity has an implied decimal point determined by the asset 'decimals' property
         *
         * @return                  Asset quantity
         */
        public long getQuantity() {
            return quantity;
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
            sb.append("  Asset:  ").append(Utils.idToString(assetId)).append("\n")
                    .append("  Quantity:  ").append(String.format("%,d", quantity)).append("\n");
            return sb;
        }
    }

    /**
     * Ask Order Placement attachment
     */
    public static class AskOrderPlacementAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new AskOrderPlacementAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new AskOrderPlacementAttachment(txType, buffer);
        }

        private long assetId;
        private long quantity;
        private long price;

        AskOrderPlacementAttachment() {
        }

        AskOrderPlacementAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            assetId = json.getId("asset");
            quantity = json.getLong("quantityQNT");
            price = json.getLong("priceNQTPerShare");
        }

        AskOrderPlacementAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            assetId = buffer.getLong();
            quantity = buffer.getLong();
            price = buffer.getLong();
        }

        /**
         * Get the asset identifier
         *
         * @return                  Asset identifier
         */
        public long getAssetId() {
            return assetId;
        }

        /**
         * Get the asset quantity
         * <p>
         * The asset quantity has an implied decimal point determined by the asset 'decimals' property
         *
         * @return                  Asset quantity
         */
        public long getQuantity() {
            return quantity;
        }

        /**
         * Get the ask price
         * <p>
         * The ask price has an implied decimal point determined by the asset 'decimals' property
         */
        public long getPrice() {
            return price;
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
            sb.append("  Asset:  ").append(Utils.idToString(assetId)).append("\n")
                    .append("  Quantity:  ").append(String.format("%,d", quantity)).append("\n")
                    .append("  Price:  ").append(String.format("%,d", price)).append("\n");
            return sb;
        }
    }

    /**
     * Bid Order Placement attachment
     */
    public static class BidOrderPlacementAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new BidOrderPlacementAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new BidOrderPlacementAttachment(txType, buffer);
        }

        private long assetId;
        private long quantity;
        private long price;

        BidOrderPlacementAttachment() {
        }

        BidOrderPlacementAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            assetId = json.getId("asset");
            quantity = json.getLong("quantityQNT");
            price = json.getLong("priceNQTPerShare");
        }

        BidOrderPlacementAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            assetId = buffer.getLong();
            quantity = buffer.getLong();
            price = buffer.getLong();
        }

        /**
         * Get the asset identifier
         *
         * @return                  Asset identifier
         */
        public long getAssetId() {
            return assetId;
        }

        /**
         * Get the asset quantity
         * <p>
         * The asset quantity has an implied decimal point determined by the asset 'decimals' property
         *
         * @return                  Asset quantity
         */
        public long getQuantity() {
            return quantity;
        }

        /**
         * Get the ask price
         * <p>
         * The ask price has an implied decimal point determined by the asset 'decimals' property
         */
        public long getPrice() {
            return price;
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
            sb.append("  Asset:  ").append(Utils.idToString(assetId)).append("\n")
                    .append("  Quantity:  ").append(String.format("%,d", quantity)).append("\n")
                    .append("  Price:  ").append(String.format("%,d", price)).append("\n");
            return sb;
        }
    }

    /**
     * Ask Order Cancellation attachment
     */
    public static class AskOrderCancellationAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new AskOrderCancellationAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new AskOrderCancellationAttachment(txType, buffer);
        }

        private long orderId;
        private byte[] orderHash;

        AskOrderCancellationAttachment() {
        }

        AskOrderCancellationAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            orderHash = json.getHexString("orderHash");
            orderId = Utils.fullHashToId(orderHash);
        }

        AskOrderCancellationAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            orderHash = new byte[32];
            buffer.get(orderHash);
            orderId = Utils.fullHashToId(orderHash);
        }

        /**
         * Get the order identifier
         *
         * @return                  Order identifier
         */
        public long getOrderId() {
            return orderId;
        }

        /**
         * Get the order hash
         *
         * @return                  Order hash
         */
        public byte[] getOrderHash() {
            return orderHash;
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
            sb.append("  Order:  ").append(Utils.idToString(orderId)).append("\n");
            return sb;
        }
    }

    /**
     * Bid Order Cancellation attachment
     */
    public static class BidOrderCancellationAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new BidOrderCancellationAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new BidOrderCancellationAttachment(txType, buffer);
        }

        private long orderId;
        private byte[] orderHash;

        BidOrderCancellationAttachment() {
        }

        BidOrderCancellationAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            orderHash = json.getHexString("orderHash");
            orderId = Utils.fullHashToId(orderHash);
        }

        BidOrderCancellationAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            orderHash = new byte[32];
            buffer.get(orderHash);
            orderId = Utils.fullHashToId(orderHash);
        }

        /**
         * Get the order identifier
         *
         * @return                  Order identifier
         */
        public long getOrderId() {
            return orderId;
        }

        /**
         * Get the order hash
         *
         * @return                  Order hash
         */
        public byte[] getOrderHash() {
            return orderHash;
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
            sb.append("  Order:  ").append(Utils.idToString(orderId)).append("\n");
            return sb;
        }
    }

    /**
     * Dividend Payment attachment
     */
    public static class DividendPaymentAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new DividendPaymentAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new DividendPaymentAttachment(txType, buffer);
        }

        private long assetId;
        private int height;
        private long dividendAmount;
        private long holdingId;
        private int holdingType;

        DividendPaymentAttachment() {
        }

        DividendPaymentAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            assetId = json.getId("asset");
            height = json.getInt("height");
            dividendAmount = json.getLong("amountNQTPerShare");
            holdingId = json.getId("holding");
            holdingType = json.getInt("holdingType");
        }

        DividendPaymentAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            holdingId = buffer.getLong();
            holdingType = buffer.get();
            assetId = buffer.getLong();
            height = buffer.getInt();
            dividendAmount = buffer.getLong();
        }

        /**
         * Get the asset identifier
         *
         * @return                  Asset identifier
         */
        public long getAssetId() {
            return assetId;
        }

        /**
         * Get the payment height
         *
         * @return                  Payment height
         */
        public int getHeight() {
            return height;
        }

        /**
         * Get the dividend amount expressed as NQT per share
         *
         * @return                  Dividend amount
         */
        public long getDividendAmount() {
            return dividendAmount;
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
         * Get the holding type
         *
         * @return                  Holding type
         */
        public int getHoldingType() {
            return holdingType;
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
            sb.append("  Asset:  ").append(Utils.idToString(assetId)).append("\n")
                    .append("  Height:  ").append(height).append("\n")
                    .append("  Dividend:  ").append(String.format("%,d", dividendAmount)).append("\n")
                    .append("  Holding:  ").append(Utils.idToString(holdingId)).append("\n")
                    .append("  Holding Type:  ").append(Nxt.getHoldingType(holdingType)).append("\n");
            return sb;
        }
    }

    /**
     * Asset Delete attachment
     */
    public static class DeleteAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new DeleteAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new DeleteAttachment(txType, buffer);
        }

        private long assetId;
        private long quantity;

        DeleteAttachment() {
        }

        DeleteAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            assetId = json.getId("asset");
            quantity = json.getLong("quantityQNT");
        }

        DeleteAttachment(TransactionType txType, ByteBuffer buffer)
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
        public long getAssetId() {
            return assetId;
        }

        /**
         * Get the quantity
         * <p>
         * The quantity has an implied decimal point determined by the asset 'decimals' property
         *
         * @return                  Quantity
         */
        public long getQuantity() {
            return quantity;
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
            sb.append("  Asset:  ").append(Utils.idToString(assetId)).append("\n")
                    .append("  Quantity:  ").append(String.format("%,d", quantity)).append("\n");
            return sb;
        }
    }
}
