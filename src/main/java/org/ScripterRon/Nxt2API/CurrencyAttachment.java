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
import java.util.StringJoiner;

/**
 * Monetary System attachments
 */
public abstract class CurrencyAttachment {

    /**
     * Currency Issuance attachment
     */
    public static class IssuanceAttachment extends Attachment {

        private String name;
        private String code;
        private String description;
        private int type;
        private long initialSupply;
        private long reserveSupply;
        private long maxSupply;
        private int issuanceHeight;
        private long minReserve;
        private int minDifficulty;
        private int maxDifficulty;
        private int ruleset;
        private int algorithm;
        private int decimals;

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

        IssuanceAttachment() {
        }

        IssuanceAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            name = json.getString("name");
            code = json.getString("code");
            description = json.getString("description");
            type = json.getInt("type");
            initialSupply = json.getLong("initialSupply");
            reserveSupply = json.getLong("reserveSupply");
            maxSupply = json.getLong("maxSupply");
            issuanceHeight = json.getInt("issuanceHeight");
            minReserve = json.getLong("minReservePerUnitNQT");
            minDifficulty = json.getInt("minDifficulty");
            maxDifficulty = json.getInt("maxDifficulty");
            ruleset = json.getInt("ruleset");
            algorithm = json.getInt("algorithm");
            decimals = json.getInt("decimals");
        }

        IssuanceAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.get(), buffer);
            code = readString(buffer.get(), buffer);
            description = readString(buffer.getShort(), buffer);
            type = buffer.get();
            initialSupply = buffer.getLong();
            reserveSupply = buffer.getLong();
            maxSupply = buffer.getLong();
            issuanceHeight = buffer.getInt();
            minReserve = buffer.getLong();
            minDifficulty = (int)buffer.get() & 0xff;
            maxDifficulty = (int)buffer.get() & 0xff;
            ruleset = buffer.get();
            algorithm = buffer.get();
            decimals = buffer.get();
        }

        /**
         * Return the currency name
         *
         * @return                      Currency name
         */
        public String getCurrencyName() {
            return name;
        }

        /**
         * Return the currency code
         *
         * @return                      Currency code
         */
        public String getCurrencyCode() {
            return code;
        }

        /**
         * Return the currency description
         *
         * @return                      Currency description
         */
        public String getCurrencyDescription() {
            return description;
        }

        /**
         * Return the currency type codes
         *
         * @return                      Currency type
         */
        public int getCurrencyType() {
            return type;
        }

        /**
         * Return the initial supply
         * <p>
         * The initial supply has an implied decimal point determined by the currency 'decimals' property
         *
         * @return                      Initial supply
         */
        public long getInitialSupply() {
            return initialSupply;
        }

        /**
         * Return the reserve supply
         * <p>
         * The reserve supply has an implied decimal point determined by the currency 'decimals' property
         *
         * @return                      Reserve supply
         */
        public long getReserveSupply() {
            return reserveSupply;
        }

        /**
         * Return the maximum supply
         * <p>
         * The maximum supply has an implied decimal point determined by the currency 'decimals' property
         *
         * @return                      Maximum supply
         */
        public long getMaxSupply() {
            return maxSupply;
        }

        /**
         * Return the issuance height
         *
         * @return                      Issuance height
         */
        public int getIssuanceHeight() {
            return issuanceHeight;
        }

        /**
         * Return the minimum reserve per unit
         *
         * @return                     Minimum reserve
         */
        public long getMinReserve() {
            return minReserve;
        }

        /**
         * Return the minimum difficulty
         *
         * @return                      Minimum difficulty
         */
        public int getMinDifficulty() {
            return minDifficulty;
        }

        /**
         * Return the maximum difficulty
         *
         * @return                      Maximum difficulty
         */
        public int getMaxDifficulty() {
            return maxDifficulty;
        }

        /**
         * Return the rule set
         *
         * @return                      Rule set
         */
        public int getRuleset() {
            return ruleset;
        }

        /**
         * Return the minting algorithm
         *
         * @return                      Minting algorithm
         */
        public int getMintingAlgorithm() {
            return algorithm;
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
                    .append("  Code:  ").append(code).append("\n")
                    .append("  Description:  ").append(description).append("\n");
            StringJoiner sj = new StringJoiner(",");
            Nxt.getCurrencyTypes().entrySet().forEach(entry -> {
                int flag = entry.getKey();
                if ((type & flag) != 0)
                    sj.add(entry.getValue());
            });
            sb.append("  Type:  ").append(sj.toString()).append("\n")
                    .append("  Initial Supply:  ").append(String.format("%,d", initialSupply)).append("\n")
                    .append("  Reserve Supply:  ").append(String.format("%,d", reserveSupply)).append("\n")
                    .append("  Maximum Supply:  ").append(String.format("%,d", maxSupply)).append("\n")
                    .append("  Issuance Height:  ").append(issuanceHeight).append("\n")
                    .append("  Minimum Reserve:  ").append(minReserve).append("\n")
                    .append("  Minimum Difficulty:  ").append(minDifficulty).append("\n")
                    .append("  Maximum Difficulty:  ").append(maxDifficulty).append("\n")
                    .append("  Rule Set:  ").append(ruleset).append("\n")
                    .append("  Minting Algorithm:  ").append(Nxt.getMintingHashAlgorithm(algorithm)).append("\n")
                    .append("  Decimals:  ").append(decimals).append("\n");
            return sb;
        }
    }

    /**
     * Currency Minting attachment
     */
    public static class MintingAttachment extends Attachment {

        private long nonce;
        private long currencyId;
        private long units;
        private long counter;

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new MintingAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new MintingAttachment(txType, buffer);
        }

        MintingAttachment() {
        }

        MintingAttachment(TransactionType txType, Response response)
                    throws IdentifierException, NumberFormatException {
            super(txType, response);
            nonce = response.getLong("nonce");
            currencyId = response.getId("currency");
            units = response.getLong("units");
            counter = response.getLong("counter");
        }

        MintingAttachment(TransactionType txType, ByteBuffer buffer)
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
         * The minting units has an implied decimal point determined by the currency 'decimals' property
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

        /**
         * Return a string representation of the attachment
         *
         * @param   sb                  String builder
         * @return                      The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Currency:  ").append(Utils.idToString(currencyId)).append("\n")
                    .append("  Units:  ").append(units).append("\n")
                    .append("  Nonce:  ").append(nonce).append("\n")
                    .append("  Counter:  ").append(counter).append("\n");
            return sb;
        }
    }
}
