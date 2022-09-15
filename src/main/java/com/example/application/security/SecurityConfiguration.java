package com.example.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import com.example.application.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    setLoginView(http, LoginView.class);
  }

  /**
   * Allows access to static resources, bypassing Spring security.
   */
  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/images/**");
    super.configure(web);
  }

  /**
   * Demo UserDetailService which only provide two hardcoded in memory users and their roles. NOTE:
   * This should not be used in real world applications.
   */
  @Bean
  public UserDetailsManager userDetailsService() {
    UserDetails homer = User.withUsername("homer").password("{noop}homer").roles("USER").build();
    UserDetails marge = User.withUsername("marge").password("{noop}marge").roles("USER").build();
    return new InMemoryUserDetailsManager(homer, marge);
  }
}
