group = 'main.java.Main'
apply plugin: 'java'
apply plugin: 'application'

sourceCompatibility = 16
targetCompatibility = 16
mainClassName = "main.java.Main"

sourceSets {
    main {
        java {
            srcDirs 'src'
        }
        resources {
            srcDir('src/main/java/main/resources')
        }

    }
}

compileJava.options.encoding = 'UTF-8'

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}

dependencies {
    implementation("net.dv8tion:JDA:4.3.0_327")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.yaml:snakeyaml:1.29")
    implementation("org.json:json:20210307")
    implementation("com.sedmelluq:lavaplayer:1.3.78")
    implementation("com.github.messenger4j:messenger4j:1.1.0")
}

repositories {
    mavenCentral()
    maven {
        name 'm2-dv8tion'
        url 'https://m2.dv8tion.net/releases'
    }
}