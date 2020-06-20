package com.minelittlepony.intermediary2srg.test

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * This test runs the example project in `example/` and verifies that it
 * finishes successfully.
 */
class BuildLogicFunctionalTest {

    @get:Rule
    var tempFolder = TemporaryFolder()

    @Before
    fun init() {
        // copy everything from the base project directory.
        val testResources = File("example")
        listOf("src", "build.gradle", "gradle.properties", "settings.gradle").forEach {
            File(testResources, it).copyRecursively(File(tempFolder.root, it))
        }

        assertTrue(File(tempFolder.root, "build.gradle").exists())
    }

    private fun newGradleRunner(vararg args: String) = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(tempFolder.root)
            .withArguments(*args)
            .build()

    @Test
    fun `test apply`() {
        val result = newGradleRunner("check")

        assertNotEquals(FAILED, result.task(":check")?.outcome)
    }

    @Test
    fun `test generating searge mappings`() {
        val result = newGradleRunner("generateInt2SrgMappings")

        assertEquals(SUCCESS, result.task(":generateInt2SrgMappings")?.outcome)
        assertEquals(1, File(tempFolder.root, "build/tmp/generateInt2SrgMappings").list()?.size)
    }

    @Test
    fun `test full build`() {
        val result = newGradleRunner("build")

        assertEquals(SUCCESS, result.task(":remapSrgJar")?.outcome)
        assertEquals(SUCCESS, result.task(":remapSrgSourcesJar")?.outcome)

        assertTrue(File(tempFolder.root, "build/libs/example-1.0.jar").exists())
        assertTrue(File(tempFolder.root, "build/libs/example-1.0-sources.jar").exists())
        assertTrue(File(tempFolder.root, "build/libs/example-intermediary-1.0.jar").exists())
        assertTrue(File(tempFolder.root, "build/libs/example-intermediary-1.0-sources.jar").exists())
        assertTrue(File(tempFolder.root, "build/libs/example-searge-1.0-sources.jar").exists())
        assertTrue(File(tempFolder.root, "build/libs/example-searge-1.0.jar").exists())
    }
}
