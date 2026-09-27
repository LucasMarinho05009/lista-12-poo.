package br.com.technexus.main;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Desafio didático: testa somente as combinações e o texto cifrado da lista.
 * O processamento é local; nenhuma URL é acessada.
 */
public class ForcaBruta {
    public static void main(String[] args) {
        String encryptedB64 = "U2FsdGVkX189CvKETNa+k2wHIMpbCwNk7HKB3nBRzOD9bBaPt2nFMCdElvKoRfTmmqVv41Trh37ORXFWRVNOX3vpgPHULkkaoyh9DfmzrGBXkGnu/SJfQkGuU08zbgMQNSwCGTwoIHkUMzRFQELN0Q==";
        byte[] dados = Base64.getDecoder().decode(encryptedB64);
        byte[] salt = Arrays.copyOfRange(dados, 8, 16);
        byte[] textoCifrado = Arrays.copyOfRange(dados, 16, dados.length);
        String charset = "0123456789abcdefghijklmnopqrstuvwxyz";
        int tamanhoSufixo = 3;
        int limite = (int) Math.pow(charset.length(), tamanhoSufixo);
        long inicio = System.currentTimeMillis();

        try {
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            for (int tentativa = 0; tentativa < limite; tentativa++) {
                int indice = tentativa;
                char[] sufixo = new char[tamanhoSufixo];
                for (int pos = tamanhoSufixo - 1; pos >= 0; pos--) {
                    sufixo[pos] = charset.charAt(indice % charset.length());
                    indice /= charset.length();
                }
                String senha = "lam" + new String(sufixo);
                PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, 1000, 384);
                byte[] keyAndIv;
                try {
                    keyAndIv = fabrica.generateSecret(spec).getEncoded();
                } finally {
                    spec.clearPassword();
                }
                byte[] key = Arrays.copyOfRange(keyAndIv, 0, 32);
                byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                        new IvParameterSpec(iv));
                try {
                    String resultado = new String(cipher.doFinal(textoCifrado),
                            StandardCharsets.UTF_8).trim();
                    // Padding válido sozinho não comprova que a senha está correta.
                    if (resultado.matches("https?://[^\\s\\p{Cntrl}]+")) {
                        System.out.println("Senha encontrada: " + senha);
                        System.out.println("Link revelado: " + resultado);
                        System.out.println("Tentativas: " + (tentativa + 1));
                        System.out.println("Tempo (ms): " + (System.currentTimeMillis() - inicio));
                        return;
                    }
                } catch (BadPaddingException e) {
                    // Senha incorreta: segue para a próxima combinação.
                }
            }
            System.out.println("Senha não encontrada.");
        } catch (GeneralSecurityException e) {
            // Falhas de configuração não são confundidas com uma senha incorreta.
            System.err.println("Falha no mecanismo criptográfico: " + e.getMessage());
        }
    }
}
