package cosmodrome;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public abstract class User {


    User() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        this.channel = connection.createChannel();

        this.channel.exchangeDeclare(CosmodromeExchange, BuiltinExchangeType.TOPIC);

        this.channel.exchangeDeclare(agencyExchange, BuiltinExchangeType.FANOUT);
        this.channel.exchangeDeclare(carrierExchange, BuiltinExchangeType.FANOUT);
    }



    protected Channel channel;
    protected final static String CosmodromeExchange = "COSMODROME_EXCHANGE";
    protected final static String carrierExchange = "CARRIER_EXCHANGE";
    protected final static String agencyExchange = "AGENCY_EXCHANGE";

    protected final static String AgencyKey = "agencies";
    protected final static String carrierKey = "carriers";

    protected final static String agencyPrefix = AgencyKey + ".agency";

    protected final static String POCarrierQueueKey = AgencyKey + "." + carrierKey + ".PO";
    protected final static String CCCarrierQueueKey = AgencyKey + "." + carrierKey + ".CC";
    protected final static String CPCarrierQueueKey = AgencyKey + "." + carrierKey + ".CP";

    protected final static String POCarrierQueue = "CARRIER_PO";
    protected final static String CCCarrierQueue = "CARRIER_CC";
    protected final static String CPCarrierQueue = "CARRIER_PC";
}
