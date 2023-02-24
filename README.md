# Convenient RPC makes writing Java rpc clients easier

Convenient RPC is a Java to async client binder inspired by [OpenFeign](https://github.com/OpenFeign/feign). Convenient's first goal was reducing the complexity of RPC call.

---
### How does Feign work?

Convenient RPC client works by processing annotations into a templatized request. Arguments are applied to these templates in a straightforward fashion before output.  

<img width="1062" alt="image" src="https://user-images.githubusercontent.com/5346020/196175336-a5adb957-99a6-4d1a-85b6-9a24017f396a.png">


## How to use?
### Gradle
```
repositories {
    mavenCentral()
    maven {
        url "https://github.com/galamat-kz/mvn-repo/raw/main"
    }
}
...

dependencies {
    ...
    implementation 'kz.galamat:spring-boot-starter-rabbit-rpc-to-request-dispatcher:0.0.4'
    ...
}
```
### application.yml
```
spring:
    rabbit:
        rpc:
            queue: user-service-queue
            append-random-for-reply-queue-name: true
            reply-queue-prefix: user-service-reply-queue
            exchange: rpc-exchange
            replyTimeout: 5000

```

## Spring Cloud Gateway
If you using Spring Cloud Gateway you can easily add this filter and route to the service like this:
### RpcFilter.java
```
@Component
public class RpcFilter implements GlobalFilter {

    private final Logger logger = LoggerFactory.getLogger(RpcFilter.class.getName());

    public static final String ORIGINAL_PATH_HEADER_KEY = "Original-Path";
    public static final String ORIGINAL_METHOD_HEADER_KEY = "Original-Method";
    public static final String SERVICE_NAME_HEADER_KEY = "Service-Name";

    private final ObjectProvider<DispatcherHandler> dispatcherHandlerProvider;

    // do not use this dispatcherHandler directly, use getDispatcherHandler() instead.
    private volatile DispatcherHandler dispatcherHandler;

    public RpcFilter(
            ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        this.dispatcherHandlerProvider = dispatcherHandlerProvider;
    }

    private DispatcherHandler getDispatcherHandler() {
        if (dispatcherHandler == null) {
            dispatcherHandler = dispatcherHandlerProvider.getIfAvailable();
        }

        return dispatcherHandler;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        assert route != null;
        URI routeUri = route.getUri();
        String scheme = routeUri.getScheme();
        if (isAlreadyRouted(exchange) || !"rpc".equals(scheme)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();

        ServerHttpRequest newRequest = request.mutate()
                .path("/rpc")
                .method(HttpMethod.POST)
                .header(ORIGINAL_PATH_HEADER_KEY, request.getPath().value())
                .header(ORIGINAL_METHOD_HEADER_KEY, request.getMethodValue())
                .header(SERVICE_NAME_HEADER_KEY, route.getUri().getHost())
                .build();

        return Mono.just(exchange.mutate()
                        .request(newRequest)
                        .build())
                .flatMap(webExchange -> this.getDispatcherHandler().handle(webExchange));
    }

}

```
### application.yml
```
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated
    publisher-returns: true
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: rpc://user-service
          predicates:
            - Path=/users/**
...
```
### RpcController.java
```
@RestController
@AllArgsConstructor
@RequestMapping("/rpc")
public class RpcController {

    private final RabbitTemplate rabbitTemplate;
    private final RpcConfigStorage rpcConfigStorage;

    @SneakyThrows
    @PostMapping
    public Object post(HttpEntity<String> httpEntity,
                       @RequestHeader(name = ORIGINAL_METHOD_HEADER_KEY) String originalMethod,
                       @RequestHeader(name = ORIGINAL_PATH_HEADER_KEY) String originalPath,
                       @RequestHeader(name = SERVICE_NAME_HEADER_KEY) String serviceName,
                       @RequestParam MultiValueMap<String, String> queryParamsMV) {

        Map<String, String[]> queryParams = queryParamsMV.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toArray(new String[0])
                ));

        ObjectMapper objectMapper = new ObjectMapper();
        var dto = RpcRequestDto.builder()
                .serviceName(serviceName)
                .path(originalPath)
                .method(originalMethod)
                .queryParams(queryParams)
                .build();
        if (httpEntity.hasBody()) {
            dto.setBody(objectMapper.readValue(httpEntity.getBody(), Object.class));
        }
        // Create a message subject
        Message newMessage = MessageBuilder.withBody(objectMapper.writeValueAsBytes(dto)).build();
        // The customer sends a message
        Message result = rabbitTemplate.sendAndReceive(rpcConfigStorage.getRpcExchangeName(),
                RpcUtil.getServiceQueue(serviceName), newMessage);
        if (result != null) {
            try {
                RpcErrorResponseDto errorResponseDto = objectMapper.readValue(result.getBody(), RpcErrorResponseDto.class);
                throw new ResponseStatusException(HttpStatus.valueOf(errorResponseDto.getStatus()),
                        errorResponseDto.getMessage());
            } catch (ResponseStatusException e) {
                throw e;
            }
            catch (Exception ignored) {
            }
            return objectMapper.readValue(result.getBody(), Object.class);
        }
        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Request timeout");
    }
    
}

```


## MIT License

Copyright (c) [2022] [Yersin Mukay]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
