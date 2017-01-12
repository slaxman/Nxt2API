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

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Transaction appendix
 * <p>
 * A transaction can have zero or more appendices.  These appendices
 * are common to all transactions and are not dependent on the
 * transaction type.
 * <p>
 * A prunable appendix has a data hash as part of the signed transaction
 * and the prunable data is in a separate JSON object.  This allows the
 * prunable data to be discarded.
 */
public abstract class Appendix {

    /** UTF-8 character set */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /** Appendix types */
    public enum AppendixType {
        MessageAppendix(1, new MessageAppendix()),
        EncryptedMessageAppendix(2, new EncryptedMessageAppendix()),
        EncryptToSelfMessageAppendix(4, new EncryptToSelfMessageAppendix()),
        PrunablePlainMessageAppendix(8, new PrunablePlainMessageAppendix()),
        PrunableEncryptedMessageAppendix(16, new PrunableEncryptedMessageAppendix()),
        PublicKeyAnnouncementAppendix(32, new PublicKeyAnnouncementAppendix()),
        PhasingAppendix(64, new PhasingAppendix());

        private static final SortedMap<Integer, AppendixType> sortedMap = new TreeMap<>();
        static {
            for (AppendixType type : values()) {
                sortedMap.put(type.code, type);
            }
        }

        private final int code;
        private final Appendix appendix;

        private AppendixType(int code, Appendix appendix) {
            this.code = code;
            this.appendix = appendix;
        }

        public static AppendixType get(int code) {
            return sortedMap.get(code);
        }

        static SortedMap<Integer, AppendixType> getAppendixMap() {
            return sortedMap;
        }

        public int getCode() {
            return code;
        }

        Appendix getAppendix() {
            return appendix;
        }
    }

    /** Appendix version */
    int version;

    /** Appendix name */
    String name;

    /** Appendix type */
    AppendixType appendixType;

    /**
     * Create an appendix
     *
     * @param   name                Appendix name
     * @param   appendixType        Appendix type
     */
    private Appendix(String name, AppendixType appendixType) {
        this.name = name;
        this.appendixType = appendixType;
        this.version = 0;
    }

    /**
     * Create an appendix
     *
     * @param   name                Appendix name
     * @param   appendixType        Appendix type
     * @param   json                Appendix JSON
     */
    private Appendix(String name, AppendixType appendixType, Response json) {
        this.name = name;
        this.appendixType = appendixType;
        this.version = json.getInt("version." + name);
    }

    /**
     * Create an appendix
     *
     * @param   name                Appendix name
     * @param   appendixType        Appendix type
     * @param   buffer              Appendix bytes
     */
    private Appendix(String name, AppendixType appendixType, ByteBuffer buffer) {
        this.name = name;
        this.appendixType = appendixType;
        this.version = buffer.get();
    }

    /**
     * Get the appendix version
     *
     * @return                              Appendix version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Get the appendix name
     *
     * @return                              Appendix name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the appendix type
     *
     * @return                      Appendix type
     */
    public AppendixType getAppendixType() {
        return appendixType;
    }

