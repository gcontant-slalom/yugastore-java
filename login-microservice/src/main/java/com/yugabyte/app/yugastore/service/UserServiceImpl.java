package com.yugabyte.app.yugastore.service;

import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(UserRepository userRepository,
    PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void save(User user) {
    String normalizedEmail = normalizeEmail(user.getEmail());
    user.setEmail(normalizedEmail);
    if (user.getUsername() == null || user.getUsername().isBlank()) {
      user.setUsername(normalizedEmail);
    }
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.save(user);
  }

  @Override
  public User findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  @Override
  public User findByEmail(String email) {
    return userRepository.findByEmail(normalizeEmail(email));
  }

  private String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
