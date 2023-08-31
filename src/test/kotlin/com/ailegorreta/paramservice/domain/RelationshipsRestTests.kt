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
 *  RelationshipsRestTests.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain

import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONArray
import com.ailegorreta.paramservice.CleanDatabase
import com.ailegorreta.paramservice.ParamServiceSpringDataREST
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.hamcrest.Matchers.containsInAnyOrder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt

/**
 * Testing for all database relationships (in this case OneToMany).
 *
 * The testing is calling from WebMvc in two relationships:
 * - Datasource entity with DatasourceField and,
 * - Template with TemplateField.
 *
 * For more details see:
 * https://aregall.tech/building-and-testing-a-rest-hal-api-using-kotlin-spring-data-rest-and-jpa
 *
 * For example if we want to create two new DatasourceFields that belongs to Datasource we van execute:
 * curl --location 'http://localhost:8080/api/data_source' \
 * --header 'Content-Type: application/json' \
 * --data '{
 *     "name": "DS name",
 *     "campos": ["http://localhost:8080/api/data_source_field/1",
 *                "http://localhost:8080/api/data_source_field/2"]
 * }'
 *
 * @project param-service
 * @author rlh
 * @date August
 */
@CleanDatabase
class RelationshipsRestTests(@Autowired val mockMvc: MockMvc) : ParamServiceSpringDataREST() {

    @Test
    fun `Should create a Datasource with two Datasource field with HAL association links` () {
        // Create the Datasource with no relationships
        val datasourceLink = createAndReturnSelfHref("/param/api/data_source",
            """
               { "nombre":"DS name",
                 "json":"",
                 "blockly":"",
                 "config":null,
                 "configBlockly":null,
                 "mapping":null,
                 "fechaCreacion":[2023,8,30],
                 "fechaModificacion":[2023,8,30],
                 "autor":"TEST",
                 "activo":true,
                 "campos": []
               }
            """.trimIndent())
        // given two DatasourceFields
        createAndReturnSelfHref("/param/api/data_source_field",
            """
               { "nombre": "ciudad",
                 "tipo": "Texto", 
                 "validaciones": "",
                 "datasource": "$datasourceLink"
               }
            """.trimIndent())
        createAndReturnSelfHref("/param/api/data_source_field",
            """
               { "nombre": "numHabitantes",
                 "tipo": "Entero", 
                 "validaciones": "",
                 "datasource": "$datasourceLink"
               }
            """.trimIndent())

        // now validate when and then
        performGet(datasourceLink)
            .andExpectAll(
                jsonPath("$.id").isNotEmpty,
                jsonPath("$.autor").value("TEST"),
                jsonPath("$.fechaCreacion").value("2023-08-30"),
                jsonPath("$.fechaModificacion").value("2023-08-30")
            )
            .andDo{
                val datasourceResponse = it.response.contentAsString

                performGet(JsonPath.read(datasourceResponse, "$._links.campos.href"))
                    .andExpectAll(
                        jsonPath("$._embedded.campos.length()").value(2),
                        jsonPath("$._embedded.campos[*].nombre",
                                                containsInAnyOrder("ciudad", "numHabitantes"))
                    )
                    .andDo { camposResult ->

                        val camposLinks = JsonPath.read<JSONArray?>(camposResult.response.contentAsString, "$._embedded.campos[*]._links.campo.href")
                                                  .filterIsInstance<String>()
                                                  .sorted()

                        performGet(camposLinks[0])
                            .andExpectAll(
                                jsonPath("$.nombre").value("numHabitantes"),
                                jsonPath("$.tipo").value("Entero")
                            )

                        performGet(camposLinks[1])
                            .andExpectAll(
                                jsonPath("$.nombre").value("ciudad"),
                                jsonPath("$.tipo").value("Texto")
                            )

                    }
            }
    }

    @Test
    fun `Should create a Template with two Template fields with HAL association links` () {
        // Create the Template
        val templateLink = createAndReturnSelfHref("/param/api/template",
            """
               { "nombre":"Template name",
                 "fileRepo":"",
                 "destino":"Email",
                 "json":null,
                 "blockly":null,
                 "fechaCreacion":[2023,8,30],
                 "fechaModificacion":[2023,8,30],
                 "autor":"TEST",
                 "activo":true,
                 "campos": []
               }
            """.trimIndent())
        // given two TemplateField
        createAndReturnSelfHref("/param/api/template_field",
            """
               { "nombre": "nombre",
                 "tipo": "Texto", 
                 "valorDefault": null,
                 "template": "$templateLink"
               }
            """.trimIndent())
        createAndReturnSelfHref("/param/api/template_field",
            """
               { "nombre": "diasVencimiento",
                 "tipo": "Entero", 
                 "valorDefault": null,
                 "template": "$templateLink"
               }
            """.trimIndent())


        // now validate when and then
        performGet(templateLink)
            .andExpectAll(
                jsonPath("$.id").isNotEmpty,
                jsonPath("$.autor").value("TEST"),
                jsonPath("$.fechaCreacion").value("2023-08-30"),
                jsonPath("$.fechaModificacion").value("2023-08-30")
            )
            .andDo{
                val templateResponse = it.response.contentAsString

                performGet(JsonPath.read(templateResponse, "$._links.campos.href"))
                    .andExpectAll(
                        jsonPath("$._embedded.campos.length()").value(2),
                        jsonPath("$._embedded.campos[*].nombre",
                            containsInAnyOrder("nombre", "diasVencimiento"))
                    )
                    .andDo { camposResult ->

                        val camposLinks = JsonPath.read<JSONArray?>(camposResult.response.contentAsString, "$._embedded.campos[*]._links.campo.href")
                            .filterIsInstance<String>()
                            .sorted()

                        performGet(camposLinks[0])
                            .andExpectAll(
                                jsonPath("$.nombre").value("nombre"),
                                jsonPath("$.tipo").value("Texto")
                            )

                        performGet(camposLinks[1])
                            .andExpectAll(
                                jsonPath("$.nombre").value("diasVencimiento"),
                                jsonPath("$.tipo").value("Entero")
                            )

                    }
            }
    }

    /**
     * Utility function to perform GET requests
     */
    fun performGet(path: String): ResultActions {
        return mockMvc.perform(get(path)
                      .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                     SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                      )
                      .accept(HAL_JSON))
                      .andExpect(status().isOk)
                      .andExpect(content().contentType(HAL_JSON))
                      .andDo(print())
    }

    /**
     * Utility functions to help us perform a POST request with a given payload on a given path, verify the resource
     * was created by checking the response status code, and retuning the link of the created resource.
     */
    fun performPost(path: String, body: String) : ResultActions {
        return mockMvc.perform(post(path)
                      .with(jwt().authorities(listOf(SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                     SimpleGrantedAuthority("ROLE_ADMINLEGO")))
                      )
                      .accept(HAL_JSON)
                      .content(body))
                      .andExpect(status().isCreated)
                      .andExpect(content().contentType(HAL_JSON))
                      .andDo(print())
    }

    fun createAndReturnSelfHref(path: String, body: String): String {
        return JsonPath.read(performPost(path, body)
                       .andReturn()
                       .response.contentAsString, "_links.self.href")
    }
}