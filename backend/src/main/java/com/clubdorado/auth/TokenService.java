package com.clubdorado.auth;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

  private final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<>();

  public String issue(String username) {
    String token = UUID.randomUUID().toString();
    tokens.put(token, username);
    return token;
  }

  public Optional<String> username(String token) {
    return Optional.ofNullable(tokens.get(token));
  }

  public void revoke(String token) {
    tokens.remove(token);
  }
}
