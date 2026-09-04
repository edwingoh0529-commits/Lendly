package com.example.groupassignment2app;

import android.app.Activity;
import android.content.Context;
import android.os.CancellationSignal;

import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.groupassignment2app.data.Repo;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class GoogleSignInHelper {

    public interface Callback {
        void onSuccess(FirebaseUser user, boolean isNewUser);
        void onFailure(String message);
        void onCancelled();
    }

    private final Activity activity;
    private final CredentialManager credentialManager;
    private final Repo repo = Repo.get();

    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
        this.credentialManager = CredentialManager.create(activity);
    }

    
    private static String webClientId(Context context) {
        int resId = context.getResources().getIdentifier(
                "default_web_client_id", "string", context.getPackageName());
        return resId == 0 ? null : context.getString(resId);
    }

    
    public void signIn(Callback callback) {
        String clientId = webClientId(activity);
        if (clientId == null) {
            callback.onFailure("Google sign-in is not set up yet.\n\n"
                    + "In the Firebase console: add your SHA-1 fingerprint, enable "
                    + "Google under Authentication, then re-download "
                    + "google-services.json.");
            return;
        }

        
        GetSignInWithGoogleOption option =
                new GetSignInWithGoogleOption.Builder(clientId).build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        handleCredential(response, callback);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        String type = e.getType() == null ? "" : e.getType();
                        
                        if (type.contains("USER_CANCELED") || type.contains("Cancel")) {
                            callback.onCancelled();
                            return;
                        }
                        if (type.contains("NO_CREDENTIAL")) {
                            callback.onFailure("No Google account found on this device.\n\n"
                                    + "Add one in Settings, then try again.");
                            return;
                        }
                        callback.onFailure("Google sign-in failed: " + e.getMessage());
                    }
                });
    }

    
    private void handleCredential(GetCredentialResponse response, Callback callback) {
        androidx.credentials.Credential credential = response.getCredential();

        if (!(credential instanceof CustomCredential)
                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(credential.getType())) {
            callback.onFailure("Unexpected credential type from Google");
            return;
        }

        GoogleIdTokenCredential googleCredential =
                GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());

        String idToken = googleCredential.getIdToken();
        String displayName = googleCredential.getDisplayName();

        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

        repo.auth().signInWithCredential(firebaseCredential)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    boolean isNew = result.getAdditionalUserInfo() != null
                            && result.getAdditionalUserInfo().isNewUser();

                    if (user == null) {
                        callback.onFailure("Signed in, but no user was returned");
                        return;
                    }

                    
                    String name = displayName != null && !displayName.trim().isEmpty()
                            ? displayName
                            : (user.getDisplayName() == null ? "Lendly User" : user.getDisplayName());

                    repo.saveUserProfile(name,
                                    user.getEmail() == null ? "" : user.getEmail(), "")
                            .addOnCompleteListener(t -> callback.onSuccess(user, isNew));
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Firebase rejected the Google account: "
                                + e.getMessage()));
    }
}