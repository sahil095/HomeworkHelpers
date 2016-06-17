package com.example.sahil.homeworkhelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ImageActivity extends AppCompatActivity {

    ImageView mImageView;
    JSONObject obj;
    Uri mCurrentPhotoPath;
    String base64;
    String url = "http://0.0.0.0:5000/test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mImageView = (ImageView) findViewById(R.id.image_view);
        Bundle bundle = getIntent().getBundleExtra("bundle");
        mCurrentPhotoPath = Uri.parse(bundle.getString("uri"));
      //  Toast.makeText(ImageActivity.this, "" + mCurrentPhotoPath, Toast.LENGTH_LONG).show();
        setPic();

    }
    private void setPic() {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath.getPath(), bmOptions);
        mImageView.setImageBitmap(bitmap);

        CropImage.activity(mCurrentPhotoPath)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri resultUri = null;
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inSampleSize = 8;
                Bitmap bitmap = BitmapFactory.decodeFile(resultUri.getPath(), bmOptions);
                mImageView.setImageBitmap(bitmap);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();

                base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
                try {
                    obj=new JSONObject();
                    obj.put("image", base64);
                    new postData().execute();
//                    Toast.makeText(ImageActivity.this, "" + obj.toString(), Toast.LENGTH_LONG).show();
//
//                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(
//                            Request.Method.PUT, url, obj,
//                            new Response.Listener<JSONObject>() {
//                                @Override
//                                public void onResponse(JSONObject response) {
//                                    Log.e("ABC", "" + response.toString());
//                                    Toast.makeText(ImageActivity.this, "Success onResponse", Toast.LENGTH_SHORT).show();
//                                }
//                            }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            Log.e("error", error.toString());
//                            Toast.makeText(ImageActivity.this, ""+ error.toString(), Toast.LENGTH_SHORT).show();
//                        }
//                    }){
//                        @Override
//                        public Map<String, String> getHeaders() throws AuthFailureError {
//                            HashMap<String, String> headers = new HashMap<String, String>();
//                            headers.put("Content-Type", "application/json");
//                            return headers;
//                        }
//                    };
//
//                    MySingleton.getInstance(this).addToRequestQueue(jsonObjReq);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public class postData extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL("http://angelrss.herokuapp.com/test");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
//set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(String.valueOf(obj));
                Log.e("value", String.valueOf(obj));
// json data
                writer.close();
                InputStream inputStream = urlConnection.getInputStream();
//input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                String response = buffer.toString();

                    return response;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(ImageActivity.this, "" + s, Toast.LENGTH_SHORT).show();
        }
    }
}
