package kz.galamat.convenient.rpc.client.rabbitmq.handlers;

import kz.galamat.convenient.rpc.client.rabbitmq.annotations.RpcClient;
import kz.galamat.convenient.rpc.client.rabbitmq.services.RpcRequestService;
import kz.galamat.i.convenient.rpc.dtos.RpcRequest;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.web.bind.annotation.*;
import org.thepavel.icomponent.handler.MethodHandler;
import org.thepavel.icomponent.metadata.MethodMetadata;
import org.thepavel.icomponent.metadata.ParameterMetadata;

import java.util.*;

/**
 * Created by Yersin Mukay on 18.10.2022
 */
@AllArgsConstructor
public class RpcClientMethodHandler implements MethodHandler {

    private static final String METHOD_ATTR = "method";
    private static final String PATH_ATTR = "path";
    private static final String VALUE_ATTR = "value";
    private final RpcRequestService rpcRequestService;

    @SneakyThrows
    @Override
    public Object handle(Object[] arguments, MethodMetadata methodMetadata) {
        final MergedAnnotation<RequestMapping> requestMapping = methodMetadata.getAnnotations().get(RequestMapping.class);
        final List<ParameterMetadata> parametersMetadata = methodMetadata.getParametersMetadata();
        final Map<String, List<String>> queryParams = new HashMap<>();
        final Map<String, List<String>> headers = new HashMap<>();
        final Map<String, String> pathVariables = new HashMap<>();
        Object requestBody = null;
        for (int i = 0; i < parametersMetadata.size(); i++) {
            final ParameterMetadata parameterMetadata = parametersMetadata.get(i);
            final MergedAnnotations parameterAnnotations = parameterMetadata.getAnnotations();
            final Object parameterValue = arguments[i];

            if (parameterAnnotations.isPresent(RequestBody.class)) {
                requestBody = parameterValue;
            } else if (parameterAnnotations.isPresent(RequestParam.class)) {
                final String parameterName = parameterAnnotations.get(RequestParam.class).getString(VALUE_ATTR);
                queryParams.putIfAbsent(parameterName, new ArrayList<>());
                final var listOfValues = queryParams.get(parameterName);
                listOfValues.add(parameterValue.toString());
            } else if (parameterAnnotations.isPresent(RequestHeader.class)) {
                final String parameterName = parameterAnnotations.get(RequestHeader.class).getString(VALUE_ATTR);
                headers.putIfAbsent(parameterName, new ArrayList<>());
                final var listOfValues = headers.get(parameterName);
                listOfValues.add(parameterValue.toString());
            } else if (parameterAnnotations.isPresent(PathVariable.class)) {
                final String parameterName = parameterAnnotations.get(PathVariable.class).getString(VALUE_ATTR);
                pathVariables.put("{" + parameterName + "}", parameterValue.toString());
            }
          }
        final String pathPattern = requestMapping.getStringArray(PATH_ATTR)[0];
        final var requestMethod = requestMapping.getEnumArray(METHOD_ATTR, RequestMethod.class)[0];

        final var rpcRequest = RpcRequest.builder()
                .path(buildPath(pathPattern, pathVariables))
                .method(requestMethod.name())
                .headers(headers)
                .queryParams(queryParams)
                .body(requestBody)
                .build();

        final String queueName = getQueueName(methodMetadata);
        return rpcRequestService.request(queueName, rpcRequest, methodMetadata.getSourceMethod().getReturnType());
    }

    private String buildPath(String pathPattern, Map<String, String> pathVariables) {
        final String[] pathTokens = pathPattern.split("/");
        final StringBuffer pathBuffer = new StringBuffer();
        for (var token : pathTokens) {
            if (token.isEmpty()) continue;
            pathBuffer.append("/");
            pathBuffer.append(pathVariables.getOrDefault(token, token));
        }
        return pathBuffer.toString();
    }

    private String getQueueName(MethodMetadata methodMetadata) {
        return methodMetadata.getSourceClassMetadata()
                .getSourceClass().getAnnotation(RpcClient.class).value();
    }

}
