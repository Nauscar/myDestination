package cs446.leviathan.mydestination.yelp;

import android.content.Context;
import android.location.Location;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Aaron Kelly-Barker on 2015-07-04.
 */
public class YelpService {

    private static String TAG = YelpService.class.getSimpleName();

    private OAuthService service;
    private Token token;
    private AsyncTask<Context, Void, List<YelpBusinessData>> mtask;
    private List<YelpBusinessData> results = new LinkedList<>();

    public YelpService(String consumerKey, String consumerSecret, String tokenKey, String tokenSecret) {
        this.service = new ServiceBuilder()
                .provider(YelpAuth.class)
                .apiKey(consumerKey)
                .apiSecret(consumerSecret).build();
        this.token = new Token(tokenKey, tokenSecret);
    }

    //Not used
    public boolean getLocalPlaces(final LatLng location) {
        if (location == null) {
            return false;
        }
        OAuthRequest request = createOAuthRequest("/v2/search");
        request.addQuerystringParameter("limit", "15");
        request.addQuerystringParameter("sort", "1");
        request.addQuerystringParameter("radius_filter", "5000");
        request.addQuerystringParameter("ll", String.valueOf(location.latitude) + "," + String.valueOf(location.longitude));

        this.service.signRequest(token, request);

        mtask = new HandleAPICalls(request);
        mtask.execute();

        return true;
    }

    public YelpBusinessData getBusinessData(final LatLng location, final CharSequence businessName) {
        if (location == null) {
            return null;
        }
        OAuthRequest request = createOAuthRequest("/v2/search");
        request.addQuerystringParameter("term", businessName.toString());
        request.addQuerystringParameter("limit", "1");
        request.addQuerystringParameter("sort", "1");
        request.addQuerystringParameter("radius_filter", "2500");
        request.addQuerystringParameter("ll", String.valueOf(location.latitude) + "," + String.valueOf(location.longitude));

        return getData(request);

    }

    public YelpBusinessData getBusinessDataWithAddress(final CharSequence address, final CharSequence businessName) {
        if (address == null) {
            return null;
        }
        OAuthRequest request = createOAuthRequest("/v2/search");
        request.addQuerystringParameter("term", businessName.toString());
        request.addQuerystringParameter("limit", "1");
        request.addQuerystringParameter("sort", "1"); //Sort by closest distance
        request.addQuerystringParameter("radius_filter", "2500"); //Radius of 2.5km
        request.addQuerystringParameter("location", address.toString());

        return getData(request);
    }

    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.yelp.com" + path);
        return request;
    }

    private YelpBusinessData getData(OAuthRequest request) {
        this.service.signRequest(token, request);

        //Todo: Make this async
        mtask = new HandleAPICalls(request);
        try {
            List<YelpBusinessData> results = mtask.execute().get();
            if (results.isEmpty()) {
                return null;
            } else {
                return results.get(0);
            }
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        }
    }

    class HandleAPICalls extends AsyncTask<Context, Void , List<YelpBusinessData>>
    {
        private OAuthRequest request;

        public HandleAPICalls(OAuthRequest request) {
            this.request = request;
        }

        protected List<YelpBusinessData> doInBackground(Context... params)
        {
            Response response = request.send();
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode businessesNodes = mapper.readValue(response.getBody(), JsonNode.class).get("businesses");
                List<YelpBusinessData> results = mapper.readValue(businessesNodes.toString(), mapper.getTypeFactory().constructCollectionType(List.class, YelpBusinessData.class));
                return results;
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
            return null;
        }
    }
}

