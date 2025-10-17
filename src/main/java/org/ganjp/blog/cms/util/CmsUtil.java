package org.ganjp.blog.cms.util;

public class CmsUtil {
    private CmsUtil() {}
    /**
     * Determine content type based on file extension
     * @param filename The filename to check
     * @return Content type string (e.g., "image/png", "image/svg+xml")
     */
    public static String determineContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerFilename.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerFilename.endsWith(".webm")) {
            return "video/webm";
        } else if (lowerFilename.endsWith(".ogv") || lowerFilename.endsWith(".ogg")) {
            return "video/ogg";
        } else if (lowerFilename.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerFilename.endsWith(".mkv")) {
            return "video/x-matroska";
        } else {
            return "application/octet-stream"; // fallback
        }
    }
}
