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
 *  ParamServiceApplication.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * System parameters server repository.
 *
 * This server repo maintains al system parameters and variables. Two main functions:
 *
 * - An API to maintain the system parameters and variables. One for queries and also
 *   to modify them
 * - Use Kafka to send events when a mutation exists in order to keep it un sync with
 *   the Cache microservice.
 *
 *
 * Param-service microservice is an example i how to use the following Spring Data frameworks:
 * - Spring JPA
 * - Use os Querydsl
 * - Spring GraphQL
 * - Spring Data REST
 *
 * Even though this microservice is accessed by cache-service and system-ui using just GraphQL, a lot of examples
 * of other type os access: by Querydsl and Spring Data REST are shown in the Test clases.
 *
 * @author rlh
 * @project : param-service
 * @date August 2023
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = ["com.ailegorreta.paramservice", "com.ailegorreta.resourceserver"])
				// ^ this package must be included in order to instantiate de UserContext
class ParamServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(ParamServiceApplication::class.java, *args)
        }

        @Bean
        fun kotlinPropertyConfigurer(): PropertySourcesPlaceholderConfigurer {
            val propertyConfigurer = PropertySourcesPlaceholderConfigurer()

            propertyConfigurer.setPlaceholderPrefix("@{")
            propertyConfigurer.setPlaceholderSuffix("}")
            propertyConfigurer.setIgnoreUnresolvablePlaceholders(true)

            return propertyConfigurer
        }

        @Bean
        fun defaultPropertyConfigurer() = PropertySourcesPlaceholderConfigurer()
    }

    @Bean
    fun mapperConfigurer() = Jackson2ObjectMapperBuilder().apply {
        serializationInclusion(JsonInclude.Include.NON_NULL)
        failOnUnknownProperties(true)
        featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        indentOutput(true)
        modules(listOf(KotlinModule.Builder().build(), JavaTimeModule(), Jdk8Module()))
    }
}
