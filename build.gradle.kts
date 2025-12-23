plugins {
    java
    `java-library`
    id("io.freefair.lombok") version "8.4" apply false
    id("net.ltgt.errorprone") version "3.1.0" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")
    apply(plugin = "net.ltgt.errorprone")

    group = "com.fintech.ledger"
    version = "0.0.1-SNAPSHOT"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        // MapStruct
        implementation("org.mapstruct:mapstruct:1.5.5.Final")
        annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

        // ErrorProne
        errorprone("com.google.errorprone:error_prone_core:2.23.0")

        // Testing
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.mockito:mockito-core:5.7.0")
        testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
        testImplementation("org.assertj:assertj-core:3.24.2")
    }

    tasks.test {
        useJUnitPlatform()
    }
    
    // Lombok is handled by the freefair plugin
}
