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
 *  QuerydslTests.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.domain;

import com.ailegorreta.paramservice.util.SearchCriteria;
import com.querydsl.core.types.dsl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.ailegorreta.paramservice.EnableTestContainers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;

/**
 * GraphQL with JPA utilizes Querydsl as a "link" that connects GraphQL query with JPA because it expresses query
 * predicates by generating a metamodel. Nevertheless, we can use Querydsl without GraphQL and simplify and clarify
 * the queries to the relational database.
 *
 * For Querydsl tests using Spring Data REST we developed DataSourceRestRepositoryTests, TemplateRestRepositoryTest
 * and for relationships RelationshipRestTest.
 *
 * This test are more customize Querydsl...
 *
 * In these tests we show how can we use Querydsl library without GraphQL and without Spring Data REST using two plain
 * Querydsl repositories, called directly:
 * DocumentTypeQuerydslRepository and SystemRateQuerydslRepository.
 *
 * Examples:
 * - Puts the Querydsl inside the @Repository
 * - How can we create custom predicates based in some arbitrary constraints.
 *   see for this example: https://www.baeldung.com/rest-api-search-language-spring-data-querydsl
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
@ActiveProfiles("integration-tests")
@DirtiesContext                /* will make sure this context is cleaned and reset between different tests */
public class QuerydslTests {

    @MockBean
    private StreamBridge streamBridge;
    @MockBean
    private JwtDecoder jwtDecoder;
    @Autowired
    private DocumentTypeQuerydslRepository documentTypeRepository;
    @Autowired
    private SystemRateQuerydslRepository systemRateRepository;

    /**
     * Test for Querydsl inside a repository
     */
    @Test
    void documentTypeQuerydslRepositoryTest() {
        List<DocumentType> result = documentTypeRepository.findDocumentTypeByName("Co%");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Comprobante domicilio");
        assertThat(result.get(1).getName()).isEqualTo("Contrato");
    }

    /**
     * Example of a custom Querydsl but modifying the predicate with a custom DSL
     */
    @Test
    public void givenName_whenGettingListOfSystemRates_thenCorrect() {
        var tiif = systemRateRepository.findSystemRateByName("TIIF");

        assertThat(tiif).isNotNull();

        var builder = new MySystemRatePredicatesBuilder().with("name", ":", "TIIF");

        Iterable<SystemRate> results = systemRateRepository.findAll(builder.build());

        org.hamcrest.MatcherAssert.assertThat(results, contains(tiif));
    }

    /**
     * A more complicated custom DSL
     */
    @Test
    public void givenNameAndRate_whenGettingListOfSystemRates_thenCorrect() {
        var tiif = systemRateRepository.findSystemRateByName("TIIF");

        assertThat(tiif).isNotNull();

        var builder = new MySystemRatePredicatesBuilder().with("name", ":", "TIIF")
                                                         .with("rate",">", "4.00");

        Iterable<SystemRate> results = systemRateRepository.findAll(builder.build());

        org.hamcrest.MatcherAssert.assertThat(results, contains(tiif));
    }

    @Test
    public void givenWrongNameAndRate_whenGettingListOfSystemRates_thenCorrect() {
        var tiif = systemRateRepository.findSystemRateByName("TIIF");

        assertThat(tiif).isNotNull();

        var builder = new MySystemRatePredicatesBuilder().with("name", ":", "TIIF")
                                                         .with("rate","<", "4.00");

        Iterable<SystemRate> results = systemRateRepository.findAll(builder.build());

        org.hamcrest.MatcherAssert.assertThat(results, emptyIterable());
    }

    /**
     * Predicate is generically dealing with multiple types of operations. This is because the query language is by
     * definition an open language where you can potentially filter by any field, using any supported operation.
     */
    class MySystemRatePredicate {
        private SearchCriteria criteria;

        public MySystemRatePredicate(final SearchCriteria criteria) {
            this.criteria = criteria;
        }
        public BooleanExpression getPredicate() {
            PathBuilder<SystemRate> entityPath = new PathBuilder<>(SystemRate.class, "systemRate");

            if (isBigDecimal(criteria.getValue().toString())) {
                NumberPath<BigDecimal> path = entityPath.getNumber(criteria.getKey(), BigDecimal.class);
                BigDecimal value = BigDecimal.valueOf(Double.parseDouble(criteria.getValue().toString()));

                switch (criteria.getOperation()) {
                    case ":":
                        return path.eq(value);
                    case ">":
                        return path.goe(value);
                    case "<":
                        return path.loe(value);
                }
            } else {
                StringPath path = entityPath.getString(criteria.getKey());

                if (criteria.getOperation().equalsIgnoreCase(":")) {
                    return path.containsIgnoreCase(criteria.getValue().toString());
                }
            }
            return null;
        }

        public static boolean isBigDecimal(final String str) {
            try {
                BigDecimal.valueOf(Double.parseDouble(str));
            } catch (final NumberFormatException e) {
                return false;
            }
            return true;
        }
    }

    class MySystemRatePredicatesBuilder {
        private List<SearchCriteria> params = params = new ArrayList<>();

        public MySystemRatePredicatesBuilder with(String key, String operation, Object value) {
            params.add(new SearchCriteria(key, operation, value));
            return this;
        }
        public BooleanExpression build() {
            if (params.size() == 0) {
                return null;
            }

            List<BooleanExpression> predicates = params.stream()
                                                       .map(param -> {
                                                                var predicate = new MySystemRatePredicate(param);
                                                                return predicate.getPredicate();
                                                            })
                                                       .filter(Objects::nonNull)
                                                       .collect(Collectors.toList());

            BooleanExpression result = Expressions.asBoolean(true).isTrue();
            for (BooleanExpression predicate : predicates) {
                result = result.and(predicate);
            }
            return result;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        static class BooleanExpressionWrapper {
            private BooleanExpression result;
        }
    }
}