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
 *  DatasourceRepository.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain;

import com.ailegorreta.data.jpa.querydsl.QuerydslRepository;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.graphql.data.GraphQlRepository;

import java.util.UUID;

/**
 * Repository for datasources table. A relation to datasource_fields exist.
 *
 * Whe use Spring-GraphQL auto-registration instead of declaring the DataFetchers in the
 * RuntimeWiringConfigurer. See class PostsRuntimeWiring
 *
 * This repository use Querydsl library to be more simplistic than use a @mapping
 * annotations.
 *
 * It is subclassed for QuerydslRepository in order to declare custom filters, see:
 * https://aregall.tech/integration-between-querydsl-and-spring-data-rest-using-kotlin-gradle-and-spring-boot-3
 * note: This has to be in Java because if we declared it in Kotlin we received and
 *       error "No property customize found in 'XXXX'. See:
 *       https://github.com/spring-projects/spring-data-jpa/issues/2576
 *
 * @project param-service
 * @autho rlh
 * @date August 2023
 */
@RepositoryRestResource(path = "data_source", collectionResourceRel = "data_sources", itemResourceRel = "data_source")
@GraphQlRepository
public interface DatasourceRepository extends QuerydslRepository<Datasource, UUID, QDatasource> {
    Datasource findDatasourceByNombre(String nombre);

    /**
     * For more detail how dateBetween has been developed (in order to add extra custom queries) you can
     * see the super class QuerydslRepository
     * @param bindings
     * @param root
     */
    @Override
    default void customizeBindings(QuerydslBindings bindings, QDatasource root) {
        bindDateBetween(bindings, root.fechaCreacion);
        bindDateBetween(bindings, root.fechaModificacion);
    }
}
