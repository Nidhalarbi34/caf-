package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constents.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtil;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.CachingUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;
    @Autowired
    CustomerUserDetailsService customerUserDetailsService;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtFilter jwtFilter;
    @Autowired
    EmailUtil emailUtil;
    @Autowired
    JwtUtil jwtUtil;
    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login {}", requestMap);
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );

            if (auth.isAuthenticated()) {
                User userDetails = customerUserDetailsService.getUserDatails();
                log.info("User Details: {}", userDetails);
                if (userDetails != null) {
                    // Check if the status is "true"
                    if ("true".equalsIgnoreCase(userDetails.getStatus())) {
                        String token = jwtUtil.generateToken(userDetails.getEmail(), userDetails.getRole());
                        String responseBody = "{\"token\":\"" + token + "\"}";
                        return new ResponseEntity<>(responseBody, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(
                                "{\"message\":\"Wait for admin approval.\"}",
                                HttpStatus.BAD_REQUEST
                        );
                    }
                } else {
                    return new ResponseEntity<>(
                            "{\"message\":\"User details not found.\"}",
                            HttpStatus.BAD_REQUEST
                    );
                }
            }

        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            return new ResponseEntity<>(
                    "{\"message\":\"Invalid email or password.\"}",
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Exception ex) {
            log.error("An error occurred: {}", ex.getMessage());
            return new ResponseEntity<>(
                    "{\"message\":\"An error occurred during login.\"}",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return new ResponseEntity<>(
                "{\"message\":\"An unexpected error occurred.\"}",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try{
            if(jwtFilter.isAdmin()){
                return new ResponseEntity<>(userDao.getAllUser(),HttpStatus.OK);
            }else {
                return new ResponseEntity<>(new ArrayList<>(),HttpStatus.UNAUTHORIZED);
            }

        }catch (Exception ex){
            ex.printStackTrace();

        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
               Optional<User> optional= userDao.findById(Integer.parseInt(requestMap.get("id")));
               if (!optional.isEmpty()){
                   userDao.updateStatus(requestMap.get("status"),Integer.parseInt(requestMap.get("id")));
                   log.info("optionaa {}",optional);
                   sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
                   log.info("option22 {}",optional);
                   return CafeUtils.getResponeEntity("User status update ", HttpStatus.OK);

               }else {
                   return CafeUtils.getResponeEntity("User id is not exist", HttpStatus.OK);
               }
            }else {
                return CafeUtils.getResponeEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> checkToken() {
       return CafeUtils.getResponeEntity("true",HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> chagePassword(Map<String, String> requestMap) {
        try {
            User userObj = userDao.findByEmail(jwtFilter.getCurrentEmail());
            log.info("userObj{}",userObj);
            if (userObj != null) {
                if(userObj.getPassword().equals(requestMap.get("oldPassword"))){
                    userObj.setPassword(requestMap.get("newPassword"));
                    userDao.save(userObj);
                    return CafeUtils.getResponeEntity("Password update Successfully ", HttpStatus.OK);

                }
                return CafeUtils.getResponeEntity("Incorrect Password ", HttpStatus.BAD_REQUEST);

            }
            return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgetPassword(Map<String, String> requestMap) {
            try {
                User user =userDao.findByEmail(requestMap.get("email"));
                System.out.print(user);
                if(!Objects.isNull(user)&&!Strings.isNullOrEmpty((user.getEmail()))){
                    emailUtil.forgetMail(user.getEmail(),"Credenticals by cafe managment System",user.getPassword());
                }
                return CafeUtils.getResponeEntity("Check u email for Credentials ", HttpStatus.OK);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private void sendMailToAllAdmin(String status, String email, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUsername());
        log.info("list admin3{}",userDao.getAllAdmin());
        if (status != null && status.equalsIgnoreCase("true")) {
            log.info("list status{}",status);
            log.info("list user{}",email);

            emailUtil.SendSimpleMessage(jwtFilter.getCurrentUsername(), "Account Approved", "USER:- " + email + "\n is approved by\nADMIN:-" + jwtFilter.getCurrentUsername(), allAdmin);
        } else {
            log.info("list status{}",status);
            log.info("list user{}",email);
            emailUtil.SendSimpleMessage(jwtFilter.getCurrentUsername(), "Account Disabled", "USER:- " + email + "\n is disabled by\nADMIN:-" + jwtFilter.getCurrentUsername(), allAdmin);
        }
    }


    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside signun{}",requestMap);
        try {
            if (validaSignUpMap(requestMap)) {
                //System.out.println("inside validaSignUpMap");
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    //System.out.println("Successfully  Registered.");
                    return CafeUtils.getResponeEntity("Successfully  Registered.", HttpStatus.OK);
                } else {
                    //System.out.println("Email already exits.");
                    return CafeUtils.getResponeEntity("Email already exits.", HttpStatus.BAD_REQUEST);
                }
            } else {
                //System.out.println(CafeConstants.INVALID_DATA);
                return CafeUtils.getResponeEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println(CafeConstants.SOMETHING_WENT_WRONG);
        return CafeUtils.getResponeEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    ;

    private boolean validaSignUpMap(Map<String, String> requestMap) {
        if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber") && requestMap.containsKey("email") && requestMap.containsKey("password")) {
            return true;
        }
        return false;
    }
    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus(requestMap.get("status"));
        user.setRole("user");
        return user;
    }
}
