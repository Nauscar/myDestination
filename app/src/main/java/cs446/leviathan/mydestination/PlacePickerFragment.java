package cs446.leviathan.mydestination;

/**
 * Created by nause on 16/07/15.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import cs446.leviathan.mydestination.cardstream.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Sample demonstrating the use of {@link PlacePicker}.
 * This sample shows the construction of an {@link Intent} to open the PlacePicker from the
 * Google Places API for Android and select a {@link Place}.
 *
 * This sample uses the CardStream sample template to create the UI for this demo, which is not
 * required to use the PlacePicker API. (Please see the Readme-CardStream.txt file for details.)
 *
 * @see com.google.android.gms.location.places.ui.PlacePicker.IntentBuilder
 * @see com.google.android.gms.location.places.ui.PlacePicker
 * @see com.google.android.gms.location.places.Place
 */

public class PlacePickerFragment extends Fragment implements OnCardClickListener {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "PlacePickerSample";

    private CardStreamFragment mCards = null;

    // Buffer used to display list of place types for a place
    private final StringBuffer mPlaceTypeDisplayBuffer = new StringBuffer();

    // Tags for cards
    private static final String CARD_INTRO = "INTRO";
    private static final String CARD_PICKER = "PICKER";
    private static final String CARD_DETAIL = "DETAIL";

    /**
     * Action to launch the PlacePicker from a card. Identifies the card action.
     */
    private static final int ACTION_PICK_PLACE = 1;
    private static final int ACTION_TAKE_PICTURE = 2;
    private static final int ACTION_YELP = 3;
    private static final int ACTION_FACEBOOK = 4;
    private static final int ACTION_INSTAGRAM = 5;

    /**
     * Request code passed to the PlacePicker intent to identify its result when it returns.
     */
    private static final int REQUEST_PLACE_PICKER = 1;
    private static final int REQUEST_TAKE_PICTURE = 2;
    private static final int REQUEST_YELP = 3;
    private static final int REQUEST_FACEBOOK = 4;
    private static final int REQUEST_INSTAGRAM = 5;

