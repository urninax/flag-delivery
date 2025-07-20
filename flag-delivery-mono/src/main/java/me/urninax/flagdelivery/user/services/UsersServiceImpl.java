package me.urninax.flagdelivery.user.services;

import lombok.extern.slf4j.Slf4j;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.user.security.enums.InternalRole;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import me.urninax.flagdelivery.user.utils.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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
        userEntity.setInternalRoles(List.of(InternalRole.ROLE_USER));

        usersRepository.save(userEntity);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Optional<UserEntity> userEntityOptional = usersRepository.findByEmail(username);

        if(userEntityOptional.isEmpty()){
            throw new UsernameNotFoundException("User was not found");
        }

        UserEntity userEntity = userEntityOptional.get();
        List<SimpleGrantedAuthority> authorities = userEntity
                .getInternalRoles()
                .stream()
                .map(role ->
                        new SimpleGrantedAuthority(role.name()))
                .toList();

        return new User(userEntity.getEmail(), userEntity.getPassword(), authorities);
    }

    public UserEntity findUserByEmail(String email){
        Optional<UserEntity> userEntityOptional = usersRepository.findByEmail(email);

        if(userEntityOptional.isEmpty()){
            throw new UsernameNotFoundException("User was not found");
        }

        return userEntityOptional.get();
    }
}
