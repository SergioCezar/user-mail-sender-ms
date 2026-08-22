package br.com.meuprojeto.user.controller;

import br.com.meuprojeto.user.domain.UserModel;
import br.com.meuprojeto.user.dto.UserDto;
import br.com.meuprojeto.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserModel> createUser(@Valid @RequestBody UserDto userDto) {
        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.savePublish(userModel));
    }

    @GetMapping("/user/list")
    public ResponseEntity<List<UserModel>> listAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<UserModel> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserDto userDto
    ) {
        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        return ResponseEntity.ok(userService.update(id, userModel));
    }

    @DeleteMapping("/user/delete/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable UUID id) {
        if (!userService.deleteById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