    private static int cardCount = 0;
    private static String cardActionTag;
//    private ShareDialog shareDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        shareDialog = new ShareDialog(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if cards are visible, at least the picker card is always shown.
        CardStreamFragment stream = getCardStream();
        if (stream.getVisibleCardCount() < 1) {
            // No cards are visible, sample is started for the first time.
            // Prepare all cards and show the intro card.
            initialiseCards();
            // Show the picker card and make it non-dismissible.
            getCardStream().showCard(CARD_PICKER, false);
        }

    }

    @Override
    public void onCardClick(int cardActionId, String cardTag) {
        cardActionTag = cardTag;
        if (cardActionId == ACTION_PICK_PLACE) {
            // BEGIN_INCLUDE(intent)
            /* Use the PlacePicker Builder to construct an Intent.
            Note: This sample demonstrates a basic use case.
            The PlacePicker Builder supports additional properties such as search bounds.
             */
            try {
                PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                Intent intent = intentBuilder.build(getActivity());
                // Start the Intent by requesting a result, identified by a request code.
                startActivityForResult(intent, REQUEST_PLACE_PICKER);

            } catch (GooglePlayServicesRepairableException e) {
                GooglePlayServicesUtil
                        .getErrorDialog(e.getConnectionStatusCode(), getActivity(), 0);
            } catch (GooglePlayServicesNotAvailableException e) {
                Toast.makeText(getActivity(), "Google Play Services is not available.",
                        Toast.LENGTH_LONG)
                        .show();
            }

            // END_INCLUDE(intent)
        }
        else if (cardActionId == ACTION_TAKE_PICTURE) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    mCards.getCard(cardActionTag).setPhotoPath(Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE);
                }
            }
            else
            {
                Toast.makeText(getActivity(), "Camera is not available.",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
        else if(cardActionId == ACTION_FACEBOOK){
            //TODO: Share to Facebook
            Bitmap bitmap = getBitmap(mCards.getCard(cardActionTag).getPhotoPath());

            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();

            ShareDialog.show(getActivity(), content);
        }
        else if(cardActionId == ACTION_INSTAGRAM){
            // Create the new Intent using the 'Send' action.
            Intent share = new Intent(Intent.ACTION_SEND);

            // Set the MIME type
            share.setType("image/*");

            // Add the URI and the caption to the Intent.
            share.putExtra(Intent.EXTRA_STREAM, mCards.getCard(cardActionTag).getPhotoPath());
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.discovered));

            try {
                ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo("com.instagram.android", 0);
            } catch (PackageManager.NameNotFoundException e) {
                Toast.makeText(getActivity(), "Instagram is not installed.",
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }
            share.setPackage("com.instagram.android");
            startActivity(share);
        }
        else if(cardActionId == ACTION_YELP){
            //TODO: Launch Yelp
            Card card = mCards.getCard(cardActionTag);
            //Feel free to create fields in the Card class.
            //Async call goes here.
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    /**
     * Extracts data from PlacePicker result.
     * This method is called when an Intent has been started by calling
     * {@link #startActivityForResult(android.content.Intent, int)}. The Intent for the
     * {@link com.google.android.gms.location.places.ui.PlacePicker} is started with
     * {@link #REQUEST_PLACE_PICKER} request code. When a result with this request code is received
     * in this method, its data is extracted by converting the Intent data to a {@link Place}
     * through the
     * {@link com.google.android.gms.location.places.ui.PlacePicker#getPlace(android.content.Intent,
     * android.content.Context)} call.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // BEGIN_INCLUDE(activity_result)
        Card actionCard = mCards.getCard(cardActionTag);
        if (requestCode == REQUEST_PLACE_PICKER) {
            // This result is from the PlacePicker dialog.

            if (resultCode == Activity.RESULT_OK) {
                /* User has picked a place, extract data.
                   Data is extracted from the returned intent by retrieving a Place object from
                   the PlacePicker.
                 */
                final Place place = PlacePicker.getPlace(data, getActivity());

                /* A Place object contains details about that place, such as its name, address
                and phone number. Extract the name, address, phone number, place ID and place types.
                 */
                final CharSequence name = place.getName();
                final CharSequence address = place.getAddress();
                final CharSequence phone = place.getPhoneNumber();
                final String placeId = place.getId();
                String attribution = PlacePicker.getAttributions(data);
                if (attribution == null) {
                    attribution = "";
                }

                // Print data to debug log
                Log.d(TAG, "Place selected: " + placeId + " (" + name.toString() + ")");

                // Build the card.
                StringBuilder cardName = new StringBuilder();
                cardName.append(CARD_DETAIL);
                cardName.append(cardCount++);
                Card c = new Card.Builder(this, cardName.toString())
                        .setTitle(name.toString())
                        .setDescription(getString(R.string.detail_text, placeId, address, phone,
                                attribution))
                        .addAction("Take a picture", ACTION_TAKE_PICTURE, Card.ACTION_NEUTRAL)
                        .addAction("Review on Yelp", ACTION_YELP, Card.ACTION_POSITIVE)
                        .addAction("Share on Facebook", ACTION_FACEBOOK, Card.ACTION_POSITIVE)
                        .addAction("Post on Instagram", ACTION_INSTAGRAM, Card.ACTION_POSITIVE)
                        .build(getActivity());
                getCardStream().addCard(c, false);

                // Show the card.
                getCardStream().showCard(cardName.toString());
                showAction(true, cardName.toString(), ACTION_TAKE_PICTURE);
                showAction(true, cardName.toString(), ACTION_YELP);
                showAction(true, cardName.toString(), ACTION_FACEBOOK);
                showAction(false, cardName.toString(), ACTION_INSTAGRAM);

                mCards.getCard(cardName.toString()).setActionAreaVisibility(true);

            } else {
                // User has not selected a place, hide the card.
                //getCardStream().hideCard(CARD_DETAIL);
            }

        }
        else if(requestCode == REQUEST_TAKE_PICTURE  && resultCode == Activity.RESULT_OK) {

            Bitmap bitmap = getBitmap(actionCard.getPhotoPath());
            if(bitmap != null)
                actionCard.setPicture(bitmap);
            showAction(true, actionCard.getTag(), ACTION_INSTAGRAM);
            showAction(false, actionCard.getTag(), ACTION_TAKE_PICTURE);
        }
        else if(requestCode == REQUEST_FACEBOOK){
            //TODO: Share to Facebook
        }
        else if(requestCode == REQUEST_INSTAGRAM){
            //TODO: Post to Instagram
        }
        else if(requestCode == REQUEST_YELP){
            //TODO: Launch Yelp
            //Feel free to create fields in the Card class.
            //Async callback goes here.
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        // END_INCLUDE(activity_result)
    }

    /**
     * Initializes the picker and detail cards and adds them to the card stream.
     */
    private void initialiseCards() {
        // Add picker card.
        Card c = new Card.Builder(this, CARD_PICKER)
                .setTitle(getString(R.string.pick_title))
                .setDescription(getString(R.string.pick_text))
                .addAction(getString(R.string.pick_action), ACTION_PICK_PLACE, Card.ACTION_NEUTRAL)
                .setLayout(R.layout.card_google)
                .build(getActivity());
        getCardStream().addCard(c, false);

        // Add and show introduction card.
        c = new Card.Builder(this, CARD_INTRO)
                .setTitle(getString(R.string.intro_title))
                .setDescription(getString(R.string.intro_message))
                .build(getActivity());
        getCardStream().addCard(c, true);

        for(Card card : mCards.getVisibleCards()){
            if(card.hasPicture()){
                Bitmap bitmap = getBitmap(card.getPhotoPath());
                if(bitmap != null)
                    card.setPicture(bitmap);
            }
        }
    }

    private Bitmap getBitmap(Uri uri){
        Bitmap bitmap = null;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        } catch (Exception e){
            Toast.makeText(getActivity(), "Image not found.",
                    Toast.LENGTH_LONG)
                    .show();
        }
        return bitmap;
    }

    /**
     * Sets the visibility of the 'Pick Action' option on the 'Pick a place' card.
     * The action should be hidden when the PlacePicker Intent has been fired to prevent it from
     * being launched multiple times simultaneously.
     *
     * @param show
     */

    private void showAction(boolean show, String cardTag, int actionId) {
        mCards.getCard(cardTag).setActionVisibility(actionId, show);
    }
    /**
     * Returns the CardStream.
     *
     * @return
     */
    private CardStreamFragment getCardStream() {
        if (mCards == null) {
            mCards = ((CardStream) getActivity()).getCardStream();
        }
        return mCards;
    }
}