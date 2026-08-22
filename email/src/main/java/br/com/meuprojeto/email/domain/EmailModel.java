package br.com.meuprojeto.email.domain;

import br.com.meuprojeto.email.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="TB_EMAIL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID emailId;
    private UUID userId;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "email_from")
    private String from;

    @Column(name = "email_to")
    private String to;

    private LocalDateTime sentDateEmail;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

}
