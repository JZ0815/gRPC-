import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class OrderMgtServer2 {

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50054;
        server = ServerBuilder.forPort(port)
                .addService(new OrderMgtServiceImpl(port))
                .build()
                .start();
        System.out.println("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.out.println("*** shutting down gRPC server since JVM is shutting down");
            OrderMgtServer2.this.stop();
            System.out.println("*** server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final OrderMgtServer2 server = new OrderMgtServer2();
        server.start();
        server.blockUntilShutdown();
    }


}
