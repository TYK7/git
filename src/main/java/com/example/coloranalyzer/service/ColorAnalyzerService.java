package com.example.coloranalyzer.service;

import com.helger.commons.charset.StandardCharsets;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.*;
import com.helger.css.reader.CSSReader;
import com.helger.css.reader.CSSReaderDeclarationList;
import com.helger.css.utils.CSSColorHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashMap;
import com.helger.css.utils.ECSSColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ColorAnalyzerService {

    private Set<String> extractedRawColors;
    private HttpClient httpClient; // Re-use client for external stylesheets
    private static final Pattern RGB_VALUE_PATTERN = Pattern.compile("\\d+");


    public ColorAnalyzerService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<AnalyzedColor> getAnalyzedColors(String urlString) {
        this.extractedRawColors = new HashSet<>();
        try {
            Document doc = fetchHtmlDocument(urlString);
            extractColorsFromInlineStyles(doc);
            extractColorsFromInternalStylesheets(doc);
            extractColorsFromExternalStylesheets(doc);
            extractColorsFromImages(doc);

            if (this.extractedRawColors.isEmpty()) {
                System.out.println("No raw colors extracted from URL: " + urlString);
                return Collections.emptyList();
            }

            Map<String, Long> colorFrequencies = new HashMap<>();
            for (String rawColor : this.extractedRawColors) {
                String hexColor = normalizeColorToHex(rawColor);
                if (hexColor != null) {
                    colorFrequencies.put(hexColor, colorFrequencies.getOrDefault(hexColor, 0L) + 1);
                }
            }

            if (colorFrequencies.isEmpty()) {
                System.out.println("No valid hex colors after normalization from URL: " + urlString);
                return Collections.emptyList();
            }

            List<Map.Entry<String, Long>> sortedColors = colorFrequencies.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            List<AnalyzedColor> result = new ArrayList<>();
            final int MAX_PRIMARY_COLORS = 3;
            final int MAX_TOTAL_COLORS = 10;
            int primaryColorCount = 0;

            for (Map.Entry<String, Long> entry : sortedColors) {
                if (result.size() >= MAX_TOTAL_COLORS) {
                    break;
                }
                String category = (primaryColorCount < MAX_PRIMARY_COLORS) ? "Primary" : "Secondary";
                if (primaryColorCount < MAX_PRIMARY_COLORS) {
                    primaryColorCount++;
                }
                result.add(new AnalyzedColor(entry.getKey(), category, entry.getValue()));
            }
            return result;

        } catch (IOException | InterruptedException e) {
            System.err.println("Error processing URL " + urlString + ": " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            // Consider returning a specific error AnalyzedColor or just empty
            return Collections.singletonList(new AnalyzedColor("Error", "Error processing URL: " + e.getMessage(), 1L));
        } catch (IllegalArgumentException e) {
            System.err.println("Error processing URL " + urlString + ": " + e.getMessage());
            return Collections.singletonList(new AnalyzedColor("Error", "Invalid argument: " + e.getMessage(), 1L));
        }
    }

    private String normalizeColorToHex(String rawColor) {
        if (rawColor == null || rawColor.trim().isEmpty()) {
            return null;
        }
        String color = rawColor.trim().toLowerCase();

        // Handle special keywords
        if (color.equals("transparent") || color.equals("currentcolor")) {
            return null; // Or a special marker like "#TRANSPARENT" if needs counting
        }

        // Already a hex color?
        if (color.startsWith("#")) {
            if (color.matches("^#([a-f0-9]{3})$")) { // #rgb
                return String.format("#%c%c%c%c%c%c", color.charAt(1), color.charAt(1), color.charAt(2), color.charAt(2), color.charAt(3), color.charAt(3));
            }
            if (color.matches("^#([a-f0-9]{4})$")) { // #rgba
                char r = color.charAt(1);
                char g = color.charAt(2);
                char b = color.charAt(3);
                return String.format("#%c%c%c%c%c%c", r, r, g, g, b, b);
            }
            if (color.matches("^#([a-f0-9]{6})$")) { // #rrggbb
                return color;
            }
            if (color.matches("^#([a-f0-9]{8})$")) { // #rrggbbaa
                return color.substring(0, 7); // Strip alpha
            }
            System.err.println("Invalid hex color format: " + rawColor);
            return null; // Invalid hex
        }

        // Named colors
        ECSSColor namedColor = ECSSColor.getFromNameCaseInsensitive(color);
        if (namedColor != null) {
            return namedColor.getAsHexRGB(); // Returns #rrggbb
        }

        // rgb(r,g,b) or rgba(r,g,b,a)
        if (color.startsWith("rgb(") || color.startsWith("rgba(")) {
            Matcher matcher = RGB_VALUE_PATTERN.matcher(color);
            List<Integer> values = new ArrayList<>();
            while (matcher.find()) {
                try {
                    values.add(Integer.parseInt(matcher.group()));
                } catch (NumberFormatException e) {
                    // ignore, not a number
                }
            }
            if (values.size() >= 3) {
                int r = values.get(0);
                int g = values.get(1);
                int b = values.get(2);
                if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                    return String.format("#%02x%02x%02x", r, g, b);
                }
            }
            System.err.println("Could not parse RGB/RGBA values from: " + rawColor);
            return null;
        }

        // HSL/HSLA colors are not explicitly handled here for brevity but could be added
        // For now, they will likely fall through and be logged.

        System.err.println("Could not normalize color: " + rawColor);
        return null;
    }

    public Document fetchHtmlDocument(String urlString) throws IOException, InterruptedException {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty or null.");
        }

        URI uri;
        try {
            uri = new URI(urlString);
            // Basic scheme check, though HttpClient would also fail for unsupported schemes
            if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Invalid URL scheme: " + urlString + ". Only HTTP/HTTPS is supported.");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed URL: " + urlString, e);
        }


        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "ColorAnalyzerBot/1.0")
                .GET()
                .build();

        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch HTML: HTTP status code " + response.statusCode());
        }

        String htmlContent = response.body();
        return Jsoup.parse(htmlContent, urlString);
    }

    public String getPageTitle(String urlString) {
        try {
            Document doc = fetchHtmlDocument(urlString);
            return doc.title();
        } catch (IOException | InterruptedException e) {
            // In a real application, consider more specific error handling or logging
            return "Error fetching page title: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Error: " + e.getMessage();
        }
    }

    private void extractColorsFromInlineStyles(Document doc) {
        if (doc == null) return;
        Elements elementsWithStyle = doc.select("[style]");
        for (Element element : elementsWithStyle) {
            String styleAttributeValue = element.attr("style");
            // Generate a source description for context, e.g., tag name and first few characters of style
            String sourceDesc = "Inline style on <" + element.tagName() + ">";
            parseAndCollectColorsFromDeclarationList(styleAttributeValue, sourceDesc);
        }
    }

    private void extractColorsFromInternalStylesheets(Document doc) {
        if (doc == null) return;
        Elements styleTags = doc.select("style");
        for (Element styleTag : styleTags) {
            String cssContent = styleTag.html(); // .data() or .html() can be used. html() is generally safer.
            String sourceDesc = "Internal stylesheet <style> tag (line " + styleTag.siblingIndex() + ")"; // Example description
            parseAndCollectColorsFromStylesheet(cssContent, sourceDesc);
        }
    }

    private void extractColorsFromExternalStylesheets(Document doc) {
        if (doc == null) return;
        Elements cssLinks = doc.select("link[rel=stylesheet][href]");
        for (Element link : cssLinks) {
            String externalCssUrl = link.absUrl("href");
            if (externalCssUrl == null || externalCssUrl.trim().isEmpty()) {
                System.err.println("Found empty external stylesheet URL in link: " + link.outerHtml());
                continue;
            }

            try {
                URI uri = new URI(externalCssUrl);
                 // Basic scheme check
                if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
                    System.err.println("Skipping external stylesheet with non-HTTP/HTTPS scheme: " + externalCssUrl);
                    continue;
                }

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofSeconds(15)) // Shorter timeout for CSS files
                        .header("User-Agent", "ColorAnalyzerBot/1.0 (fetching CSS)")
                        .GET()
                        .build();

                HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    parseAndCollectColorsFromStylesheet(response.body(), externalCssUrl);
                } else {
                    System.err.println("Failed to fetch external stylesheet " + externalCssUrl + ". Status: " + response.statusCode());
                }
            } catch (URISyntaxException e) {
                System.err.println("Malformed URI for external stylesheet: " + externalCssUrl + " - " + e.getMessage());
            } catch (IOException | InterruptedException e) {
                System.err.println("Error fetching external stylesheet " + externalCssUrl + ": " + e.getMessage());
                // If interrupted, restore interruption status
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            } catch (IllegalArgumentException e) {
                 System.err.println("Illegal argument for external stylesheet URI: " + externalCssUrl + " - " + e.getMessage());
            }
        }
    }

    private void extractColorsFromImages(Document doc) {
        if (doc == null) return;
        Elements imgTags = doc.select("img[src]");
        for (Element imgTag : imgTags) {
            String imageUrl = imgTag.absUrl("src");
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                // Basic check for common image extensions, though ImageIO will ultimately determine usability
                if (imageUrl.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp|webp)$")) {
                     analyzeImageForColors(imageUrl, "img src: " + imageUrl);
                } else {
                    System.err.println("Skipping image with non-standard extension: " + imageUrl);
                }
            } else {
                System.err.println("Found img tag with empty src: " + imgTag.outerHtml());
            }
        }
    }

    private void analyzeImageForColors(String imageUrl, String sourceDescription) {
        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                System.err.println("Failed to read image (null image returned) from: " + sourceDescription);
                return;
            }

            Map<Integer, Integer> colorHistogram = new HashMap<>();
            int width = image.getWidth();
            int height = image.getHeight();

            if (width == 0 || height == 0) {
                System.err.println("Image has zero dimension: " + sourceDescription + " (width: " + width + ", height: " + height +")");
                return;
            }

            // Pixel Sampling
            int step = Math.max(1, Math.min(width, height) / 100); // Sample ~100 points along the smaller dimension

            for (int x = 0; x < width; x += step) {
                for (int y = 0; y < height; y += step) {
                    int rgb = image.getRGB(x, y);
                    int rgbOpaque = rgb & 0x00FFFFFF; // Ignore alpha
                    colorHistogram.put(rgbOpaque, colorHistogram.getOrDefault(rgbOpaque, 0) + 1);
                }
            }

            if (colorHistogram.isEmpty()) {
                System.err.println("No colors extracted from image (empty histogram): " + sourceDescription);
                return;
            }

            List<Map.Entry<Integer, Integer>> sortedColors = new ArrayList<>(colorHistogram.entrySet());
            // Sort by frequency in descending order
            sortedColors.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            int maxColorsFromImage = 5;
            for (int i = 0; i < Math.min(maxColorsFromImage, sortedColors.size()); i++) {
                int rgb = sortedColors.get(i).getKey();
                String hexColor = String.format("#%06x", (0xFFFFFF & rgb)); // Ensure lowercase hex
                this.extractedRawColors.add(hexColor);
            }

        } catch (MalformedURLException e) {
            System.err.println("Malformed URL for image: " + sourceDescription + " - " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException reading image " + sourceDescription + ": " + e.getMessage());
        } catch (Exception e) {
            // Catch-all for other unexpected errors during image processing
            System.err.println("Unexpected error processing image " + sourceDescription + ": " + e.getMessage());
            e.printStackTrace(); // Print stack trace for unexpected errors
        }
    }


    private void parseAndCollectColorsFromStylesheet(String cssContent, String sourceDescription) {
        if (cssContent == null || cssContent.trim().isEmpty()) {
            return;
        }
        final CascadingStyleSheet aCSS = CSSReader.readFromString(cssContent, StandardCharsets.UTF_8, ECSSVersion.CSS30);
        if (aCSS == null) {
            System.err.println("Failed to parse CSS content from: " + sourceDescription);
            return;
        }

        for (final ICSSSupportsAwareRule aRule : aCSS.getAllRules()) {
            if (aRule instanceof CSSStyleRule) {
                final CSSStyleRule styleRule = (CSSStyleRule) aRule;
                for (final CSSDeclaration declaration : styleRule.getAllDeclarations()) {
                    processDeclarationForColor(declaration);
                }
            }
            // Potentially handle other rule types like @media, @supports if they can contain style rules
            // For now, focusing on direct CSSStyleRule
        }
    }

    private void parseAndCollectColorsFromDeclarationList(String styleAttributeValue, String sourceDescription) {
        if (styleAttributeValue == null || styleAttributeValue.trim().isEmpty()) {
            return;
        }
        final CSSDeclarationList declarations = CSSReaderDeclarationList.readFromString(styleAttributeValue, ECSSVersion.CSS30);
        if (declarations != null) {
            for (final CSSDeclaration declaration : declarations.getAllDeclarations()) {
                processDeclarationForColor(declaration);
            }
        } else if (!styleAttributeValue.trim().isEmpty()) { // Don't log for empty strings, but log for actual parsing errors
            System.err.println("Failed to parse style attribute: [" + sourceDescription + "] content: " + styleAttributeValue);
        }
    }

    private void processDeclarationForColor(CSSDeclaration declaration) {
        String property = declaration.getProperty().toLowerCase();
        // Common color-related properties
        Set<String> colorProperties = Set.of(
                "color", "background-color", "border-color", "outline-color",
                "text-decoration-color", "text-emphasis-color", "fill", "stroke",
                "background", // Can contain color, e.g., background: #fff url(...)
                "border", "border-top", "border-right", "border-bottom", "border-left",
                "border-top-color", "border-right-color", "border-bottom-color", "border-left-color",
                "box-shadow", "text-shadow" // These can contain colors
        );

        if (!colorProperties.contains(property)) {
            return;
        }

        for (CSSExpressionMember oMember : declaration.getExpression().getAllMembers()) {
            if (oMember instanceof CSSExpressionMemberTermSimple) {
                CSSExpressionMemberTermSimple term = (CSSExpressionMemberTermSimple) oMember;
                String value = term.getValue();
                if (isPotentialColorValue(value)) {
                    this.extractedRawColors.add(value);
                }
            } else if (oMember instanceof CSSExpressionMemberFunction) {
                CSSExpressionMemberFunction function = (CSSExpressionMemberFunction) oMember;
                String funcName = function.getFunctionName().toLowerCase();
                // Check if it's a known color function
                if (funcName.equals("rgb") || funcName.equals("rgba") ||
                    funcName.equals("hsl") || funcName.equals("hsla") ||
                    funcName.equals("hwb") || funcName.equals("lab") ||
                    funcName.equals("lch") || funcName.equals("oklab") ||
                    funcName.equals("oklch") || funcName.equals("color")) {
                    this.extractedRawColors.add(function.getAsCSSString(false));
                }
            }
        }
    }

    private boolean isPotentialColorValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String lowerValue = value.toLowerCase();

        // Hex codes: #rgb, #rgba, #rrggbb, #rrggbbaa
        Pattern hexPattern = Pattern.compile("^#([a-f0-9]{3}|[a-f0-9]{4}|[a-f0-9]{6}|[a-f0-9]{8})$", Pattern.CASE_INSENSITIVE);
        if (hexPattern.matcher(lowerValue).matches()) {
            return true;
        }

        // Named colors
        if (CSSColorHelper.isDefaultColorName(lowerValue)) {
            return true;
        }

        // Keywords
        return lowerValue.equals("transparent") || lowerValue.equals("currentcolor");
    }
}
