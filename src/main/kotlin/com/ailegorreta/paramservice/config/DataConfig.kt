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
 *  DataConfig.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.paramservice.config

import com.ailegorreta.paramservice.domain.Datasource
import com.ailegorreta.paramservice.domain.DatasourceField
import com.ailegorreta.paramservice.domain.Template
import com.ailegorreta.paramservice.domain.TemplateField
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.rest.core.config.RepositoryRestConfiguration
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.config.annotation.CorsRegistry
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import javax.sql.DataSource

/**
 * Enables Postgres auditing operations
 *
 * To optimize the starting for this microservice in terms of synchronous starting the JPA repositories, see:
 *
 * https://www.baeldung.com/jpa-bootstrap-mode
 *
 * The DEFAULT bootstrapMode is for synchronous repositories start up.
 *
 * DEFERRED is the right option to use when bootstrapping JPA asynchronously. As a result, repositories don't wait for
 * the EntityManagerFactory‘s initialization.
 *
 * @author rlh
 * @project : param-service
 * @date August 2023
 *
 */
@Configuration
@EnableJpaAuditing
// ^ this package must be included in order to instantiate de UserContext
class DataConfig {

    @Bean
    fun auditorAware(): AuditorAware<String>? {
        return AuditorAware {
            Optional.ofNullable(SecurityContextHolder.getContext())
                .map { auth: SecurityContext -> auth.authentication }
                .filter { auth: Authentication -> auth.isAuthenticated }
                .map { user: Authentication -> user.name }
        }
    }

    /**
     * This bean is for Deferred is the right option to use when bootstrapping JPA asynchronously.
     *
     * Declare an AsyncTaskExecutor in a configuration class by using ThreadPoolTaskExecutor – one of its Spring
     * implementations – and override the submit method, which returns a Future:
     */
    @Bean
    fun delayedTaskExecutor(): AsyncTaskExecutor {
        return object : ThreadPoolTaskExecutor() {
            override fun <T> submit(task: Callable<T>): Future<T> {
                return super.submit(Callable {
                    Thread.sleep(5000)
                    task.call()
                })
            }
        }
    }

    /**
     * Add an EntityManagerFactory bean to our configuration, and indicate that we want to use our asynchronous
     * executor for background bootstrapping
     */
    @Bean
    fun entityManagerFactory(dataSource: DataSource, delayedTaskExecutor: AsyncTaskExecutor):
            LocalContainerEntityManagerFactoryBean? {
                val factory = LocalContainerEntityManagerFactoryBean()

                factory.setPackagesToScan("com.ailegorreta.paramservice.domain")
                factory.jpaVendorAdapter = HibernateJpaVendorAdapter()
                factory.dataSource = dataSource
                factory.bootstrapExecutor = delayedTaskExecutor

                val properties: MutableMap<String, Any> = HashMap()

                properties["hibernate.hbm2ddl.auto"] = "none"
                factory.setJpaPropertyMap(properties)

                return factory
    }

    /**
     * This configuration is for Spring Data REST. The example so we include in the API response the ID entities
     * because by default they are not included. Many other configuration are possible, like the Jackson ObjectMapper
     * definition and validation event listeners, among others. See Spring Date REST reference documentation.
     */
    @Bean
    fun repositoryRestConfigurer(): RepositoryRestConfigurer {
        return object : RepositoryRestConfigurer {

            override fun configureRepositoryRestConfiguration(config: RepositoryRestConfiguration, cors: CorsRegistry?) {
                config.exposeIdsFor(
                    Datasource::class.java,
                    DatasourceField::class.java,
                    Template::class.java,
                    TemplateField::class.java
                )
            }
        }
    }
}