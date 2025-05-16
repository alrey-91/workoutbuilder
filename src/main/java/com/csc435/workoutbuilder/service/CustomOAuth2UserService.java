package com.csc435.workoutbuilder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.csc435.workoutbuilder.config.CustomOAuth2User;
import com.csc435.workoutbuilder.model.User;
import com.csc435.workoutbuilder.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepo;
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    public CustomOAuth2UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        
        //create temporary customoauth user to get the email
        CustomOAuth2User tempUser = new CustomOAuth2User(oAuth2User, null);
        String email = tempUser.getEmail();
        String provider = request.getClientRegistration().getRegistrationId();
    
        String username = email;

        //load or create user in db
        User user = userRepo.findByUsername(username).orElseGet(() -> {
            User newUser = new User(username, null);
            newUser.setPassword(""); //prevent login via form
            newUser.setProvider(provider);
            User saved = userRepo.save(newUser);
            logger.info("New OAuth user saved to database - UserId: {}, provider: {}", saved.getId(), saved.getProvider());
            return saved;
        });
        //create final oauth2 user
        return new CustomOAuth2User(oAuth2User, user);
    }
}