package io.vertx.example.grpc.consumer;

import com.google.protobuf.ByteString;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.streams.WriteStream;
import io.vertx.example.grpc.Messages;
import io.vertx.example.grpc.Messages.PayloadType;
import io.vertx.example.grpc.VertxConsumerServiceGrpcServer;
import io.vertx.grpc.server.GrpcServer;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * @author <a href="mailto:plopes@redhat.com">Paulo Lopes</a>
 */
public class ServerWithStub extends AbstractVerticle {

  public static void main(String[] args) {
    Launcher.executeCommand("run", ServerWithStub.class.getName());
  }

  @Override
  public void start() {

    // The rpc service
    VertxConsumerServiceGrpcServer.ConsumerServiceApi service = new VertxConsumerServiceGrpcServer.ConsumerServiceApi() {
      @Override
      public void streamingOutputCall(Messages.StreamingOutputCallRequest request, WriteStream<Messages.StreamingOutputCallResponse> response) {
        final AtomicInteger counter = new AtomicInteger();
        vertx.setPeriodic(1000L, t -> {
          response.write(Messages.StreamingOutputCallResponse.newBuilder().setPayload(
            Messages.Payload.newBuilder()
              .setTypeValue(PayloadType.COMPRESSABLE.getNumber())
              .setBody(ByteString.copyFrom(
                String.valueOf(counter.incrementAndGet()), StandardCharsets.UTF_8))
          ).build());
        });
      }
    };

    // Create the server
    GrpcServer rpcServer = GrpcServer.server(vertx);

    // Bind the service
    service.bind_streamingOutputCall(rpcServer);

    // start the server
    vertx.createHttpServer().requestHandler(rpcServer).listen(8080)
      .onFailure(cause -> {
        cause.printStackTrace();
      });
  }
}
