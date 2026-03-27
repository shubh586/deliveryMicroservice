package com.foodexpress.auth.security;


import com.foodexpress.auth.model.entity.User;
import com.foodexpress.auth.repository.RefreshTokenRepository;
import com.foodexpress.auth.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService  implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user= userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("user not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword()!=null?user.getPassword() : "",
                user.getEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
    @Transactional(readOnly=true)
    public UserDetails loadUserById(String id){
        User user= userRepository.findById(id)
                .orElseThrow(()->new UsernameNotFoundException("user not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword()!=null?user.getPassword():"",
                user.getEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
