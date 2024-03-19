package vote.directdemocracy.integrity;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.tasks.Task;

import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.android.play.core.integrity.IntegrityTokenResponse;
import com.google.android.play.core.integrity.model.IntegrityErrorCode;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class Integrity extends CordovaPlugin {
  @Override public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
     if (action.equals("requestIntegrityToken")) {
            requestIntegrityToken(args, callbackContext);
            return true;
        }
        return false;
  }
  private void requestIntegrityToken(JSONArray args, CallbackContext callbackContext) {
        try {
            JSONObject options = args.getJSONObject(0);
            String nonce = options.getString("nonce");
            long googleCloudProjectNumber = options.getLong("googleCloudProjectNumber");

            Context context = cordova.getActivity().getApplicationContext();
            if (GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
                callbackContext.error("Play Services not found");
                return;
            }
            IntegrityManager integrityManager = IntegrityManagerFactory.create(context);

            IntegrityTokenRequest req = IntegrityTokenRequest.builder().setNonce(nonce).build();
            if (googleCloudProjectNumber != 0) {
                req = IntegrityTokenRequest.builder().setNonce(nonce).setCloudProjectNumber(googleCloudProjectNumber).build();
            }
            Task<IntegrityTokenResponse> integrityTokenResponse = integrityManager.requestIntegrityToken(req);
            integrityTokenResponse.addOnSuccessListener(
                new OnSuccessListener<IntegrityTokenResponse>() {
                    @Override
                    public void onSuccess(IntegrityTokenResponse integrityTokenResponse1) {
                        String integrityToken = integrityTokenResponse1.token();
                        JSONObject ret = new JSONObject();
                        try {
                            ret.put("token", integrityToken);
                            callbackContext.success(ret);
                        } catch (JSONException e) {
                            callbackContext.error("Error creating JSON response");
                        }
                    }
                }
            );
            integrityTokenResponse.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            );
        } catch (JSONException e) {
            callbackContext.error("Error parsing parameters: " + e.getMessage());
        }
    }
}
