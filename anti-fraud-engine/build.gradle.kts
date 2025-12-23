dependencies {
    implementation(project(":common-lib"))
    implementation(platform("io.projectreactor:reactor-bom:2023.0.0"))
    implementation("io.projectreactor:reactor-core")
    
    // Redis for caching (mentioned in README)
    implementation("io.lettuce:lettuce-core:6.3.0.RELEASE")
}
