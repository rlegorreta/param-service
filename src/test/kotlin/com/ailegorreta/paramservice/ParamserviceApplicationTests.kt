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
*  ParamserviceApplicationTests.kt
*
*  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
*/
package com.ailegorreta.paramservice

import com.ailegorreta.paramservice.domain.*
import com.ailegorreta.paramservice.service.EventService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.*
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.servlet.handler.HandlerMappingIntrospector

/**
 * For a good test slices for testing @SpringBootTest, see:
 * https://reflectoring.io/spring-boot-test/
 * https://www.diffblue.com/blog/java/software%20development/testing/spring-boot-test-slices-overview-and-usage/
 *
 * This class test all context with @SpringBootTest annotation and checks that everything is loaded correctly.
 * Also creates the classes needed for all slices in @TestConfiguration annotation
 *
 * Testcontainers:
 *
 * Use for test containers Postgres & Kafka following the next's ticks:
 *
 * - As little overhead as possible:
 * - Containers are started only once for all tests
 * - Containers are started in parallel
 * - No requirements for test inheritance
 * - Declarative usage.
 *
 * see article: https://maciejwalkowiak.com/blog/testcontainers-spring-boot-setup/
 *
 * For GraphQL tester see:
 * https://piotrminkowski.com/2023/01/18/an-advanced-graphql-with-spring-boot/
 *
 * @project param-service
 * @autho: rlh
 * @date: August 2023
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
/* ^ SpringBootTest download 'all' App context */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/* ^ Disables the default behavior of relying on an embedded test database since we want to use Testcontainers */
@EnableTestContainers
/* ^ This is a custom annotation to load the containers */
@ExtendWith(MockitoExtension::class)
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
/* ^ this is because: https://blog.mimacom.com/embeddedkafka-kafka-auto-configure-springboottest-bootstrapserversproperty/ */
@ActiveProfiles("integration-tests")
@Import(ParamserviceApplicationTests.ObjectMapperConfiguration::class)
@DirtiesContext /* will make sure this context is cleaned and reset between different tests */
class ParamserviceApplicationTests {
	/* StreamBridge instance is used by EventService but in @Test mode it is not instanciated, so we need to mock it:
       see: https://stackoverflow.com/questions/67276613/streambridge-final-cannot-be-mocked
       StreamBridge is a final class, With Mockito2 we can mock the final class, but by default this feature is disabled
       and that need to enable with below steps:

       1. Create a directory ‘mockito-extensions’ in src/test/resources/ folder.
       2. Create a file ‘org.mockito.plugins.MockMaker’ in ‘src/test/resources/mockito-extensions/’ directory.
       3. Write the content 'mock-maker-inline' in org.mockito.plugins.MockMaker file.

        At test class level use ‘@ExtendWith(MockitoExtension.class)’
        Then StreamBridge will be mocked successfully.

        note: Instead of mocking the final class (which is possible with the latest versions of mockito using the
        mock-maker-inline extension), you can wrap StreamBridge into your class and use it in your business logic.
        This way, you can mock and test it any way you need.

        This is a common practice for writing unit tests for code where some dependencies are final or static classes
     */
	@MockBean
	private val streamBridge: StreamBridge? = null
	@MockBean
	private var jwtDecoder: JwtDecoder? = null			// Mocked the security JWT

	@Autowired
	private val systemDateRepository: SystemDateRepository? = null

	@Autowired
	private val systemRateRepository: SystemRateRepository? = null

	@Autowired
	private val documentTypeRepository: DocumentTypeRepository? = null

	@Autowired
	private val eventService: EventService? = null
	@Test
	fun contextLoads() {
		println("Stream bridge:$streamBridge")
		println("System date repository:$systemDateRepository")
		println("System rate repository:$systemRateRepository")
		println("System document repository:$documentTypeRepository")
		println("Event service:$eventService")
		println("JwtDecoder:$jwtDecoder")
	}

	/**
	 * This TestConfiguration is for ALL file testers, so do not delete this class.
	 *
	 * This is to configure the ObjectMapper with JSR310Module and Java 8 JavaTime()
	 * module that it is not initialized for test mode. i.e., ObjectMapper @Autowired does not exist
	 */
	@TestConfiguration
	class ObjectMapperConfiguration {
		@Bean
		fun objectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()

		@Bean(name = ["mvcHandlerMappingIntrospector"])
		fun mvcHandlerMappingIntrospector(): HandlerMappingIntrospector {
			return HandlerMappingIntrospector()
		}
	}
}
