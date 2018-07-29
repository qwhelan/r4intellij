package com.r4intellij.skeletons

import com.r4intellij.RFileType.DOT_R_EXTENSION
import com.r4intellij.RTestCase
import com.r4intellij.packages.RHelperUtil
import com.r4intellij.packages.RSkeletonGenerator.RHELPER_SKELETONIZE_PACKAGE
import com.r4intellij.packages.RSkeletonGenerator.isValidSkeleton
import org.intellij.lang.annotations.Language
import java.io.File

/**
 * @author Holger Brandl
 */

class SkeletonTest : RTestCase() {

    val TEST_DIRECTORY = File("testData", "unit_test_skeletons")

    //    companion object {
    //
    //        @BeforeClass @JvmStatic
    //        fun buildSkeletons() {
    //
    //        }
    //
    //    }

    fun testParsability() {
        val testPackages = listOf(//"base", // must work
                "stats" // must work
//                "dplyr", // must work
//                "ggplot2", // correct serialization of objects like GeomBar
//                "lubridate", // issues with embedded <s4 objects?
//                "R.utils", // issues with GenericSummary
//                "graphics"
        )

        TEST_DIRECTORY.mkdir()

        val buildSkeleton = { pckg: String ->
            RHelperUtil.runHelperWithArgs(RHELPER_SKELETONIZE_PACKAGE, pckg, File(TEST_DIRECTORY, pckg + DOT_R_EXTENSION).absolutePath)
        }

        val parseRunStatii = testPackages.map(buildSkeleton)
        assertFalse(parseRunStatii.any { runResult -> runResult!!.exitCode != 0 })

        testPackages.forEach { pckg ->
            System.out.println(pckg)
            val skeletonFile = File(TEST_DIRECTORY, pckg + DOT_R_EXTENSION)
            System.out.println(skeletonFile.toString())

            assertTrue(isValidSkeleton(skeletonFile))

            myFixture.configureByFile(skeletonFile.toString().replace(File.separatorChar, '/'))
            myFixture.checkHighlighting() // should be all green
        }

        // test a few assumptions about what the skeletons should incldue

        // numeric package constants
        assertContainsLine("ggplot2", """.pt <- 2.84527559055118""")

        // complex objects
        assertContainsLine("ggplot2", """GeomBar <- "<environment>"""")

        // re-exported symbols
        assertContainsLine("dplyr", """data_frame <- tibble::data_frame""")

        // bundled data sets
        assertContainsLine("dplyr", """nasa <- dplyr::nasa""")

        // base tests
        //        assertContainsLine("base", """nasa <- dplyr::nasa""")

    }

    private fun assertContainsLine(packageName: String, @Language("R") line: String) {
        assertTrue(File(TEST_DIRECTORY, "$packageName.R").readLines().any { x ->
            x.contains(line)
        })
    }
}