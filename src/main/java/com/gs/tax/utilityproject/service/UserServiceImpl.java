package com.gs.tax.utilityproject.service;

import com.gs.tax.utilityproject.dto.UserDto;
import com.gs.tax.utilityproject.entity.User;
import com.gs.tax.utilityproject.exception.ExternalApiException;
import com.gs.tax.utilityproject.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String API_URL = "https://api.example.com/users";
    private WebClient webClient;
    @Autowired
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Autowired
    public UserServiceImpl(WebClient.Builder webClientBuilder, UserRepository userRepository, ModelMapper modelMapper) {
        this.webClient = webClientBuilder.baseUrl(API_URL).build();
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public List<UserDto> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(u -> modelMapper.map(u, UserDto.class));
    }

    @Override
    public UserDto save(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public Mono<List<UserDto>> fetchUsersFromExternalApi() {
        return webClient.get()
                .uri("/")
                .retrieve()
                .onStatus(status -> status.is5xxServerError() || status.is4xxClientError(), response ->
                        Mono.error(new ExternalApiException("Error fetching users from external API")))
                .bodyToFlux(UserDto.class)
                .collectList()
                .retryWhen(Retry.max(3)
                        .filter(throwable -> throwable instanceof ExternalApiException)
                        .doBeforeRetry(retrySignal -> System.out.println("Retrying...")))
                .timeout(Duration.ofSeconds(30), Mono.error(new ExternalApiException("Fetching users timed out after 30 seconds")));
    }

}
