package de.tum.ase.kleo.application.service;

import de.tum.ase.kleo.domain.User;
import de.tum.ase.kleo.domain.UserRepository;
import de.tum.ase.kleo.domain.UserRole;
import de.tum.ase.kleo.domain.id.UserId;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUser(UserId userId) {
        val user = userRepository.findOne(userId);
        return Optional.ofNullable(user);
    }
    
    public Stream<User> getUsers() {
        val users = userRepository.findAll();
        return stream(users.spliterator(), false);
    }
    
    @Transactional
    public boolean updateUserRoles(UserId userId, List<UserRole> userRoles) {
        val userOpt = getUser(userId);
        if (!userOpt.isPresent())
            return false;

        userOpt.get().userRoles(userRoles);
        return true;
    }

    @Transactional
    public boolean deleteUser(UserId userId) {
        val userOpt = getUser(userId);
        if (!userOpt.isPresent())
            return false;
        
        userRepository.delete(userOpt.get());
        return true;
    }
}
