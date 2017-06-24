package modules.music;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.PlaylistRequest;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.ClientCredentials;
import com.wrapper.spotify.models.Playlist;
import com.wrapper.spotify.models.PlaylistTrack;
import tools.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpotifyToYoutube {

    public ArrayList<music.Track> convert(String URL) {
        final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
        final JsonFactory JSON_FACTORY = new JacksonFactory();

        try {
            Api api = Api.builder().clientId(Constants.SPOTIFY_CLIENT_ID).clientSecret(Constants.CLIENT_SECRET).build();
            final ClientCredentialsGrantRequest request = api.clientCredentialsGrant().build();
            final SettableFuture<ClientCredentials> responseFuture = request.getAsync();
            Futures.addCallback(responseFuture, new FutureCallback<ClientCredentials>() {
                @Override
                public void onSuccess(ClientCredentials clientCredentials) {
                    api.setAccessToken(clientCredentials.getAccessToken());
                }
                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
            final PlaylistRequest request2 = api.getPlaylist(URL.split("/")[4], URL.split("/")[6]).build();
            final Playlist playlist = request2.get();


            ArrayList<music.Track> tracks = new ArrayList<>();

            // Search YouTube for a similar tracks
            YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, request1 -> {
            }).setApplicationName("JukeBot").build();
            YouTube.Search.List search = youtube.search().list("id,snippet");
            search.setKey(Constants.YOUTUBE_API_KEY);
            search.setType("video");
            search.setMaxResults((long)1);

            for(PlaylistTrack t : playlist.getTracks().getItems()) {
                search.setQ(t.getTrack().getName());
                SearchListResponse searchListResponse = search.execute();
                List<SearchResult> result = searchListResponse.getItems();
                tracks.add(new music.Track("https://www.youtube.com/watch?v="+getID(result.iterator())));
            }
            return tracks;
        } catch(Exception e) {
            System.err.println("An error occured while trying to process Spotify playlist");
        }
        return null;
    }
    private static String getID(Iterator<SearchResult> iteratorSearchResults) {
        return iteratorSearchResults.next().getId().getVideoId();
    }

}
