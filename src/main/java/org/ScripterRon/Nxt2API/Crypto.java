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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Cryptographic functions using Curve25519
 *
 * Based on the Nxt reference software (NRS)
 */
public class Crypto {

    /** UTF-8 character set */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /** Instance of a SHA-256 digest which we will use as needed */
    private static final MessageDigest digest;
    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);      // Never happen
        }
    }

    /** Create a secure random generator for each thread */
    private static final ThreadLocal<SecureRandom> secureRandom = new ThreadLocal<SecureRandom>() {
        @Override
        protected SecureRandom initialValue() {
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBoolean();
            return secureRandom;
        }
    };

    /**
     * Return a secure random generator for the current thread
     *
     * @return                      Random number generator
     */
    public static SecureRandom getSecureRandom() {
        return secureRandom.get();
    }

    /**
     * Calculate the SHA-256 hash of a string
     *
     * @param       input           Data to be hashed
     * @return                      The hash digest
     */
    public static byte[] singleDigest(String input) {
        return singleDigest(input.getBytes(UTF8));
    }

    /**
     * Calculate the SHA-256 hash of a byte array
     *
     * @param       input           Data to be hashed
     * @return                      The hash digest
     */
    public static byte[] singleDigest(byte[] input) {
        byte[] bytes;
        synchronized (digest) {
            digest.reset();
            bytes = digest.digest(input);
        }
        return bytes;
    }

    /**
     * Calculate the SHA-256 hash of one or more byte arrays
     *
     * @param       inputs          Data to be hashed
     * @return                      The hash digest
     */
    public static byte[] singleDigest(byte[]... inputs) {
        byte[] bytes;
        synchronized (digest) {
            digest.reset();
            for (byte[] input : inputs)
                digest.update(input);
            bytes = digest.digest();
        }
        return bytes;
    }

    /**
     * Calculate the SHA-256 hash for a list of byte arrays
     *
     * @param       inputList           Data to be hashed
     * @return                          The hash digest
     */
    public static byte[] singleDigest(List<byte[]> inputList) {
        byte[] bytes;
        synchronized(digest) {
            digest.reset();
            inputList.forEach(input -> digest.update(input));
            bytes = digest.digest();
        }
        return bytes;
    }

    /**
     * Return the private key for the supplied secret phrase
     *
     * @param   secretPhrase            Account secret phrase
     * @return                          Private key
     */
    public static byte[] getPrivateKey(String secretPhrase) {
        byte[] k = singleDigest(secretPhrase.getBytes(UTF8));
        Curve25519.clamp(k);
        return k;
    }

    /**
     * Return the public key for the supplied secret phrase
     *
     * @param       secretPhrase        Account secret phrase
     * @return                          Public key
     * @throws      KeyException        Public key is not canonical
     */
    public static byte[] getPublicKey(String secretPhrase) throws KeyException {
        byte[] publicKey = new byte[32];
        Curve25519.keygen(publicKey, null, singleDigest(secretPhrase));
        if (!Curve25519.isCanonicalPublicKey(publicKey))
            throw new KeyException("Public key is not canonical");
        return publicKey;
    }

    /**
     * Get a shared key for use by accounts A and B.  User A uses the shared
     * key by providing the private key for A and the public key for B.
     * User b uses the shared key by providing the private key for B and
     * the public key for A.
     *
     * @param   privateKey              Private key of first account
     * @param   publicKey               Public key of second account
     * @param   nonce                   32-byte nonce
     * @return                          Shared key
     */
    public static byte[] getSharedKey(byte[] privateKey, byte[] publicKey, byte[] nonce) {
        byte[] sharedSecret = getSharedSecret(privateKey, publicKey);
        for (int i=0; i<32; i++) {
            sharedSecret[i] ^= nonce[i];
        }
        return singleDigest(sharedSecret);
    }

    /**
     * Get the shared secret for two accounts.
     *
     * @param   privateKey              Private key of account A
     * @param   publicKey               Public key of account B
     * @return                          Shared secret
     */
    private static byte[] getSharedSecret(byte[] privateKey, byte[] publicKey) {
        byte[] sharedSecret = new byte[32];
        Curve25519.curve(sharedSecret, privateKey, publicKey);
        return sharedSecret;
    }

    /**
     * Sign a message
     *
     * @param       message             The message to be signed
     * @param       secretPhrase        Private key phrase
     * @return                          The signed message
     * @throws      KeyException        Unable to sign message
     */
    public static byte[] sign(byte[] message, String secretPhrase) throws KeyException {
        byte[] signature = new byte[64];
        synchronized(digest) {
            digest.reset();
            byte[] P = new byte[32];
            byte[] s = new byte[32];
            Curve25519.keygen(P, s, digest.digest(secretPhrase.getBytes(UTF8)));

            byte[] m = digest.digest(message);

            digest.update(m);
            byte[] x = digest.digest(s);

            byte[] Y = new byte[32];
            Curve25519.keygen(Y, null, x);

            digest.update(m);
            byte[] h = digest.digest(Y);

            byte[] v = new byte[32];
            Curve25519.sign(v, h, x, s);

            System.arraycopy(v, 0, signature, 0, 32);
            System.arraycopy(h, 0, signature, 32, 32);

            if (!Curve25519.isCanonicalSignature(signature)) {
                throw new KeyException("Signature is not canonical");
            }
        }
        return signature;
    }

    /**
     * Encrypt data using the AES block cipher
     *
     * @param   plainText                   Text to encrypt
     * @param   key                         Encryption key
     * @return                              Encrypted text
     * @throws  IllegalArgumentException    AES encryption failed
     */
    public static byte[] aesEncrypt(byte[] plainText, byte[] key) throws IllegalArgumentException {
        byte[] result;
        try {
            byte[] iv = new byte[16];
            secureRandom.get().nextBytes(iv);
            PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
            CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
            aes.init(true, ivAndKey);
            byte[] output = new byte[aes.getOutputSize(plainText.length)];
            int ciphertextLength = aes.processBytes(plainText, 0, plainText.length, output, 0);
            ciphertextLength += aes.doFinal(output, ciphertextLength);
            result = new byte[iv.length + ciphertextLength];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(output, 0, result, iv.length, ciphertextLength);
        } catch (InvalidCipherTextException exc) {
            throw new IllegalArgumentException("Unable to encrypt text", exc);
        }
        return result;
    }

    /**
     * Decrypt data using the AES block cipher
     *
     * @param   encryptedData               Encrypted data
     * @param   key                         Encryption key
     * @return                              Decrypted text
     * @throws  IllegalArgumentException    AES decryption failed
     */
    public static byte[] aesDecrypt(byte[] encryptedData, byte[] key) throws IllegalArgumentException {
        byte[] result;
        try {
            if (encryptedData.length < 16 || encryptedData.length % 16 != 0) {
                throw new IllegalArgumentException("Encrypted data is not valid");
            }
            byte[] iv = Arrays.copyOfRange(encryptedData, 0, 16);
            byte[] ciphertext = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);
            PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
            CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
            aes.init(false, ivAndKey);
            byte[] output = new byte[aes.getOutputSize(ciphertext.length)];
            int plaintextLength = aes.processBytes(ciphertext, 0, ciphertext.length, output, 0);
            plaintextLength += aes.doFinal(output, plaintextLength);
            result = new byte[plaintextLength];
            System.arraycopy(output, 0, result, 0, result.length);
        } catch (InvalidCipherTextException exc) {
            throw new IllegalArgumentException("Unable to decrypt text", exc);
        }
        return result;
    }
}
