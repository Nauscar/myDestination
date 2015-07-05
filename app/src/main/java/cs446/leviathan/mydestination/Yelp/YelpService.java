package cs446.leviathan.mydestination.yelp;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * Created by Aaron Kelly-Barker on 2015-07-04.
 */
public class YelpService {

    private OAuthService service;
    private APICallResult apiCallResult;
    private Token token;
    private AsyncTask<Context, Void, Void> mtask;

    public YelpService(String consumerKey, String consumerSecret, String tokenKey, String tokenSecret) {
        this.service = new ServiceBuilder()
                .provider(YelpAuth.class)
                .apiKey(consumerKey)
                .apiSecret(consumerSecret).build();
        this.token = new Token(tokenKey, tokenSecret);
    }

    public boolean getLocalPlaces(final Location location, APICallResult result) {
        if (location == null) {
            return false;
        }
        this.apiCallResult = result;
        OAuthRequest request = createOAuthRequest("/v2/search");
        request.addQuerystringParameter("limit", "15");
        request.addQuerystringParameter("sort", "1");
        request.addQuerystringParameter("radius_filter", "5000");
        request.addQuerystringParameter("ll", String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));

        this.service.signRequest(token, request);

        mtask = new HandleAPICalls(request);
        mtask.execute();

        return true;
    }

    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com" + path);
        return request;
    }

    class HandleAPICalls extends AsyncTask<Context, Void, Void>
    {
        private OAuthRequest request;

        public HandleAPICalls(OAuthRequest request) {
            this.request = request;
        }

        protected Void doInBackground(Context... params)
        {
            Response response = request.send();
            apiCallResult.APICallback(response);
            return null;
        }
    }

    public static abstract class APICallResult {
        public abstract void APICallback(Response response);
    }

}
