package com.gpgex;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import org.haxe.extension.Extension;
import org.haxe.lime.HaxeObject;
import android.os.AsyncTask;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.security.MessageDigest;
import android.app.AlertDialog;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.AchievementsClient;


public class GooglePlayGames extends Extension {
	
	public static final String TAG = "EXTENSION-GOOGLEPLAYGAMES";
	final static int RC_SIGN_IN = 9001;

	private static GooglePlayGames instance=null;
	private static boolean userRequiresLogin=false;
	private static SecureHaxeObject callbackObject = null;
	

  	private GoogleSignInClient mGoogleSignInClient = null;
	private GoogleSignInAccount mSignedInAccount = null;
	
	public void onCreate (Bundle savedInstanceState) {
		Log.i(TAG, "PlayGames: onCreate");

		String webclientId = getString(R.string.webclient_id);

		Log.i(TAG, "PlayGames: webclientId" + webclientId);

		GoogleSignInOptions options = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
				.requestIdToken(webclientId)
                .build();

		mGoogleSignInClient = GoogleSignIn.getClient(mainActivity, options);
		instance = this;
	}

	public static void init(HaxeObject callbackObj) {
		Log.i(TAG, "PlayGames: START INIT");
		if(callbackObj!=null) GooglePlayGames.callbackObject = new SecureHaxeObject(callbackObj, mainActivity, TAG);
	}

	public void startSignInIntent() {
		onSignInStart();
		mainActivity.runOnUiThread(new Runnable() {
			public void run() {
    			mainActivity.startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
			}
		});
  	}

	public void signInSilently() {
		Log.d(TAG, "signInSilently()");
		onSignInStart();

		mGoogleSignInClient.silentSignIn().addOnCompleteListener(mainActivity,
			new OnCompleteListener<GoogleSignInAccount>() {
				@Override
				public void onComplete(Task<GoogleSignInAccount> task) {
					if (task.isSuccessful()) {
						Log.d(TAG, "signInSilently(): success");
						GooglePlayGames.getInstance().onConnected(task.getResult());
					} else {
						Log.d(TAG, "signInSilently(): failure", task.getException());
						GooglePlayGames.getInstance().onSignInFailed();
						GooglePlayGames.getInstance().onDisconnected();
					}
				}
		});
	}

	public GoogleSignInAccount getAccount() {
		return mSignedInAccount;
	}

	public AchievementsClient getAchievementsClient() {
		return (mSignedInAccount == null) ? null : Games.getAchievementsClient(mainActivity, mSignedInAccount);
	}

	public static void login(){
		Log.i(TAG, "PlayGames: Login");
		GooglePlayGames.getInstance().startSignInIntent();
	}

	public static void loginSilently(){
		Log.i(TAG, "PlayGames: Login silently");
		GooglePlayGames.getInstance().signInSilently();
	}

	public static void logout(){
	}

	@Override public boolean onActivityResult (int requestCode, int resultCode, Intent intent)
	{
		if (requestCode == RC_SIGN_IN) {

			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);

			try {				
				Log.i(TAG, "onActivityResult RC_SIGN_IN, responseCode="+ resultCode + ", intent=" + intent);
				GoogleSignInAccount account = task.getResult(ApiException.class);
				onConnected(account);
			} catch (ApiException apiException) {
				showExceptionMessage(apiException);
				onSignInFailed();
				onDisconnected();
			}
		}
		return true;
	}

	public void showExceptionMessage(Exception exception) {
        Log.i(TAG, "Exception: " + exception.getMessage(), exception);
        String errorMessage = null;
        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            int errorCode = apiException.getStatusCode();
            errorMessage = GoogleApiAvailability.getInstance().getErrorString(errorCode);
        }

        if (errorMessage == null) {
            errorMessage = "Exception encountered!";
        }
        (new AlertDialog.Builder(mainActivity))
                .setMessage(errorMessage)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

	private void onConnected(GoogleSignInAccount googleSignInAccount) {

		Log.d(TAG, "onConnected(): connected to Google APIs");
		if (mSignedInAccount != googleSignInAccount) {
			mSignedInAccount = googleSignInAccount;
		}

		Log.i(TAG, "mSignedInAccount.getId(): " + mSignedInAccount.getId());
		Log.i(TAG, "mSignedInAccount.getIdToken(): " + mSignedInAccount.getIdToken());
		Log.i(TAG, "mSignedInAccount.getDisplayName(): " + mSignedInAccount.getDisplayName());

		onSignInSucceeded();
	}

	public String getString(int resId)
	{
		Context ctx = mainActivity;
		return ctx.getString(resId);
	}

	public void onDisconnected() {
		callbackObject.call0("onLogoutCallback");
		Log.i(TAG, "PlayGames: onLogoutCallback");
	}

	public static GooglePlayGames getInstance(){
		if(instance==null) instance=new GooglePlayGames();
		return instance;
	}

	private static AchievementsClient achievementsClient() {
		return getInstance().getAchievementsClient();
	}

    public void onSignInFailed() {
		callbackObject.call1("loginResultCallback",-1);
        Log.i(TAG, "PlayGames: onSignInFailed");
    }

    public void onSignInSucceeded() {
		callbackObject.call1("loginResultCallback",1);
        Log.i(TAG, "PlayGames: onSignInSucceeded");
    }
	
    public void onSignInStart() {
		callbackObject.call1("loginResultCallback",0);
        Log.i(TAG, "PlayGames: onSignInStart");
    }

	public static boolean unlock(String id){
		if (achievementsClient() == null) {
			return false;
		}
		achievementsClient().unlock(id);
		return true;
	}

	public static boolean reveal(String id){
		if (achievementsClient() == null) {
			return false;
		}
		achievementsClient().reveal(id);
		return true;
	}

	public static boolean increment(String id, int step){
		if (achievementsClient() == null) {
			return false;
		}
		Log.i(TAG, "PlayGames: achievementsClient().increment");
		achievementsClient().increment(id, step);
		return true;
	}

	public static boolean setSteps(String id, int steps) {
		if (achievementsClient() == null) {
			return false;
		}
		achievementsClient().setSteps(id, steps);
		return true;
	}

	public static String getPlayerId() {
		GoogleSignInAccount acc = getInstance().getAccount();
		return (acc == null) ? "acc is none" : acc.getId();
	}

	public static String getPlayerDisplayName() {
		Log.i(TAG, "getPlayerDisplayName()");
		GoogleSignInAccount acc = getInstance().getAccount();
		return (acc == null) ? "acc is none" : acc.getDisplayName();
	}

	public static String getIdToken() {
		GoogleSignInAccount acc = getInstance().getAccount();
		return (acc == null) ? "acc is none" : acc.getIdToken();
	}

	public static boolean getAchievementStatus(final String idAchievement) {
		return true;
	}

	public static boolean getCurrentAchievementSteps(final String idAchievement) {
		return true;
	}
}
