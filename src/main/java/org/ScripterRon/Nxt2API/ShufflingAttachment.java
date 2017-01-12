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
import java.util.ArrayList;
import java.util.List;

/**
 * Shuffling attachment
 */
public abstract class ShufflingAttachment extends Attachment {

    protected byte[] shufflingFullHash;
    protected byte[] shufflingStateHash;

    ShufflingAttachment() {
    }

    ShufflingAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
        super(txType, json);
        shufflingFullHash = json.getHexString("shufflingFullHash");
        shufflingStateHash = json.getHexString("shufflingStateHash");
    }

    ShufflingAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
        super(txType, buffer);
        shufflingFullHash = new byte[32];
        buffer.get(shufflingFullHash);
        shufflingStateHash = new byte[32];
        buffer.get(shufflingStateHash);
    }

    /**
     * Get the shuffling full hash
     *
     * @return                  Shuffling full hash
     */
    public byte[] getShufflingFullHash() {
        return shufflingFullHash;
    }

    /**
     * Get the shuffling state hash
     *
     * @return                  Shuffling state has
     */
    public byte[] getShufflingStateHash() {
        return shufflingStateHash;
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
        sb.append("  Shuffling Full Hash:  ").append(Utils.toHexString(shufflingFullHash)).append("\n")
                .append("  Shuffling State Hash:  ").append(Utils.toHexString(shufflingStateHash)).append("\n");
        return sb;
    }

    /**
     * Creation attachment
     */
    public static class CreationAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new CreationAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new CreationAttachment(txType, buffer);
        }

        private long holdingId;
        private int holdingType;
        private long amount;
        private int participantCount;
        private int registrationPeriod;

        CreationAttachment() {
        }

        CreationAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            holdingId = json.getId("holding");
            holdingType = json.getInt("holdingType");
            amount = json.getLong("amount");
            participantCount = json.getInt("participantCount");
            registrationPeriod = json.getInt("registrationPeriod");
        }

        CreationAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            holdingId = buffer.getLong();
            holdingType = buffer.get();
            amount = buffer.getLong();
            participantCount = buffer.get();
            registrationPeriod = buffer.getShort();
        }

        /**
         * Get the holding identifier
         *
         * @return              Holding identifier
         */
        public long getHoldingId() {
            return holdingId;
        }

        /**
         * Get the holding type
         *
         * @return              Holding type
         */
        public int getHoldingType() {
            return holdingType;
        }

        /**
         * Get the amount
         *
         * @return              Amount
         */
        public long getAmount() {
            return amount;
        }

        /**
         * Get the participant count
         *
         * @return              Participant count
         */
        public int getParticipantCount() {
            return participantCount;
        }

        /**
         * Get the registration period
         *
         * @return              Registration period
         */
        public int getRegistrationPeriod() {
            return registrationPeriod;
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
            sb.append("  Holding Type:  ").append(Nxt.getHoldingType(holdingType)).append("\n");
            if (holdingType == 0) {
                Chain chain = Nxt.getChain((int)holdingId);
                sb.append("  Chain:  ").append(chain.getName()).append("\n")
                        .append("  Amount:  ").append(Utils.nqtToString(amount, chain.getDecimals())).append("\n");
            } else {
                sb.append("  Holding:  ").append(holdingId).append("\n")
                        .append("  Amount:  ").append(String.format("%,d", amount)).append("\n");
            }
            sb.append("  Participant Count:  ").append(participantCount).append("\n")
                .append("  Registration Period:  ").append(registrationPeriod).append("\n");
            return sb;
        }
    }

    /**
     * Registration attachment
     */
    public static class RegistrationAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new RegistrationAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new RegistrationAttachment(txType, buffer);
        }

        private byte[] shufflingFullHash;

        RegistrationAttachment() {
        }

        RegistrationAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            shufflingFullHash = json.getHexString("shufflingFullHash");
        }

        RegistrationAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            shufflingFullHash = new byte[32];
            buffer.get(shufflingFullHash);
        }

        /**
         * Get the shuffling hash
         *
         * @return              Shuffling hash
         */
        public byte[] getShufflingHash() {
            return shufflingFullHash;
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
            sb.append("  Shuffling Hash:  ").append(Utils.toHexString(shufflingFullHash)).append("\n");
            return sb;
        }
    }

    /**
     * Processing attachment
     */
    public static class ProcessingAttachment extends ShufflingAttachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new ProcessingAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new ProcessingAttachment(txType, buffer);
        }

        private List<byte[]> processingData;
        private byte[] hash;

        ProcessingAttachment() {
        }

        ProcessingAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            List<String> dataList = json.getStringList("data");
            if (!dataList.isEmpty()) {
                processingData = new ArrayList<>(dataList.size());
                dataList.forEach(hex -> processingData.add(Utils.parseHexString(hex)));
            } else {
                hash = json.getHexString("hash");
            }
        }

        ProcessingAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            int flags = buffer.get();
            if ((flags & 1) != 0) {
                int count = buffer.get();
                processingData = new ArrayList<>(count);
                for (int i=0; i<count; i++) {
                    int length = buffer.getShort();
                    byte[] bytes = new byte[length];
                    buffer.get(bytes);
                    processingData.add(bytes);
                }
            } else {
                hash = new byte[32];
                buffer.get(hash);
            }
        }

        /**
         * Get the processing data
         *
         * @return              Processing data (null if the data has been pruned)
         */
        public List<byte[]> getProcessingData() {
            return processingData;
        }

        /**
         * Get the processing data hash
         *
         * @return              Data hash
         */
        public byte[] getDataHash() {
            if (hash == null) {
                hash = Crypto.singleDigest(processingData);
            }
            return hash;
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
            sb.append("  Processing Data Hash:  ").append(Utils.toHexString(getDataHash())).append("\n");
            return sb;
        }
    }

    /**
     * Recipients attachment
     */
    public static class RecipientsAttachment extends ShufflingAttachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new RecipientsAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new RecipientsAttachment(txType, buffer);
        }

        private List<byte[]> recipientPublicKeys;

        RecipientsAttachment() {
        }

        RecipientsAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            List<String> dataList = json.getStringList("recipientPublicKeys");
            recipientPublicKeys = new ArrayList<>(dataList.size());
            dataList.forEach(hex -> recipientPublicKeys.add(Utils.parseHexString(hex)));
        }

        RecipientsAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            int count = buffer.get();
            recipientPublicKeys = new ArrayList<>(count);
            for (int i=0; i<count; i++) {
                byte[] bytes = new byte[32];
                buffer.get(bytes);
                recipientPublicKeys.add(bytes);
            }
        }

        /**
         * Get the recipient public keys
         *
         * @return              Recipient public keys
         */
        public List<byte[]> getRecipientPublicKeys() {
            return recipientPublicKeys;
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
            recipientPublicKeys.forEach(key ->
                sb.append("  Recipient Public Key:  ").append(Utils.toHexString(key)).append("\n"));
            return sb;
        }
    }

    /**
     * Verification attachment
     */
    public static class VerificationAttachment extends ShufflingAttachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new VerificationAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new VerificationAttachment(txType, buffer);
        }

        VerificationAttachment() {
        }

        VerificationAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
        }

        VerificationAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
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
            return sb;
        }
    }

    /**
     * Cancellation attachment
     */
    public static class CancellationAttachment extends ShufflingAttachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new CancellationAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new CancellationAttachment(txType, buffer);
        }

        private List<byte[]> blameData;
        private List<byte[]> keySeeds;
        private long cancellingAccountId;

        CancellationAttachment() {
        }

        CancellationAttachment(TransactionType txType, Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super(txType, json);
            List<String> dataList = json.getStringList("blameData");
            blameData = new ArrayList<>(dataList.size());
            dataList.forEach(hex -> blameData.add(Utils.parseHexString(hex)));
            dataList = json.getStringList("keySeeds");
            keySeeds = new ArrayList<>(dataList.size());
            dataList.forEach(hex -> keySeeds.add(Utils.parseHexString(hex)));
            cancellingAccountId = json.getId("cancellingAccount");
        }

        CancellationAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            int count = buffer.get();
            blameData = new ArrayList<>(count);
            for (int i=0; i<count; i++) {
                int length = buffer.getInt();
                byte[] bytes = new byte[length];
                buffer.get(bytes);
                blameData.add(bytes);
            }
            count = buffer.get();
            keySeeds = new ArrayList<>(count);
            for (int i=0; i<count; i++) {
                byte[] bytes = new byte[32];
                buffer.get(bytes);
                keySeeds.add(bytes);
            }
            cancellingAccountId = buffer.getLong();
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
            sb.append("  Cancelling Account:  ").append(Utils.getAccountRsId(cancellingAccountId)).append("\n");
            return sb;
        }
    }
}
