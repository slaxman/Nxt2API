Nxt2API
=======

Nxt2API provides support routines for accessing the Nxt2 API.  It communicates with a Nxt2 node using an HTTP/HTTPS connection to the API port.  Your secret phrase is never sent to the API server, so it is safe to use with a remote node.

The Nxt2Mint, Nxt2Monitor and Nxt2Wallet projects provide examples of how to configure and use the Nxt2API library.


Build
=====

I use the Netbeans IDE but any build environment with Maven and the Java compiler available should work.  The documentation is generated from the source code using javadoc.

Here are the steps for a manual build.  You will need to install Maven 3 and Java SE Development Kit 8 if you don't already have them.

  - Create the executable: mvn clean install
  - [Optional] Create the documentation: mvn javadoc:javadoc
  - [Optional] Copy the .jar files from the target directory to wherever you want to store the executables.

  
Tutorial
========

Refer to the Nxt2API Javadoc for a detailed description of the classes and methods.  The Nxt2Mint, Nxt2Monitor and Nxt2Wallet projects provide examples of how to use the various API methods.

Before any Nxt2API function can be used, the library must be initialized.  The Nxt.init() method sets the NRS host name/address (usually "localhost") and the server port (usually 27876).  You can also enable the use of SSL for the server connection.

    Nxt.init(String serverHost, int serverPort, boolean useSSL)     
    
The response for a Nxt2 API request is returned as a Response object which encapsulates the JSON response.  Response provides utility methods to return the JSON data in a format useful to the application.

    Response Nxt.api-name(api-parameters)   

Refer to the documentation for more information on the supported Nxt APIs.

