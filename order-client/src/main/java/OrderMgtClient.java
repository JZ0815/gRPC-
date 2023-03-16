import com.google.protobuf.StringValue;
import ecommerce.OrderManagementGrpc;
import ecommerce.Ordermanagement;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OrderMgtClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                "localhost", 50053).usePlaintext().build();
        OrderManagementGrpc.OrderManagementBlockingStub stub = OrderManagementGrpc.newBlockingStub(channel);
        OrderManagementGrpc.OrderManagementStub asyncStub = OrderManagementGrpc.newStub(channel);
        // Add Order
        if (args.length > 0 && args[0] == "0") {
            addOrder(stub);
        } else {
            searchOrder(stub);
        }

    }

    private static void searchOrder(OrderManagementGrpc.OrderManagementBlockingStub stub) {

        Ordermanagement.Order order = Ordermanagement.Order
                .newBuilder()
                .setId("101")
                .addItems("iPhone XS").addItems("Mac Book Pro")
                .setDestination("San Jose, CA")
                .setPrice(2300)
                .build();

        StringValue searchStr = StringValue.newBuilder().setValue("Google").build();
        Iterator<Ordermanagement.Order> matchingOrdersItr;
        matchingOrdersItr = stub.withDeadline(Deadline.after(2, TimeUnit.SECONDS)).searchOrders(searchStr);
        while (matchingOrdersItr.hasNext()) {
            Ordermanagement.Order matchingOrder = matchingOrdersItr.next();
            System.out.println("Search Order Response -> Matching Order - " + matchingOrder.getId());
            System.out.println(" Order : " + order.getId() + "\n "
                    + matchingOrder.toString());
        }
    }

    private static void addOrder(OrderManagementGrpc.OrderManagementBlockingStub stub) {
        Ordermanagement.Order order = Ordermanagement.Order
                .newBuilder()
                .setId("101")
                .addItems("iPhone XS").addItems("Mac Book Pro")
                .setDestination("San Jose, CA")
                .setPrice(2300)
                .build();

        StringValue result = stub.withDeadline(Deadline.after(2, TimeUnit.SECONDS)).addOrder(order);
        System.out.println("AddOrder Response -> : " + result.getValue());
    }
    private static void invokeOrderUpdate(OrderManagementGrpc.OrderManagementStub asyncStub) {

        Ordermanagement.Order updOrder1 = Ordermanagement.Order.newBuilder()
                .setId("102")
                .addItems("Google Pixel 3A").addItems("Google Pixel Book")
                .setDestination("Mountain View, CA")
                .setPrice(1100)
                .build();
        Ordermanagement.Order updOrder2 = Ordermanagement.Order.newBuilder()
                .setId("103")
                .addItems("Apple Watch S4").addItems("Mac Book Pro").addItems("iPad Pro")
                .setDestination("San Jose, CA")
                .setPrice(2800)
                .build();
        Ordermanagement.Order updOrder3 = Ordermanagement.Order.newBuilder()
                .setId("104")
                .addItems("Google Home Mini").addItems("Google Nest Hub").addItems("iPad Mini")
                .setDestination("Mountain View, CA")
                .setPrice(2200)
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<StringValue> updateOrderResponseObserver = new StreamObserver<StringValue>() {
            @Override
            public void onNext(StringValue value) {
                System.out.println("Update Orders Res : " + value.getValue());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Update orders response  completed!");
                finishLatch.countDown();
            }
        };

        StreamObserver<Ordermanagement.Order> updateOrderRequestObserver = asyncStub.updateOrders(updateOrderResponseObserver);
        updateOrderRequestObserver.onNext(updOrder1);
        updateOrderRequestObserver.onNext(updOrder2);
        updateOrderRequestObserver.onNext(updOrder3);
        updateOrderRequestObserver.onNext(updOrder3);


        if (finishLatch.getCount() == 0) {
            System.out.println("RPC completed or errored before we finished sending.");
            return;
        }
        updateOrderRequestObserver.onCompleted();

        // Receiving happens asynchronously

        try {
            if (!finishLatch.await(10, TimeUnit.SECONDS)) {
                System.out.println("FAILED : Process orders cannot finish within 10 seconds");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}

