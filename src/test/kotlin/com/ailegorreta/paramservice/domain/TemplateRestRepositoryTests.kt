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
 *  TemplateRestRepositoryTests.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain

import com.ailegorreta.paramservice.CleanDatabase
import com.ailegorreta.paramservice.ParamServiceSpringDataREST
import com.ailegorreta.paramservice.util.streamToIsMatcher
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.hamcrest.Matchers.containsInAnyOrder
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.notNullValue
import com.jayway.jsonpath.JsonPath
import org.assertj.core.api.Assertions.assertThat

import java.util.*
import java.time.LocalDate
import java.util.stream.IntStream

private const val BASE_PATH = "/param/api/template"

/**
 * Testing for Template entity using Spring Data REST.
 *
 * The testing is calling from WebMvc in two areas:
 * - A CRUD testing for Datasource entity and,
 * - Testing a Querydsl.
 *
 * For more details see:
 * https://aregall.tech/building-and-testing-a-rest-hal-api-using-kotlin-spring-data-rest-and-jpa
 *
 * @project param-service
 * @author rlh
 * @date August 2023
 */
@CleanDatabase
class TemplateRestRepositoryTests @Autowired constructor(
    val mockMvc: MockMvc,
    val templateRepository: TemplateRepository): ParamServiceSpringDataREST() {

    @Nested
    @DisplayName("GET $BASE_PATH")
    inner class Get {
        @Test
        fun `Should find available Templates using pagination` () {
            val templates = IntStream.rangeClosed(1, 50)
                .mapToObj {index -> templateRepository.save(
                    Template.builder()
                        .nombre("Template name $index")
                        .fileRepo("TEST")
                        .destino(Template.DestinoType.Email)
                        .json("")
                        .blockly("")
                        .fechaCreacion(LocalDate.now())
                        .fechaModificacion(LocalDate.now())
                        .autor("TEST")
                        .activo(true)
                        .build()
                )}
                .toList()

            mockMvc.perform(get(BASE_PATH)
                .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                               SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                )
                .accept(HAL_JSON)
                .param("page", "0")
                .param("size", templates.size.toString()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$._embedded.templates.length()").value(templates.size),
                    jsonPath("$._embedded.templates[*].id",
                        containsInAnyOrder(streamToIsMatcher(templates.stream()
                            .map { it.id!!.toString() }))
                    ),
                    jsonPath("$._embedded.templates[*].nombre",
                        containsInAnyOrder(streamToIsMatcher(templates.stream()
                                                                      .map(Template::getNombre)))
                    ),
                    jsonPath("$._embedded.templates[*].fechaCreacion",
                        containsInAnyOrder(streamToIsMatcher(templates.stream()
                                                                      .map(Template::getFechaCreacion)
                                                                      .map(LocalDate::toString)))
                    ),
                    jsonPath("$._embedded.templates[*].activo",
                        containsInAnyOrder(streamToIsMatcher(templates.stream()
                                                                      .map(Template::getActivo)))
                    ),
                    jsonPath("$.page").isNotEmpty,
                    jsonPath("$.page.size").value(templates.size),
                    jsonPath("$.page.number").value(0)
                )
            // .andDo(print());

        }

        @Test
        fun `Should Get a Template by ID` () {
            val template = templateRepository.save(Template.builder()
                                            .nombre("Template name")
                                            .fileRepo("TEST")
                                            .destino(Template.DestinoType.Email)
                                            .json("")
                                            .blockly("")
                                            .fechaCreacion(LocalDate.now())
                                            .fechaModificacion(LocalDate.now())
                                            .autor("TEST")
                                            .activo(false)
                                            .build()
                                            )

            mockMvc.perform(get("${BASE_PATH}/${template.id}")
                .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                               SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                )
                .accept(HAL_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(template.id!!.toString()),
                    jsonPath("$.nombre").value(template.nombre),
                    jsonPath("$.autor").value(template.autor),
                    jsonPath("$.fechaCreacion").value(template.fechaCreacion.toString()),
                    jsonPath("$.fechaModificacion").value(template.fechaModificacion.toString()),
                    jsonPath("$.activo").value(false),
                    jsonPath("$._links").isNotEmpty,
                    jsonPath("$._links.self.href", containsString("${BASE_PATH}/${template.id}")),
                    jsonPath("$._links.template.href", containsString("${BASE_PATH}/${template.id}")),
                )
                .andDo(print())
        }
    }

    @Nested
    @DisplayName("GET $BASE_PATH with QueryDSL filters")
    inner class Filter {
        @Test
        fun `Should filter all Datasources by nombre and author`() {
            val template1 = templateRepository.save(Template.builder()
                                            .nombre("Carta mail")
                                            .fileRepo("TEST")
                                            .destino(Template.DestinoType.Email)
                                            .json("")
                                            .blockly("")
                                            .fechaCreacion(LocalDate.now())
                                            .fechaModificacion(LocalDate.now())
                                            .autor("Juan")
                                            .activo(false)
                                            .build()
                                        )
            val template2 = templateRepository.save(Template.builder()
                                            .nombre("Carta SMS")
                                            .fileRepo("TEST")
                                            .destino(Template.DestinoType.Email)
                                            .json("")
                                            .blockly("")
                                            .fechaCreacion(LocalDate.now())
                                            .fechaModificacion(LocalDate.now())
                                            .autor("Luis")
                                            .activo(false)
                                            .build()
                                        )

            mockMvc.perform(get(BASE_PATH)
                   .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                  SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                   )
                   .accept(HAL_JSON)
                   .param("nombre", "mAiL")
                   )
                   .andExpect(status().isOk)
                   .andExpect(content().contentType(HAL_JSON))
                   .andExpectAll(
                        jsonPath("$._embedded.templates.length()").value(1),
                        jsonPath("$._embedded.templates[0].id").value(template1.id!!.toString()),
                        jsonPath("$._embedded.templates[0].nombre").value(template1.nombre),
                        jsonPath("$._embedded.templates[0].autor").value(template1.autor),
                        jsonPath("$.page").isNotEmpty,
                    )
                    .andDo(print())

            mockMvc.perform(get(BASE_PATH)
                   .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                  SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                   )
                   .accept(HAL_JSON)
                   .param("autor", "LUIS")
                   )
                   .andExpect(status().isOk)
                   .andExpect(content().contentType(HAL_JSON))
                   .andExpectAll(
                        jsonPath("$._embedded.templates.length()").value(1),
                        jsonPath("$._embedded.templates[0].id").value(template2.id!!.toString()),
                        jsonPath("$._embedded.templates[0].nombre").value(template2.nombre),
                        jsonPath("$._embedded.templates[0].autor").value(template2.autor),
                        jsonPath("$.page").isNotEmpty,
                   )
                   .andDo(print())
        }

        @Test
        fun `Should filter all Templates by fechaCreacion between` () {
            val template1 = templateRepository.save(Template.builder()
                                        .nombre("Carta mail")
                                        .fileRepo("TEST")
                                        .destino(Template.DestinoType.Email)
                                        .json("")
                                        .blockly("")
                                        .fechaCreacion(LocalDate.of(2017, 5, 23))
                                        .fechaModificacion(LocalDate.of(2023, 8, 31))
                                        .autor("Juan")
                                        .activo(false)
                                        .build()
                                    )
            templateRepository.save(Template.builder()
                            .nombre("Carta mail")
                            .fileRepo("TEST")
                            .destino(Template.DestinoType.Email)
                            .json("")
                            .blockly("")
                            .fechaCreacion(LocalDate.of(2020, 5, 20))
                            .fechaModificacion(LocalDate.of(2023, 8, 29))
                            .autor("Juan")
                            .activo(true)
                            .build()
                        )

            mockMvc.perform(get(BASE_PATH)
                   .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                  SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                   )
                   .accept(HAL_JSON)
                   .queryParam("fechaCreacion", "2017-01-01", "2017-12-31"))
                   .andExpect(status().isOk)
                   .andExpect(content().contentType(HAL_JSON))
                   .andExpectAll(
                        jsonPath("$._embedded.templates.length()").value(1),
                        jsonPath("$._embedded.templates[0].id").value(template1.id!!.toString()),
                        jsonPath("$._embedded.templates[0].activo").value(false),
                        jsonPath("$.page").isNotEmpty,
                   )
                   .andDo(print())
        }
    }
    @Nested
    @DisplayName("POST $BASE_PATH")
    inner class Post {

        @Test
        fun `Should Create a Template` (@Autowired objectMapper: ObjectMapper) {
            val template = Template.builder()
                                    .nombre("Template name")
                                    .fileRepo("TEST")
                                    .destino(Template.DestinoType.Email)
                                    .json("")
                                    .blockly("")
                                    .fechaCreacion(LocalDate.now())
                                    .fechaModificacion(LocalDate.now())
                                    .autor("TEST")
                                    .activo(true)
                                    .build()
            val result = mockMvc.perform(post(BASE_PATH)
                .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                               SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                )
                .accept(HAL_JSON)
                .contentType(HAL_JSON)
                .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isCreated)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(notNullValue()),
                    jsonPath("$.nombre").value(template.nombre),
                    jsonPath("$.autor").value(template.autor),
                    jsonPath("$.fechaCreacion").value(template.fechaCreacion.toString()),
                    jsonPath("$.fechaModificacion").value(template.fechaModificacion.toString()),
                    jsonPath("$.activo").value(true)
                )
                .andDo(print())
                .andReturn()

            val templateID = UUID.fromString(JsonPath.read<String>(result.response.contentAsString, "$.id"))

            assertThat(templateRepository.getReferenceById(templateID))
                                         .isNotNull
                                /* It is commented since it raises a Hibernate not in session exception
                                .extracting(
                                    Template::getNombre,
                                    Template::getAutor,
                                    Template::getFechaCreacion,
                                    Template::getFechaModificacion)
                                .containsExactly(
                                    template.nombre,
                                    template.autor,
                                    template.fechaCreacion,
                                    template.fechaModificacion)

                                 */
        }
    }

    @Nested
    @DisplayName("PATCH $BASE_PATH/\$id")
    inner class Patch {

        @Test
        fun `Should Patch (Update) a Template` () {
            val template = templateRepository.save(Template.builder()
                                                .nombre("Template name")
                                                .fileRepo("TEST")
                                                .destino(Template.DestinoType.Email)
                                                .json("")
                                                .blockly("")
                                                .fechaCreacion(LocalDate.now())
                                                .fechaModificacion(LocalDate.now())
                                                .autor("TEST")
                                                .activo(false)
                                                .build()
                                                )

            mockMvc.perform(patch("${BASE_PATH}/${template.id}")
                .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                               SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                )
                .accept(HAL_JSON)
                .content("""
                               { "nombre": "Template name modificado",
                                 "autor": "New tester", 
                                 "fechaCreacion": "1953-05-16", 
                                 "fechaModificacion": "1960-11-26"
                               }
                            """.trimIndent()))
                .andExpect(status().isOk)
                .andExpect(content().contentType(HAL_JSON))
                .andExpectAll(
                    jsonPath("$.id").value(template.id!!.toString()),
                    jsonPath("$.nombre").value("Template name modificado"),
                    jsonPath("$.autor").value("New tester"),
                    jsonPath("$.fechaCreacion").value("1953-05-16"),
                    jsonPath("$.fechaModificacion").value("1960-11-26"),
                    jsonPath("$.activo").value(false)
                )
                .andDo(print())
        }

    }

    @Nested
    @DisplayName("DELETE $BASE_PATH/\$id")
    inner class Delete {

        @Test
        fun `Should Delete a Template` () {
            val template = templateRepository.save(Template.builder()
                                            .nombre("Template name")
                                            .fileRepo("TEST")
                                            .destino(Template.DestinoType.Email)
                                            .json("")
                                            .blockly("")
                                            .fechaCreacion(LocalDate.now())
                                            .fechaModificacion(LocalDate.now())
                                            .autor("TEST")
                                            .activo(false)
                                            .build()
                                            )
            mockMvc.perform(delete("${BASE_PATH}/${template.id}")
                .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                               SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                ))
                .andExpect(status().isNoContent)

            assertThat(templateRepository.findById(template.id!!)).isEmpty
        }

    }

}