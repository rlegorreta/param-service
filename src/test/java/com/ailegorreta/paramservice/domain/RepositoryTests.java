/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  RepositoryTests.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain;

import com.ailegorreta.paramservice.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.ailegorreta.paramservice.EnableTestContainers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class is just for test the repositories without GraphQL schema, without Querydsl and without Spring Data REST,
 * in summary just plain Spring Data JPA repositories. These repositories even they are declared as GraphQLRepositories
 * they can use as simple JPA repositories.
 *
 * @project param-service
 * @autho: rlh
 * @date: August 2023
 */
@DataJpaTest
/* ^ This is just the case we wanted to test just the JPA Repositories and download all context */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/* ^ Disables the default behavior of relying on an embedded test database since we want to use Testcontainers */
@EnableTestContainers
@ExtendWith(MockitoExtension.class)
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
/* ^ this is because: https://blog.mimacom.com/embeddedkafka-kafka-auto-configure-springboottest-bootstrapserversproperty/ */
@ActiveProfiles("integration-tests")
@DirtiesContext                /* will make sure this context is cleaned and reset between different tests */
public class RepositoryTests {

    @MockBean
    private StreamBridge streamBridge;
    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private SystemDateRepository systemDateRepository;
    @Autowired
    private  SystemRateRepository systemRateRepository;
    @Autowired
    private DocumentTypeRepository documentTypeRepository;
    @Autowired
    private EventService eventService;

    @Test
    void dataInitialization() {
        /*  Calendar initialization */
        var days = systemDateRepository.findAll();

        assertThat(days.size()).isEqualTo(5);

        var today = systemDateRepository.findSystemDateByName(DayType.HOY);
        var tomorrow = systemDateRepository.findSystemDateByName(DayType.MANANA);
        var yesterday = systemDateRepository.findSystemDateByName(DayType.AYER);

        assertThat(today).isNotNull();
        assertThat(tomorrow).isNotNull();
        assertThat(yesterday).isNotNull();

        /* Rates initialization */
        var rates = systemRateRepository.findAll();

        assertThat(rates.size()).isEqualTo(3);

        var tiff = systemRateRepository.findSystemRateByName("TIIF");
        var mxnDlr = systemRateRepository.findSystemRateByName("MXN-DLR");
        var mxnYen = systemRateRepository.findSystemRateByName("MXN-YEN");

        assertThat(tiff).isNotNull();
        assertThat(mxnDlr).isNotNull();
        assertThat(mxnYen).isNotNull();

        /* Document types */
        var documentTypes = documentTypeRepository.findAll();

        assertThat(documentTypes.size()).isEqualTo(7);

        var license = documentTypeRepository.findDocumentTypeByName("Licencia");

        assertThat(license).isNotNull();
    }

    @Test
    void testSomeUpdates() {
        /* Calendar */
        var newHoliday = new SystemDate(null, DayType.FESTIVO, LocalDate.now());

        assertThat(systemDateRepository.findAll().size()).isEqualTo(5);
        systemDateRepository.save(newHoliday);
        assertThat(systemDateRepository.findAll().size()).isEqualTo(6);

        /* Rates */
        var newRate = new SystemRate(null, "MXN-EUR", BigDecimal.valueOf(20.34));

        systemRateRepository.save(newRate);

        var rate = systemRateRepository.findSystemRateByName("MXN-EUR");

        assertThat(rate).isNotNull();
        systemRateRepository.deleteById(rate.getId());
        assertThat(systemRateRepository.findSystemRateByName("MXN-EUR")).isNull();

        /* Document types */
        var visa = documentTypeRepository.findDocumentTypeByName("Visa");

        assertThat(visa).isNotNull();
        assertThat(visa.getExpiration()).isEqualTo("12m");
        visa.setExpiration("24m");
        documentTypeRepository.save(visa);

        var updatedVisa = documentTypeRepository.findDocumentTypeByName("Visa");

        assertThat(updatedVisa).isNotNull();
        assertThat(updatedVisa.getExpiration()).isEqualTo("24m");
    }

}
