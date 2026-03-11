package org.craftarix.monitoring.api;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.craftarix.monitoring.api.model.CurrentServerModel;
import org.craftarix.monitoring.api.model.TakeVoteModel;
import org.craftarix.monitoring.util.BukkitTasks;
import org.craftarix.monitoring.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class McEcoServiceAsync implements VoteService {

    // FIX: HTTP без таймаута - добавлены connectTimeout и requestTimeout
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String API_URL = "https://api.minecraft.eco";

    private final String apiKey;

    public McEcoServiceAsync(String apiKey) {
        // FIX: Открытый API-ключ - проверяем наличие и предупреждаем, но не логируем значение
        if (apiKey == null || apiKey.isBlank()) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "[MinecraftEcoVote] API-ключ не задан! Укажите apiKey в config.yml");
        } else {
            // Показываем только первые 4 символа, остальное маскируем
            String masked = apiKey.substring(0, Math.min(4, apiKey.length()))
                    + "*".repeat(Math.max(0, apiKey.length() - 4));
            Bukkit.getLogger().log(Level.INFO,
                    "[MinecraftEcoVote] API-ключ загружен: " + masked);
        }
        this.apiKey = apiKey;
    }

    public int getVotes(Player player) {
        return getVotes(player.getName());
    }

    @Override
    public int getVotes(String playerName) {
        return 0;
    }

    public CompletableFuture<HttpResponse<String>> getVotesAsync(String playerName) {
        return getResponse("/mc-server-plugin/" + playerName, "GET", null);
    }

    @Override
    public void takeVote(String playerName, int votes) {
        var model = new TakeVoteModel();
        model.setValue(votes);

        BukkitTasks.runTaskAsync(() -> {
            getResponse("/mc-server-plugin/" + playerName, "POST", JsonUtil.parseJson(model));
        });
    }

    @Override
    public CurrentServerModel info() {
        return null;
    }

    private CompletableFuture<HttpResponse<String>> getResponse(
            @NonNull String requestAddress,
            @NonNull String method,
            String writableJson) {
        try {
            var body = writableJson == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(writableJson);

            var httpRequest = HttpRequest.newBuilder(new URI(API_URL + requestAddress))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "PT " + apiKey)
                    // FIX: HTTP без таймаута - таймаут на весь запрос
                    .timeout(REQUEST_TIMEOUT)
                    .method(method, body)
                    .build();

            return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // FIX: Открытый API-ключ - не выводим apiKey в стектрейсе
            Bukkit.getLogger().log(Level.SEVERE,
                    "[MinecraftEcoVote] Ошибка HTTP-запроса к " + requestAddress, e);
            return null;
        }
    }
}
