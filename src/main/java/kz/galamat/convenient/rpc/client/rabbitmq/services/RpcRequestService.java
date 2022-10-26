package kz.galamat.convenient.rpc.client.rabbitmq.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.galamat.convenient.rpc.rabbitmq.settings.RpcProperties;
import kz.galamat.i.convenient.rpc.dtos.RpcErrorResponse;
import kz.galamat.i.convenient.rpc.dtos.RpcRequest;
import kz.galamat.i.convenient.rpc.exceptions.RpcResponseException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by Yersin Mukay on 21.10.2022
 */
@AllArgsConstructor
public class RpcRequestService {

    private final RabbitTemplate rabbitTemplate;
    private final RpcProperties rpcProperties;

    @SneakyThrows
    public <T> T request(String sendToQueue, RpcRequest rpcRequest, Class<T> responseType) {
        final ObjectMapper objectMapper = new ObjectMapper();
        // Create a message subject
        final Message newMessage = MessageBuilder.withBody(objectMapper.writeValueAsBytes(rpcRequest)).build();
        // The customer sends a message
        final Message result = rabbitTemplate.sendAndReceive(rpcProperties.getExchange(), sendToQueue, newMessage);
        if (result != null) {
            RpcErrorResponse errorResponseDto = null;
            try {
                errorResponseDto = objectMapper.readValue(result.getBody(), RpcErrorResponse.class);
            }
            catch (Exception ignored) {
            }
            if (errorResponseDto != null) {
                throw new RpcResponseException(errorResponseDto.getError(), errorResponseDto.getStatus(),
                        errorResponseDto.getMessage());
            }

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(result.getBody(), responseType);
        }
        throw new RpcResponseException(408, "Request Timeout");
    }

}
