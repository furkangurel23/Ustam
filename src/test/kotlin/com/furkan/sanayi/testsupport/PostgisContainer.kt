package com.furkan.sanayi.testsupport

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Testcontainers'ın PostgreSQLContainer'ını PostGIS imajıyla kullanmak için tip güvenli alt sınıf.
 * Bu sayede withDatabaseName/withUsername/withPassword zincirlenirken tip bozulmaz.
 * With zincirine cozum
 */
class PostgisContainer private constructor(imageName: DockerImageName) :
    PostgreSQLContainer<PostgisContainer>(imageName) {

    companion object {
        fun instance(): PostgisContainer {
            val img = DockerImageName
                .parse("postgis/postgis:16-3.4")
                .asCompatibleSubstituteFor("postgres")
            return PostgisContainer(img)
        }
    }
}