package cosmodrome;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class Administrator extends User {

    private String LogListenQueue;

    Administrator() throws IOException, TimeoutException {

        super();

        this.LogListenQueue = this.channel.queueDeclare().getQueue();
        
        this.channel.queueBind(this.LogListenQueue, CosmodromeExchange, "agencies.#");
    }

    public static void main(String args[]) throws IOException, TimeoutException {

        Administrator admin = new Administrator();

        admin.ListenAndCommunicate();

    }

    private void ListenAndCommunicate() throws IOException {
        
        System.out.println("Listening for logs :) \nYou can send message in format: all/agencies/carriers, message");
        
        ReceiveLogs();
        
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input = br.readLine();

            List<String> complexMessage = Arrays.asList(input.split(","));
            String toWho = complexMessage.get(0);
            String message = complexMessage.get(1);

            SendMessage(message, toWho);
        }
    }



    private void ReceiveLogs() throws IOException {

        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("Log: " + new String(body, "UTF-8"));
            }
        };

        this.channel.basicConsume(this.LogListenQueue, true, adminConsumer);
    }

    private void SendMessage(String message, String toWho) throws IOException {
        if (toWho.equals("agencies"))
            this.channel.basicPublish(agencyExchange, "", null, message.getBytes());
        else if (toWho.equals("carriers"))
            this.channel.basicPublish(carrierExchange, "", null, message.getBytes());
        else if (toWho.equals("all")) {
            this.channel.basicPublish(carrierExchange, "", null, message.getBytes());
            this.channel.basicPublish(agencyExchange, "", null, message.getBytes());
        } else {
            System.err.println("Invalid group!");
            System.exit(1);
        }
    }
}
