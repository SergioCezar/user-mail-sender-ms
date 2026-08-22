package br.com.meuprojeto.email.dto;

import java.util.UUID;

public record EmailDto (

    UUID userId,
    String to,
    String subject,
    String body

) {}
