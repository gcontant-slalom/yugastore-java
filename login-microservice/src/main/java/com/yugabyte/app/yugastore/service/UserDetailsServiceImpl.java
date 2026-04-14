package com.yugabyte.app.yugastore.service;

import com.yugabyte.app.yugastore.model.User;
import com.yugabyte.app.yugastore.repo.UserRepository;
import java.util.Collections;
import java.util.Locale;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsServiceImpl(
    UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsernameOrEmail(username, normalizeEmail(username));
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }

    return new org.springframework.security.core.userdetails.User(resolveLoginIdentifier(user),
      user.getPassword(), Collections.<GrantedAuthority>emptySet());
  }

  private String normalizeEmail(String email) {
    if (email == null) {
      return null;
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String resolveLoginIdentifier(User user) {
    return user.getEmail() != null ? user.getEmail() : user.getUsername();
  }
}
