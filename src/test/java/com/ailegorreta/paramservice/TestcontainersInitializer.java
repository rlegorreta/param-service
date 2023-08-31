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
 *  TestcontainersInitializer.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice;

import com.ailegorreta.commons.event.EventDTO;
import com.ailegorreta.commons.event.EventDTODeSerializer;
import com.ailegorreta.commons.event.EventDTOSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

/**
 * This is a class to start the containers only once for all tests
 *
 * Algo it starts the container in parallel
 *
 * @project param-service
 * @author rlh
 * @date August 2023
 */
class TestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.1"));

    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

    static {
        Startables.deepStart(postgres, kafka).join();
        await().until( postgres::isRunning);
        await().until( kafka::isRunning);
    }

    /**
     * Kafka container Test configuration class (optional)
     */
    // @TestConfiguration
    static class KafkaTestContainersConfiguration {
        /** These configurations for consumers are not necessary (but leave the for example purpose) since the
         *  consumer configuration is taken from the application.yml file
         */
        // @Bean
        ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

            factory.setConsumerFactory(consumerFactory());

            return factory;
        }

        // @Bean
        ConsumerFactory<Integer, String> consumerFactory() {
            return new DefaultKafkaConsumerFactory<>(consumerConfigs());
        }

        // @Bean
        Map<String, Object> consumerConfigs() {
            HashMap<String, Object> props = new HashMap<>();

            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventDTODeSerializer.class);

            return props;
        }

        // @Bean
        ProducerFactory<String, EventDTO> producerFactory() {
            HashMap<String, Object> configProps = new HashMap<>();

            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventDTOSerializer.class);

            return new DefaultKafkaProducerFactory<>(configProps);
        }

        // @Bean
        KafkaTemplate<String, EventDTO> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }
    }

    /**
     * Sets all environment variables without the need to create a
     * @ActiveProfiles("integration-flyway")
     *
     * @param ctx the application to configure
     */
    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues.of(
                "spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.flyway.user=" + postgres.getUsername(),
                "spring.flyway.password=" + postgres.getPassword()

                ).applyTo(ctx.getEnvironment());
    }
}
