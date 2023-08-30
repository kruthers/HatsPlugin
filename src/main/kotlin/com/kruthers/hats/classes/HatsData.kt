package com.kruthers.hats.classes

import com.kruthers.hats.HatsPlugin
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class HatsData(private val plugin: HatsPlugin) {
    private val data = YamlConfiguration()
    private val file = File(plugin.dataFolder, "hats.yml")
    private var isInitialised = false

    fun init() {
        if (isInitialised) throw  Exception("Manager already initialized")

        //load config
        this.loadConfig()

        //start runner
        this.isInitialised = true

        //load hats
        this.loadHats()
    }

    private fun loadConfig(): Boolean {
        var loaded = false

        try {
            data.load(file)
            loaded = true
        } catch (error: FileNotFoundException) {
            this.plugin.logger.warning("Failed to find data file, creating new one")
            this.createConfig()
        } catch (error: IOException) {
            this.plugin.logger.warning("Unable to read data file $error")
        } catch (error: InvalidConfigurationException) {
            this.plugin.logger.warning("Unable to load data $error")
        }

//        if (!loaded) {
//            this.plugin.getResource("hats.yml").let { this.data.load(it.toString()) }
//        }
        return loaded
    }

    private fun createConfig() {
        if(!file.exists()) {
            plugin.logger.fine("hats.yml does not exist, creating it now")
            plugin.saveResource("hats.yml", false)
        }

        data.load(file)
    }

    fun loadHats() {
        val hats: HashMap<String, Hat> = hashMapOf()
        this.data.getList("hats")?.forEach { it ->
            if (it is Hat) {
                hats[it.id] = it
            }
        }
        HatsPlugin.hats.putAll(hats)
    }

    fun saveHats() {
        if (isInitialised) {
            this.data.set("hats", HatsPlugin.hats.values.toList())
            this.data.save(this.file)
            this.plugin.logger.info("Saved hats")
        }
    }

}