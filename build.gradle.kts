import org.gradle.internal.classpath.Instrumented.systemProperty
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.jetbrains.kotlin.plugin.allopen") version "1.8.21"
	kotlin("jvm") version "1.8.21"
	kotlin("plugin.spring") version "1.8.21"
	kotlin("plugin.jpa") version "1.8.21"
	kotlin("kapt") version "1.8.21"
	kotlin("plugin.lombok") version "1.9.0"
	id("io.freefair.lombok") version "8.1.0"
}

group = "com.ailegorreta"
version = "2.0.0"
description = "Micro service for any system parameter, variables, templates and catalogs. Imperative stack"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}


repositories {
	mavenLocal()
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }

	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/" +
		project.findProperty("registryPackageUrl") as String? ?:
			System.getenv("URL_PACKAGE") ?:
			"rlegorreta/ailegorreta-kit")
		credentials {
			username = project.findProperty("registryUsername") as String? ?:
					System.getenv("USERNAME") ?:
					"rlegorreta"
			password = project.findProperty("registryToken") as String? ?: System.getenv("TOKEN")
		}
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

extra["springCloudVersion"] = "2022.0.3"
extra["testcontainersVersion"] = "1.18.1"
extra["otelVersion"] = "1.26.0"
extra["ailegorreta-kit-version"] = "2.0.0"
extra["queryDslVersion"] = "5.0.0"

dependencies {
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	// implementation("org.springframework.boot:spring-boot-starter-webflux")		// Reactive version

	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	// implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")	// Reactive version
	// implementation("org.springframework.retry:spring-retry")						// Reactive version
	implementation("org.springframework.boot:spring-boot-starter-graphql")

	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client") {
		exclude(group = "org.springframework.cloud", module = "spring-cloud-starter-ribbon")
		exclude(group = "com.netflix.ribbon", module = "ribbon-eureka")
	}

	implementation("org.springframework.cloud:spring-cloud-stream")
	implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	kapt("org.springframework.boot:spring-boot-configuration-processor")
	// ^ for information of kapt example see:
	// https://aregall.tech/integration-between-querydsl-and-spring-data-rest-using-kotlin-gradle-and-spring-boot-3

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	// implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")		// Reactive version
	// implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")      // Reactive version
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")

	implementation("org.projectlombok:lombok")
	kapt("org.projectlombok:lombok")

	implementation("com.ailegorreta:ailegorreta-kit-commons-utils:${property("ailegorreta-kit-version")}")
	implementation("com.ailegorreta:ailegorreta-kit-resource-server-security:${property("ailegorreta-kit-version")}")
	implementation("com.ailegorreta:ailegorreta-kit-commons-event:${property("ailegorreta-kit-version")}")
	implementation("com.ailegorreta:ailegorreta-kit-data-jpa:${property("ailegorreta-kit-version")}")

	implementation("com.querydsl:querydsl-core:${property("queryDslVersion")}")
	implementation(group = "com.querydsl",name = "querydsl-jpa", version = "${property("queryDslVersion")}", classifier = "jakarta")
	kapt(group = "com.querydsl", name = "querydsl-apt", version = "${property("queryDslVersion")}", classifier = "jakarta")

	/* Flyway does not support R2DBC yet, so we need to provide a JDBC driver to communicate with the database */
	runtimeOnly("org.flywaydb:flyway-core")
	runtimeOnly("org.postgresql:postgresql")
	// runtimeOnly("org.postgresql:r2dbc-postgresql")					// Reactive version
	runtimeOnly("org.springframework:spring-jdbc")
	/* ^ Spring integration with the JDBC API. It’s part of the Spring Framework, not to be confused with
	     Spring Data JDBC. */

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	// testImplementation("org.testcontainers:r2dbc")					// Reactive version
	testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
	// testImplementation("io.projectreactor:reactor-test")				// this is for web-flux testing
	testImplementation("com.squareup.okhttp3:mockwebserver")

	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:kafka")
	testImplementation("org.testcontainers:postgresql")

}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
		mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
	}
}

tasks.named<BootBuildImage>("bootBuildImage") {
	environment.set(environment.get() + mapOf("BP_JVM_VERSION" to "17.*"))
	imageName.set("ailegorreta/${project.name}")
	docker {
		publishRegistry {
			username.set(project.findProperty("registryUsername").toString())
			password.set(project.findProperty("registryToken").toString())
			url.set(project.findProperty("registryUrl").toString())
		}
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		freeCompilerArgs += "-Xjvm-default=all-compatibility"			// needed to override default methods on interfaces
		jvmTarget = "17"
	}
}

kapt {
	keepJavacAnnotationProcessors = true

	javacOptions {
		option("querydsl.entityAccessors", true)
	}
	arguments {
		arg("plugin", "com.querydsl.apt.jpa.JPAAnnotationProcessor")
	}
}

kotlinLombok {
	lombokConfigurationFile(file("lombok.config"))
}

tasks.withType<Test> {
	useJUnitPlatform()
}

allOpen {
	annotation("jakarta.persistence.Entity")
}
