package com.pinterest.ktlint.internal

import com.pinterest.ktlint.core.initKtLintKLogger
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}.initKtLintKLogger()

class ApplyToIDEACommandHelper(
    private val applyToProject: Boolean,
    private val forceApply: Boolean,
    private val isAndroidCodeStyle: Boolean
) {
    fun apply() {
        try {
            val workDir = Paths.get(".")

            if (!forceApply && !getUserAcceptanceToUpdateFiles(workDir)) {
                logger.error { "Update canceled." }
                exitProcess(1)
            }

            IntellijIDEAIntegration.apply(
                workDir,
                false,
                isAndroidCodeStyle,
                applyToProject
            )
        } catch (e: IntellijIDEAIntegration.ProjectNotFoundException) {
            logger.error { ".idea directory not found. Are you sure you are inside project root directory?" }
            exitProcess(1)
        }

        logger.info {
            """
            |Updated.
            |Please restart your IDE.
            |If you experience any issues please report them at https://github.com/pinterest/ktlint/issues.
            """.trimMargin()
        }
    }

    private fun getUserAcceptanceToUpdateFiles(workDir: Path): Boolean {
        val fileList = IntellijIDEAIntegration.apply(
            workDir,
            true,
            isAndroidCodeStyle,
            applyToProject
        )
        logger.info {
            """
            |The following files are going to be updated:
            |${fileList.joinToString(prefix = "\t", separator = "\n\t")}
            |
            |Do you wish to proceed? [y/n]
            |(in future, use -y flag if you wish to skip confirmation)
            """.trimMargin()
        }

        val userInput = generateSequence { readLine() }
            .filter { it.trim().isNotBlank() }
            .first()

        return "y".equals(userInput, ignoreCase = true)
    }
}
