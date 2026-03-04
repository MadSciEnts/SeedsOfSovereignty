/*
 * Copyright 2020 damios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Note, the above license and copyright applies to this file only.
package com.srw.seedsofsovereignty.lwjgl3

import com.badlogic.gdx.Version
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader
import org.lwjgl.system.JNI
import org.lwjgl.system.linux.UNISTD
import org.lwjgl.system.macosx.LibC
import org.lwjgl.system.macosx.ObjCRuntime
import java.io.File
import java.lang.management.ManagementFactory
import java.util.*

/**
 * A helper object for game startup, featuring three utilities related to LWJGL3 on various operating systems.
 */
object StartupHelper {
    private const val JVM_RESTARTED_ARG = "jvmIsRestarted"

    /**
     * Must only be called on Linux. Check OS first (or use short-circuit evaluation)!
     * @return whether NVIDIA drivers are present on Linux.
     */
    fun isLinuxNvidia(): Boolean {
        val drivers = File("/proc/driver").list { _, path -> path.uppercase(Locale.ROOT).contains("NVIDIA") }
        return drivers?.isNotEmpty() ?: false
    }

    /**
     * Applies the utilities as described by [StartupHelper]'s Javadoc.
     * @return whether a child JVM process was spawned or not.
     */
    @JvmOverloads
    fun startNewJvmIfRequired(inheritIO: Boolean = true): Boolean {
        val osName = System.getProperty("os.name").lowercase(Locale.ROOT)
        if (osName.contains("mac")) return startNewJvm0(true, inheritIO)
        if (osName.contains("windows")) {
            val programData = System.getenv("ProgramData") ?: "C:\\Temp"
            val prevTmpDir = System.getProperty("java.io.tmpdir", programData)
            val prevUser = System.getProperty("user.name", "libGDX_User")
            System.setProperty("java.io.tmpdir", "$programData\\libGDX-temp")
            System.setProperty("user.name", "User_${prevUser.hashCode()}_GDX${Version.VERSION}".replace('.', '_'))
            Lwjgl3NativesLoader.load()
            System.setProperty("java.io.tmpdir", prevTmpDir)
            System.setProperty("user.name", prevUser)
            return false
        }
        return startNewJvm0(false, inheritIO)
    }

    private const val MAC_JRE_ERR_MSG = "A Java installation could not be found. If you are distributing this app with a bundled JRE, be sure to set the '-XstartOnFirstThread' argument manually!"
    private const val LINUX_JRE_ERR_MSG = "A Java installation could not be found. If you are distributing this app with a bundled JRE, be sure to set the environment variable '__GL_THREADED_OPTIMIZATIONS' to '0'!"
    private const val CHILD_LOOP_ERR_MSG = "The current JVM process is a spawned child JVM process, but StartupHelper has attempted to spawn another child JVM process! This is a broken state, and should not normally happen! Your game may crash or not function properly!"

    fun startNewJvm0(isMac: Boolean, inheritIO: Boolean): Boolean {
        val processID = getProcessID(isMac)
        if (!isMac) {
            if (!isLinuxNvidia()) return false
            if ("0" == System.getenv("__GL_THREADED_OPTIMIZATIONS")) return false
        } else {
            if (System.getProperty("org.graalvm.nativeimage.imagecode", "").isNotEmpty()) return false

            val objcMsgSend = ObjCRuntime.getLibrary().getFunctionAddress("objc_msgSend")
            val nsThread = ObjCRuntime.objc_getClass("NSThread")
            val currentThread = JNI.invokePPP(nsThread, ObjCRuntime.sel_getUid("currentThread"), objcMsgSend)
            val isMainThread = JNI.invokePPZ(currentThread, ObjCRuntime.sel_getUid("isMainThread"), objcMsgSend)
            if (isMainThread) return false

            if ("1" == System.getenv("JAVA_STARTED_ON_FIRST_THREAD_$processID")) return false
        }

        if ("true" == System.getProperty(JVM_RESTARTED_ARG)) {
            System.err.println(CHILD_LOOP_ERR_MSG)
            return false
        }

        val jvmArgs = mutableListOf<String>()
        val javaExecPath = System.getProperty("java.home") + "/bin/java"
        if (!File(javaExecPath).exists()) {
            System.err.println(if (isMac) MAC_JRE_ERR_MSG else LINUX_JRE_ERR_MSG)
            return false
        }

        jvmArgs.add(javaExecPath)
        if (isMac) jvmArgs.add("-XstartOnFirstThread")
        jvmArgs.add("-D$JVM_RESTARTED_ARG=true")
        jvmArgs.addAll(ManagementFactory.getRuntimeMXBean().inputArguments)
        jvmArgs.add("-cp")
        jvmArgs.add(System.getProperty("java.class.path"))
        var mainClass = System.getenv("JAVA_MAIN_CLASS_$processID")
        if (mainClass == null) {
            val trace = Thread.currentThread().stackTrace
            if (trace.isNotEmpty()) mainClass = trace[trace.size - 1].className
            else {
                System.err.println("The main class could not be determined.")
                return false
            }
        }
        jvmArgs.add(mainClass!!)

        try {
            val processBuilder = ProcessBuilder(jvmArgs)
            if (!isMac) processBuilder.environment()["__GL_THREADED_OPTIMIZATIONS"] = "0"

            if (!inheritIO) processBuilder.start()
            else processBuilder.inheritIO().start().waitFor()
        } catch (e: Exception) {
            System.err.println("There was a problem restarting the JVM.")
            e.printStackTrace()
        }

        return true
    }

    private fun getProcessID(isMac: Boolean): Long {
        return if (isMac) LibC.getpid() else UNISTD.getpid().toLong()
    }
}
