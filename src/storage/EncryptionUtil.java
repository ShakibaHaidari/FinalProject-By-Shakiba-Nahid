package storage;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil{
    private static final String massegesecret = "ChatRoomKey12345";
    private EncryptionUtil() {}
    public static String encrypt(String text){

        try {
            SecretKeySpec key = new SecretKeySpec(massegesecret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(text.getBytes());

            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e){
            throw new IllegalStateException("Could not encrypt message", e);
        }
    }
    public static String decrypt(String textencrypt){
        try{
            SecretKeySpec key = new SecretKeySpec(massegesecret.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(textencrypt);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);

        } catch (Exception e) {throw new IllegalStateException("Could not decrypt message", e);
        }
    }
}