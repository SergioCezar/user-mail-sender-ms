package br.com.meuprojeto.user.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Usuário com id '" + id + "' não encontrado.");
    }
}
