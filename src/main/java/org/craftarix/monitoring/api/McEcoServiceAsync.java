package org.craftarix.monitoring.api;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.craftarix.monitoring.api.model.CurrentServerModel;
import org.craftarix.monitoring.api.model.TakeVoteModel;
import org.craftarix.monitoring.util.BukkitTasks;
import org.craftarix.monitoring.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class McEcoServiceAsync implements VoteService {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private final static String user_url = "https://minecraft.eco"; // ?????
    private final static String api_url = "https://api.minecraft.eco";

    private final String apiKey;

    public McEcoServiceAsync(String apiKey) {
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

    private CompletableFuture<HttpResponse<String>> getResponse(@NonNull String requestAddress, @NonNull String method, String writableJson) {
        try {
            var body = writableJson == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(writableJson);
            var httpRequest = HttpRequest.newBuilder(new URI(api_url + requestAddress))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "PT " + apiKey)
                    .method(method, body)
                    .build();
            return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
