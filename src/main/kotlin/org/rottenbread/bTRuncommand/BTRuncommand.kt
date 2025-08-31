package org.rottenbread.bTRuncommand

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class BTRuncommand : JavaPlugin() {

    override fun onEnable() {
        loadCommand()
        getCommand("btruncmd")?.setExecutor(this)
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTasks(this)
    }

    private fun reloadCmd(sender: CommandSender) {
        Bukkit.getScheduler().cancelTasks(this)
        loadCommand()
        sender.sendMessage("§a[서버]§7 설정을 성공적으로 리로드했습니다.")
    }

    private fun loadCommand() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        val configFile = File(dataFolder, "run.yml")
        if (!configFile.exists()) {
            saveResource("run.yml", false)
        }
        val config = YamlConfiguration.loadConfiguration(configFile)

        val tasks = config.getMapList("tasks")
        if (tasks.isEmpty()) {
            logger.warning("run.yml에 실행할 작업이 없습니다.")
            return
        }

        val console = Bukkit.getConsoleSender()
        logger.info("${tasks.size}개의 작업을 스케줄에 등록합니다.")

        for (task in tasks) {
            val name = task["name"] as? String ?: "이름없음"
            val timeStr = task["time"] as? String
            val commands = task["commands"] as? List<*>

            if (timeStr == null || commands == null || commands.isEmpty()) {
                logger.warning("$name 작업의 설정이 올바르지 않아 건너뜁니다.")
                continue
            }

            try {
                val targetTime = LocalTime.parse(timeStr)
                val now = LocalTime.now()

                var delaySeconds = ChronoUnit.SECONDS.between(now, targetTime)
                if (delaySeconds < 0) {
                    delaySeconds += 24 * 60 * 60
                }

                val delayTicks = delaySeconds * 20L
                val periodTicks = 24 * 60 * 60 * 20L

                Bukkit.getScheduler().runTaskTimer(this, Runnable {
                    logger.info("$name 작업을 실행합니다.")
                    commands.forEach { command ->
                        Bukkit.dispatchCommand(console, command.toString())
                    }
                }, delayTicks, periodTicks)

                logger.info("$name 작업이 등록되었습니다. (다음 실행까지 약 ${delaySeconds / 60}분)")

            } catch (e: Exception) {
                logger.warning("$name 작업의 시간 형식('$timeStr')이 잘못되었습니다: ${e.message}")
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name.equals("btruncmd", ignoreCase = true)) {
            if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
                if (!sender.hasPermission("btruncmd.reload")) {
                    sender.sendMessage("§c당신은 이 명령어를 사용할 권한이 없습니다.")
                    return true
                }

                reloadCmd(sender)
                sender.sendMessage("§a[서버]§7 run.yml 설정을 성공적으로 리로드했습니다.")
                return true
            } else {
                sender.sendMessage("§a[서버]§7 잘못된 사용법입니다. /btruncmd reload")
                return true
            }
        }
        return false
    }
}