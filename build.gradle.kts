plugins {
  kotlin("jvm") version "1.5.31"
  application
  id("org.flywaydb.flyway") version "8.5.12"
}

fun getFromEnv(value: String): String? = emptyStringToNull(System.getenv(value))
fun emptyStringToNull(value: String?): String? = if (value?.trim()?.isEmpty() != false) null else value

flyway {
  val dbURL = getFromEnv("DATABASE_HOST") ?: "localhost"
  val dbUser = getFromEnv("DATABASE_USER") ?: "postgres"
  val dbPassword = getFromEnv("DATABASE_PASSWORD") ?: "postgres"
  val dbName = getFromEnv("DATABASE_NAME") ?: "postgres"
  val dbPort = getFromEnv("DATABASE_PORT") ?: "5432"

  url = "jdbc:postgresql://$dbURL:$dbPort/$dbName"
  user = dbUser
  password = dbPassword
}

application {
  mainClass.set("MainKt")
}

group = "org.traderepublic.candlesticks"
version = "1.1.1"

repositories {
  mavenCentral()
}

object DependencyVersions {
  const val coroutines = "1.5.2"
  const val http4k = "4.13.1.0"
  const val jackson = "2.13.+"
  const val mockk = "1.12.0"
  const val postgres = "42.3.6"
  const val hikariCP = "4.0.3"
  const val typesafeConfig = "1.4.2"
  const val exposed = "0.38.2"
}

dependencies {
  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))

  implementation("com.typesafe:config:${DependencyVersions.typesafeConfig}")

  implementation(platform("org.http4k:http4k-bom:4.13.1.0"))
  implementation("org.http4k:http4k-core")
  implementation("org.http4k:http4k-server-netty")
  implementation("org.http4k:http4k-client-websocket:${DependencyVersions.http4k}")
  implementation("org.http4k:http4k-format-jackson:${DependencyVersions.http4k}")
  testImplementation("org.http4k:http4k-testing-hamkrest:${DependencyVersions.http4k}")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.coroutines}")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${DependencyVersions.coroutines}")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${DependencyVersions.jackson}")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${DependencyVersions.jackson}")
  testImplementation("io.mockk:mockk:${DependencyVersions.mockk}")

  implementation("org.postgresql:postgresql:${DependencyVersions.postgres}")
  implementation("com.zaxxer:HikariCP:${DependencyVersions.hikariCP}")

  implementation("org.jetbrains.exposed:exposed-core:${DependencyVersions.exposed}")
  implementation("org.jetbrains.exposed:exposed-dao:${DependencyVersions.exposed}")
  implementation("org.jetbrains.exposed:exposed-jdbc:${DependencyVersions.exposed}")
  implementation("org.jetbrains.exposed:exposed-java-time:${DependencyVersions.exposed}")
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    events(org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED)
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
  }
}
