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
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Account attachments
 */
public abstract class AccountAttachment {

    /**
     * Set Phasing Only attachment
     */
    public static class SetPhasingOnlyAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new SetPhasingOnlyAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new SetPhasingOnlyAttachment(txType, buffer);
        }

        private PhasingParameters phasingParams;
        private SortedMap<Chain, Long> maxFees;
        private int minDuration;
        private int maxDuration;

        SetPhasingOnlyAttachment() {
        }

        SetPhasingOnlyAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            phasingParams = new PhasingParameters(json.getObject("phasingControlParams"));
            maxFees = new TreeMap<>();
            json.getObject("controlMaxFees").getObjectMap().entrySet().forEach(entry -> {
                int chainId = Integer.valueOf(entry.getKey());
                Chain chain = Nxt.getChain(chainId);
                if (chain == null)
                    throw new IllegalArgumentException("Chain '" + chainId + "' is not defined");
                maxFees.put(chain, (Long)entry.getValue());
            });
            minDuration = json.getInt("controlMinDuration");
            maxDuration = json.getInt("controlMaxDuration");
        }

        SetPhasingOnlyAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            phasingParams = new PhasingParameters(buffer);
            int count = buffer.get();
            maxFees = new TreeMap<>();
            for (int i=0; i<count; i++) {
                int chainId = buffer.getInt();
                Chain chain = Nxt.getChain(chainId);
                if (chain == null)
                    throw new IllegalArgumentException("Chain '" + chainId + "' is not defined");
                maxFees.put(chain, buffer.getLong());
            }
            minDuration = buffer.getShort();
            maxDuration = buffer.getShort();
        }

        /**
         * Get the phasing parameters
         *
         * @return                  Phasing parameters
         */
        public PhasingParameters getPhasingParams() {
            return phasingParams;
        }

        /**
         * Get the maximum fee for each chain
         *
         * @return                  Maximum fees
         */
        public SortedMap<Chain, Long> getMaxFees() {
            return maxFees;
        }

        /**
         * Get the minimum duration
         *
         * @return                  Minimum duration
         */
        public int getMinDuration() {
            return minDuration;
        }

        /**
         * Get the maximum duration
         *
         * @return                  Maximum duration
         */
        public int getMaxDuration() {
            return maxDuration;
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
            phasingParams.toString(sb);
            if (!maxFees.isEmpty()) {
                maxFees.entrySet().forEach(entry ->
                    sb.append("  ").append(entry.getKey().getName()).append(":  ")
                        .append(entry.getValue()).append("\n"));
            }
            sb.append("  Minimum Duration:  ").append(minDuration).append("\n")
                    .append("  Maximum Duration:  ").append(maxDuration).append("\n");
            return sb;
        }
    }

    /**
     * Account Info attachment
     */
    public static class AccountInfoAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new AccountInfoAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new AccountInfoAttachment(txType, buffer);
        }

        private String name;
        private String description;

        AccountInfoAttachment() {
        }

        AccountInfoAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            name = json.getString("name");
            description = json.getString("description");
        }

        AccountInfoAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.get(), buffer);
            description = readString(buffer.getShort(), buffer);
        }

        /**
         * Get the account name
         *
         * @return                  Account name
         */
        public String getAccountName() {
            return name;
        }

        /**
         * Get the account description
         *
         * @return                  Account description
         */
        public String getAccountDescription() {
            return description;
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
            sb.append("  Account Name:  ").append(name).append("\n")
                    .append("  Account Description:  ").append(description).append("\n");
            return sb;
        }
    }

    /**
     * Property Set attachment
     */
    public static class PropertySetAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new PropertySetAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new PropertySetAttachment(txType, buffer);
        }

        private String property;
        private String value;

        PropertySetAttachment() {
        }

        PropertySetAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            property = json.getString("property");
            value = json.getString("value");
        }

        PropertySetAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            property = readString(buffer.get(), buffer);
            value = readString(buffer.get(), buffer);
        }

        /**
         * Get the property name
         *
         * @return                  Property name
         */
        public String getPropertyName() {
            return property;
        }

        /**
         * Get the property value
         *
         * @return                  Property value
         */
        public String getPropertyValue() {
            return value;
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
            sb.append("  Property Name:  ").append(property).append("\n")
                    .append("  Property Value:  ").append(value).append("\n");
            return sb;
        }
    }

    /**
     * Property Delete attachment
     */
    public static class PropertyDeleteAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new PropertyDeleteAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new PropertyDeleteAttachment(txType, buffer);
        }

        private long propertyId;

        PropertyDeleteAttachment() {
        }

        PropertyDeleteAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            propertyId = json.getId("property");
        }

        PropertyDeleteAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            propertyId = buffer.getLong();
        }

        /**
         * Get the property identifier
         *
         * @return                  Property identifier
         */
        public long getPropertyId() {
            return propertyId;
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
            sb.append("  Property:  ").append(Utils.idToString(propertyId)).append("\n");
            return sb;
        }
    }
}
