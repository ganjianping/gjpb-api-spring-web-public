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
        } else {
            return "application/octet-stream"; // fallback
        }
    }
}
