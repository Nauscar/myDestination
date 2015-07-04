package cs446.leviathan.mydestination;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Aaron Kelly-Barker on 2015-07-04.
 */
public class YelpService {

    private OAuthService service;
    private APICallResult apiCallResult;
    private Token token;

    AsyncTask<Context, Void, Void> mtask;

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
        request.addQuerystringParameter("limit", "25");
        request.addQuerystringParameter("sort", "1");
        request.addQuerystringParameter("radius_filter", "5000");
        request.addQuerystringParameter("latitude", String.valueOf(location.getLatitude()));
        request.addQuerystringParameter("longitude", String.valueOf(location.getLongitude()));

        this.service.signRequest(token, request);

        mtask = new HandleAPICalls(request);
        mtask.execute();

        return true;
    }

    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com/v2/search");
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
            Handler mHandler = new Handler(Looper.getMainLooper());

            mHandler.post(new Runnable() {
                public void run() {
                    Response response = request.send();
                    Log.d("hi", response.toString());
                    apiCallResult.APICallback(response);
                }
            });


            return null;
        }
    }

    public static abstract class APICallResult {
        public abstract void APICallback(Response response);
    }

}
