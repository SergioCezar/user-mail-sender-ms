package br.com.meuprojeto.user.service;

import br.com.meuprojeto.user.domain.UserModel;
import br.com.meuprojeto.user.exception.EmailAlreadyExistsException;
import br.com.meuprojeto.user.exception.UserNotFoundException;
import br.com.meuprojeto.user.producer.UserProducer;
import br.com.meuprojeto.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProducer userProducer;

    public UserService(UserRepository userRepository, UserProducer userProducer) {
        this.userRepository = userRepository;
        this.userProducer = userProducer;
    }

    @Transactional
    public UserModel savePublish(UserModel user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        var savedUser = save(user);
        userProducer.publishEvent(savedUser);
        return savedUser;
    }

    @Transactional
    public UserModel update(UUID id, UserModel updatedUser) {
        var existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (userRepository.existsByEmailAndIdNot(updatedUser.getEmail(), id)) {
            throw new EmailAlreadyExistsException(updatedUser.getEmail());
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        return save(existingUser);
    }

    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public boolean deleteById(UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    private UserModel save(UserModel user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
    }
}
