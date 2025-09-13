package com.example.grpc.chat;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

// This class represents the gRPC chat client.
public class ChatClient2 {

    // Define the host and port of the server.
    private static final String HOST = "localhost";
    private static final int PORT = 50051;
    private final ManagedChannel channel;
    private final ChatServiceGrpc.ChatServiceStub asyncStub;

    // Constructor to set up the client.
    public ChatClient2() {
        // Create a managed channel to connect to the server.
        this.channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                                            .usePlaintext() // Use plaintext for development (no SSL).
                                            .build();
        // Create an asynchronous stub for the ChatService.
        this.asyncStub = ChatServiceGrpc.newStub(channel);
    }

    // Method to send and receive chat messages.
    public void startChat() {
        // Create a StreamObserver to handle responses from the server.
        StreamObserver<ChatMessage> responseObserver = new StreamObserver<ChatMessage>() {
            @Override
            public void onNext(ChatMessage message) {
                // This method is called when a new message is received from the server.
                System.out.println("Received from server: " + message.getContent());
            }

            @Override
            public void onError(Throwable t) {
                // This method is called if an error occurs.
                System.err.println("Error from server: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                // This method is called when the server completes the stream.
                System.out.println("Server completed the stream.");
            }
        };

        // Create a StreamObserver to send messages to the server.
        StreamObserver<ChatMessage> requestObserver = asyncStub.chat(responseObserver);

        // Read user input from the console and send messages to the server.
        System.out.println("Type your messages and press Enter. Type 'exit' to quit.");
        Scanner scanner = new Scanner(System.in);
        try {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                // Build a new chat message and send it to the server.
                requestObserver.onNext(ChatMessage.newBuilder().setContent(input).build());
            }
        } finally {
            // Once finished, complete the request stream.
            requestObserver.onCompleted();
            scanner.close(); // Close the scanner.
        }

        // Wait for the server to complete its stream (or for a timeout).
        try {
            channel.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Client channel interrupted.");
        }
    }

    // Main method to run the client.
    public static void main(String[] args) {
        final ChatClient2 client = new ChatClient2(); // Create a new client instance.
        client.startChat(); // Start the chat session.
    }
}
