package br.com.meuprojeto.user.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("O e-mail '" + email + "' já está cadastrado.");
    }
}
