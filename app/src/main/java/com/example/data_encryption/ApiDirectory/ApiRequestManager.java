package com.example.data_encryption.ApiDirectory;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.data_encryption.ApiDirectory.model.UserModel;
import com.example.data_encryption.Fragments.ChooseRecipientFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiRequestManager {

    private RequestQueue requestQueue;
    private static final String BASE_URL = "https://go-fiber-jcpc.onrender.com";

    public ApiRequestManager(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void signUp(UserModel u, final ApiResponseListener<String> listener) {
        String url = BASE_URL + "/auth/signup";

        JSONObject payload = new JSONObject();
        try {
            if (u.getName() != null) {
                payload.put("name", u.getName());
            }
            if (u.getEmail() != null) {
                payload.put("email", u.getEmail());
            }
            if (u.getPassword() != null) {
                payload.put("password", u.getPassword());
            }
            if (u.getRsa_public_key() != null) {
                payload.put("encryption_token", u.getRsa_public_key());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    listener.onSuccess(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    listener.onError(new VolleyError("Failed to signup!!!"));
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error);
                    }
                }) {
            @Override
            public byte[] getBody() {
                return payload.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    public void authenticate(String email, String password, final ApiResponseListener<JSONObject> listener) {
        String url = BASE_URL + "/auth/login";

        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            listener.onSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onError(new VolleyError("Failed to authenticate"));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error);
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    public void fetchUsers(final ApiResponseListener<List<ChooseRecipientFragment.Recipient>> listener, String token) {
        String url = BASE_URL + "/files/getAllUsers";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<ChooseRecipientFragment.Recipient> recipientList = new ArrayList<>();
                            JSONArray jsonArray = response.getJSONArray("users");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String email = jsonObject.getString("email");
                                String RSAPublicKey = jsonObject.getString("encryption_token");
                                recipientList.add(new ChooseRecipientFragment.Recipient(email, RSAPublicKey, false));
                            }
                            listener.onSuccess(recipientList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            listener.onError(new VolleyError("Failed to parse users"));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }
}
