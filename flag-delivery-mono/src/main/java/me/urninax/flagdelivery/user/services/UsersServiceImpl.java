package me.urninax.flagdelivery.user.services;

import me.urninax.flagdelivery.shared.security.enums.InternalRole;
import me.urninax.flagdelivery.shared.security.principals.UserPrincipal;
import me.urninax.flagdelivery.shared.utils.EntityMapper;
import me.urninax.flagdelivery.shared.utils.PersistenceExceptionUtils;
import me.urninax.flagdelivery.user.models.UserEntity;
import me.urninax.flagdelivery.user.repositories.UsersRepository;
import me.urninax.flagdelivery.user.ui.models.requests.ChangePasswordRequest;
import me.urninax.flagdelivery.user.ui.models.requests.SignupRequest;
import me.urninax.flagdelivery.user.ui.models.requests.UpdatePersonalInfoRequest;
import me.urninax.flagdelivery.user.utils.exceptions.EmailAlreadyExistsException;
import me.urninax.flagdelivery.user.utils.exceptions.IncorrectCurrentPasswordException;
import me.urninax.flagdelivery.user.utils.exceptions.PasswordConfirmationMismatchException;
import me.urninax.flagdelivery.user.utils.exceptions.UserNotFoundException;
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
                throw new EmailAlreadyExistsException();
            }
            throw exc;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserEntity userEntity = usersRepository.findByEmail(username)
                .orElseThrow(UserNotFoundException::new);

        return new UserPrincipal(userEntity);
    }


    public void updateUser(UpdatePersonalInfoRequest request, UUID userId){
        UserEntity userEntity = usersRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if(request.getFirstName() != null && !request.getFirstName().isBlank()){
            userEntity.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null && !request.getLastName().isBlank()){
            userEntity.setLastName(request.getLastName());
        }

        if(request.getEmail() != null && !request.getEmail().isBlank()){
            if(usersRepository.existsByEmail(request.getEmail())){
                throw new EmailAlreadyExistsException();
            }
            userEntity.setEmail(request.getEmail());
        }

        usersRepository.save(userEntity);
    }

    public void changeUserPassword(ChangePasswordRequest request, UUID userId){
        UserEntity userEntity = usersRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if(!request.getNewPassword().equals(request.getNewPasswordConfirmation())){
            throw new PasswordConfirmationMismatchException();
        }

        if(!passwordEncoder.matches(request.getCurrentPassword(), userEntity.getPassword())){
            throw new IncorrectCurrentPasswordException();
        }

        userEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(userEntity);
    }
}
