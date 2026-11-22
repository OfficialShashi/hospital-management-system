
package com.hms.user;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hms.role.Role;
import com.hms.role.repository.RoleRepository;
import com.hms.user.repository.UserRepository;

@Component
public class PharmacistSeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public PharmacistSeedRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String email = "pharmacist@test.com";

        if (userRepository.existsByEmail(email)) {
            System.out.println("Pharmacist account already exists.");
            return;
        }

        Role pharmacistRole = roleRepository.findByRoleName("PHARMACIST");

        if (pharmacistRole == null) {
            throw new IllegalStateException("PHARMACIST role not found");
        }

        User pharmacist = new User();

        pharmacist.setFullName("Demo Pharmacist");
        pharmacist.setEmail(email);
        pharmacist.setPhone("9876543212");
        pharmacist.setPasswordHash(
                passwordEncoder.encode(
    System.getenv("PHARMACIST_PASSWORD")
)
        pharmacist.setAccountStatus(User.AccountStatus.ACTIVE);
        pharmacist.setRoles(Set.of(pharmacistRole));

        User savedUser = userRepository.save(pharmacist);

        System.out.println(
                "Demo pharmacist created. User ID: "
                + savedUser.getUserId());
    }
}
