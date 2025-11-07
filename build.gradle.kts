plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "OTC"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.flywaydb:flyway-core")
    // Add MySQL database support module for Flyway to fix "Unsupported Database: MySQL 8.0"
    implementation("org.flywaydb:flyway-mysql:11.7.2")

    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.12")
	testImplementation("org.testcontainers:junit-jupiter:1.20.2")
	testImplementation("org.testcontainers:mysql:1.20.2")
	testImplementation("org.testcontainers:kafka:1.20.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 基础构建门禁：禁止 DSV/DAT 层出现“野生导入”外部类（Aliases.kt 白名单）
tasks.register("checkWildImports") {
    group = "verification"
    description = "Fail build if disallowed external imports are used outside Aliases.kt"
    doLast {
        val projectDir = project.projectDir
        val targetDirs = listOf(
            File(projectDir, "src/main/kotlin/com/example/otc/dsv"),
            File(projectDir, "src/main/kotlin/com/example/otc/dat1"),
            File(projectDir, "src/main/kotlin/com/example/otc/dat2")
        ).filter { it.exists() }

        val disallowed = listOf(
            Regex("^import\\s+java\\.time\\.(Instant|Duration|LocalDate|LocalDateTime)", RegexOption.MULTILINE),
            Regex("^import\\s+java\\.util\\.UUID", RegexOption.MULTILINE),
            Regex("^import\\s+java\\.math\\.BigDecimal", RegexOption.MULTILINE),
            Regex("^import\\s+com\\.fasterxml\\.jackson\\.databind\\.ObjectMapper", RegexOption.MULTILINE),
            Regex("^import\\s+org\\.springframework\\.data\\.mongodb\\.core\\.MongoTemplate", RegexOption.MULTILINE),
            Regex("^import\\s+org\\.springframework\\.kafka\\.core\\.KafkaTemplate", RegexOption.MULTILINE),
            Regex("^import\\s+org\\.slf4j\\.LoggerFactory", RegexOption.MULTILINE),
            Regex("^import\\s+org\\.springframework\\.http\\.ResponseEntity", RegexOption.MULTILINE),
            Regex("^import\\s+org\\.springframework\\.core\\.io\\.ResourceLoader", RegexOption.MULTILINE)
        )

        val allowList = setOf(
            File(projectDir, "src/main/kotlin/com/example/otc/common/lang/Aliases.kt").absolutePath
        )

        val violations = mutableListOf<String>()
        targetDirs.forEach { dir ->
            dir.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
                val path = file.absolutePath
                if (allowList.contains(path)) return@forEach
                val content = file.readText()
                disallowed.forEach { re ->
                    val match = re.find(content)
                    if (match != null) {
                        violations.add("${file.relativeTo(projectDir)} -> '${match.value.trim()}'")
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            logger.error("Disallowed external imports detected (use Aliases.kt).\n" + violations.joinToString("\n"))
            throw GradleException("Wild imports detected in DSV/DAT layers. See log above.")
        } else {
            logger.lifecycle("checkWildImports passed: no wild imports in DSV/DAT layers.")
        }
    }
}

tasks.named("check") {
    dependsOn("checkWildImports")
}
