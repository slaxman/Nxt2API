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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.ScripterRon.JSON.JSONObject;
import org.ScripterRon.JSON.JSONParser;

/**
 * Interface between the Nxt application and the Nxt server
 */
public class Nxt {

    /** FXT chain name */
    public static final String FXT_CHAIN = "ARDR";

    /** Nxt chains */
    private static final Map<Integer, Chain> chains = new HashMap<>();

    /** Nxt transaction types */
    private static final Map<Integer, TransactionType> transactionTypes = new HashMap<>();

    /** Nxt voting models */
    private static final Map<Integer, String> votingModels = new HashMap<>();

    /** UTF-8 character set */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    /** Default connect timeout (milliseconds) */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    /** Default read timeout (milliseconds) */
    private static final int DEFAULT_READ_TIMEOUT = 30000;

    /** SSL initialized */
    private static boolean sslInitialized = false;

    /** Nxt epoch beginning */
    private static long epochBeginning;

    /** Nxt server address */
    private static String serverAddress;

    /** Nxt server port */
    private static int serverPort;

    /** Use SSL connections */
    private static boolean useSSL;

    private Nxt() {}

    /**
     * Initialize the Nxt API library
     * <p>
     * The library can be re-initialized in order to change the server and port assignment.
     * HTTPS connections will be used if 'useSSL' is TRUE with the exception that connections
     * to 'localhost' will always use HTTP connections.
     *
     * @param   server              Nxt server address
     * @param   port                Nxt server port
     * @param   useSSL              TRUE to use SSL connections
     * @throws  IOException         Unable to initialize the API library
     */
    @SuppressWarnings("unchecked")
    public static void init(String server, int port, boolean useSSL) throws IOException {
        Nxt.serverAddress = server;
        Nxt.serverPort = port;
        Nxt.useSSL = useSSL;
        if (useSSL && !sslInitialized)
            sslInit();
        //
        // Get the server configuration
        //
        Response response = Nxt.getConstants();
        epochBeginning = response.getLong("epochBeginning");
        //
        // Get the chains
        //
        chains.clear();
        response.getObject("chainProperties").getObjectMap().values().forEach(entry -> {
            Response chainProperties = new Response((JSONObject<String, Object>)entry);
            Chain chain = new Chain(chainProperties.getString("name"),
                                    chainProperties.getInt("id"),
                                    chainProperties.getInt("decimals"));
            chains.put(chain.getId(), chain);
        });
        //
        // Get the transaction types
        //
        transactionTypes.clear();
        response.getObject("transactionTypes").getObjectMap().entrySet().forEach(entry -> {
            int type = Integer.valueOf(entry.getKey());
            Map<String, Object> subtypes =
                    (Map<String, Object>)((Map<String, Object>)entry.getValue()).get("subtypes");
            Set<Map.Entry<String, Object>> subtypeSet = subtypes.entrySet();
            Map<Integer, String> transactionSubtypes = new HashMap<>();
            subtypeSet.forEach(subentry -> {
                int subtype = Integer.valueOf(subentry.getKey());
                String name = (String)((Map<String, Object>)subentry.getValue()).get("name");
                transactionTypes.put((type<<8) | subtype, new TransactionType(type, subtype, name));
            });
        });
        //
        // Get the voting models
        //
        votingModels.clear();
        response.getObject("votingModels").getObjectMap().entrySet().forEach(entry -> {
            votingModels.put(((Long)entry.getValue()).intValue(), entry.getKey());
        });
    }

    /**
     * Get the Nxt epoch
     *
     * @return                      Epoch expressed as milliseconds since Jan 1, 1970
     */
    public static long getEpoch() {
        return epochBeginning;
    }

    /**
     * Get all of the Nxt chains
     *
     * @return                      Chain collection
     */
    public static Collection<Chain> getAllChains() {
        return chains.values();
    }

    /**
     * Get the chain for the supplied chain identifier
     *
     * @param   chainId             Chain identifier
     * @return                      Chain (null if the chain is not defined)
     */
    public static Chain getChain(int chainId) {
        return chains.get(chainId);
    }

