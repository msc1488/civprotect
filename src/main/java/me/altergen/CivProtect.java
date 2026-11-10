package me.altergen;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author altergen
 * Version: 2.0
 */
public class CivProtect extends JavaPlugin implements Listener {
    private List<String> whitelistStar = new ArrayList<>();
    private List<String> whitelistOp = new ArrayList<>();
    private String webhookUrl = "";
    private String banReasonStar = "⛔ Нельзя взламывать звезду! Обратитесь к администрации.";
    private String banReasonOp = "⛔ Вы были забанены за незаконное использование прав администратора!";
    private String webhookMessageStar = "🔨 {player} забанен за наличие звезды!";
    private String webhookMessageOp = "⚠️ {player} (OP) забанен за злоупотребление правами!";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("civprotect").setExecutor(new CivProtectCommand(this));
        getCommand("unbanstar").setExecutor(new UnbanStarCommand());
        
        // Запускаем периодическую проверку всех игроков
        startPeriodicCheck();
        
        getLogger().info("✅ CivProtect от altergen включён!");
    }
    
    private void startPeriodicCheck() {
        // Проверяем каждый тик (максимальная частота)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkAndBan(player);
            }
        }, 1L, 1L);
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ CivProtect выключён!");
    }

    public void reload() {
        reloadConfig();
        loadConfig();
        getLogger().info("✅ CivProtect перезагружен!");
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        whitelistStar = config.getStringList("whitelist-star");
        if (whitelistStar == null) whitelistStar = new ArrayList<>();
        whitelistOp = config.getStringList("whitelist-op");
        if (whitelistOp == null) whitelistOp = new ArrayList<>();
        webhookUrl = config.getString("webhook-url", "");
        banReasonStar = config.getString("ban-reason-star", "⛔ Нельзя взламывать звезду! Обратитесь к администрации.");
        banReasonOp = config.getString("ban-reason-op", "⛔ Вы были забанены за незаконное использование прав администратора!");
        webhookMessageStar = config.getString("webhook-message-star", "🔨 {player} забанен за наличие звезды!");
        webhookMessageOp = config.getString("webhook-message-op", "⚠️ {player} (OP) забанен за злоупотребление правами!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkAndBan(player);
    }

    private void checkAndBan(Player player) {
        // Проверяем, не забанен ли уже игрок
        if (Bukkit.getBanList(BanList.Type.NAME).isBanned(player.getName())) return;
        
        // === Если игрок в whitelist-op — полный иммунитет (OP И звёздочки) ===
        if (whitelistOp.contains(player.getName())) {
            return;
        }
        
        // === Проверка на звёздочки (только если не в whitelist-op) ===
        if (!whitelistStar.contains(player.getName())) {
            if (player.hasPermission("*") || player.hasPermission("luckperms.*") ||
                player.hasPermission("bukkit.*") || player.hasPermission("minecraft.*") ||
                player.hasPermission("*.*")) {
                
                String reason = ChatColor.translateAlternateColorCodes('&', banReasonStar);
                banPlayer(player, reason, "star");
                return;
            }
        }
        
        // === Проверка на OP (только если не в whitelist-op) ===
        if (player.isOp()) {
            String reason = ChatColor.translateAlternateColorCodes('&', banReasonOp);
            banPlayer(player, reason, "op");
        }
    }
    
    private void banPlayer(Player player, String reason, String type) {
        Bukkit.getScheduler().runTask(this, () -> {
            player.kickPlayer(reason);
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, null);
        });
        
        if (type.equals("star")) {
            getLogger().info("🔨 " + player.getName() + " забанен за звёздочку!");
            sendDiscordEmbed(player, banReasonStar, webhookMessageStar, "star");
        } else {
            getLogger().info("⚠️ " + player.getName() + " забанен за незаконное OP!");
            sendDiscordEmbed(player, banReasonOp, webhookMessageOp, "op");
        }
    }

    private void sendDiscordEmbed(Player player, String reason, String message, String type) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(webhookUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Цвет Embed: красный для звёздочки, оранжевый для OP
            String color = type.equals("star") ? "16711680" : "16744448";
            String title = type.equals("star") ? "🔨 Бан за звёздочку" : "⚠️ Бан за OP";
            
            String reasonText = ChatColor.stripColor(reason);
            
            String json = "{" +
                "\"embeds\": [{" +
                    "\"title\": \"" + title + "\"," +
                    "\"color\": " + color + "," +
                    "\"fields\": [" +
                        "{\"name\": \"Ник\", \"value\": \"" + player.getName() + "\", \"inline\": true}," +
                        "{\"name\": \"UUID\", \"value\": \"" + player.getUniqueId().toString() + "\", \"inline\": true}," +
                        "{\"name\": \"Причина\", \"value\": \"" + reasonText + "\", \"inline\": false}" +
                    "]," +
                    "\"thumbnail\": {\"url\": \"https://crafatar.com/avatars/" + player.getUniqueId().toString() + "?size=128\"}," +
                    "\"footer\": {\"text\": \"CivProtect v2.0 by altergen\"}," +
                    "\"timestamp\": \"" + java.time.Instant.now() + "\"" +
                "}]" +
            "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            getLogger().warning("❌ Не удалось отправить в вебхук: " + e.getMessage());
        }
    }

    public List<String> getWhitelistStar() {
        return whitelistStar;
    }

    public List<String> getWhitelistOp() {
        return whitelistOp;
    }

    public void addToWhitelistStar(String name) {
        if (!whitelistStar.contains(name)) {
            whitelistStar.add(name);
            saveWhitelists();
        }
    }

    public void removeFromWhitelistStar(String name) {
        whitelistStar.remove(name);
        saveWhitelists();
    }

    public void addToWhitelistOp(String name) {
        if (!whitelistOp.contains(name)) {
            whitelistOp.add(name);
            saveWhitelists();
        }
    }

    public void removeFromWhitelistOp(String name) {
        whitelistOp.remove(name);
        saveWhitelists();
    }

    private void saveWhitelists() {
        FileConfiguration config = getConfig();
        config.set("whitelist-star", whitelistStar);
        config.set("whitelist-op", whitelistOp);
        saveConfig();
    }

    public String getBanReasonStar() {
        return banReasonStar;
    }
    
    public String getBanReasonOp() {
        return banReasonOp;
    }
}