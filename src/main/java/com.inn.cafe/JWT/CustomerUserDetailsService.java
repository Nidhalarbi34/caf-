package com.inn.cafe.JWT;

import com.inn.cafe.dao.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User ;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Inside loadUserByEmail {}", email);

        // Attempt to retrieve the user details from the database
        userDatails = userDao.findByEmailId(email);

        // Check if userDatails is null
        if (userDatails != null) {
            log.info("User found: {}", userDatails);
            // Return a User object from Spring Security
            return new User(userDatails.getEmail(), userDatails.getPassword(), new ArrayList<>());
        } else {
            log.warn("User not found for email: {}", email);
            // Throw an exception if user is not found
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }

    private com.inn.cafe.POJO.User userDatails;

    public com.inn.cafe.POJO.User getUserDatails() {
        return userDatails;
    }
}
