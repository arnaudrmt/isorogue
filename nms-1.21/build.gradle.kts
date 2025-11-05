dependencies {
    implementation(project(":core"))
    compileOnly("org.spigotmc:spigot:1.21.1-R0.1-SNAPSHOT:remapped-mojang")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}