    /**
     * Parse the appendix JSON
     *
     * @param   json                        Appendix JSON
     * @return                              Attachment or null if transaction not supported
     * @throws  IdentifierException         Invalid Nxt object identifier
     * @throws  IllegalArgumentException    Response is not valid
     * @throws  NumberFormatException       Invalid numeric value
     */
    abstract protected Appendix parseAppendix(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException;

    /**
     * Parse the appendix bytes
     *
     * @param   buffer                      Appendix bytes
     * @return                              Attachment or null if transaction not supported
     * @throws  BufferUnderflowException    End-of-data reached parsing attachment
     * @throws  IllegalArgumentException    Invalid attachment
     */
    abstract protected Appendix parseAppendix(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException;

    /**
     * Plain message appendix
     */
    public static class MessageAppendix extends Appendix {

        @Override
        protected Appendix parseAppendix(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new MessageAppendix(json);
        }

        @Override
        protected Appendix parseAppendix(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new MessageAppendix(buffer);
        }

        private byte[] messageBytes;
        private boolean isText;

        private MessageAppendix() {
            super("Message", AppendixType.MessageAppendix);
        }

        private MessageAppendix(Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super("Message", AppendixType.MessageAppendix, json);
            isText = json.getBoolean("messageIsText");
            if (isText) {
                messageBytes = json.getString("message").getBytes(UTF8);
            } else {
                messageBytes = Utils.parseHexString(json.getString("message"));
            }
        }

        private MessageAppendix(ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super("Message", AppendixType.MessageAppendix, buffer);
            int flags = buffer.get();
            isText = ((flags & 1) != 0);
            int length = buffer.getShort();
            messageBytes = new byte[length];
            buffer.get(messageBytes);
        }

        /**
         * Check if this is a text message
         *
         * @return                  TRUE if this is a text message
         */
        public boolean isText() {
            return isText;
        }

        /**
         * Get the message bytes
         * <p>
         * A text message is returned as a UTF-8 encoded byte array
         *
         * @return                  Message bytes
         */
        public byte[] getMessageBytes() {
            return messageBytes;
        }

        /**
         * Get the message text
         * <p>
         * A binary message is returned as a hexadecimal string
         *
         * @return                  Message text
         */
        public String getMessage() {
            return (isText ? new String(messageBytes, UTF8) : Utils.toHexString(messageBytes));
        }

        /**
         * Return a string representation of this appendix
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Is Text:  ").append(isText).append("\n")
                    .append("  Message:  ").append(getMessage()).append("\n");
            return sb;
        }
    }

    /**
     * Encrypted message appendix
     */
    public static class EncryptedMessageAppendix extends Appendix {

        @Override
        protected Appendix parseAppendix(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new EncryptedMessageAppendix(json);
        }

        @Override
        protected Appendix parseAppendix(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new EncryptedMessageAppendix(buffer);
        }

        private byte[] encryptedData;
        private byte[] nonce;
        private boolean isText;
        private boolean isCompressed;

        private EncryptedMessageAppendix() {
            super("EncryptedMessage", AppendixType.EncryptedMessageAppendix);
        }

        private EncryptedMessageAppendix(Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super("EncryptedMessage", AppendixType.EncryptedMessageAppendix, json);
            Response data = json.getObject("encryptedMessage");
            isText = data.getBoolean("isText");
            isCompressed = data.getBoolean("isCompressed");
            encryptedData = data.getHexString("data");
            if (encryptedData.length < 16 || encryptedData.length % 16 != 0)
                throw new IllegalArgumentException(
                        "Encrypted data length " + encryptedData.length + " is not valid");
            nonce = data.getHexString("nonce");
            if (nonce.length != 32)
                throw new IllegalArgumentException("Nonce length " + nonce.length + " is not valid");
        }

        private EncryptedMessageAppendix(ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super("EncryptedMessage", AppendixType.EncryptedMessageAppendix, buffer);
            int flags = buffer.get();
            isText = ((flags & 1) != 0);
            isCompressed = ((flags & 2) != 0);
            int length = buffer.getShort();
            if (length < 16 || length % 16 != 0)
                throw new IllegalArgumentException(
                        "Encrypted data length " + length + " is not valid");
            encryptedData = new byte[length];
            buffer.get(encryptedData);
            nonce = new byte[32];
            buffer.get(nonce);
        }

        /**
         * Check if this is a text message
         *
         * @return                  TRUE if this is a text message
         */
        public boolean isText() {
            return isText;
        }

        /**
         * Check if the message is compressed
         *
         * @return                  TRUE if the message is compressed
         */
        public boolean isCompressed() {
            return isCompressed;
        }

        /**
         * Get the decrypted and uncompressed message bytes
         * <p>
         * A text message is returned as a UTF-8 encode byte array
         *
         * @param   secretPhrase    Account A secret phrase
         * @param   publicKey       Account B public key
         * @return                  Decrypted message bytes
         * @throws  IOException     Unable to uncompress the message data
         */
        public byte[] getMessageBytes(String secretPhrase, byte[] publicKey) throws IOException {
            byte[] privateKey = Crypto.getPrivateKey(secretPhrase);
            byte[] sharedKey = Crypto.getSharedKey(privateKey, publicKey, nonce);
            byte[] decryptedBytes = Crypto.aesDecrypt(encryptedData, sharedKey);
            if (isCompressed) {
                decryptedBytes = Utils.uncompressBytes(decryptedBytes);
            }
            return decryptedBytes;
        }

        /**
         * Get the decrypted and uncompressed message text
         * <p>
         * A binary message is returned as a hexadecimal string
         *
         * @param   secretPhrase    Account A secret phrase
         * @param   publicKey       Account B public key
         * @return                  Message text
         * @throws  IOException     Unable to uncompress the message data
         */
        public String getMessage(String secretPhrase, byte[] publicKey) throws IOException {
            byte[] data = getMessageBytes(secretPhrase, publicKey);
            return (isText ? new String(data, UTF8) : Utils.toHexString(data));
        }

        /**
         * Return a string representation of this appendix
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Is Text:  ").append(isText).append("\n")
                    .append("  Is Compressed:  ").append(isCompressed).append("\n")
                    .append("  Nonce:  ").append(Utils.toHexString(nonce)).append("\n")
                    .append("  Encrypted Data:  ").append(Utils.toHexString(encryptedData)).append("\n");
            return sb;
        }
    }

    /**
     * Encrypted message to self appendix
     */
    public static class EncryptToSelfMessageAppendix extends Appendix {

        @Override
        protected Appendix parseAppendix(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new EncryptToSelfMessageAppendix(json);
        }

        @Override
        protected Appendix parseAppendix(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new EncryptToSelfMessageAppendix(buffer);
        }

        private byte[] encryptedData;
        private byte[] nonce;
        private boolean isText;
        private boolean isCompressed;

        private EncryptToSelfMessageAppendix() {
            super("EncryptToSelfMessage", AppendixType.EncryptToSelfMessageAppendix);
        }

        private EncryptToSelfMessageAppendix(Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super("EncryptToSelfMessage", AppendixType.EncryptToSelfMessageAppendix, json);
            Response data = json.getObject("encryptToSelfMessage");
            isText = data.getBoolean("isText");
            isCompressed = data.getBoolean("isCompressed");
            encryptedData = data.getHexString("data");
            if (encryptedData.length < 16 || encryptedData.length % 16 != 0)
                throw new IllegalArgumentException(
                        "Encrypted data length " + encryptedData.length + " is not valid");
            nonce = data.getHexString("nonce");
            if (nonce.length != 32)
                throw new IllegalArgumentException("Nonce length " + nonce.length + " is not valid");
        }

        private EncryptToSelfMessageAppendix(ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super("EncryptToSelfMessage", AppendixType.EncryptToSelfMessageAppendix, buffer);
            int flags = buffer.get();
            isText = ((flags & 1) != 0);
            isCompressed = ((flags & 2) != 0);
            int length = buffer.getShort();
            if (length < 16 || length % 16 != 0)
                throw new IllegalArgumentException(
                        "Encrypted data length " + length + " is not valid");
            encryptedData = new byte[length];
            buffer.get(encryptedData);
            nonce = new byte[32];
            buffer.get(nonce);
        }

        /**
         * Check if this is a text message
         *
         * @return                  TRUE if this is a text message
         */
        public boolean isText() {
            return isText;
        }

        /**
         * Check if the message is compressed
         *
         * @return                  TRUE if the message is compressed
         */
        public boolean isCompressed() {
            return isCompressed;
        }

        /**
         * Get the decrypted and uncompressed message bytes
         * <p>
         * A text message is returned as a UTF-8 encoded byte array
         *
         * @param   secretPhrase    Account secret phrase
         * @return                  Decrypted message bytes
         * @throws  IOException     Unable to uncompress the message data
         * @throws  KeyException    Unable to get public key from secret phrase
         */
        public byte[] getMessageBytes(String secretPhrase) throws IOException, KeyException {
            byte[] decryptedBytes;
            byte[] privateKey = Crypto.getPrivateKey(secretPhrase);
            byte[] publicKey = Crypto.getPublicKey(secretPhrase);
            byte[] sharedKey = Crypto.getSharedKey(privateKey, publicKey, nonce);
            decryptedBytes = Crypto.aesDecrypt(encryptedData, sharedKey);
            if (isCompressed) {
                decryptedBytes = Utils.uncompressBytes(decryptedBytes);
            }
            return decryptedBytes;
        }

        /**
         * Get the decrypted and uncompressed message text
         * <p>
         * A binary message is returned as a hexadecimal string
         *
         * @param   secretPhrase    Account secret phrase
         * @return                  Message text
         * @throws  IOException     Unable to uncompress the message data
         * @throws  KeyException    Unable to get public key from secret phrase
         */
        public String getMessage(String secretPhrase) throws IOException, KeyException {
            byte[] data = getMessageBytes(secretPhrase);
            return (isText ? new String(data, UTF8) : Utils.toHexString(data));
        }

        /**
         * Return a string representation of this appendix
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Is Text:  ").append(isText).append("\n")
                    .append("  Is Compressed:  ").append(isCompressed).append("\n")
                    .append("  Nonce:  ").append(Utils.toHexString(nonce)).append("\n")
                    .append("  Encrypted Data:  ").append(Utils.toHexString(encryptedData)).append("\n");
            return sb;
        }
    }

    /**
     * Prunable plain message appendix
     */
    public static class PrunablePlainMessageAppendix extends Appendix {

        @Override
        protected Appendix parseAppendix(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new PrunablePlainMessageAppendix(json);
        }

        @Override
        protected Appendix parseAppendix(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new PrunablePlainMessageAppendix(buffer);
        }

        private byte[] hashBytes;
        private byte[] messageBytes;
        private boolean isText;

        private PrunablePlainMessageAppendix() {
            super("PrunablePlainMessage", AppendixType.PrunablePlainMessageAppendix);
        }

        private PrunablePlainMessageAppendix(Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super("PrunablePlainMessage", AppendixType.PrunablePlainMessageAppendix, json);
            String data = json.getString("message");
            //
            // We have just the hash if the message has been pruned
            //
            if (data.length() > 0) {
                isText = json.getBoolean("messageIsText");
                messageBytes = (isText ? data.getBytes(UTF8) : Utils.parseHexString(data));
            } else {
                hashBytes = json.getHexString("hash");
            }
        }

        private PrunablePlainMessageAppendix(ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super("PrunablePlainMessage", AppendixType.PrunablePlainMessageAppendix, buffer);
            int flags = buffer.get();
            //
            // We have just the hash if the message has been pruned
            //
            if ((flags & 1) != 0) {
                isText = ((flags & 2) != 0);
                int length = buffer.getInt();
                messageBytes = new byte[length];
                buffer.get(messageBytes);
            } else {
                hashBytes = new byte[32];
                buffer.get(hashBytes);
            }
        }

        /**
         * Check if this is a text message
         *
         * @return                  TRUE if this is a text message
         */
        public boolean isText() {
            return isText;
        }

        /**
         * Get the message bytes
         * <p>
         * A text message is returned as a UTF-8 encoded byte array
         *
         * @return                  Message bytes or null if the message has been pruned
         */
        public byte[] getMessageBytes() {
            return messageBytes;
        }

        /**
         * Get the message hash
         *
         * @return                  32-byte message hash
         */
        public byte[] getMessageHash() {
            if (hashBytes == null) {
                byte[] flagByte = new byte[1];
                flagByte[0] = (byte)(isText ? 1 : 0);
                hashBytes = Crypto.singleDigest(flagByte, messageBytes);
            }
            return hashBytes;
        }

        /**
         * Get the message text
         * <p>
         * A binary message is returned as a hexadecimal string
         *
         * @return                  Message text or null if the message has been pruned
         */
        public String getMessage() {
            return (messageBytes != null ?
                    (isText ? new String(messageBytes, UTF8) : Utils.toHexString(messageBytes)) : null);
        }

        /**
         * Return a string representation of this appendix
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            if (messageBytes != null)
                sb.append("  Is Text:  ").append(isText).append("\n")
                        .append("  Message:  ").append(getMessage()).append("\n");
            sb.append("  Message Hash:  ").append(Utils.toHexString(getMessageHash())).append("\n");
            return sb;
        }
    }

    /**
     * Prunable encrypted message appendix
     */
    public static class PrunableEncryptedMessageAppendix extends Appendix {

        @Override
        protected Appendix parseAppendix(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new PrunableEncryptedMessageAppendix(json);
        }

        @Override
        protected Appendix parseAppendix(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new PrunableEncryptedMessageAppendix(buffer);
        }

        private byte[] hash;
        private byte[] encryptedData;
        private byte[] nonce;
        private boolean isText;
        private boolean isCompressed;

        private PrunableEncryptedMessageAppendix() {
            super("PrunableEncryptedMessage", AppendixType.PrunableEncryptedMessageAppendix);
        }

        private PrunableEncryptedMessageAppendix(Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super("PrunableEncryptedMessage", AppendixType.PrunableEncryptedMessageAppendix, json);
            Response data = json.getObject("encryptedMessage");
            if (!data.getObjectMap().isEmpty()) {
                isText = data.getBoolean("isText");
                isCompressed = data.getBoolean("isCompressed");
                encryptedData = data.getHexString("data");
                if (encryptedData.length < 16 || encryptedData.length % 16 != 0)
                    throw new IllegalArgumentException(
                            "Encrypted data length " + encryptedData.length + " is not valid");
                nonce = data.getHexString("nonce");
                if (nonce.length != 32)
                    throw new IllegalArgumentException("Nonce length " + nonce.length + " is not valid");
            } else {
                hash = json.getHexString("encryptedMessageHash");
            }
        }

        private PrunableEncryptedMessageAppendix(ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super("PrunableEncryptedMessage", AppendixType.PrunableEncryptedMessageAppendix, buffer);
            int flags = buffer.get();
            if ((flags & 1) != 0) {
                isText = ((flags & 2) != 0);
                isCompressed = ((flags & 4) != 0);
                int length = buffer.getInt();
                if (length < 16 || length % 16 != 0)
                    throw new IllegalArgumentException(
                            "Encrypted data length " + length + " is not valid");
                encryptedData = new byte[length];
                buffer.get(encryptedData);
                nonce = new byte[32];
                buffer.get(nonce);
            } else {
                hash = new byte[32];
                buffer.get(hash);
            }
        }

        /**
         * Check if this is a text message
         *
         * @return                  TRUE if this is a text message
         */
        public boolean isText() {
            return isText;
        }

        /**
         * Check if the message is compressed
         *
         * @return                  TRUE if the message is compressed
         */
        public boolean isCompressed() {
            return isCompressed;
        }

        /**
         * Get the message hash
         *
         * @return                  32-byte message hash
         */
        public byte[] getMessageHash() {
            if (hash == null) {
                byte[] flags = new byte[2];
                flags[0] = (byte)(isText ? 1 : 0);
                flags[1] = (byte)(isCompressed ? 1 : 0);
                hash = Crypto.singleDigest(flags, encryptedData, nonce);
            }
            return hash;
        }

        /**
         * Get the decrypted and uncompressed message bytes
         * <p>
         * A text message is returned as a UTF-8 encode byte array
         *
         * @param   secretPhrase    Account A secret phrase
         * @param   publicKey       Account B public key
         * @return                  Decrypted message bytes or null if the message has been pruned
         * @throws  IOException     Unable to uncompress the message data
         */
        public byte[] getMessageBytes(String secretPhrase, byte[] publicKey) throws IOException {
            if (encryptedData == null)
                return null;
            byte[] privateKey = Crypto.getPrivateKey(secretPhrase);
            byte[] sharedKey = Crypto.getSharedKey(privateKey, publicKey, nonce);
            byte[] decryptedBytes = Crypto.aesDecrypt(encryptedData, sharedKey);
            if (isCompressed) {
                decryptedBytes = Utils.uncompressBytes(decryptedBytes);
            }
            return decryptedBytes;
        }

        /**
         * Get the decrypted and uncompressed message text
         * <p>
         * A binary message is returned as a hexadecimal string
         *
         * @param   secretPhrase    Account A secret phrase
         * @param   publicKey       Account B public key
         * @return                  Message text
         * @throws  IOException     Unable to uncompress the message data
         */
        public String getMessage(String secretPhrase, byte[] publicKey) throws IOException {
            byte[] data = getMessageBytes(secretPhrase, publicKey);
            return (data == null ? null :
                    (isText ? new String(data, UTF8) : Utils.toHexString(data)));
        }

        /**
         * Return a string representation of this appendix
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            if (encryptedData != null)
                sb.append("  Is Text:  ").append(isText).append("\n")
                        .append("  Is Compressed:  ").append(isCompressed).append("\n")
                        .append("  Nonce:  ").append(Utils.toHexString(nonce)).append("\n")
                        .append("  Encrypted Data:  ").append(Utils.toHexString(encryptedData)).append("\n");
            sb.append("  Message Hash:  ").append(Utils.toHexString(getMessageHash())).append("\n");
            return sb;
        }
    }

    /**
     * Public key announcement appendix
     */
    public static class PublicKeyAnnouncementAppendix extends Appendix {

        @Override
        protected Appendix parseAppendix(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new PublicKeyAnnouncementAppendix(json);
        }

        @Override
        protected Appendix parseAppendix(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new PublicKeyAnnouncementAppendix(buffer);
        }

        private byte[] publicKey;

        private PublicKeyAnnouncementAppendix() {
            super("PublicKeyAnnouncement", AppendixType.PublicKeyAnnouncementAppendix);
        }

        private PublicKeyAnnouncementAppendix(Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super("PublicKeyAnnouncement", AppendixType.PublicKeyAnnouncementAppendix, json);
            publicKey = json.getHexString("recipientPublicKey");
        }

        private PublicKeyAnnouncementAppendix(ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super("PublicKeyAnnouncement", AppendixType.PublicKeyAnnouncementAppendix, buffer);
            publicKey = new byte[32];
            buffer.get(publicKey);
        }

        /**
         * Get the public key
         *
         * @return                  32-byte public key
         */
        public byte[] getPublicKey() {
            return publicKey;
        }

        /**
         * Return a string representation of this appendix
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Public Key:  ").append(Utils.toHexString(getPublicKey())).append("\n");
            return sb;
        }
    }

    /**
     * Phasing appendix
     */
    public static class PhasingAppendix extends Appendix {

        @Override
        protected Appendix parseAppendix(Response json)
                throws IdentifierException, IllegalArgumentException, NumberFormatException {
            return new PhasingAppendix(json);
        }

        @Override
        protected Appendix parseAppendix(ByteBuffer buffer)
                throws BufferUnderflowException, IllegalArgumentException {
            return new PhasingAppendix(buffer);
        }

        private PhasingParameters phasingParams;
        private int finishHeight;
        private List<ChainTransactionId> linkedTransactions;
        private byte[] hashedSecret;
        private int hashedSecretAlgorithm;

        private PhasingAppendix() {
            super("Phasing", AppendixType.PhasingAppendix);
        }

        private PhasingAppendix(Response json)
                    throws IdentifierException, IllegalArgumentException, NumberFormatException {
            super("Phasing", AppendixType.PhasingAppendix, json);
            finishHeight = json.getInt("phasingFinishHeight");
            phasingParams = new PhasingParameters(json);
            List<Response> linkedList = json.getObjectList("phasingLinkedTransactions");
            linkedTransactions = new ArrayList<>(linkedList.size());
            for (Response link : linkedList) {
                linkedTransactions.add(new ChainTransactionId(link.getInt("chain"),
                        link.getHexString("transactionFullHash")));
            }
            hashedSecret = json.getHexString("phasingHashedSecret");
            if (hashedSecret != null)
                hashedSecretAlgorithm = json.getInt("phasingHashedSecretAlgorithm");
        }

        private PhasingAppendix(ByteBuffer buffer)
                    throws BufferUnderflowException, IllegalArgumentException {
            super("Phasing", AppendixType.PhasingAppendix, buffer);
            finishHeight = buffer.getInt();
            phasingParams = new PhasingParameters(buffer);
            int count = buffer.get();
            linkedTransactions = new ArrayList<>(count);
            for (int i=0; i<count; i++) {
                byte[] hash = new byte[32];
                int chainId = buffer.getInt();
                buffer.get(hash);
                linkedTransactions.add(new ChainTransactionId(chainId, hash));
            }
            count = buffer.get();
            if (count > 0) {
                hashedSecret = new byte[count];
                buffer.get(hashedSecret);
                hashedSecretAlgorithm = buffer.get();
            }
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
         * Get the phasing parameters
         *
         * @return                  Phasing parameters
         */
        public PhasingParameters getPhasingParams() {
            return phasingParams;
        }

        /**
         * Get the linked transactions
         *
         * @return                  List of linked transactions
         */
        public List<ChainTransactionId> getLinkedTransactions() {
            return linkedTransactions;
        }

        /**
         * Get the hashed secret
         *
         * @return                  Hashed secret or null if no secret specified
         */
        public byte[] getHashedSecret() {
            return hashedSecret;
        }

        /**
         * Get the hashed secret algorithm
         *
         * @return                  Hashed secret algorithm
         */
        public int getHashedSecretAlgorithm() {
            return hashedSecretAlgorithm;
        }

        /**
         * Return a string representation of this appendix
         *
         * @param   sb              String builder
         * @return                  The supplied string builder
         */
        @Override
        public StringBuilder toString(StringBuilder sb) {
            super.toString(sb);
            sb.append("  Finish Height:  ").append(getFinishHeight()).append("\n");
            phasingParams.toString(sb);
            if (!getLinkedTransactions().isEmpty())
                getLinkedTransactions().forEach(link -> sb.append("  Linked Transaction:").append("\n")
                        .append("    Chain:  ").append(link.getChain().getName()).append("\n")
                        .append("    Full Hash: ")
                                .append(Utils.toHexString(link.getFullHash()))
                                .append("\n"));
            if (getHashedSecret() != null)
                sb.append("  Hashed Secret:  ")
                                .append(Utils.toHexString(getHashedSecret())).append("\n")
                        .append("  Hashed Secret Algorithm:  ")
                                .append(Nxt.getPhasingHashAlgorithm(getHashedSecretAlgorithm()))
                                .append("\n");
            return sb;
        }
    }

    /**
     * Return a string representation of the appendix
     *
     * @param   sb                  String builder
     * @return                      The supplied string builder
     */
    public StringBuilder toString(StringBuilder sb) {
        sb.append("Appendix:  ").append(getName()).append("\n");
        return sb;
    }

    /**
     * Return a string representation of the appendix
     *
     * @return                      String representation
     */
    @Override
    public String toString() {
        return toString(new StringBuilder(64)).toString();
    }
}
