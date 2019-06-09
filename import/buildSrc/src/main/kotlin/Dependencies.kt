object Versions {
  const val kotlin = "1.3.31"
  const val aws_lambda = "1.1.0"

  const val junit_jupiter = "5.3.1"
  const val mockk = "1.9.1.kotlin12"
  const val junit_extensions = "2.3.0"
  const val kotlin_test = "3.3.2"

  const val commons_csv = "1.6"
}

object Plugins {
  const val kotlin_gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
  const val kotlin_allopen = "org.jetbrains.kotlin:kotlin-allopen:${Versions.kotlin}"
}

object Libs {
  const val kotlin_stdlib_jdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
  const val aws_lambda_java_core = "com.amazonaws:aws-lambda-java-core:${Versions.aws_lambda}"
  const val aws_lambda_java_events = "com.amazonaws:aws-lambda-java-events:${Versions.aws_lambda}"
  const val apache_csv = "org.apache.commons:commons-csv:${Versions.commons_csv}"
}

object TestLibs {
  const val junit_jupiter_api = "org.junit.jupiter:junit-jupiter-api:${Versions.junit_jupiter}"
  const val junit_jupiter_params = "org.junit.jupiter:junit-jupiter-params:${Versions.junit_jupiter}"
  const val junit_engine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit_jupiter}"
  const val mockk = "io.mockk:mockk:${Versions.mockk}"
  const val junit_extensions = "io.github.glytching:junit-extensions:${Versions.junit_extensions}"
  const val kotlin_test_runner = "io.kotlintest:kotlintest-runner-junit5:${Versions.kotlin_test}"

}
