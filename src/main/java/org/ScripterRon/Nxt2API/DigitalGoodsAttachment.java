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
import java.util.Date;

/**
 * Digital Goods Store attachment
 */
public abstract class DigitalGoodsAttachment {

    /**
     * Listing attachment
     */
    public static class ListingAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IllegalArgumentException, NumberFormatException {
            return new DigitalGoodsAttachment.ListingAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new DigitalGoodsAttachment.ListingAttachment(txType, buffer);
        }

        private String name;
        private String description;
        private String tags;
        private int quantity;
        private long price;

        ListingAttachment() {
        }

        ListingAttachment(TransactionType txType, Response json)
                throws IllegalArgumentException, NumberFormatException {
            super(txType, json);
            name = json.getString("name");
            description = json.getString("description");
            tags = json.getString("tags");
            quantity = json.getInt("quantity");
            price = json.getLong("priceNQT");
        }

        ListingAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.getShort(), buffer);
            description = readString(buffer.getShort(), buffer);
            tags = readString(buffer.getShort(), buffer);
            quantity = buffer.getInt();
            price = buffer.getLong();
        }

        /**
         * Get the DGS name
         *
         * @return                  Name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the DGS description
         *
         * @return                  Description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get the DGS tags
         *
         * @return                  Tags
         */
        public String getTags() {
            return tags;
        }

        /**
         * Get the DGS quantity
         *
         * @return                  Quantity
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Get the DGS price
         * <p>
         * The price has an implied decimal point determined by the chain 'decimals' property
         *
         * @return                  Price
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
            sb.append("  Name:  ").append(name).append("\n")
                    .append("  Description:  ").append(description).append("\n")
                    .append("  Tags:  ").append(tags).append("\n")
                    .append("  Quantity:  ").append(String.format("%,d", quantity)).append("\n")
                    .append("  Price:  ").append(String.format("%,d", price)).append("\n");
            return sb;
        }
    }

    /**
     * Delisting attachment
     */
    public static class DelistingAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new DigitalGoodsAttachment.DelistingAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new DigitalGoodsAttachment.DelistingAttachment(txType, buffer);
        }

        private long goodsId;

        DelistingAttachment() {
        }

        DelistingAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            super(txType, json);
            goodsId = json.getId("goods");
        }

        DelistingAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            goodsId = buffer.getLong();
        }

        /**
         * Get the goods identifier
         *
         * @return                      Digital Goods Store identifier
         */
        public long getGoodsId() {
            return goodsId;
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
            sb.append("  Goods:  ").append(Utils.idToString(goodsId)).append("\n");
            return sb;
        }
    }

    /**
     * Price Change attachment
     */
    public static class PriceChangeAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new DigitalGoodsAttachment.PriceChangeAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new DigitalGoodsAttachment.PriceChangeAttachment(txType, buffer);
        }

        private long goodsId;
        private long price;

        PriceChangeAttachment() {
        }

        PriceChangeAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            super(txType, json);
            goodsId = json.getId("goods");
            price = json.getLong("priceNQT");
        }

        PriceChangeAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            goodsId = buffer.getLong();
            price = buffer.getLong();
        }

        /**
         * Get the goods identifier
         *
         * @return                      Digital Goods Store identifier
         */
        public long getGoodsId() {
            return goodsId;
        }

        /**
         * Get the new price
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
            sb.append("  Goods:  ").append(Utils.idToString(goodsId)).append("\n")
                    .append("  Price:  ").append(String.format("%,d", price)).append("\n");
            return sb;
        }
    }

    /**
     * Quantity Change attachment
     */
    public static class QuantityChangeAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new DigitalGoodsAttachment.QuantityChangeAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new DigitalGoodsAttachment.QuantityChangeAttachment(txType, buffer);
        }

        private long goodsId;
        private int deltaQuantity;

        QuantityChangeAttachment() {
        }

        QuantityChangeAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            super(txType, json);
            goodsId = json.getId("goods");
            deltaQuantity = json.getInt("deltaQuantity");
        }

        QuantityChangeAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            goodsId = buffer.getLong();
            deltaQuantity = buffer.getInt();
        }

        /**
         * Get the goods identifier
         *
         * @return                      Digital Goods Store identifier
         */
        public long getGoodsId() {
            return goodsId;
        }

        /**
         * Get the quantity change
         *
         * @return                      Quantity change
         */
        public int getDeltaQuantity() {
            return deltaQuantity;
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
            sb.append("  Goods:  ").append(Utils.idToString(goodsId)).append("\n")
                    .append("  Delta Quantity:  ").append(String.format("%,d", deltaQuantity)).append("\n");
            return sb;
        }
    }

    /**
     * Purchase attachment
     */
    public static class PurchaseAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new DigitalGoodsAttachment.PurchaseAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new DigitalGoodsAttachment.PurchaseAttachment(txType, buffer);
        }

        private long goodsId;
        private int quantity;
        private long price;
        private Date deliveryDeadline;

        PurchaseAttachment() {
        }

        PurchaseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            super(txType, json);
            goodsId = json.getId("goods");
            quantity = json.getInt("quantity");
            price = json.getLong("priceNQT");
            deliveryDeadline =
                    new Date(json.getLong("deliveryDeadlineTimestamp") * 1000L + Nxt.getEpoch());
        }

        PurchaseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            goodsId = buffer.getLong();
            quantity = buffer.getInt();
            price = buffer.getLong();
            deliveryDeadline = new Date((long)buffer.getInt() * 1000L + Nxt.getEpoch());
        }

        /**
         * Get the goods identifier
         *
         * @return                      Digital Goods Store identifier
         */
        public long getGoodsId() {
            return goodsId;
        }

        /**
         * Get the quantity
         *
         * @return                      Quantity
         */
        public int getQuantity() {
            return quantity;
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
         * Get the delivery deadline
         *
         * @return                      Delivery deadline
         */
        public Date getDeliveryDeadline() {
            return deliveryDeadline;
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
            sb.append("  Goods:  ").append(Utils.idToString(goodsId)).append("\n")
                    .append("  Quantity:  ").append(String.format("%,d", quantity)).append("\n")
                    .append("  Price:  ").append(String.format("%,d", price)).append("\n")
                    .append("  Delivery Deadline:  ").append(deliveryDeadline).append("\n");
            return sb;
        }
    }

    /**
     * Delivery attachment
     */
    public static class DeliveryAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new DigitalGoodsAttachment.DeliveryAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new DigitalGoodsAttachment.DeliveryAttachment(txType, buffer);
        }

        private long purchaseId;
        private boolean goodsIsText;
        private byte[] encryptedData;
        private byte[] nonce;
        private long discount;

        DeliveryAttachment() {
        }

        DeliveryAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            super(txType, json);
            purchaseId = json.getId("purchase");
            goodsIsText = json.getBoolean("goodsIsText");
            encryptedData = json.getHexString("goodsData");
            nonce = json.getHexString("goodsNonce");
            discount = json.getLong("discountNQT");
        }

        DeliveryAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            purchaseId = buffer.getLong();
            int length = buffer.getInt();
            if ((length & 0x80000000) != 0) {
                length &= 0x7fffffff;
                goodsIsText = true;
            }
            encryptedData = new byte[length];
            buffer.get(encryptedData);
            nonce = new byte[32];
            buffer.get(nonce);
            discount = buffer.getLong();
        }

        /**
         * Get the purchase identifier
         *
         * @return                      Purchase identifier
         */
        public long getPurchaseId() {
            return purchaseId;
        }

        /**
         * Check if the encrypted data is text
         *
         * @return                      TRUE if the encrypted data is text
         */
        public boolean isGoodsText() {
            return goodsIsText;
        }

        /**
         * Get the encrypted data
         *
         * @return                      Encrypted data
         */
        public byte[] getEncryptedData() {
            return encryptedData;
        }

        /**
         * Get the encryption nonce
         *
         * @return                      Encryption nonce
         */
        public byte[] getNonce() {
            return nonce;
        }

        /**
         * Get the discount
         * <p>
         * The discount has an implied decimal point determined by the chain 'decimals' property
         *
         * @return                      Discount
         */
        public long getDiscount() {
            return discount;
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
            sb.append("  Purchase:  ").append(Utils.idToString(purchaseId)).append("\n")
                    .append("  Discount:  ").append(String.format("%,d", discount)).append("\n")
                    .append("  Encrypted Data:  ").append(Utils.toHexString(encryptedData)).append("\n")
                    .append("  Nonce:  ").append(Utils.toHexString(nonce)).append("\n");
            return sb;
        }
    }

    /**
     * Feedback attachment
     */
    public static class FeedbackAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new DigitalGoodsAttachment.FeedbackAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new DigitalGoodsAttachment.FeedbackAttachment(txType, buffer);
        }

        private long purchaseId;

        FeedbackAttachment() {
        }

        FeedbackAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            super(txType, json);
            purchaseId = json.getId("purchase");
        }

        FeedbackAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            purchaseId = buffer.getLong();
        }

        /**
         * Get the purchase identifier
         *
         * @return                      Purchase identifier
         */
        public long getPurchaseId() {
            return purchaseId;
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
            sb.append("  Purchase:  ").append(Utils.idToString(purchaseId)).append("\n");
            return sb;
        }
    }

    /**
     * Refund attachment
     */
    public static class RefundAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            return new DigitalGoodsAttachment.RefundAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new DigitalGoodsAttachment.RefundAttachment(txType, buffer);
        }

        private long purchaseId;
        private long refund;

        RefundAttachment() {
        }

        RefundAttachment(TransactionType txType, Response json)
                throws IdentifierException, NumberFormatException {
            super(txType, json);
            purchaseId = json.getId("purchase");
            refund = json.getLong("refundNQT");
        }

        RefundAttachment(TransactionType txType, ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            purchaseId = buffer.getLong();
            refund = buffer.getLong();
        }

        /**
         * Get the purchase identifier
         *
         * @return                      Purchase identifier
         */
        public long getPurchaseId() {
            return purchaseId;
        }

        /**
         * Get the refund amount
         * <p>
         * The refund has an implied decimal point determined by the chain 'decimals' property
         */
        public long getRefund() {
            return refund;
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
            sb.append("  Purchase:  ").append(Utils.idToString(purchaseId)).append("\n")
                    .append("  Refund:  ").append(String.format("%,d", refund)).append("\n");
            return sb;
        }
    }
}