    /**
     * Get the chain for the supplied chain name
     *
     * @param   chainName           Chain name
     * @return                      Chain (null if the chain is not defined)
     */
    public static Chain getChain(String chainName) {
        for (Chain chain : chains.values()) {
            if (chain.getName().equals(chainName))
                return chain;
        }
        return null;
    }

    /**
     * Get the transaction type for the specified type and subtype
     *
     * @param   type                Transaction type
     * @param   subtype             Transaction subtype
     * @return                      Transaction type (null if the transaction type is not defined)
     */
    public static TransactionType getTransactionType(int type, int subtype) {
        return transactionTypes.get((type<<8) | subtype);
    }

    /**
     * Get the voting model name for the supplied identifier
     *
     * @param   id                  Voting model identifier
     * @return                      Voting model name or null if the model is not defined
     */
    public static String getVotingModelName(int id) {
        return votingModels.get(id);
    }

    /**
     * Get the voting model identifier for the supplied name
     *
     * @param   name                Voting model name
     * @return                      Voting model identifier or -1 if the model is not defined
     */
    public static int getVotingModelId(String name) {
        for (Map.Entry<Integer, String> entry : votingModels.entrySet()) {
            if (entry.getValue().equals(name))
                return entry.getKey();
        }
        return -1;
    }

