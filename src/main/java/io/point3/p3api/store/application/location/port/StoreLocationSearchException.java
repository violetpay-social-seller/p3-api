package io.point3.p3api.store.application.location.port;

public class StoreLocationSearchException extends RuntimeException {

  public enum Type {
    CONFIGURATION,
    UNAVAILABLE
  }

  private final Type type;

  public StoreLocationSearchException(Type type) {
    this.type = type;
  }

  public StoreLocationSearchException(Type type, Throwable cause) {
    super(cause);
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}
