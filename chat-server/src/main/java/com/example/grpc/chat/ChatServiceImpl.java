package com.example.grpc.chat;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;

// This class implements the ChatService defined in the .proto file.
public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    // A list to keep track of all connected client observers.
    // This allows us to broadcast messages to all connected clients.
    private static final List<StreamObserver<ChatMessage>> observers = new ArrayList<>();

    // This method handles the bidirectional streaming for the chat RPC.
    // It is called once for each client connection.
    @Override
    public StreamObserver<ChatMessage> chat(final StreamObserver<ChatMessage> responseObserver) {
        // Add the new client's observer to our list.
        observers.add(responseObserver);

        // Return a new StreamObserver to handle incoming messages from the client.
        return new StreamObserver<ChatMessage>() {
            @Override
            public void onNext(ChatMessage message) {
                // This method is called when a new message is received from the client.
                System.out.println("Received message from client: " + message.getContent());

                // Broadcast the received message to all connected observers (clients).
                for (StreamObserver<ChatMessage> observer : observers) {
                    observer.onNext(ChatMessage.newBuilder()
                                               .setContent("Server received: " + message.getContent())
                                               .build());
                }
            }

            @Override
            public void onError(Throwable t) {
                // This method is called if an error occurs.
                System.err.println("Error from client: " + t.getMessage());
                observers.remove(responseObserver); // Remove the observer on error.
            }

            @Override
            public void onCompleted() {
                // This method is called when the client has finished sending messages.
                System.out.println("Client completed the stream.");
                observers.remove(responseObserver); // Remove the observer when the stream is completed.
                responseObserver.onCompleted(); // Complete the server's side of the stream.
            }
        };
    }
}
