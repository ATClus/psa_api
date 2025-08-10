package com.clusterat.psa_api.infrastructure.config;

import com.clusterat.psa_api.domain.entities.*;
import com.clusterat.psa_api.domain.value_objects.Intensity;
import com.clusterat.psa_api.domain.value_objects.Region;
import com.clusterat.psa_api.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Profile("dev")
public class DatabaseSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final SpringDataJpaCountryRepository countryRepository;
    private final SpringDataJpaStateRepository stateRepository;
    private final SpringDataJpaCityRepository cityRepository;
    private final SpringDataJpaAddressRepository addressRepository;
    private final SpringDataJpaUserRepository userRepository;
    private final SpringDataJpaPoliceDepartmentRepository policeDepartmentRepository;
    private final SpringDataJpaOccurrenceRepository occurrenceRepository;

    public DatabaseSeeder(
            SpringDataJpaCountryRepository countryRepository,
            SpringDataJpaStateRepository stateRepository,
            SpringDataJpaCityRepository cityRepository,
            SpringDataJpaAddressRepository addressRepository,
            SpringDataJpaUserRepository userRepository,
            SpringDataJpaPoliceDepartmentRepository policeDepartmentRepository,
            SpringDataJpaOccurrenceRepository occurrenceRepository) {
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.policeDepartmentRepository = policeDepartmentRepository;
        this.occurrenceRepository = occurrenceRepository;
    }

    public void seedData() {
        log.info("Starting database seeding for development environment...");

        try {
            // Check if data already exists to avoid duplicates
            if (countryRepository.count() > 0) {
                log.info("Database already contains data, skipping seeding");
                return;
            }

            // Seed in order due to foreign key dependencies
            seedCountries();
            seedStates();
            seedCities();
            seedAddresses();
            seedUsers();
            seedPoliceDepartments();
            seedOccurrences();

            log.info("Database seeding completed successfully!");

        } catch (Exception e) {
            log.error("Error during database seeding", e);
            throw new RuntimeException("Failed to seed database", e);
        }
    }

    private void seedCountries() {
        log.info("Seeding countries...");

        List<CountryEntity> countries = List.of(
            CountryEntity.create("Brazil", "BR", "BRA"),
            CountryEntity.create("United States", "US", "USA"),
            CountryEntity.create("Argentina", "AR", "ARG"),
            CountryEntity.create("Chile", "CL", "CHL"),
            CountryEntity.create("Colombia", "CO", "COL")
        );

        countryRepository.saveAll(countries);
        log.info("Seeded {} countries", countries.size());
    }

    private void seedStates() {
        log.info("Seeding states...");

        CountryEntity brazil = countryRepository.findByIsoCode("BRA").orElseThrow();

        List<StateEntity> states = List.of(
            StateEntity.create("São Paulo", "SP", Region.SUDESTE, "35", brazil),
            StateEntity.create("Rio de Janeiro", "RJ", Region.SUDESTE, "33", brazil),
            StateEntity.create("Minas Gerais", "MG", Region.SUDESTE, "31", brazil),
            StateEntity.create("Bahia", "BA", Region.NORDESTE, "29", brazil),
            StateEntity.create("Paraná", "PR", Region.SUL, "41", brazil),
            StateEntity.create("Rio Grande do Sul", "RS", Region.SUL, "43", brazil),
            StateEntity.create("Pernambuco", "PE", Region.NORDESTE, "26", brazil),
            StateEntity.create("Ceará", "CE", Region.NORDESTE, "23", brazil),
            StateEntity.create("Pará", "PA", Region.NORTE, "15", brazil),
            StateEntity.create("Goiás", "GO", Region.CENTRO_OESTE, "52", brazil)
        );

        stateRepository.saveAll(states);
        log.info("Seeded {} states", states.size());
    }

    private void seedCities() {
        log.info("Seeding cities...");

        StateEntity sãoPaulo = stateRepository.findByIbgeCode("35").orElseThrow();
        StateEntity rioDeJaneiro = stateRepository.findByIbgeCode("33").orElseThrow();
        StateEntity minasGerais = stateRepository.findByIbgeCode("31").orElseThrow();

        List<CityEntity> cities = List.of(
            CityEntity.create("São Paulo", "São Paulo", "3550308", sãoPaulo),
            CityEntity.create("Guarulhos", "Guarulhos", "3518800", sãoPaulo),
            CityEntity.create("Campinas", "Campinas", "3509502", sãoPaulo),
            CityEntity.create("São Bernardo do Campo", "SBC", "3548708", sãoPaulo),
            CityEntity.create("Santos", "Santos", "3548500", sãoPaulo),
            
            CityEntity.create("Rio de Janeiro", "Rio de Janeiro", "3304557", rioDeJaneiro),
            CityEntity.create("Niterói", "Niterói", "3303302", rioDeJaneiro),
            CityEntity.create("Nova Iguaçu", "Nova Iguaçu", "3303500", rioDeJaneiro),
            
            CityEntity.create("Belo Horizonte", "BH", "3106200", minasGerais),
            CityEntity.create("Uberlândia", "Uberlândia", "3170206", minasGerais)
        );

        cityRepository.saveAll(cities);
        log.info("Seeded {} cities", cities.size());
    }

    private void seedAddresses() {
        log.info("Seeding addresses...");

        CityEntity sãoPaulo = cityRepository.findByIbgeCode("3550308").orElseThrow();
        CityEntity rio = cityRepository.findByIbgeCode("3304557").orElseThrow();
        CityEntity beloHorizonte = cityRepository.findByIbgeCode("3106200").orElseThrow();

        List<AddressEntity> addresses = List.of(
            AddressEntity.create("Rua da Consolação", "1000", "Conjunto 101", "Consolação", sãoPaulo),
            AddressEntity.create("Avenida Paulista", "1578", "Andar 12", "Bela Vista", sãoPaulo),
            AddressEntity.create("Rua Augusta", "2690", "Loja 1", "Jardim Paulista", sãoPaulo),
            AddressEntity.create("Rua Oscar Freire", "379", "Térreo", "Jardins", sãoPaulo),
            AddressEntity.create("Avenida Brigadeiro Faria Lima", "3064", "Torre Norte", "Itaim Bibi", sãoPaulo),
            
            AddressEntity.create("Avenida Atlântica", "1702", "Cobertura", "Copacabana", rio),
            AddressEntity.create("Rua Visconde de Pirajá", "550", "Loja A", "Ipanema", rio),
            AddressEntity.create("Avenida Rio Branco", "156", "15º Andar", "Centro", rio),
            
            AddressEntity.create("Avenida Afonso Pena", "1377", "Sala 201", "Centro", beloHorizonte),
            AddressEntity.create("Rua da Bahia", "1148", "Conjunto 302", "Centro", beloHorizonte)
        );

        addressRepository.saveAll(addresses);
        log.info("Seeded {} addresses", addresses.size());
    }

    private void seedUsers() {
        log.info("Seeding users...");

        List<UserEntity> users = List.of(
            UserEntity.create(12345),
            UserEntity.create(23456),
            UserEntity.create(34567),
            UserEntity.create(45678),
            UserEntity.create(56789),
            UserEntity.create(67890),
            UserEntity.create(78901),
            UserEntity.create(89012),
            UserEntity.create(90123),
            UserEntity.create(11111)
        );

        userRepository.saveAll(users);
        log.info("Seeded {} users", users.size());
    }

    private void seedPoliceDepartments() {
        log.info("Seeding police departments...");

        List<AddressEntity> addresses = addressRepository.findAll();

        List<PoliceDepartmentEntity> departments = List.of(
            PoliceDepartmentEntity.create(
                "way/123456789",
                "1º Distrito Policial",
                "1º DP",
                "Polícia Civil",
                "public",
                "+5511999999999",
                "contato@policia.sp.gov.br",
                "-23.550520",
                "-46.633309",
                addresses.get(0)
            ),
            PoliceDepartmentEntity.create(
                "way/234567890",
                "2º Distrito Policial",
                "2º DP",
                "Polícia Civil",
                "public",
                "+5511888888888",
                "contato2@policia.sp.gov.br",
                "-23.561414",
                "-46.656271",
                addresses.get(1)
            ),
            PoliceDepartmentEntity.create(
                "way/345678901",
                "3º Distrito Policial",
                "3º DP",
                "Polícia Civil",
                "public",
                "+5511777777777",
                "contato3@policia.sp.gov.br",
                "-23.563280",
                "-46.653450",
                addresses.get(2)
            ),
            PoliceDepartmentEntity.create(
                "way/456789012",
                "Delegacia de Copacabana",
                "DP Copacabana",
                "Polícia Civil",
                "public",
                "+5521666666666",
                "copacabana@policia.rj.gov.br",
                "-22.971177",
                "-43.182543",
                addresses.get(5)
            ),
            PoliceDepartmentEntity.create(
                "way/567890123",
                "Delegacia Centro BH",
                "DP Centro BH",
                "Polícia Civil",
                "public",
                "+5531555555555",
                "centro@policia.mg.gov.br",
                "-19.924501",
                "-43.935071",
                addresses.get(8)
            )
        );

        policeDepartmentRepository.saveAll(departments);
        log.info("Seeded {} police departments", departments.size());
    }

    private void seedOccurrences() {
        log.info("Seeding occurrences...");

        List<AddressEntity> addresses = addressRepository.findAll();
        List<UserEntity> users = userRepository.findAll();
        
        Date now = new Date();
        Date oneHourAgo = new Date(now.getTime() - 3600000); // 1 hour ago
        Date twoHoursAgo = new Date(now.getTime() - 7200000); // 2 hours ago
        Date yesterday = new Date(now.getTime() - 86400000); // 24 hours ago

        List<OccurrenceEntity> occurrences = List.of(
            OccurrenceEntity.create(
                "Incêndio em Edifício Comercial",
                "Incêndio reportado no 5º andar de edifício comercial na Avenida Paulista. Bombeiros mobilizados.",
                twoHoursAgo,
                oneHourAgo,
                oneHourAgo,
                false,
                Intensity.HIGH,
                addresses.get(1),
                users.get(0)
            ),
            OccurrenceEntity.create(
                "Acidente de Trânsito",
                "Colisão entre dois veículos na Rua da Consolação, próximo ao metrô. Trânsito intenso na região.",
                oneHourAgo,
                null,
                now,
                true,
                Intensity.MODERATE,
                addresses.get(0),
                users.get(1)
            ),
            OccurrenceEntity.create(
                "Assalto à Mão Armada",
                "Tentativa de assalto reportada na Rua Oscar Freire. Suspeito fugiu a pé.",
                yesterday,
                yesterday,
                yesterday,
                false,
                Intensity.HIGH,
                addresses.get(3),
                users.get(2)
            ),
            OccurrenceEntity.create(
                "Manifestação Pacífica",
                "Manifestação estudantil pacífica na Avenida Rio Branco. Trânsito desviado.",
                now,
                null,
                now,
                true,
                Intensity.LOW,
                addresses.get(7),
                users.get(3)
            ),
            OccurrenceEntity.create(
                "Queda de Energia",
                "Queda de energia elétrica afetando o bairro de Copacabana. Concessionária trabalhando na solução.",
                oneHourAgo,
                null,
                now,
                true,
                Intensity.MODERATE,
                addresses.get(5),
                users.get(4)
            ),
            OccurrenceEntity.create(
                "Operação Policial",
                "Operação policial em andamento contra o tráfico de drogas na região central.",
                twoHoursAgo,
                null,
                oneHourAgo,
                true,
                Intensity.SEVERE,
                addresses.get(8),
                users.get(5)
            ),
            OccurrenceEntity.create(
                "Alagamento",
                "Alagamento na Rua Augusta devido ao rompimento de tubulação. Trânsito comprometido.",
                yesterday,
                yesterday,
                yesterday,
                false,
                Intensity.MODERATE,
                addresses.get(2),
                users.get(6)
            ),
            OccurrenceEntity.create(
                "Princípio de Incêndio",
                "Princípio de incêndio controlado em restaurante na Rua da Bahia. Sem feridos.",
                twoHoursAgo,
                oneHourAgo,
                oneHourAgo,
                false,
                Intensity.LOW,
                addresses.get(9),
                users.get(7)
            ),
            OccurrenceEntity.create(
                "Suspeita de Bomba",
                "Objeto suspeito encontrado na estação de metrô. Área isolada preventivamente.",
                now,
                null,
                now,
                true,
                Intensity.CRITICAL,
                addresses.get(4),
                users.get(8)
            ),
            OccurrenceEntity.create(
                "Evento Público",
                "Show ao ar livre na praia de Copacabana. Aumento da segurança na região.",
                now,
                null,
                now,
                true,
                Intensity.LOW,
                addresses.get(6),
                users.get(9)
            )
        );

        occurrenceRepository.saveAll(occurrences);
        log.info("Seeded {} occurrences", occurrences.size());
    }
}