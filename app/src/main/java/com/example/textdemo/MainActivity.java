package com.example.textdemo;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    Button button;
    // Variables to store extracted texts
    private String name = "";
    private String ID = "";

    private String Section = "";

    private static final int PICK_IMAGE = 100;


    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find imageview
        imageView = findViewById(R.id.imageId);
        //find textview
        textView = findViewById(R.id.textId);
        button = (Button) findViewById(R.id.buttonLoadPicture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        //check app level permission is granted for Camera
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //grant the permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void saveToCSV(String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.append("Name of Student,Section,Student ID\n"); // Write column headers
            writer.append(name).append(",").append(Section).append(",").append(ID).append("\n"); // Write extracted texts
            writer.flush();
            writer.close();
            Toast.makeText(MainActivity.this, "Data saved to CSV file: " + filePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error writing CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }








    private void recognizeText(final Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        recognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        // Get the detected text blocks
                        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
                        StringBuilder selectedTextBuilder = new StringBuilder();
                        // Create a Canvas object to draw on the bitmap
                        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
                        Canvas canvas = new Canvas(tempBitmap);
                        canvas.drawBitmap(bitmap, 0, 0, null);

                        // Get the recognized text as a string
                        String recognizedText = firebaseVisionText.getText();

                        // Write the recognized text to a CSV file in local storage
//                        try {
//                            String filePath = "/storage/emulated/0/Documents/text.csv";
//                            FileWriter writer = new FileWriter(filePath);
//                            writer.write(recognizedText);
//                            writer.flush();
//                            writer.close();
//                            Toast.makeText(MainActivity.this, "Recognized text saved to " + filePath, Toast.LENGTH_SHORT).show();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            Toast.makeText(MainActivity.this, "Error writing CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
                        String filePath = "/storage/emulated/0/Documents/text.csv";
//                        try {
//                            FileWriter writer = new FileWriter(filePath);
//                            writer.append("Name of Student,Section,Student ID\n"); // Write column headers
//                            writer.append(name).append(",").append(Section).append(",").append(ID).append("\n"); // Write extracted texts
//                            writer.flush();
//                            writer.close();
//                            Toast.makeText(MainActivity.this, "Data saved to CSV file: " + filePath, Toast.LENGTH_SHORT).show();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            Toast.makeText(MainActivity.this, "Error writing CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }

                        // Set the recognized text on the TextView
                        textView.setText(recognizedText);

                        // Loop through each text block
                        for (FirebaseVisionText.TextBlock block : blocks) {
                            Rect boundingBox = block.getBoundingBox();

                            // Create a Paint object to draw the bounding box
                            Paint paint = new Paint();
                            paint.setColor(Color.GREEN);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setStrokeWidth(4.0f);

                            // Draw the bounding box on the bitmap
                            canvas.drawRect(boundingBox, paint);

                            // Get the text lines in the current block
                            List<FirebaseVisionText.Line> lines = block.getLines();


                            // Loop through each text line
                            for (FirebaseVisionText.Line line : lines) {
                                Rect lineBoundingBox = line.getBoundingBox();
                                String lineText = line.getText();
                                // Create a Paint object to draw the line bounding box
                                Paint linePaint = new Paint();
                                linePaint.setColor(Color.RED);
                                linePaint.setStyle(Paint.Style.STROKE);
                                linePaint.setStrokeWidth(2.0f);

                                // Draw the line bounding box on the bitmap
                                canvas.drawRect(lineBoundingBox, linePaint);

                                // Get the text elements in the current line
                                List<FirebaseVisionText.Element> elements = line.getElements();
                                // Extract specific texts based on criteria
                                if (lineText.startsWith("Name of Student ")) {
                                    name = lineText.substring("Name of Student ".length()).trim();
                                } else if (lineText.startsWith("Student ID:")) {
                                    ID = lineText.substring("Student ID:".length()).trim();
                                } else if (lineText.startsWith("Section:")) {
                                    Section = lineText.substring("Section:".length()).trim();
                                }

                                // Loop through each text element
                                for (FirebaseVisionText.Element element : elements) {
                                    Rect elementBoundingBox = element.getBoundingBox();

                                    // Create a Paint object to draw the element bounding box
                                    Paint elementPaint = new Paint();
                                    elementPaint.setColor(Color.BLUE);
                                    elementPaint.setStyle(Paint.Style.STROKE);
                                    elementPaint.setStrokeWidth(1.0f);

                                    // Draw the element bounding box on the bitmap
                                    canvas.drawRect(elementBoundingBox, elementPaint);
                                }
                            }
                            String text = firebaseVisionText.getText();

                            // Split the text into an array of rows
                            String[] rows = text.split("\n");
                            saveToCSV(filePath);
                            // Pass the rows to the saveToCSV method
//                             saveToCSV(rows, "/storage/emulated/0/Documents");
                        }

                        // Set the processed bitmap on the ImageView
                        imageView.setImageBitmap(tempBitmap);

                        // Set the recognized text on the TextView
                        textView.setText(firebaseVisionText.getText());


                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void doProcess(View view) {
        //open the camera => create an Intent object
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 101);

    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    recognizeText(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 101 && data != null) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                imageView.setImageBitmap(bitmap);
                recognizeText(bitmap);
            }
        }
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
        if (requestCode == 101 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                //from bundle, extract the image
                Bitmap bitmap = (Bitmap) bundle.get("data");
                //set image in imageview
                imageView.setImageBitmap(bitmap);
                //process the image
                //1. create a FirebaseVisionImage object from a Bitmap object
                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
                //2. Get an instance of FirebaseVision
                FirebaseVision firebaseVision = FirebaseVision.getInstance();
                //3. Create an instance of FirebaseVisionTextRecognizer
                FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();
                //4. Create a task to process the image
                Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);
                //5. if task is success
                task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String s = firebaseVisionText.getText();
                        textView.setText(s);
                    }
                });
                //6. if task is failure
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}