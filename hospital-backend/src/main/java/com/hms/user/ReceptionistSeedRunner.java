
package com.hms.user;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hms.role.Role;
import com.hms.role.repository.RoleRepository;
import com.hms.user.repository.UserRepository;

@Component
public class ReceptionistSeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ReceptionistSeedRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String email = "receptionist@test.com";

        if (userRepository.existsByEmail(email)) {
            System.out.println("Receptionist account already exists.");
            return;
        }

        Role receptionistRole =
                roleRepository.findByRoleName("RECEPTIONIST");

        if (receptionistRole == null) {
            throw new IllegalStateException(
                    "RECEPTIONIST role not found");
        }

        User receptionist = new User();

        receptionist.setFullName("Demo Receptionist");
        receptionist.setEmail(email);
        receptionist.setPasswordHash(
                passwordEncoder.encode(
    System.getenv("RECEPTIONIST_PASSWORD")
)
        receptionist.setAccountStatus(User.AccountStatus.ACTIVE);
        receptionist.setRoles(Set.of(receptionistRole));

        User savedUser = userRepository.save(receptionist);

        System.out.println(
                "Demo receptionist created. User ID: "
                + savedUser.getUserId());
    }
}
