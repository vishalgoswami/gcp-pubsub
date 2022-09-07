package com.vishal.learn.gcppubsub;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class GcpPubsubApplication {
	private static final Log LOGGER = LogFactory.getLog
			(GcpPubsubApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GcpPubsubApplication.class, args);
	}

	@Bean
	@ServiceActivator(inputChannel = "pubsubOutputChannel")
	public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
		return new PubSubMessageHandler(pubsubTemplate, "test-topic");
	}
	@MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
	public interface PubsubOutboundGateway {

		void sendToPubsub(String text);
	}
	@Bean
	public PubSubInboundChannelAdapter messageChannelAdapter(
			@Qualifier("pubsubInputChannel") MessageChannel inputChannel,
			PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter =
				new PubSubInboundChannelAdapter(pubSubTemplate, "test-subscription");
		adapter.setOutputChannel(inputChannel);
		adapter.setAckMode(AckMode.AUTO_ACK);

		return adapter;
	}
	@Bean
	public MessageChannel pubsubInputChannel() {
		return new DirectChannel();
	}
	@Bean

	@ServiceActivator(inputChannel = "pubsubInputChannel")
	public MessageHandler messageReceiver() {
		return m -> {
			BasicAcknowledgeablePubsubMessage originalMessage =
					m.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);

			String mid = originalMessage.getPubsubMessage().getMessageId();

			LOGGER.info
					("Message arrived! Payload: " +
					m.toString() + "/" +
					m.getHeaders().get(GcpPubSubHeaders.ORDERING_KEY) + "/"+
					mid+"/"+
					new String((byte[]) m.getPayload()));

			originalMessage.ack();
		};



	}
}
