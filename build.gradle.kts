plugins {
    base
    idea
    id("java")
}

group = "git.str1llax"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.15.0")
}

sourceSets {
    main {
        java.srcDir("src/main/java")
    }
}