    /**
     * Add a peer to the server peer list and connect to the peer
     *
     * @param       announcedAddress        The announced address of the peer
     * @param       adminPassword           Administrator password
     * @return                              Server response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response addPeer(String announcedAddress, String adminPassword) throws IOException {
        return issueRequest("addPeer",
                    String.format("peer=%s&adminPassword=%s",
                            URLEncoder.encode(announcedAddress, "UTF-8"),
                            URLEncoder.encode(adminPassword, "UTF-8")),
                    DEFAULT_READ_TIMEOUT);
    }

    /**
     * Blacklist a peer
     *
     * @param       address                 Peer address
     * @param       adminPassword           Administrator password
     * @return                              Server response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response blacklistPeer(String address, String adminPassword) throws IOException {
        return issueRequest("blacklistPeer",
                    String.format("peer=%s&adminPassword=%s",
                            URLEncoder.encode(address, "UTF-8"),
                            URLEncoder.encode(adminPassword, "UTF-8")),
                    DEFAULT_READ_TIMEOUT);
    }

    /**
     * Sign and broadcast a transaction
     * <p>
     * The transaction is signed locally and the secret phrase is not sent
     * to the Nxt server.
     *
     * @param       transactionBytes        Unsigned transaction bytes
     * @param       secretPhrase            Account secret phrase
     * @return                              Broadcast response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      KeyException            Unable to sign the transaction
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response broadcastTransaction(byte[] transactionBytes, String secretPhrase)
                    throws IOException, KeyException {
        return broadcastTransaction(transactionBytes, null, secretPhrase);
    }

    /**
     * Sign and broadcast a transaction
     * <p>
     * The transaction is signed locally and the secret phrase is not sent
     * to the Nxt server.
     *
     * @param       transactionBytes        Unsigned transaction bytes
     * @param       prunableJSON            Prunable attachment JSON or null
     * @param       secretPhrase            Account secret phrase
     * @return                              Broadcast response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      KeyException            Unable to sign the transaction
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response broadcastTransaction(byte[] transactionBytes,
                    String prunableJSON, String secretPhrase) throws IOException, KeyException {
        byte[] signature = Crypto.sign(transactionBytes, secretPhrase);
        System.arraycopy(signature, 0, transactionBytes, Transaction.SIGNATURE_OFFSET, 64);
        if (prunableJSON != null && prunableJSON.length() > 0) {
            return issueRequest("broadcastTransaction",
                    String.format("transactionBytes=%s&prunableAttachmentJSON=%s",
                            Utils.toHexString(transactionBytes),
                            URLEncoder.encode(prunableJSON, "UTF-8")),
                    DEFAULT_READ_TIMEOUT);
        } else {
            return issueRequest("broadcastTransaction",
                    "transactionBytes=" + Utils.toHexString(transactionBytes),
                    DEFAULT_READ_TIMEOUT);
        }
    }

    /**
     * Create a currency mint transaction and return the unsigned transaction
     *
     * @param       currencyId              Currency identifier
     * @param       chain                   Chain
     * @param       nonce                   Minting nonce
     * @param       units                   Units minted
     * @param       counter                 Minting counter
     * @param       fee                     Transaction fee
     * @param       publicKey               Sender public key
     * @return                              Transaction
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response currencyMint(long currencyId, Chain chain, long nonce, long units,
                    long counter, long fee, byte[] publicKey) throws IOException {
        return issueRequest("currencyMint",
                String.format("currency=%s&chain=%s&nonce=%d&units=%d&counter=%d&"
                                + "feeNQT=%s&publicKey=%s&deadline=30&broadcast=false",
                        Utils.idToString(currencyId), chain.getName(),
                        nonce, units, counter, Long.toUnsignedString(fee),
                        Utils.toHexString(publicKey)),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Register wait events
     *
     * An existing event list can be modified by specifying 'addEvents=true' or 'removeEvents=true'.
     * A new event list will be created if both parameters are false.  An existing event listener
     * will be canceled if all of the registered events are removed.
     *
     * @param       events                  List of events to register
     * @param       token                   Event registration token or 0
     * @param       addEvents               TRUE to add events to an existing event list
     * @param       removeEvents            TRUE to remove events from an existing event list
     * @return                              Event registration response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response eventRegister(List<String> events, long token,
                    boolean addEvents, boolean removeEvents) throws IOException {
        StringBuilder sb = new StringBuilder(1000);
        for (String event : events) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append("event=").append(URLEncoder.encode(event, "UTF-8"));
        }
        if (token != 0) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append("token=").append(Long.toString(token));
        }
        if (addEvents) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append("add=true");
        }
        if (removeEvents) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append("remove=true");
        }
        return issueRequest("eventRegister", (sb.length()>0 ? sb.toString() : null), DEFAULT_READ_TIMEOUT);
    }

    /**
     * Wait for an event
     *
     * @param       token                   Event registration token
     * @param       timeout                 Wait timeout (seconds)
     * @return                              Event list
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<Event> eventWait(long token, int timeout) throws IOException {
        List<Event> events = new ArrayList<>();
        Response response = issueRequest("eventWait",
                String.format("token=%d&timeout=%d", token, timeout),
                (timeout+5)*1000);
        List<Response> eventList = response.getObjectList("events");
        eventList.forEach(resp -> events.add(new Event(resp)));
        return events;
    }

    /**
     * Create a transaction to exchange coins and return the unsigned transaction
     *
     * @param       chain                   Chain
     * @param       exchangeChain           Exchange chain
     * @param       amount                  Exchange amount
     * @param       price                   Exchange price
     * @param       fee                     Transaction fee
     * @param       rate                    Bundler rate
     * @param       publicKey               Sender public key
     * @return                              Unsigned transaction
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response exchangeCoins(Chain chain, Chain exchangeChain, long amount, long price,
                    long fee, long rate, byte[] publicKey) throws IOException {
        return issueRequest("exchangeCoins",
                String.format("chain=%s&exchange=%s&amountNQT=%s&priceNQT=%s&feeNQT=%s&"
                            + "feeRateNQTPerFXT=%s&publicKey=%s&deadline=30&broadcast=false",
                        chain.getName(), exchangeChain.getName(),
                        Long.toUnsignedString(amount), Long.toUnsignedString(price),
                        Long.toUnsignedString(fee), Long.toUnsignedString(rate),
                        Utils.toHexString(publicKey)),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get an account
     *
     * @param       accountId               Account identifier
     * @return                              Account information
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getAccount(long accountId) throws IOException {
        return issueRequest("getAccount",
                String.format("account=%s", Utils.idToString(accountId)),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get the account balance
     *
     * @param       accountId               Account identifier
     * @param       chain                   Chain
     * @return                              Account balance
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getBalance(long accountId, Chain chain) throws IOException {
        return issueRequest("getBalance",
                String.format("account=%s&chain=%s",
                        Utils.idToString(accountId), chain.getName()),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get a block
     *
     * @param       blockId                 Block identifier
     * @param       includeTransactions     TRUE to include the block transactions or
     *                                      FALSE to include just the transaction identifiers
     * @return                              Block response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getBlock(String blockId, boolean includeTransactions) throws IOException {
        return issueRequest("getBlock",
                String.format("block=%s&includeTransactions=%s", blockId, includeTransactions),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get a list of blocks
     *
     * @param       firstIndex              Start index (chain head is index 0)
     * @param       lastIndex               Stop index
     * @param       includeTransactions     TRUE to include the block transactions or
     *                                      FALSE to include just the transaction identifiers
     * @return                              Block list
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<Response> getBlocks(int firstIndex, int lastIndex, boolean includeTransactions)
                                            throws IOException {
        if (firstIndex < 0 || lastIndex < firstIndex)
            throw new IllegalArgumentException("Illegal index values");
        Response response = issueRequest("getBlocks",
                String.format("firstIndex=%d&lastIndex=%d&includeTransactions=%s",
                        firstIndex, lastIndex, includeTransactions),
                DEFAULT_READ_TIMEOUT);
        return response.getObjectList("blocks");
    }

    /**
     * Get the blockchain status
     *
     * @return                              Blockchain status
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getBlockchainStatus() throws IOException {
        return issueRequest("getBlockchainStatus", null, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get the blockchain transactions for an account
     *
     * @param       accountId               Account identifier
     * @param       chain                   Chain
     * @param       firstIndex              Index of first transaction to return
     * @param       lastIndex               Index of last transaction to return
     * @return                              Account transaction list
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<Response> getBlockchainTransactions(long accountId, Chain chain,
                                            int firstIndex, int lastIndex) throws IOException {
        Response response = issueRequest("getBlockchainTransactions",
                String.format("account=%s&chain=%s&firstIndex=%d&lastIndex=%d",
                        Utils.idToString(accountId), chain.getName(),
                firstIndex, lastIndex),
                DEFAULT_READ_TIMEOUT);
        return response.getObjectList("transactions");
    }

    /**
     * Get the bundler rates
     *
     * @return                              Bundler rates
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getBundlerRates() throws IOException {
        return issueRequest("getBundlerRates", null, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get the server bundler status
     *
     * @param       adminPassword           Administrator password
     * @return                              List of bundlers
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<Response> getBundlers(String adminPassword) throws IOException {
        Response response = issueRequest("getBundlers",
                String.format("adminPassword=%s", URLEncoder.encode(adminPassword, "UTF-8")),
                            DEFAULT_READ_TIMEOUT);
        return response.getObjectList("bundlers");
    }

    /**
     * Get coin exchange orders
     *
     * @param       chain                   Exchange orders for this chain
     * @return                              Exchange order list
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<Response> getCoinExchangeOrders(Chain chain) throws IOException {
        Response response = issueRequest("getCoinExchangeOrders",
                String.format("exchange=%s", chain.getName()),
                DEFAULT_READ_TIMEOUT);
        return response.getObjectList("orders");
    }

    /**
     * Get the server constants
     *
     * @return                              Server constants
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getConstants() throws IOException {
        return issueRequest("getConstants", null, DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get a currency
     *
     * @param       code                    Currency code
     * @param       chain                   Chain
     * @return                              Currency response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getCurrency(String code, Chain chain) throws IOException {
        return issueRequest("getCurrency",
                String.format("code=%s&chain=%s", code, chain.getName()),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get the server forging status
     *
     * @param       adminPassword           Administrator password
     * @return                              List of generators
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<Response> getForging(String adminPassword) throws IOException {
        Response response = issueRequest("getForging",
                String.format("adminPassword=%s", URLEncoder.encode(adminPassword, "UTF-8")),
                DEFAULT_READ_TIMEOUT);
        return response.getObjectList("generators");
    }

    /**
     * Get the server log
     *
     * @param       count                   Number of records to get
     * @param       adminPassword           Administrator password
     * @return                              Log messages
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<String> getLog(int count, String adminPassword) throws IOException {
        Response response = issueRequest("getLog",
                String.format("count=%d&adminPassword=%s",
                        count, URLEncoder.encode(adminPassword, "UTF-8")),
                DEFAULT_READ_TIMEOUT);
        return response.getStringList("messages");
    }

    /**
     * Get the minting target
     *
     * @param       currencyId              Currency identifier
     * @param       accountId               Account identifier
     * @param       units                   Units to be minted
     * @return                              Target response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getMintingTarget(long currencyId, long accountId, long units) throws IOException {
        return issueRequest("getMintingTarget",
                String.format("currency=%s&account=%s&units=%d",
                        Utils.idToString(currencyId), Utils.idToString(accountId), units),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get a peer
     *
     * @param       networkAddress          The network address of the peer
     * @return                              Peer
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getPeer(String networkAddress) throws IOException {
        return issueRequest("getPeer",
                "peer=" + URLEncoder.encode(networkAddress, "UTF-8"),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get the current peer information
     *
     * @param       state                   Return peers in this state
     * @return                              Peer list
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<Response> getPeers(String state) throws IOException {
        Response response = issueRequest("getPeers",
                String.format("state=%s&includePeerInfo=true", state),
                DEFAULT_READ_TIMEOUT);
        return response.getObjectList("peers");
    }

    /**
     * Get a transaction
     *
     * @param       fullHash                Transaction full hash
     * @param       chain                   Transaction chain
     * @return                              Transaction
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response getTransaction(byte[] fullHash, Chain chain) throws IOException {
        return issueRequest("getTransaction",
                String.format("fullHash=%s&chain=%s", Utils.toHexString(fullHash), chain.getName()),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Get the unconfirmed transactions for an account
     *
     * @param       accountId               Account identifier
     * @param       chain                   Transaction chain
     * @return                              Transaction list
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static List<Response> getUnconfirmedTransactions(long accountId, Chain chain) throws IOException {
        Response response = issueRequest("getUnconfirmedTransactions",
                String.format("account=%s&chain=%s", Utils.idToString(accountId), chain.getName()),
                DEFAULT_READ_TIMEOUT);
        return response.getObjectList("unconfirmedTransactions");
    }

    /**
     * Create a transaction to send money and return the unsigned transaction
     *
     * @param       recipientId             Recipient account identifier
     * @param       chain                   Transaction chain
     * @param       amount                  Amount to send
     * @param       fee                     Transaction fee (0 to use exchange rate)
     * @param       exchangeRate            Exchange rate (ignored if fee is non-zero)
     * @param       publicKey               Sender public key
     * @param       message                 Text message or null if no message
     * @return                              Transaction
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response sendMoney(long recipientId, Chain chain, long amount, long fee,
                                            long exchangeRate, byte[] publicKey, String message)
                                            throws IOException {
        if (message != null && message.length() > 0) {
            return issueRequest("sendMoney",
                String.format("recipient=%s&chain=%s&amountNQT=%s&feeNQT=%s&feeRateNQTPerFXT=%s&"
                                + "publicKey=%s&message=%s&messageIsText=true&messageIsPrunable=true&"
                                + "deadline=30&broadcast=false",
                        Utils.idToString(recipientId), chain.getName(),
                                Long.toUnsignedString(amount), Long.toUnsignedString(fee),
                                Long.toUnsignedString(exchangeRate), Utils.toHexString(publicKey),
                                message),
                DEFAULT_READ_TIMEOUT);
        } else {
            return issueRequest("sendMoney",
                String.format("recipient=%s&chain=%s&amountNQT=%s&feeNQT=%s&feeRateNQTPerFXT=%s&"
                                + "publicKey=%s&deadline=30&broadcast=false",
                        Utils.idToString(recipientId), chain.getName(),
                                Long.toUnsignedString(amount), Long.toUnsignedString(fee),
                                Long.toUnsignedString(exchangeRate), Utils.toHexString(publicKey)),
                DEFAULT_READ_TIMEOUT);
        }
    }

    /**
     * Set server logging
     *
     * @param       logLevel                Log level
     * @param       adminPassword           Administrator password
     * @return                              Server response
     * @throws      IOException             Unable to set server logging
     * @throws      NxtException            Nxt server returned an error
     */
    public static Response setLogging(String logLevel, String adminPassword) throws IOException {
        return issueRequest("setLogging",
                String.format("logLevel=%s&adminPassword=%s",
                        logLevel, URLEncoder.encode(adminPassword, "UTF-8")),
                DEFAULT_READ_TIMEOUT);
    }

