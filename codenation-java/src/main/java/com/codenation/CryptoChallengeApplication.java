package main.java.com.codenation;

import main.java.com.codenation.utility.MultipartUtility;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CryptoChallengeApplication {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String TOKEN = "bdb8061c657acc21727f3ac92c6c057c3269f9dd";
    private static final String GET_URL = "https://api.codenation.dev/v1/challenge/dev-ps/generate-data?token=".concat(TOKEN);
    private static final String POST_URL = "https://api.codenation.dev/v1/challenge/dev-ps/submit-solution?token=".concat(TOKEN);

    private static String jsonContent;
    private static BufferedWriter writer;

    public static void main(String[] args) throws IOException {
        getRequest();
        //postRequest();
    }

    private static void getRequest() throws IOException {
        URL url = new URL(GET_URL);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("GET");
        http.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = http.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    http.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            jsonContent = response.toString();
            saveResponse();
        } else {
            System.out.println("GET request not worked: Code " + responseCode);
        }

    }

    private static void postRequest() {
        String charset = "UTF-8";
        File uploadFile = new File("answer.json");
        String requestURL = POST_URL;

        //File initialFile = new File("src/main/resources/sample.txt");
        //InputStream targetStream = new FileInputStream(initialFile);

        try {
            MultipartUtility multipart = new MultipartUtility(requestURL, charset);

            multipart.addHeaderField("User-Agent", USER_AGENT);

            //multipart.addFormField("keywords", "Java,upload,Spring");

            multipart.addFilePart("answer", uploadFile);

            List<String> response = multipart.finish();

            System.out.println("SERVER REPLIED:");

            for (String line : response) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private static void sendFile(OutputStream out, String name, InputStream in) throws IOException {
        String o = "Content-Disposition: form-data; name=\"" + URLEncoder.encode(name,"UTF-8")
                + "\"; filename=\"" + "\"\r\n\r\n";
        out.write(o.getBytes(StandardCharsets.UTF_8));
        byte[] buffer = new byte[2048];
        for (int n = 0; n >= 0; n = in.read(buffer))
            out.write(buffer, 0, n);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static void saveResponse() throws IOException {
        writer = new BufferedWriter(new FileWriter("answer.json"));
        writer.write(jsonContent);
        writer.close();

        getJsonValues(jsonContent);
    }

    private static void getJsonValues(String json) throws IOException {
        String cifrado = "\"cifrado\":\"";
        String decifrado = "\",\"decifrado";
        String numeroCasas = "numero_casas\":";
        String tokenStr = ",\"token";

        int strStart = json.lastIndexOf(cifrado) + cifrado.length();
        int strEnd = json.lastIndexOf(decifrado);

        int casasStart = json.lastIndexOf(numeroCasas) + numeroCasas.length();
        int casasEnd = json.lastIndexOf(tokenStr);

        String cifradoText = json.substring(strStart, strEnd).toLowerCase();
        String decryptionKey = json.substring(casasStart, casasEnd);

        decryptJson(cifradoText, Integer.parseInt(decryptionKey));
    }

    private static void decryptJson(String text, int key) throws IOException {
        String result = "";

        for (int i = 0; i < text.length(); i++){
            // one char per time
            char letter = text.charAt(i);

            if (letter >= 'a' && letter <= 'z') {
                letter = (char) (letter + key);
                if (letter > 'z') {
                    // go to start
                    letter = (char) (letter + 'a' - 'z' - 1);
                }
                result += letter;
            } else {
                result += letter;
            }
        }

        updateDecrypted(result);
    }

    private static void updateDecrypted(String result) throws IOException {
        String decifrado = "\"decifrado\":\"";
        String resumo = "\",\"resumo";

        int strStart = jsonContent.lastIndexOf(decifrado) + decifrado.length();
        int strEnd = jsonContent.lastIndexOf(resumo);

        String jsonFirstHalf = jsonContent.substring(0, strStart);
        String jsonSecondHalf = jsonContent.substring(strEnd);

        jsonContent = jsonFirstHalf.concat(result).concat(jsonSecondHalf);

        useSha1(result);
    }

    private static void useSha1(String result) throws IOException {
        String sha1 = "";
        // sha1 algorithm starts
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(result.getBytes("utf8"));
            sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // sha1 algorithm ends

        updateResume(sha1);
    }

    private static void updateResume(String sha1) throws IOException {
        String resumo = "resumo_criptografico\":\"";
        String jsonEnd = "\"}";

        int strStart = jsonContent.lastIndexOf(resumo) + resumo.length();
        int strEnd = jsonContent.lastIndexOf(jsonEnd);

        String jsonFirstHalf = jsonContent.substring(0, strStart);
        String jsonSecondHalf = jsonContent.substring(strEnd);

        jsonContent = jsonFirstHalf.concat(sha1).concat(jsonSecondHalf);

        updateJsonFile();
    }

    private static void updateJsonFile() throws IOException {
        writer = new BufferedWriter(new FileWriter("answer.json"));
        writer.write(String.valueOf(jsonContent));
        writer.close();
    }

}
