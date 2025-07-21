package me.urninax.flagdelivery.user.services;

import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.user.security.enums.InternalRole;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import me.urninax.flagdelivery.user.utils.UserMapper;
import me.urninax.flagdelivery.user.utils.exceptions.EmailAlreadyExistsException;
import me.urninax.flagdelivery.user.utils.exceptions.PasswordMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UsersServiceImpl implements UsersService{
    private UsersRepository usersRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository, UserMapper userMapper, PasswordEncoder passwordEncoder){
        this.usersRepository = usersRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(SignupRequest signupRequest) throws DataIntegrityViolationException{
        UserEntity userEntity = userMapper.toEntity(signupRequest);
        userEntity.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        userEntity.setInternalRoles(List.of(InternalRole.ROLE_USER));

        usersRepository.save(userEntity);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserEntity userEntity = usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));

        List<SimpleGrantedAuthority> authorities = userEntity
                .getInternalRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();

        return new User(userEntity.getEmail(), userEntity.getPassword(), authorities);
    }

    public UserEntity findUserByEmail(String email){
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));
    }

    public void updateUser(UpdatePersonalInfoRequest request, UUID userId){
        UserEntity userEntity = usersRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));

        if(request.getFirstName() != null && !request.getFirstName().isBlank()){
            userEntity.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null && !request.getLastName().isBlank()){
            userEntity.setLastName(request.getLastName());
        }

        if(request.getEmail() != null && !request.getEmail().isBlank()){
            if(usersRepository.existsByEmail(request.getEmail())){
                throw new EmailAlreadyExistsException("Email is already in use");
            }
            userEntity.setEmail(request.getEmail());
        }

        usersRepository.save(userEntity);
    }

    public void changeUserPassword(ChangePasswordRequest request, UUID userId){
        UserEntity userEntity = usersRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));

        if(!request.getNewPassword().equals(request.getNewPasswordConfirmation())){
            throw new PasswordMismatchException("New password and new password confirmation do not match");
        }

        if(!passwordEncoder.matches(request.getCurrentPassword(), userEntity.getPassword())){
            throw new PasswordMismatchException("Incorrect provided current password");
        }

        userEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(userEntity);
    }
}
