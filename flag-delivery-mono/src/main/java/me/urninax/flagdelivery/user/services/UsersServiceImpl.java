package me.urninax.flagdelivery.user.services;

import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import me.urninax.flagdelivery.user.utils.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

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

        usersRepository.save(userEntity);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Optional<UserEntity> userEntityOptional = usersRepository.findByEmail(username);

        if(userEntityOptional.isEmpty()){
            throw new UsernameNotFoundException("User was not found");
        }

        UserEntity userEntity = userEntityOptional.get();

        return new User(userEntity.getEmail(), userEntity.getPassword(), Collections.emptyList());
    }

    public UserEntity findUserByEmail(String email){
        Optional<UserEntity> userEntityOptional = usersRepository.findByEmail(email);

        if(userEntityOptional.isEmpty()){
            throw new UsernameNotFoundException("User was not found");
        }

        return userEntityOptional.get();
    }
}
