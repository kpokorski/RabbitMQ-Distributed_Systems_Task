package cosmodrome;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Carrier extends User {

    private final String service_1;
    private final String service_2;
    
    private String queue1;
    private String queue2;
    
    private String queue1Key;
    private String queue2Key;
    
    private String carrierQueue;

    Carrier() throws IOException, TimeoutException {
        super();
        
        this.channel.basicQos(1);

        System.out.println("Type in two possible ways of carry split with '',''  \ncarrying people - CP\ncarrying cargo - CC\nplacing on orbit - PO");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = br.readLine();

        List<String> complexMessage = Arrays.asList(input.split(","));
        this.service_1 = complexMessage.get(0);
        this.service_2 = complexMessage.get(1);

        ServicesToQueues();

        InitializeQueues();
        InitializeAdminQueue();
    }

    public static void main(String args[]) throws IOException, TimeoutException {

        Carrier carrier = new Carrier();

        carrier.ExecuteServices();

    }

    private void ServicesToQueues() {
        if(this.service_1.equals("CP")) {
            this.queue1 = POCarrierQueue;
            this.queue1Key = CPCarrierQueueKey;
            
        } else if(this.service_1.equals("CC")) {
            this.queue1 = CPCarrierQueue;
            this.queue1Key = CCCarrierQueueKey;
            
        } else if(this.service_1.equals("PO")) {
            this.queue1 = CCCarrierQueue;
            this.queue1Key = POCarrierQueueKey;
        } else {
            System.err.println("Invalid Service!");
            System.exit(1);
        }

        if(this.service_2.equals("CP")) {
            this.queue2 = POCarrierQueue;
            this.queue2Key = CPCarrierQueueKey;
            
        } else if(this.service_2.equals("CC")) {
            this.queue2 = CPCarrierQueue;
            this.queue2Key = CCCarrierQueueKey;
            
        } else if(this.service_2.equals("PO")) {
            this.queue2 = CCCarrierQueue;
            this.queue2Key = POCarrierQueueKey;
            
        } else {
            System.err.println("Invalid Service!");
            System.exit(1);
        }
    }

    private void InitializeQueues() throws IOException {
        this.channel.queueDeclare(this.queue1, false, false, false, null);
        this.channel.queueBind(this.queue1, CosmodromeExchange, this.queue1Key);
        
        this.channel.queueDeclare(this.queue2, false, false, false, null);
        this.channel.queueBind(this.queue2, CosmodromeExchange, this.queue2Key);
    }

    private void InitializeAdminQueue() throws IOException {
        this.carrierQueue = this.channel.queueDeclare().getQueue();
        this.channel.queueBind(this.carrierQueue, carrierExchange, "");
    }

    
    private void ExecuteServices() throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                System.out.println("Received: " + message);
                SendResponse(message);
            }
        };
        System.out.println("Waiting for request");
        this.channel.basicConsume(this.queue1, true, consumer);
        this.channel.basicConsume(this.queue2, true, consumer);

        Consumer adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Message from administrator: " + message);
            }
        };
        this.channel.basicConsume(this.carrierQueue, true, adminConsumer);
    }

    private void SendResponse(String complexMessage) throws IOException {
        List<String> splitMessage = Arrays.asList(complexMessage.split(","));
        String agencyName = splitMessage.get(0);
        String service = splitMessage.get(1);
        String order_ID = splitMessage.get(2);
        String response = order_ID + "," + service + "," + "done";
        String key = agencyPrefix + "." + agencyName;
        this.channel.basicPublish(CosmodromeExchange, key,null, response.getBytes());
        System.out.println("Sent.");
    }

}
