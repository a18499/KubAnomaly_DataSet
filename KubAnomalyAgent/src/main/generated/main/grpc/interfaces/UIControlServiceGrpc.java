package interfaces;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: DAVIDAgentUIService.proto")
public final class UIControlServiceGrpc {

  private UIControlServiceGrpc() {}

  public static final String SERVICE_NAME = "UIControlService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      interfaces.BooleanStrut> METHOD_START =
      io.grpc.MethodDescriptor.<com.google.protobuf.Empty, interfaces.BooleanStrut>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "UIControlService", "start"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              interfaces.BooleanStrut.getDefaultInstance()))
          .setSchemaDescriptor(new UIControlServiceMethodDescriptorSupplier("start"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      interfaces.BooleanStrut> METHOD_STOP =
      io.grpc.MethodDescriptor.<com.google.protobuf.Empty, interfaces.BooleanStrut>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "UIControlService", "stop"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              interfaces.BooleanStrut.getDefaultInstance()))
          .setSchemaDescriptor(new UIControlServiceMethodDescriptorSupplier("stop"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      interfaces.BooleanStrut> METHOD_GET_STATUS =
      io.grpc.MethodDescriptor.<com.google.protobuf.Empty, interfaces.BooleanStrut>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "UIControlService", "getStatus"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              interfaces.BooleanStrut.getDefaultInstance()))
          .setSchemaDescriptor(new UIControlServiceMethodDescriptorSupplier("getStatus"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<interfaces.StringStruct,
      interfaces.StringStruct> METHOD_SET_HOST_IP =
      io.grpc.MethodDescriptor.<interfaces.StringStruct, interfaces.StringStruct>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "UIControlService", "setHostIP"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              interfaces.StringStruct.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              interfaces.StringStruct.getDefaultInstance()))
          .setSchemaDescriptor(new UIControlServiceMethodDescriptorSupplier("setHostIP"))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UIControlServiceStub newStub(io.grpc.Channel channel) {
    return new UIControlServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UIControlServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new UIControlServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UIControlServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new UIControlServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class UIControlServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void start(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<interfaces.BooleanStrut> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_START, responseObserver);
    }

    /**
     */
    public void stop(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<interfaces.BooleanStrut> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_STOP, responseObserver);
    }

    /**
     */
    public void getStatus(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<interfaces.BooleanStrut> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_STATUS, responseObserver);
    }

    /**
     */
    public void setHostIP(interfaces.StringStruct request,
        io.grpc.stub.StreamObserver<interfaces.StringStruct> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SET_HOST_IP, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_START,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                interfaces.BooleanStrut>(
                  this, METHODID_START)))
          .addMethod(
            METHOD_STOP,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                interfaces.BooleanStrut>(
                  this, METHODID_STOP)))
          .addMethod(
            METHOD_GET_STATUS,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                interfaces.BooleanStrut>(
                  this, METHODID_GET_STATUS)))
          .addMethod(
            METHOD_SET_HOST_IP,
            asyncUnaryCall(
              new MethodHandlers<
                interfaces.StringStruct,
                interfaces.StringStruct>(
                  this, METHODID_SET_HOST_IP)))
          .build();
    }
  }

  /**
   */
  public static final class UIControlServiceStub extends io.grpc.stub.AbstractStub<UIControlServiceStub> {
    private UIControlServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UIControlServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UIControlServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UIControlServiceStub(channel, callOptions);
    }

    /**
     */
    public void start(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<interfaces.BooleanStrut> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_START, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stop(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<interfaces.BooleanStrut> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_STOP, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getStatus(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<interfaces.BooleanStrut> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_STATUS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setHostIP(interfaces.StringStruct request,
        io.grpc.stub.StreamObserver<interfaces.StringStruct> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_HOST_IP, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class UIControlServiceBlockingStub extends io.grpc.stub.AbstractStub<UIControlServiceBlockingStub> {
    private UIControlServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UIControlServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UIControlServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UIControlServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public interfaces.BooleanStrut start(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_START, getCallOptions(), request);
    }

    /**
     */
    public interfaces.BooleanStrut stop(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_STOP, getCallOptions(), request);
    }

    /**
     */
    public interfaces.BooleanStrut getStatus(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_STATUS, getCallOptions(), request);
    }

    /**
     */
    public interfaces.StringStruct setHostIP(interfaces.StringStruct request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SET_HOST_IP, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class UIControlServiceFutureStub extends io.grpc.stub.AbstractStub<UIControlServiceFutureStub> {
    private UIControlServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private UIControlServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UIControlServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new UIControlServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<interfaces.BooleanStrut> start(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_START, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<interfaces.BooleanStrut> stop(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_STOP, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<interfaces.BooleanStrut> getStatus(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_STATUS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<interfaces.StringStruct> setHostIP(
        interfaces.StringStruct request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_HOST_IP, getCallOptions()), request);
    }
  }

  private static final int METHODID_START = 0;
  private static final int METHODID_STOP = 1;
  private static final int METHODID_GET_STATUS = 2;
  private static final int METHODID_SET_HOST_IP = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final UIControlServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(UIControlServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_START:
          serviceImpl.start((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<interfaces.BooleanStrut>) responseObserver);
          break;
        case METHODID_STOP:
          serviceImpl.stop((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<interfaces.BooleanStrut>) responseObserver);
          break;
        case METHODID_GET_STATUS:
          serviceImpl.getStatus((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<interfaces.BooleanStrut>) responseObserver);
          break;
        case METHODID_SET_HOST_IP:
          serviceImpl.setHostIP((interfaces.StringStruct) request,
              (io.grpc.stub.StreamObserver<interfaces.StringStruct>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class UIControlServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UIControlServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return interfaces.DAVIDAgentUIServiceProtos.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UIControlService");
    }
  }

  private static final class UIControlServiceFileDescriptorSupplier
      extends UIControlServiceBaseDescriptorSupplier {
    UIControlServiceFileDescriptorSupplier() {}
  }

  private static final class UIControlServiceMethodDescriptorSupplier
      extends UIControlServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    UIControlServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UIControlServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UIControlServiceFileDescriptorSupplier())
              .addMethod(METHOD_START)
              .addMethod(METHOD_STOP)
              .addMethod(METHOD_GET_STATUS)
              .addMethod(METHOD_SET_HOST_IP)
              .build();
        }
      }
    }
    return result;
  }
}
