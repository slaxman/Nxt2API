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
 * Poll attachments
 */
public abstract class PollAttachment {

    /**
     * Creation attachment
     */
    public static class CreationAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new CreationAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new CreationAttachment(txType, buffer);
        }

        private String name;
        private String description;
        private int finishHeight;
        private int votingModel;
        private int minNumberOfOptions;
        private int maxNumberOfOptions;
        private int minRangeValue;
        private int maxRangeValue;
        private long minBalance;
        private int minBalanceModel;
        private long holdingId;
        List<String> options;

        CreationAttachment() {
        }

        CreationAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            name = json.getString("name");
            description = json.getString("description");
            finishHeight = json.getInt("finishHeight");
            List<String> optionList = json.getStringList("options");
            options = new ArrayList<>(optionList.size());
            optionList.forEach(options::add);
            votingModel = json.getInt("votingModel");
            minNumberOfOptions = json.getInt("minNumberOfOptions");
            maxNumberOfOptions = json.getInt("maxNumberOfOptions");
            minRangeValue = json.getInt("minRangeValue");
            maxRangeValue = json.getInt("maxRangeValue");
            minBalance = json.getLong("minBalance");
            minBalanceModel = json.getInt("minBalanceModel");
            holdingId = json.getLong("holding");
        }

        CreationAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            name = readString(buffer.getShort(), buffer);
            description = readString(buffer.getShort(), buffer);
            finishHeight = buffer.getInt();
            int count = buffer.get();
            options = new ArrayList<>(count);
            for (int i=0; i<count; i++) {
                options.add(readString(buffer.getShort(), buffer));
            }
            votingModel = buffer.get();
            minNumberOfOptions = buffer.get();
            maxNumberOfOptions = buffer.get();
            minRangeValue = buffer.get();
            maxRangeValue = buffer.get();
            minBalance = buffer.getLong();
            minBalanceModel = buffer.get();
            holdingId = buffer.getLong();
        }

        /**
         * Get the poll name
         *
         * @return                  Poll name
         */
        public String getPollName() {
            return name;
        }

        /**
         * Get the poll description
         *
         * @return                  Poll description
         */
        public String getPollDescription() {
            return description;
        }

        /**
         * Get the finish height
         *
         * @return                  Finish height
         */
        public int getFinishHeight() {
            return finishHeight;
        }

        /**
         * Get the voting model
         *
         * @return                  Voting model
         */
        public int getVotingModel() {
            return votingModel;
        }

        /**
         * Get the minimum number of options
         *
         * @return                  Minimum number of options
         */
        public int getMinNumberOfOptions() {
            return minNumberOfOptions;
        }

        /**
         * Get the maximum number of options
         *
         * @return                  Maximum number of options
         */
        public int getMaxNumberOfOptions() {
            return maxNumberOfOptions;
        }

        /**
         * Get the minimum range value
         *
         * @return                  Minimum range value
         */
        public int getMinRangeValue() {
            return minRangeValue;
        }

        /**
         * Get the maximum range value
         *
         * @return                  Maximum range value
         */
        public int getMaxRangeValue() {
            return maxRangeValue;
        }

        /**
         * Get the minimum balance
         *
         * @return                  Minimum balance
         */
        public long getMinBalance() {
            return minBalance;
        }

        /**
         * Get the minimum balance model
         *
         * @return                  Minimum balance model
         */
        public int minBalanceModel() {
            return minBalanceModel;
        }

        /**
         * Return the poll options
         *
         * @return                  Poll options
         */
        public List<String> getPollOptions() {
            return options;
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
                    .append("  Description:  ").append(description).append("\n");
            options.forEach(option ->
                    sb.append("  Option:  ").append(option).append("\n"));
            sb.append("  Finish Height:  ").append(finishHeight).append("\n")
                    .append("  Voting Model:  ").append(Nxt.getVotingModel(votingModel)).append("\n")
                    .append("  Minimum Number of Options:  ").append(minNumberOfOptions).append("\n")
                    .append("  Maximum Number of Options:  ").append(maxNumberOfOptions).append("\n")
                    .append("  Minimum Range Value:  ").append(minRangeValue).append("\n")
                    .append("  Maximum Range Value:  ").append(maxRangeValue).append("\n")
                    .append("  Minimum Balance Model:  ").append(Nxt.getVotingModel(minBalanceModel)).append("\n");
            if (minBalance != 0) {
                if (minBalanceModel == Nxt.getVotingModel("COIN")) {
                    Chain chain = Nxt.getChain((int)holdingId);
                    if (chain != null) {
                        sb.append("  Chain:  ").append(chain.getName()).append("\n")
                                .append("  Minimum Balance:  ")
                                .append(Utils.nqtToString(minBalance, chain.getDecimals()))
                                .append("\n");
                    }
                } else if (holdingId != 0) {
                    sb.append("  Holding:  ").append(Utils.idToString(holdingId)).append("\n")
                            .append("  Minimum Balance:  ")
                            .append(String.format("%,d", minBalance))
                            .append("\n");
                }
            }
            return sb;
        }
    }

    /**
     * Vote Casting attachment
     */
    public static class VoteCastingAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new VoteCastingAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new VoteCastingAttachment(txType, buffer);
        }

        private long pollId;
        private byte[] votes;

        VoteCastingAttachment() {
        }

        VoteCastingAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            pollId = json.getId("poll");
            List<Long> voteList = json.getLongList("vote");
            votes = new byte[voteList.size()];
            int i=0;
            for (long vote : voteList) {
                votes[i++] = (byte)vote;
            }
        }

        VoteCastingAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            pollId = buffer.getLong();
            int count = buffer.get();
            votes = new byte[count];
            buffer.get(votes);
        }

        /**
         * Get the poll identifier
         *
         * @return                  Poll identifier
         */
        public long getPollId() {
            return pollId;
        }

        /**
         * Get the votes
         *
         * @return                  Votes
         */
        public byte[] getVotes() {
            return votes;
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
            sb.append("  Poll:  ").append(Utils.idToString(pollId)).append("\n");
            for (byte vote : votes) {
                sb.append("  Vote:  ").append(vote).append("\n");
            }
            return sb;
        }
    }

    /**
     * Phasing Vote Casting attachment
     */
    public static class PhasingVoteCastingAttachment extends Attachment {

        @Override
        protected Attachment parseAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            return new PhasingVoteCastingAttachment(txType, json);
        }

        @Override
        protected Attachment parseAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            return new PhasingVoteCastingAttachment(txType, buffer);
        }

        List<ChainTransactionId> phasedTransactions;
        byte[] revealedSecret;

        PhasingVoteCastingAttachment() {
        }

        PhasingVoteCastingAttachment(TransactionType txType, Response json)
                    throws IdentifierException, NumberFormatException {
            super(txType, json);
            List<Response> txList = json.getObjectList("phasedTransactions");
            phasedTransactions = new ArrayList<>(txList.size());
            for (Response chainTx : txList) {
                phasedTransactions.add(new ChainTransactionId(chainTx));
            }
            revealedSecret = json.getHexString("revealedSecret");
        }

        PhasingVoteCastingAttachment(TransactionType txType, ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super(txType, buffer);
            int count = buffer.get();
            phasedTransactions = new ArrayList<>(count);
            for (int i=0; i<count; i++) {
                phasedTransactions.add(new ChainTransactionId(buffer));
            }
            int length = buffer.getInt();
            if (length > 0) {
                revealedSecret = new byte[length];
                buffer.get(revealedSecret);
            }
        }

        /**
         * Get the phased transactions
         *
         * @return                  Phased transactions
         */
        public List<ChainTransactionId> getPhasedTransactions() {
            return phasedTransactions;
        }

        /**
         * Get the revealed secret
         *
         * @return                  Revealed secret or null
         */
        public byte[] getRevealedSecret() {
            return revealedSecret;
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
            phasedTransactions.forEach(tx ->
                sb.append("  Phased Transaction:  ").append("\n")
                        .append("    Chain:  ").append(tx.getChain().getName()).append("\n")
                        .append("    Full Hash:  ")
                        .append(Utils.toHexString(tx.getFullHash())).append("\n"));
            return sb;
        }
    }
}
