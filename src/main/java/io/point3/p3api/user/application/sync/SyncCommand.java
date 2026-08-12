package io.point3.p3api.user.application.sync;

public record SyncCommand(String cognitoSub, String email, String name) {

  public static SyncCommand of(String cognitoSub, String email, String name) {
    return new SyncCommand(cognitoSub, email, name);
  }
}
