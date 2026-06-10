package com.dsm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import com.dsm.entities.Carrier;
import com.dsm.entities.Customer;
import com.dsm.entities.Location;
import com.dsm.entities.Stock;
import com.dsm.entities.Supplier;
import com.dsm.entities.User;
import com.dsm.repositories.CarrierRepository;
import com.dsm.repositories.CustomerRepository;
import com.dsm.repositories.LocationRepository;
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
    private final LocationRepository locationRepository;
    private final StockRepository stockRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
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
            customerRepository.save(Customer.builder().name("Acme Corp").email("contact@acme.com").address("123 Market Street, New York").phone("+1 212 555 0101").build());
            customerRepository.save(Customer.builder().name("Tech Solutions").email("info@techsolutions.com").address("45 Commerce Street, Chicago").phone("+1 312 555 0102").build());
            customerRepository.save(Customer.builder().name("Northline Supply").email("contact@northline.com").address("78 Harbor Road, Boston").phone("+1 617 555 0103").build());
            customerRepository.save(Customer.builder().name("Martin Industries").email("sales@martinindustries.com").address("12 Warehouse Drive, Dallas").phone("+1 214 555 0104").build());
            log.info("customer test data created");
        }
        if (carrierRepository.count() == 0) {
            carrierRepository.save(Carrier.builder().name("FastRoute").phone("+1 800 123 4567").rating(new BigDecimal("4.5")).isActive(true).build());
            carrierRepository.save(Carrier.builder().name("DHL Express").phone("+1 800 234 5678").rating(new BigDecimal("4.2")).isActive(true).build());
            carrierRepository.save(Carrier.builder().name("UPS").phone("+1 800 345 6789").rating(new BigDecimal("4.0")).isActive(true).build());
            carrierRepository.save(Carrier.builder().name("Postal Freight").phone("+1 800 456 7890").rating(new BigDecimal("3.8")).isActive(true).build());
            log.info("carrier test data created");
        }
        if (supplierRepository.count() == 0) {
            supplierRepository.save(Supplier.builder().name("Global Parts").email("sales@globalparts.com").phone("+1 800 567 8901").address("15 Supply Lane, Seattle").isActive(true).build());
            supplierRepository.save(Supplier.builder().name("Metro Supply").email("contact@metrosupply.com").phone("+1 800 678 9012").address("22 Industrial Way, Denver").isActive(true).build());
            log.info("supplier test data created");
        }
        if (locationRepository.count() == 0) {
            locationRepository.save(Location.builder().name("Aisle A - Rack 01").code("A-01").description("Primary electronics storage").build());
            locationRepository.save(Location.builder().name("Aisle B - Rack 03").code("B-03").description("Display hardware storage").build());
            locationRepository.save(Location.builder().name("Aisle C - Rack 02").code("C-02").description("Accessory storage").build());
            log.info("location test data created");
        }
        if (stockRepository.count() == 0) {
            Location locationA = locationRepository.getLocationByCode("A-01").orElse(null);
            Location locationB = locationRepository.getLocationByCode("B-03").orElse(null);
            Location locationC = locationRepository.getLocationByCode("C-02").orElse(null);
            stockRepository.save(Stock.builder().product("Laptop").productRef("LAP-001").locationId(locationA != null ? locationA.getId() : null).location(locationA != null ? locationA.getName() : null).quantity(20).build());
            stockRepository.save(Stock.builder().product("Monitor").productRef("MON-001").locationId(locationB != null ? locationB.getId() : null).location(locationB != null ? locationB.getName() : null).quantity(35).build());
            stockRepository.save(Stock.builder().product("Keyboard").productRef("KEY-001").locationId(locationC != null ? locationC.getId() : null).location(locationC != null ? locationC.getName() : null).quantity(50).build());
            log.info("stock test data created");
        }
    }
}
