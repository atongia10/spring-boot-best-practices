package com.gs.tax.utilityproject.service;

import com.gs.tax.utilityproject.dto.UserDto;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> findAll();
    Optional<UserDto> findById(Long id);
    UserDto save(UserDto userDto);
    Mono<List<UserDto>> fetchUsersFromExternalApi();
}
