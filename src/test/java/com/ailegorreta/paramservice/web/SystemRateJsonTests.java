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
 *  SystemRateJsonTests.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.web;

import com.ailegorreta.paramservice.domain.SystemRate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Example for a JsonTest for some Postgres DTOs, many others can be added
 *
 * @project param-service
 * @author rlh
 * @date August 2023
 */
@JsonTest
@ContextConfiguration(classes = SystemRateJsonTests.class)
@ActiveProfiles("integration-tests")
public class SystemRateJsonTests {
    @Autowired
    public JacksonTester<SystemRate> json;

    @Test
    void testSerialize() throws Exception {
        var systemRate = new SystemRate(UUID.randomUUID(), "TestRate", BigDecimal.valueOf(20.45));
        var jsonContent = json.write(systemRate);

        assertThat(jsonContent).hasToString("{\"id\":\"" + systemRate.getId() + "\",\"name\":\"TestRate\",\"rate\":20.45}");
        assertThat(jsonContent).extractingJsonPathStringValue("@.id")
                               .isEqualTo(systemRate.getId().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("@.name")
                               .isEqualTo(systemRate.getName());
        assertThat(jsonContent).extractingJsonPathNumberValue("@.rate")
                               .isEqualTo(systemRate.getRate().doubleValue());
    }

    @Test
    void testDeserialize() throws Exception {
        var systemRate = new SystemRate(UUID.randomUUID(), "Test Rate", BigDecimal.valueOf(20.45));
        var content = """
                {
                    "id": 
                    """ + "\"" + systemRate.getId() + "\"," + """
                    "name": "Test Rate",
                    "rate": 
                    """ + systemRate.getRate() + """
                }
                """;
        assertThat(json.parse(content))
                       .usingRecursiveComparison()
                       .isEqualTo(systemRate);
    }
}
