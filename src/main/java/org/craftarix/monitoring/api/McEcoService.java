package org.craftarix.monitoring.api;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.craftarix.monitoring.api.model.CurrentServerModel;
import org.craftarix.monitoring.api.model.GetVotesModel;
import org.craftarix.monitoring.api.model.TakeVoteModel;
import org.craftarix.monitoring.util.JsonUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Scanner;

public class McEcoService implements VoteService {

    private final static String user_url = "https://minecraft.eco";
    private final static String api_url = "https://api.minecraft.eco";

    private final String apiKey;

    public McEcoService(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getVotes(Player player) {
        return getVotes(player.getName());
    }

    public int getVotes(String playerName) {
        var res = getResponse("/mc-server-plugin/" + playerName, "GET", null);

        if (res == null) {
            return 0;
        }

        if (res.responseCode != 200) {
            return 0;
        }

        return JsonUtil.unparseJson(res.responseString, GetVotesModel.class).getBalance();
    }

    @Override
    public void takeVote(String playerName, int votes) {
        var model = new TakeVoteModel();
        model.setValue(votes);

        getResponse("/mc-server-plugin/" + playerName, "POST", JsonUtil.parseJson(model));
    }

    @Override
    @SneakyThrows
    public CurrentServerModel info() {
        var res = getResponse("/mc-server-plugin/current-mc-server", "GET", null);

        if (res == null) {
            return null;
        }

        if (res.responseCode != 200) {
            return null;
        }

        return JsonUtil.unparseJson(res.responseString, CurrentServerModel.class);
    }

    private Response getResponse(@NonNull String requestAddress, @NonNull String method, String writableJson) {
        try {
            URL url = new URL(api_url + requestAddress);

            HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();

            httpConn.setRequestMethod(method);
            httpConn.setRequestProperty("Authorization", "PT " + apiKey);
            if (writableJson != null) {
                httpConn.setDoOutput(true);
                httpConn.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
                writer.write(writableJson);
                writer.flush();
                writer.close();
                httpConn.getOutputStream().close();
            }

            InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                    ? httpConn.getInputStream()
                    : httpConn.getErrorStream();
            Scanner s = new Scanner(responseStream).useDelimiter("\\A");
            String response = s.hasNext() ? s.next() : "";
            return new Response(httpConn.getResponseCode(), response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static class Response {
        public int responseCode;
        public String responseString;

        public Response(int responseCode, String responseString) {
            this.responseCode = responseCode;
            this.responseString = responseString;
        }
    }
}
