package com.dsm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import com.dsm.entities.Carrier;
import com.dsm.entities.Customer;
import com.dsm.entities.Stock;
import com.dsm.entities.Supplier;
import com.dsm.entities.User;
import com.dsm.repositories.CarrierRepository;
import com.dsm.repositories.CustomerRepository;
import com.dsm.repositories.StockRepository;
import com.dsm.repositories.SupplierRepository;
import com.dsm.repositories.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CarrierRepository carrierRepository;
    private final SupplierRepository supplierRepository;
    private final StockRepository stockRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) { // TEST Data Generation
        if (!userRepository.checkUsernameValidity("admin")) {
            userRepository.save(User.builder()
                    .username("admin").password(passwordEncoder.encode("admin123"))
                    .name("Administrator").email("admin@dsm.com")
                    .role(User.Role.administrator).isActive(true).build());
            log.info("admin user test data created (admin / admin123)");
        }
        if (!userRepository.checkUsernameValidity("manager")) {
            userRepository.save(User.builder()
                    .username("manager").password(passwordEncoder.encode("manager123"))
                    .name("Manager").email("manager@dsm.com")
                    .role(User.Role.manager).isActive(true).build());
        }
        if (customerRepository.count() == 0) {
            customerRepository.save(Customer.builder().name("Acme Corp").email("contact@acme.com").address("123 Rue de Paris, 75001 Paris").phone("+33 1 23 45 67 89").build());
            customerRepository.save(Customer.builder().name("Tech Solutions").email("info@techsolutions.fr").address("45 Avenue des Champs, 69002 Lyon").phone("+33 4 78 90 12 34").build());
            customerRepository.save(Customer.builder().name("Dupont SARL").email("dupont@dupont.fr").address("78 Boulevard Voltaire, 13001 Marseille").phone("+33 4 91 23 45 67").build());
            customerRepository.save(Customer.builder().name("Martin Industries").email("martin@industries.fr").address("12 Rue Gambetta, 33000 Bordeaux").phone("+33 5 56 78 90 12").build());
            log.info("customer test data created");
        }
        if (carrierRepository.count() == 0) {
            carrierRepository.save(Carrier.builder().name("Chronopost").phone("+33 800 123 456").rating(new BigDecimal("4.5")).isActive(true).build());
            carrierRepository.save(Carrier.builder().name("DHL Express").phone("+33 800 234 567").rating(new BigDecimal("4.2")).isActive(true).build());
            carrierRepository.save(Carrier.builder().name("UPS France").phone("+33 800 345 678").rating(new BigDecimal("4.0")).isActive(true).build());
            carrierRepository.save(Carrier.builder().name("La Poste Colissimo").phone("+33 800 456 789").rating(new BigDecimal("3.8")).isActive(true).build());
            log.info("carrier test data created");
        }
        if (supplierRepository.count() == 0) {
            supplierRepository.save(Supplier.builder().name("Global Parts").email("sales@globalparts.com").phone("+33 800 567 890").address("15 Rue Lafayette, 75009 Paris").isActive(true).build());
            supplierRepository.save(Supplier.builder().name("Euro Supply").email("contact@eurosupply.fr").phone("+33 800 678 901").address("22 Avenue Victor Hugo, 69003 Lyon").isActive(true).build());
            log.info("supplier test data created");
        }
        if (stockRepository.count() == 0) {
            stockRepository.save(Stock.builder().product("Laptop").productRef("LAP-001").quantity(20).build());
            stockRepository.save(Stock.builder().product("Monitor").productRef("MON-001").quantity(35).build());
            stockRepository.save(Stock.builder().product("Keyboard").productRef("KEY-001").quantity(50).build());
            log.info("stock test data created");
        }
    }
}
