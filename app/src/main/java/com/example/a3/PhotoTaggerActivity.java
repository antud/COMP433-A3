package com.example.a3;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class PhotoTaggerActivity extends AppCompatActivity {
    SQLiteDatabase db;
    EditText tagField;
    EditText searchField;
    TextView textInfoOne;
    TextView textInfoTwo;
    TextView textInfoThree;
    private ImageView imageViewOne;
    private ImageView imageViewTwo;
    private ImageView imageViewThree;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_tagger);

        // Implementation for tagging sketches goes here...
        db = this.openOrCreateDatabase("photos", Context.MODE_PRIVATE, null);
        //db.execSQL("DROP TABLE IF EXISTS PHOTOS");
        db.execSQL("CREATE TABLE IF NOT EXISTS PHOTOS (PHOTO BLOB, DATE DATETIME, TAGS TEXT)");

        imageViewOne = findViewById(R.id.top_small_image);
        textInfoOne = findViewById(R.id.top_small_image_text);

        imageViewTwo = findViewById(R.id.mid_small_image);
        textInfoTwo = findViewById(R.id.mid_small_image_text);

        imageViewThree = findViewById(R.id.bot_small_image);
        textInfoThree  = findViewById(R.id.bot_small_image_text);

        tagField = findViewById(R.id.tag_edit_text_box);
        searchField = findViewById(R.id.tag_search_edit_box);
        Cursor c = db.rawQuery("SELECT * FROM PHOTOS", null);
        showLatestImages(c);

        Button backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoTaggerActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    public void startCamera(View view) {
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(camIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");

            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void savePhoto(View view) {
        ImageView img = findViewById(R.id.imageView);
        String tagStrings = tagField.getText().toString();

        LocalDateTime currentDateTime =LocalDateTime.now();
        String formattedDateTime = formatDateTime(currentDateTime);

        Bitmap b = ((BitmapDrawable)img.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] ba = stream.toByteArray();
        ContentValues cv = new ContentValues();
        cv.put("PHOTO", ba);
        cv.put("DATE", formattedDateTime);
        cv.put("TAGS", tagStrings);
        db.insert("PHOTOS", null, cv);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String formatDateTime(LocalDateTime dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d yyyy, h a", Locale.getDefault());
        return sdf.format(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }

    public void searchTags(View view) {
        MyDrawingArea blank = findViewById(R.id.blank_drawing_area);
        Bitmap bbb = blank.getBitmap();
        Cursor c;
        String tagText = searchField.getText().toString();
        if (tagText.equals("")) {
            c = db.rawQuery("SELECT * FROM PHOTOS ORDER BY DATE DESC", null);
            showLatestImages(c);
        } else {
            try {
                // split entry by commas
                String[] searchTags = tagText.split(",");

                // make query for each tag, combine with or
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("SELECT * FROM PHOTOS WHERE ");

                for (int i = 0; i < searchTags.length; i++) {
                    if (i > 0) {
                        queryBuilder.append(" OR ");
                    }
                    queryBuilder.append("TAGS LIKE ?");
                }

                queryBuilder.append(" ORDER BY DATE DESC");
                String query = queryBuilder.toString();

                // arr for each param of search tag
                String[] queryParameters = new String[searchTags.length];

                // run the query for each param
                for (int i = 0; i < searchTags.length; i++) {
                    queryParameters[i] = "%" + searchTags[i].trim() + "%";
                }

                c = db.rawQuery(query, queryParameters);

                // clear image and text views
                imageViewOne.setImageBitmap(bbb);
                textInfoOne.setText("unavailable");
                imageViewTwo.setImageBitmap(bbb);
                textInfoTwo.setText("unavailable");
                imageViewThree.setImageBitmap(bbb);
                textInfoThree.setText("unavailable");

                int position = 1;
                // populate search images
                while (c.moveToNext() && position <= 3) {
                    byte[] ba = c.getBlob(0);
                    String date = c.getString(1);
                    String tagsInDatabase = c.getString(2);

                    if (position == 1) {
                        imageViewOne.setImageBitmap(BitmapFactory.decodeByteArray(ba, 0, ba.length));
                        textInfoOne.setText(tagsInDatabase + "\n" + date);
                    } else if (position == 2) {
                        imageViewTwo.setImageBitmap(BitmapFactory.decodeByteArray(ba, 0, ba.length));
                        textInfoTwo.setText(tagsInDatabase + "\n" + date);
                    } else if (position == 3) {
                        imageViewThree.setImageBitmap(BitmapFactory.decodeByteArray(ba, 0, ba.length));
                        textInfoThree.setText(tagsInDatabase + "\n" + date);
                    }
                    position++;
                }

                // if less than 3 images found
                while (position <= 3) {
                    if (position == 2) {
                        imageViewTwo.setImageBitmap(bbb);
                        textInfoTwo.setText("unavailable");
                    } else if (position == 3) {
                        imageViewThree.setImageBitmap(bbb);
                        textInfoThree.setText("unavailable");
                    }
                    position++;
                }

                //just in case
            } catch (CursorIndexOutOfBoundsException e) {
                imageViewTwo.setImageBitmap(bbb);
                textInfoTwo.setText("unavailable");
                imageViewThree.setImageBitmap(bbb);
                textInfoThree.setText("unavailable");
            }
        }
    }

    public void showLatestImages(Cursor c) {
        c.moveToLast();
        byte[] ba = c.getBlob(0);
        String date = c.getString(1);
        String tags = c.getString(2);

        imageViewOne.setImageBitmap((BitmapFactory.decodeByteArray(ba, 0, ba.length)));
        textInfoOne.setText(tags + "\n" + date);

        c.moveToPrevious();
        ba = c.getBlob(0);
        date = c.getString(1);
        tags = c.getString(2);
        imageViewTwo.setImageBitmap((BitmapFactory.decodeByteArray(ba, 0, ba.length)));
        textInfoTwo.setText(tags + "\n" + date);

        c.moveToPrevious();
        ba = c.getBlob(0);
        date = c.getString(1);
        tags = c.getString(2);
        imageViewThree.setImageBitmap((BitmapFactory.decodeByteArray(ba, 0, ba.length)));
        textInfoThree.setText(tags + "\n" + date);
    }
}