    /**
     * Issue the Nxt API request and return the parsed JSON response
     * <p>
     * Applications can issue API requests directly instead of using one of the
     * helper routines.  The application is responsible for correctly formatting
     * the request parameters.
     *
     * @param       requestType             Request type
     * @param       requestParams           Request parameters
     * @param       readTimeout             Read timeout (milliseconds)
     * @return                              Parsed JSON response
     * @throws      IOException             Unable to issue Nxt API request
     * @throws      NxtException            Nxt server returned an error
     */
    @SuppressWarnings("unchecked")
    public static Response issueRequest(String requestType, String requestParams, int readTimeout)
                                            throws IOException {
        Response response = null;
        try {
            URL url = new URL(String.format("%s://%s:%d/nxt",
                    (serverAddress.equals("localhost") ? "http" : (useSSL ? "https" : "http")),
                    serverAddress, serverPort));
            String request;
            if (requestParams != null)
                request = String.format("requestType=%s&%s", requestType, requestParams);
            else
                request = String.format("requestType=%s", requestType);
            byte[] requestBytes = request.getBytes(UTF8);
            //
            // Issue the request
            //
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Cache-Control", "no-cache, no-store");
            conn.setRequestProperty("Content-Length", Integer.toString(requestBytes.length));
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
            conn.setReadTimeout(readTimeout);
            conn.connect();
            try (OutputStream out = conn.getOutputStream()) {
                out.write(requestBytes);
                out.flush();
                int code = conn.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    String errorText = String.format("Response code %d for %s request\n  %s",
                                                     code, requestType, conn.getResponseMessage());
                    throw new IOException(errorText);
                }
            }
            //
            // Parse the response
            //
            String contentEncoding = conn.getHeaderField("Content-Encoding");
            try (InputStream in = conn.getInputStream()) {
                InputStreamReader reader;
                if ("gzip".equals(contentEncoding))
                    reader = new InputStreamReader(new GZIPInputStream(in), UTF8);
                else
                    reader = new InputStreamReader(in, UTF8);
                Object respObject = JSONParser.parse(reader);
                if (!(respObject instanceof JSONObject))
                    throw new IOException("Server response is not a JSON object");
                response = new Response((JSONObject<String, Object>)respObject);
                Long errorCode = (Long)response.get("errorCode");
                if (errorCode != null) {
                    String errorDesc = (String)response.get("errorDescription");
                    String errorText = String.format("Error %d returned for %s request: %s",
                                                     errorCode, requestType, errorDesc);
                    throw new NxtException(errorText, requestType, errorCode.intValue(), errorDesc);
                }
            }
        } catch (ParseException exc) {
            String errorText = String.format("JSON parse exception for %s request: Position %d: %s",
                                             requestType, exc.getErrorOffset(), exc.getMessage());
            throw new IOException(errorText);
        } catch (NxtException exc) {
            throw exc;
        } catch (IOException exc) {
            String errorText = String.format("I/O error on %s request", requestType);
            throw new IOException(errorText, exc);
        }
        return response;
    }

    /**
     * SSL initialization
     */
    private static void sslInit() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            sslInitialized = true;
        } catch (NoSuchAlgorithmException exc) {
            throw new IllegalStateException("TLSv1 algorithm is not available", exc);
        } catch (KeyManagementException exc) {
            throw new IllegalStateException("Unable to initialize SSL context", exc);
        }
    }
}
