package me.urninax.flagdelivery.user.services;

import me.urninax.flagdelivery.shared.exceptions.ConflictException;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.shared.security.principals.UserPrincipal;
import me.urninax.flagdelivery.shared.security.enums.InternalRole;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.user.utils.exceptions.EmailAlreadyExistsException;
import me.urninax.flagdelivery.user.utils.exceptions.PasswordMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UsersServiceImpl implements UsersService{
    private final UsersRepository usersRepository;
    private final EntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersServiceImpl(UsersRepository usersRepository, EntityMapper entityMapper, PasswordEncoder passwordEncoder){
        this.usersRepository = usersRepository;
        this.entityMapper = entityMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(SignupRequest signupRequest){
        UserEntity userEntity = entityMapper.toEntity(signupRequest);
        userEntity.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        userEntity.setInternalRoles(List.of(InternalRole.USER));

        try{
            usersRepository.saveAndFlush(userEntity);
        }catch(DataIntegrityViolationException exc){
            if(PersistenceExceptionUtils.isUniqueException(exc)){
                throw new ConflictException("Email already exists");
            }
            throw exc;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserEntity userEntity = usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User was not found"));

        return new UserPrincipal(userEntity);
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
