package function;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
public class convert {
    public static String encodePdfToBase64(Context context, Uri pdfUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
        if (inputStream == null) {
            throw new IOException("Unable to open InputStream for PDF");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // Buffer size can be adjusted

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        inputStream.close();

        return Base64.encodeToString(pdfBytes, Base64.DEFAULT); // Return Base64 encoded string
    }
    // Modify this method to accept the Context
    public static String encodeImageToBase64(Context context, Uri imageUri) throws IOException {
        // Use context.getContentResolver() to get the InputStream from the Uri
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Unable to open InputStream from URI");
        }

        byte[] bytes = readBytes(inputStream);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Read the InputStream into a byte array
    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private static boolean isProbablyPdf(String base64Content) {
        byte[] contentBytes = Base64.decode(base64Content, Base64.DEFAULT);
        // Check for '%PDF' header at the start of the file
        String header = new String(contentBytes, 0, Math.min(contentBytes.length, 4));
        return header.contains("%PDF");
    }

    public static String handleBase64Content(String base64Content) {
        // Extract MIME type if present
        String mimeType = "";
        if (base64Content.contains(";base64,")) {
            String[] parts = base64Content.split(",");
            mimeType = parts[0]; // e.g., "data:application/pdf;base64"
            base64Content = parts[1]; // Remove the header
        }

        // Check MIME type or analyze Base64 content
        if (mimeType.contains("application/pdf")) {
            // Decode Base64 as PDF and show in PDFView
            return "PDF";
        } else if (mimeType.contains("image")) {
            // Decode Base64 as an image and show in ImageView
            return "IMAGE";
        } else {
            // Fallback: Attempt to determine based on content
            if (isProbablyPdf(base64Content)) {
                return "PDF";
            } else {
                return "IMAGE";
            }
        }
    }

//    String base64String = encodeImageToBase64(this,selectedImageUri);
//
//    Bitmap bitmap = decodeBase64ToBitmap(base64String);
//    imageView.setImageBitmap(bitmap);
}
//pdf
// try {
//byte[] pdfBytes = Base64.decode(base64Content, Base64.DEFAULT);
//InputStream pdfStream = new ByteArrayInputStream(pdfBytes);
//            pdfView.fromStream(pdfStream).load();
//        } catch (Exception e) {
//        e.printStackTrace();
//        }

//image
//try {
//byte[] imageBytes = Base64.decode(base64Content, Base64.DEFAULT);
//Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//            imageView.setImageBitmap(bitmap);
//        } catch (Exception e) {
//        e.printStackTrace();
//        }