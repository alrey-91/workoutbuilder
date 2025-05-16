package com.csc435.workoutbuilder.service;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public MyUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        User user = userRepo.findByUsername(username).get();

        if (user == null) {
            throw new UsernameNotFoundException("could not find user");            
        }
        if (user.getProvider() != null) {
            throw new UsernameNotFoundException("OAuth user detected. Must use provider login");
        }

        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
        .password(user.getPassword())
        .roles("USER")
        .authorities("USER")
        .build();
    }
}
