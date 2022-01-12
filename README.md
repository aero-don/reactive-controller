# ReactiveController 

## Reactive Controller is a Micronaut application with tests to test why the declarative HTTP client times out on a call to a controller that returns a Flux when the producer is publishing data to the reactive stream faster than the value set for micronaut.http.client.read-idle-timeout.

### Tests
For all tests micronaut.http.client.read-idle-timeout is set to 5 seconds to reduce the amount of time to run the tests, and be long enough to demonstrate proper behavior.

#### ReactiveControllerSpec
ReactiveControllerSpec.'test reactive read' tests the ReactiveController using the PublisherJob where data is published to the stream every two seconds for 6 iterations, then emits a complete event.

With Micronaut 3.2.3 version of http-client, this test fails because the WriterIdleTimeoutTask times out.  Note the negative values for nextDelay in the following log statements.

```
10:08:27.205 [default-nioEventLoopGroup-1-5] WARN  i.n.handler.timeout.IdleStateHandler - WriterIdleTimeoutTask.run(): first=true, event=IdleStateEvent(WRITER_IDLE, first), nextDelay=-1679718
10:08:27.206 [default-nioEventLoopGroup-1-3] WARN  i.n.handler.timeout.IdleStateHandler - WriterIdleTimeoutTask.run(): first=true, event=IdleStateEvent(WRITER_IDLE, first), nextDelay=-2063543

```

#### ReactiveControllerTimeoutSpec
1. ReactiveControllerTimeoutSpec.'test reactive read timeout when no data is received' tests that the subscriber to a reactive stream, where the publisher does not publish any data, receive an error event due to a ReadTimeoutException.

With Micronaut 3.2.3 this test passes as expected.

2. ReactiveControllerTimeoutSpec.'test reactive read timeout when some data is received' tests the ReactiveController using the PublisherTimeoutJob where data is published to the stream every two seconds for 6 iterations, then stops publishing data.

With Micronaut 3.2.3 version of http-client, this test fails because the WriterIdleTimeoutTask times out.  Note the negative values for nextDelay in the following log statements.

```
10:18:49.582 [default-nioEventLoopGroup-3-9] WARN  i.n.handler.timeout.IdleStateHandler - WriterIdleTimeoutTask.run(): first=true, event=IdleStateEvent(WRITER_IDLE, first), nextDelay=-1770703
10:18:49.587 [default-nioEventLoopGroup-3-12] WARN  i.n.handler.timeout.IdleStateHandler - WriterIdleTimeoutTask.run(): first=true, event=IdleStateEvent(WRITER_IDLE, first), nextDelay=-805084
```

### Problem and Potential Fix
The problem at or around line 3186 is that the read timeout value is passed as the write timeout and idle timeout values when io.micronaut.http.client.netty.DefaultHttpClient creates the io.netty.handler.timeout.IdleStateHandler.  A potential fix to the problem is to pass -1 as the write timeout value when creating the IdleStateHandler.

This potential fix can be tested by uncommenting the following two lines in build.gradle:

```
    // exclude group: 'io.micronaut', module: 'micronaut-http-client'
...
    // implementation(files("libs/http-client-3.2.4-SNAPSHOT.jar"))
```