package dev.marvin.crud.security.controller;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.marvin.crud.dto.Message;
import dev.marvin.crud.security.dto.JwtDTO;
import dev.marvin.crud.security.dto.NewUser;
import dev.marvin.crud.security.dto.UserLogin;
import dev.marvin.crud.security.entity.Role;
import dev.marvin.crud.security.entity.User;
import dev.marvin.crud.security.enums.RoleName;
import dev.marvin.crud.security.jwt.JWTProvider;
import dev.marvin.crud.security.service.RoleService;
import dev.marvin.crud.security.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  UserService userService;
  @Autowired
  RoleService roleService;

  @Autowired
  private JWTProvider jwtProvider;

  @PostMapping(value="/register")
  public ResponseEntity<Message> register(@Valid @RequestBody NewUser newUser, BindingResult bindingResult) {
    if(bindingResult.hasErrors()) {
      return new ResponseEntity<Message>(new Message("Verify the data provided"), HttpStatus.BAD_REQUEST);
    }
    if(userService.existsByUsername(newUser.getUsername())) {
      return new ResponseEntity<Message>(new Message("The username " + newUser.getUsername() + " is already registered"), HttpStatus.BAD_REQUEST);
    }
    if(userService.existsByEmail(newUser.getEmail())) {
      return new ResponseEntity<Message>(new Message("The user with email " + newUser.getEmail() + " is already registered"), HttpStatus.BAD_REQUEST);
    }
    User user = new User(newUser.getName(), newUser.getUsername(), newUser.getEmail(), passwordEncoder.encode(newUser.getPassword()));

    Set<Role> roles = new HashSet<>();

    roles.add(roleService.getByRoleName(RoleName.ROLE_USER).get());

    if (newUser.getRoles().contains("admin"))
      roles.add(roleService.getByRoleName(RoleName.ROLE_ADMIN).get());

    user.setRoles(roles);
    
    userService.save(user);
    return new ResponseEntity<Message>(new Message("User registered successfully"), HttpStatus.CREATED);

  }
  
  @PostMapping(value="/login")
  public ResponseEntity<?> login(@Valid @RequestBody UserLogin userLogin, BindingResult bindingResult) { 
    if(bindingResult.hasErrors()) {
      return new ResponseEntity<Message>(new Message("Invalid User"), HttpStatus.UNAUTHORIZED);
    }
      Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      String jwt = jwtProvider.generateToken(authentication);
      JwtDTO jwtDTO = new JwtDTO(jwt);

      return new ResponseEntity<JwtDTO>(jwtDTO, HttpStatus.ACCEPTED);


  }

  @PostMapping(value="/refresh")
  public ResponseEntity<JwtDTO> refresh(@RequestBody JwtDTO jwtDTO) throws ParseException { 
    String token = jwtProvider.refreshToken(jwtDTO);
    JwtDTO jwt = new JwtDTO(token);

    return new ResponseEntity<JwtDTO>(jwt, HttpStatus.OK);
  }  
    
}
