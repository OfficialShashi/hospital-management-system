
package com.hms.user;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hms.role.Role;
import com.hms.role.repository.RoleRepository;
import com.hms.user.repository.UserRepository;

@Component
public class LabTechnicianSeedRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public LabTechnicianSeedRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String email = "labtechnician@test.com";

        if (userRepository.existsByEmail(email)) {
            System.out.println("Lab Technician account already exists.");
            return;
        }

        Role labRole = roleRepository.findByRoleName("LAB_TECHNICIAN");

        if (labRole == null) {
            throw new IllegalStateException("LAB_TECHNICIAN role not found");
        }

        User technician = new User();

        technician.setFullName("Demo Lab Technician");
        technician.setEmail(email);
        technician.setPhone("9876543214");
        technician.setPasswordHash(
                passwordEncoder.encode(
					System.getenv("LAB_TECHNICIAN_PASSWORD")
				)
        technician.setAccountStatus(User.AccountStatus.ACTIVE);
        technician.setRoles(Set.of(labRole));

        User savedUser = userRepository.save(technician);

        System.out.println(
                "Demo Lab Technician created. User ID: "
                + savedUser.getUserId());
    }
}
