package io.github.doorbash.myrootbypass

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import org.luckypray.dexkit.DexKitBridge

class ModuleMain : XposedModule() {

    companion object {
        const val TAG = "MyRootBypassModule"

        init {
            System.loadLibrary("dexkit")
        }
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "onModuleLoaded: " + param.processName)
        log(Log.INFO, TAG, "framework: $frameworkName($frameworkVersionCode) API $apiVersion")

        val hasProp: (Long) -> Boolean = { prop -> frameworkProperties.and(prop) != 0L }
        log(Log.INFO, TAG, "system supported: " + hasProp(PROP_CAP_SYSTEM))
        log(Log.INFO, TAG, "remote supported: " + hasProp(PROP_CAP_REMOTE))
        log(Log.INFO, TAG, "api protection: " + hasProp(PROP_RT_API_PROTECTION))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPackageLoaded(param: PackageLoadedParam) {
        log(Log.INFO, TAG, "onPackageLoaded: " + param.packageName)
        log(Log.INFO, TAG, "default classloader is " + param.defaultClassLoader)
    }

    @SuppressLint("DuplicateCreateDexKit")
    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        log(Log.INFO, TAG, "onPackageReady: " + param.packageName)
        log(Log.INFO, TAG, "app classloader is " + param.classLoader)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            log(Log.INFO, TAG, "app acf is " + param.appComponentFactory)
        }
        log(Log.INFO, TAG, "module apk path: " + this.moduleApplicationInfo.sourceDir)


        when (param.packageName) {
            "ir.nasim" -> { // bale
                DexKitBridge.create(param.applicationInfo.sourceDir).use { bridge ->
                    val method = bridge.findMethod {
                        searchPackages("ir.nasim")
                        matcher {
                            returnType = "int"
                            usingStrings("rootTypes")
                        }
                    }.singleOrNull() ?: error("The returned result is not unique")

                    hook(method.getMethodInstance(param.classLoader)).intercept {
                        // log(Log.INFO, TAG, "bypassing root check for ${param.packageName}")
                        0
                    }
                }
            }

            "com.pdpsoft.android.saapa" -> { // barghe man
                DexKitBridge.create(param.applicationInfo.sourceDir).use { bridge ->
                    val method = bridge.findMethod {
                        excludePackages("android", "androidx", "com", "de", "io", "ir", "kotlin", "kotlinx", "net", "org", "okhttp3", "retrofit2")
                        matcher {
                            returnType = "boolean"
                            invokeMethods {
                                add {
                                    returnType = "boolean"
                                    usingStrings("which", "su")
                                }
                            }
                        }
                    }.singleOrNull() ?: error("The returned result is not unique")

                    hook(method.getMethodInstance(param.classLoader)).intercept {
                        log(Log.INFO, TAG, "bypassing root check for ${param.packageName}")
                        false
                    }
                }
            }

            "com.sibche.aspardproject.app" -> { // up
                DexKitBridge.create(param.applicationInfo.sourceDir).use { bridge ->
                    val method = bridge.findMethod {
                        excludePackages("android", "androidx", "coil", "com", "dagger", "java", "javax", "junit", "kankan", "kotlin", "kotlinx", "okhttp3", "okio", "org")
                        matcher {
                            invokeMethods {
                                add {
                                    returnType = "boolean"
                                    invokeMethods {
                                        add {
                                            returnType = "boolean"
                                            usingStrings("Emulator", "FINGERPRINT")
                                        }
                                    }
                                }
                            }
                        }
                    }.singleOrNull() ?: error("The returned result is not unique")

                    hook(method.getMethodInstance(param.classLoader)).intercept {
                        // log(Log.INFO, TAG, "bypassing root check for ${param.packageName}")
                    }
                }
            }
        }
    }

    override fun onSystemServerStarting(param: XposedModuleInterface.SystemServerStartingParam) {
        log(Log.INFO, TAG, "onSystemServerStarting, system classloader: " + param.classLoader)
    }
}