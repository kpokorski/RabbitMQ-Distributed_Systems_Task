package cosmodrome;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class Agency extends User {

    private String ResponseQueue;
    private String agencyName;
    private String agencyQueue;

    Agency() throws IOException, TimeoutException {

        super();

        System.out.println("Type in Agency name: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        this.agencyName = br.readLine();

        this.ResponseQueue = channel.queueDeclare().getQueue();
        this.channel.queueBind(ResponseQueue, User.CosmodromeExchange, User.agencyPrefix + "." + this.agencyName);

        this.agencyQueue = this.channel.queueDeclare().getQueue();
        this.channel.queueBind(this.agencyQueue, User.agencyExchange, "");
    }

    public static void main(String args[]) throws IOException, TimeoutException {

        Agency agency = new Agency();

        agency.OrderCarry();

    }

    private void OrderCarry() throws IOException {

        ResponseListener();

        AdminListener();

        System.out.println("What you want to carry?\ncarrying people - CP\ncarrying cargo - CC\nplacing on orbit - PO \nafter ',' add order ID");

        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String message = br.readLine();
            message = this.agencyName + "," + message;
            PlaceOrder(message);

        }
    }



    private void PlaceOrder(String message) throws IOException {
        String service = Arrays.asList(message.split(",")).get(1);

        String key = User.AgencyKey + "." + User.carrierKey + "." + service;

        this.channel.basicPublish(User.CosmodromeExchange, key,null, message.getBytes());

        System.out.println("Order sent.");
    }


    private void ResponseListener() throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body);
                System.out.println("Received: " + message);
            }
        };
        channel.basicConsume(this.ResponseQueue, true, consumer);
    }


    private void AdminListener() throws IOException {
        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("Message from administrator: " + new String(body, "UTF-8"));
            }
        };
        this.channel.basicConsume(this.agencyQueue, true, adminConsumer);
    }

}
