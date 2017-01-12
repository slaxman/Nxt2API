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
 * Alias attachments
 */
public abstract class AliasAttachment {

    /**
     * Assignment attachment
     */
    public static class AssignmentAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new AssignmentAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new AssignmentAttachment(txType, buffer);
        }

        private String name;
        private String uri;

        AssignmentAttachment() {
        }

        AssignmentAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            name = json.getString("alias");
            uri = json.getString("uri");
        }

        AssignmentAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.get(), buffer);
            uri = readString(buffer.getShort(), buffer);
        }

        /**
         * Get the alias name
         *
         * @return                  Alias name
         */
        public String getAliasName() {
            return name;
        }

        /**
         * Get the alias URI
         *
         * @return                  Alias URI
         */
        public String getAliasURI() {
            return uri;
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
            sb.append("  Alias:  ").append(name).append("\n")
                    .append("  URI:  ").append(uri).append("\n");
            return sb;
        }
    }

    /**
     * Buy attachment
     */
    public static class BuyAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new BuyAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new BuyAttachment(txType, buffer);
        }

        private String name;

        BuyAttachment() {
        }

        BuyAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            name = json.getString("alias");
        }

        BuyAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.get(), buffer);
        }

        /**
         * Get the alias name
         *
         * @return                  Alias name
         */
        public String getAliasName() {
            return name;
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
            sb.append("  Alias:  ").append(name).append("\n");
            return sb;
        }
    }

    /**
     * Sell attachment
     */
    public static class SellAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new SellAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new SellAttachment(txType, buffer);
        }

        private String name;
        private long price;

        SellAttachment() {
        }

        SellAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            name = json.getString("alias");
            price = json.getLong("priceNQT");
        }

        SellAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.get(), buffer);
            price = buffer.getLong();
        }

        /**
         * Get the alias name
         *
         * @return                  Alias name
         */
        public String getAliasName() {
            return name;
        }

        /**
         * Get the price
         * <p>
         * The price has an implied decimal point determined by the chain 'decimals' property
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
            sb.append("  Alias:  ").append(name).append("\n")
                    .append("  Price:  ").append(String.format("%,d", price)).append("\n");
            return sb;
        }
    }

    /**
     * Delete attachment
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

        private String name;

        DeleteAttachment() {
        }

        DeleteAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            name = json.getString("alias");
        }

        DeleteAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.get(), buffer);
        }

        /**
         * Get the alias name
         *
         * @return                  Alias name
         */
        public String getAliasName() {
            return name;
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
            sb.append("  Alias:  ").append(name).append("\n");
            return sb;
        }
    }
}
