# Convenient RPC makes writing Java rpc clients easier

Convenient RPC is a Java to async client binder inspired by [OpenFeign](https://github.com/OpenFeign/feign). Convenient's first goal was reducing the complexity of RPC call.

---
### How does Convenient RPC client work?

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
    implementation 'kz.galamat:convenient-rpc-client-rabbitmq:0.0.12'
    implementation 'kz.galamat:convenient-rpc-interface:0.0.8'
    ...
}
```
### application.yml
```
spring:
  application:
    name: auth-service
  convenient:
    rpc:
      rabbitmq:
        queue: auth-service-queue
        reply-queue-prefix: auth-service-reply-queue
        exchange: rpc-exchange
        replyTimeout: 2000
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    publisher-confirm-type: correlated
    publisher-returns: true

```
### RpcConfiguration.java
```
@ConditionalOnProperty(
        prefix = "app.rpc", name = "enabled", havingValue = "true", matchIfMissing = true
)
@Configuration
@EnableRpcServer
@EnableRpcClient(basePackages = "path.to.base.package")
public class RpcConfiguration {
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
