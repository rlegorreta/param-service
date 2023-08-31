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
 *  DatasourceRestRepository.kt
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

private const val BASE_PATH = "/param/api/data_source"

/**
 * Testing for Datasource entity using Spring Data REST.
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
 * @date August
 */
@CleanDatabase
class DatasourceRestRepositoryTests @Autowired constructor(
    val mockMvc: MockMvc,
    val datasourceRepository: DatasourceRepository): ParamServiceSpringDataREST() {

    @Nested
    @DisplayName("GET $BASE_PATH")
    inner class Get {
        @Test
        fun `Should find available Datasources using pagination` () {
            val datasources = IntStream.rangeClosed(1, 50)
                                       .mapToObj {index -> datasourceRepository.save(
                                                Datasource.builder()
                                                          .nombre("DS name $index")
                                                          .autor("TEST")
                                                          .json("")
                                                          .blockly("")
                                                          .fechaCreacion(LocalDate.now())
                                                          .fechaModificacion(LocalDate.now())
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
                   .param("size", datasources.size.toString()))
                   .andExpect(status().isOk)
                   .andExpect(content().contentType(HAL_JSON))
                   .andExpectAll(
                        jsonPath("$._embedded.data_sources.length()").value(datasources.size),
                        jsonPath("$._embedded.data_sources[*].id",
                            containsInAnyOrder(streamToIsMatcher(datasources.stream()
                                                .map { it.id!!.toString() }))
                        ),
                        jsonPath("$._embedded.data_sources[*].nombre",
                            containsInAnyOrder(streamToIsMatcher(datasources.stream()
                                                .map(Datasource::getNombre)))
                        ),
                        jsonPath("$._embedded.data_sources[*].fechaCreacion",
                            containsInAnyOrder(streamToIsMatcher(datasources.stream()
                                                .map(Datasource::getFechaCreacion)
                                                .map(LocalDate::toString)))
                        ),
                        jsonPath("$._embedded.data_sources[*].activo",
                            containsInAnyOrder(streamToIsMatcher(datasources.stream()
                                                .map(Datasource::getActivo)))
                        ),
                        jsonPath("$.page").isNotEmpty,
                        jsonPath("$.page.size").value(datasources.size),
                        jsonPath("$.page.number").value(0)
                    )
                    // .andDo(print());

        }

        @Test
        fun `Should Get a Datasource by ID` () {
            val datasource = datasourceRepository.save(Datasource.builder()
                                                        .nombre("DS name")
                                                        .autor("TEST")
                                                        .json("")
                                                        .blockly("")
                                                        .fechaCreacion(LocalDate.now())
                                                        .fechaModificacion(LocalDate.now())
                                                        .activo(false)
                                                        .build()
                                                      )

            mockMvc.perform(get("${BASE_PATH}/${datasource.id}")
                   .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                  SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                        )
                   .accept(HAL_JSON))
                   .andExpect(status().isOk)
                   .andExpect(content().contentType(HAL_JSON))
                   .andExpectAll(
                        jsonPath("$.id").value(datasource.id!!.toString()),
                        jsonPath("$.nombre").value(datasource.nombre),
                        jsonPath("$.autor").value(datasource.autor),
                        jsonPath("$.fechaCreacion").value(datasource.fechaCreacion.toString()),
                        jsonPath("$.fechaModificacion").value(datasource.fechaModificacion.toString()),
                        jsonPath("$.activo").value(false),
                        jsonPath("$._links").isNotEmpty,
                        jsonPath("$._links.self.href", containsString("${BASE_PATH}/${datasource.id}")),
                        jsonPath("$._links.data_source.href", containsString("${BASE_PATH}/${datasource.id}")),
                    )
                    .andDo(print());
        }

    }

    @Nested
    @DisplayName("GET $BASE_PATH with QueryDSL filters")
    inner class Filter {
        @Test
        fun `Should filter all Datasources by nombre and author`() {
            val datasource1 = datasourceRepository.save(Datasource.builder()
                                                                .nombre("One name")
                                                                .autor("TEST")
                                                                .json("")
                                                                .blockly("")
                                                                .fechaCreacion(LocalDate.now())
                                                                .fechaModificacion(LocalDate.now())
                                                                .activo(false)
                                                                .build()
                                                        )
            val datasource2 = datasourceRepository.save(Datasource.builder()
                                                                .nombre("Two name")
                                                                .autor("John")
                                                                .json("")
                                                                .blockly("")
                                                                .fechaCreacion(LocalDate.now())
                                                                .fechaModificacion(LocalDate.now())
                                                                .activo(false)
                                                                .build()
                                                        )

            mockMvc.perform(get(BASE_PATH)
                            .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                           SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                            )
                            .accept(HAL_JSON)
                            .param("nombre", "OnE")
                            )
                            .andExpect(status().isOk)
                            .andExpect(content().contentType(HAL_JSON))
                            .andExpectAll(
                                jsonPath("$._embedded.data_sources.length()").value(1),
                                jsonPath("$._embedded.data_sources[0].id").value(datasource1.id!!.toString()),
                                jsonPath("$._embedded.data_sources[0].nombre").value(datasource1.nombre),
                                jsonPath("$._embedded.data_sources[0].autor").value(datasource1.autor),
                                jsonPath("$.page").isNotEmpty,
                            )
                            .andDo(print())

            mockMvc.perform(get(BASE_PATH)
                            .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                            )
                            .accept(HAL_JSON)
                            .param("autor", "oHn")
                            )
                            .andExpect(status().isOk)
                            .andExpect(content().contentType(HAL_JSON))
                            .andExpectAll(
                                jsonPath("$._embedded.data_sources.length()").value(1),
                                jsonPath("$._embedded.data_sources[0].id").value(datasource2.id!!.toString()),
                                jsonPath("$._embedded.data_sources[0].nombre").value(datasource2.nombre),
                                jsonPath("$._embedded.data_sources[0].autor").value(datasource2.autor),
                                jsonPath("$.page").isNotEmpty,
                            )
                            .andDo(print())
        }

        @Test
        fun `Should filter all Datasource by fechaCreacion between` () {
            val datasource1 = datasourceRepository.save(Datasource.builder()
                                                            .nombre("One name")
                                                            .autor("TEST")
                                                            .json("")
                                                            .blockly("")
                                                            .fechaCreacion(LocalDate.of(2017, 5, 23))
                                                            .fechaModificacion(LocalDate.of(2023, 8, 31))
                                                            .activo(false)
                                                            .build()
                                                    )
            datasourceRepository.save(Datasource.builder()
                                                .nombre("Two name")
                                                .autor("John")
                                                .json("")
                                                .blockly("")
                                                .fechaCreacion(LocalDate.of(2020, 5, 20))
                                                .fechaModificacion(LocalDate.of(2023, 8, 30))
                                                .activo(false)
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
                        jsonPath("$._embedded.data_sources.length()").value(1),
                        jsonPath("$._embedded.data_sources[0].id").value(datasource1.id!!.toString()),
                        jsonPath("$._embedded.data_sources[0].activo").value(false),
                        jsonPath("$.page").isNotEmpty,
                    )
                   .andDo(print())

        }
    }
    @Nested
    @DisplayName("POST $BASE_PATH")
    inner class Post {

        @Test
        fun `Should Create a Datasource` (@Autowired objectMapper: ObjectMapper) {
            val datasource = Datasource.builder()
                                        .nombre("DS name")
                                        .autor("TEST")
                                        .json("")
                                        .blockly("")
                                        .fechaCreacion(LocalDate.now())
                                        .fechaModificacion(LocalDate.now())
                                        .activo(true)
                                        .build()
            val result = mockMvc.perform(post(BASE_PATH)
                                .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                               SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                                                       )
                                .accept(HAL_JSON)
                                .contentType(HAL_JSON)
                                .content(objectMapper.writeValueAsString(datasource)))
                                .andExpect(status().isCreated)
                                .andExpect(content().contentType(HAL_JSON))
                                .andExpectAll(
                                    jsonPath("$.id").value(notNullValue()),
                                    jsonPath("$.nombre").value(datasource.nombre),
                                    jsonPath("$.autor").value(datasource.autor),
                                    jsonPath("$.fechaCreacion").value(datasource.fechaCreacion.toString()),
                                    jsonPath("$.fechaModificacion").value(datasource.fechaModificacion.toString()),
                                    jsonPath("$.activo").value(true)
                                )
                                .andDo(print())
                                .andReturn()

            val datasourceID = UUID.fromString(JsonPath.read<String>(result.response.contentAsString,
                                                             "$.id"))

            assertThat(datasourceRepository.getReferenceById(datasourceID))
                                           .isNotNull
                                           /* It is commented since raise a not in session Hibernate exception
                                           .extracting(
                                               Datasource::getNombre,
                                               Datasource::getAutor,
                                               Datasource::getFechaCreacion,
                                               Datasource::getFechaModificacion)
                                           .containsExactly(
                                               datasource.nombre,
                                               datasource.autor,
                                               datasource.fechaCreacion,
                                               datasource.fechaModificacion)

                                            */
        }
    }

    @Nested
    @DisplayName("PATCH $BASE_PATH/\$id")
    inner class Patch {

        @Test
        fun `Should Patch (Update) a Datasource` () {
            val datasource = datasourceRepository.save(Datasource.builder()
                                                                .nombre("DS name")
                                                                .autor("TEST")
                                                                .json("")
                                                                .blockly("")
                                                                .fechaCreacion(LocalDate.now())
                                                                .fechaModificacion(LocalDate.now())
                                                                .activo(true)
                                                                .build()
                                                        )

            mockMvc.perform(patch("${BASE_PATH}/${datasource.id}")
                   .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                        SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                   )
                   .accept(HAL_JSON)
                   .content("""
                               { "nombre": "DS name modificado",
                                 "autor": "New tester", 
                                 "fechaCreacion": "1953-05-16", 
                                 "fechaModificacion": "1960-11-26"
                               }
                            """.trimIndent()))
                   .andExpect(status().isOk)
                   .andExpect(content().contentType(HAL_JSON))
                   .andExpectAll(
                        jsonPath("$.id").value(datasource.id!!.toString()),
                        jsonPath("$.nombre").value("DS name modificado"),
                        jsonPath("$.autor").value("New tester"),
                        jsonPath("$.fechaCreacion").value("1953-05-16"),
                        jsonPath("$.fechaModificacion").value("1960-11-26"),
                        jsonPath("$.activo").value(true)
                   )
                   .andDo(print());
        }

    }

    @Nested
    @DisplayName("DELETE $BASE_PATH/\$id")
    inner class Delete {

        @Test
        fun `Should Delete a Datasource` () {
            val datasource = datasourceRepository.save(Datasource.builder()
                                                                .nombre("DS name")
                                                                .autor("TEST")
                                                                .json("")
                                                                .blockly("")
                                                                .fechaCreacion(LocalDate.now())
                                                                .fechaModificacion(LocalDate.now())
                                                                .activo(true)
                                                                .build()
                                                    )
            mockMvc.perform(delete("${BASE_PATH}/${datasource.id}")
                   .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                  SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                   ))
                   .andExpect(status().isNoContent)

            assertThat(datasourceRepository.findById(datasource.id!!)).isEmpty
        }

    }

}