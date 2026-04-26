package com.app.ecommerce.userservice.service;


import com.app.ecommerce.userservice.model.AppRole;
import com.app.ecommerce.userservice.model.Role;
import com.app.ecommerce.userservice.model.User;
import com.app.ecommerce.userservice.payload.*;
import com.app.ecommerce.userservice.repository.RoleRepository;
import com.app.ecommerce.userservice.repository.UserRepository;
import com.app.ecommerce.userservice.security.jwt.JwtUtil;
import com.app.ecommerce.userservice.security.request.LoginRequest;
import com.app.ecommerce.userservice.security.request.SignupRequest;
import com.app.ecommerce.userservice.security.response.MessageResponse;
import com.app.ecommerce.userservice.security.response.UserInfoResponse;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    ModelMapper modelMapper;

    // ========================
    // LOGIN
    // ========================
    public AuthenticationResult login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        org.springframework.security.core.userdetails.User user =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        List<String> roles = user.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        String token = jwtUtil.generateToken(user.getUsername(), claims);

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);

        User currentUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + user.getUsername())
                );

        UserInfoResponse response = new UserInfoResponse(
                currentUser.getUserId(),
                currentUser.getUsername(),
                roles,
                currentUser.getEmail(),
                cookie.toString()
        );

        return new AuthenticationResult(response, cookie);
    }

    // ========================
    // REGISTER
    // ========================
    public ResponseEntity<?> register(SignupRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Username already taken"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Email already exists"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        Set<Role> roles = new HashSet<>();

        Set<String> reqRoles = signUpRequest.getRole();

        if (reqRoles == null || reqRoles.isEmpty()) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            roles.add(userRole);
        } else {
            for (String role : reqRoles) {

                switch (role.toLowerCase()) {

                    case "admin":
                        roles.add(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Role not found")));
                        break;

                    case "seller":
                        roles.add(roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Role not found")));
                        break;

                    default:
                        roles.add(roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Role not found")));
                }
            }
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    // ========================
    // GET USER INFO
    // ========================
    public UserInfoResponse getCurrentUserDetails(Authentication authentication) {

        org.springframework.security.core.userdetails.User user =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        List<String> roles = user.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());

        User currentUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + user.getUsername())
                );

        UserInfoResponse response = new UserInfoResponse(currentUser.getUserId(),
                currentUser.getUsername(), roles);
        return response;
    }

    // ========================
    // LOGOUT
    // ========================
    public Cookie logoutUser() {

        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        return cookie;
    }

    public UserResponse getAllSellers(Pageable pageable) {
        Page<User> allUsers = userRepository.findByRoleName(AppRole.ROLE_SELLER, pageable);
        List<UserDTO> userDtos = allUsers.getContent()
                .stream()
                .map(p -> modelMapper.map(p, UserDTO.class))
                .collect(Collectors.toList());

        UserResponse response = new UserResponse();
        response.setContent(userDtos);
        response.setPageNumber(allUsers.getNumber());
        response.setPageSize(allUsers.getSize());
        response.setTotalElements(allUsers.getTotalElements());
        response.setTotalPages(allUsers.getTotalPages());
        response.setLastPage(allUsers.isLast());
        return response;
    }

    public ExternalServiceUserResponse validateUserForExternalService(Authentication authentication) {
        org.springframework.security.core.userdetails.User user =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        Set<String> roles = user.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        User currentUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + user.getUsername())
                );

        ExternalServiceUserResponse externalServiceUserResponse = new ExternalServiceUserResponse(
                currentUser.getUserId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getPassword(),
                roles,
                modelMapper.map(currentUser.getAddresses().getFirst(), AddressDTO.class),
                currentUser.getCartId()
        );
        return externalServiceUserResponse;
    }
}
