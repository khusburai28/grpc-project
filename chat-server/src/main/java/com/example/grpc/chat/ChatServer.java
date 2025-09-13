package com.example.grpc.chat;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

// This class represents the gRPC chat server.
public class ChatServer {

    // Define the port for the server to listen on.
    private static final int PORT = 50051;
    // Server object instance.
    private Server server;

    // Start the gRPC server.
    private void start() throws IOException {
        // Build and start a new server instance.
        // It binds to the specified port and adds the service implementation.
        server = ServerBuilder.forPort(PORT)
                              .addService(new ChatServiceImpl()) // Add the service implementation.
                              .build()
                              .start(); // Start the server.
        System.out.println("Server started, listening on " + PORT);

        // Add a shutdown hook to gracefully stop the server.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            ChatServer.this.stop(); // Call the stop method on shutdown.
            System.err.println("*** server shut down");
        }));
    }

    // Stop the gRPC server gracefully.
    private void stop() {
        if (server != null) {
            server.shutdown(); // Shut down the server.
        }
    }

    // Block until the server is terminated.
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination(); // Wait for the server to terminate.
        }
    }

    // Main method to run the server.
    public static void main(String[] args) throws IOException, InterruptedException {
        final ChatServer server = new ChatServer(); // Create a new server instance.
        server.start(); // Start the server.
        server.blockUntilShutdown(); // Block and wait for it to be terminated.
    }
}
