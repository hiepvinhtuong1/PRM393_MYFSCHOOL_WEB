package vn.edu.fpt.myfptschool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.edu.fpt.myfptschool.auth.entity.Role;
import vn.edu.fpt.myfptschool.auth.entity.User;
import vn.edu.fpt.myfptschool.auth.repository.UserRepository;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        userRepository.save(User.create("admin001",    passwordEncoder.encode("admin123"),   Role.ADMIN));
        userRepository.save(User.create("student001",  passwordEncoder.encode("student123"), Role.STUDENT));
        userRepository.save(User.create("parent001",   passwordEncoder.encode("parent123"),  Role.PARENT));
        userRepository.save(User.create("teacher001",  passwordEncoder.encode("teacher123"), Role.TEACHER));

        log.info("Dev seed: 4 users created (admin001, student001, parent001, teacher001)");
    }
}
