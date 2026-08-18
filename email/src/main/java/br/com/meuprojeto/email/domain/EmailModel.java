package br.com.meuprojeto.email.domain;

import br.com.meuprojeto.email.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="TB_EMAIL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailModel {

    private final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String emailId;
    private String userId;
    private String subject;
    @Column(columnDefinition = "BODY")
    private String body;
    private String from;
    private String to;
    private LocalDateTime sentDateEmail;
    private EmailStatus status;

}
