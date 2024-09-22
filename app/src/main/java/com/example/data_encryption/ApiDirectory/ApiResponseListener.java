package com.example.data_encryption.ApiDirectory;

import com.android.volley.VolleyError;

import org.json.JSONException;

public interface ApiResponseListener<T> {
    void onSuccess(T response) throws JSONException;
    void onError(VolleyError error);
}
