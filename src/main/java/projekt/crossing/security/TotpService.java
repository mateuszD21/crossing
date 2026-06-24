package projekt.crossing.security;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class TotpService {

    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP   = 30; // sekund
    private static final int WINDOW      = 1;  // ±1 okno tolerancji

    /** Generuje losowy 20-bajtowy secret zakodowany w Base32. */
    public String generateSecret() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return new Base32().encodeToString(bytes).replace("=", "");
    }

    /**
     * Weryfikuje kod TOTP z tolerancją ±WINDOW okien czasowych.
     *
     * @param secret  Base32-encoded secret użytkownika
     * @param code    6-cyfrowy kod wpisany przez użytkownika
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.length() != CODE_DIGITS) return false;
        try {
            int userCode = Integer.parseInt(code.trim());
            long counter = Instant.now().getEpochSecond() / TIME_STEP;
            for (int i = -WINDOW; i <= WINDOW; i++) {
                if (generateTotp(secret, counter + i) == userCode) return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getQrBase64(String username, String secret, String issuer) {
        try {
            String otpauth = "otpauth://totp/"
                    + encode(issuer + ":" + username)
                    + "?secret=" + encode(secret)
                    + "&issuer=" + encode(issuer)
                    + "&algorithm=SHA1&digits=6&period=30";

            BitMatrix matrix = new QRCodeWriter()
                    .encode(otpauth, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania QR", e);
        }
    }

    private int generateTotp(String base32Secret, long counter) {
        try {
            byte[] key   = new Base32().decode(base32Secret.toUpperCase());
            byte[] msg   = ByteBuffer.allocate(8).putLong(counter).array();
            byte[] hash  = hmacSha1(key, msg);
            int offset   = hash[hash.length - 1] & 0x0F;
            int binary   = ((hash[offset]     & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) <<  8)
                    |  (hash[offset + 3] & 0xFF);
            return binary % (int) Math.pow(10, CODE_DIGITS);
        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania TOTP", e);
        }
    }

    private byte[] hmacSha1(byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        return mac.doFinal(data);
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
