package com.sowmya.securechat;

import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by sowmya on 5/24/17.
 */

public class Encryption {

    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'S', 'e', 'c', 'u', 'r', 'e', 'c',
                    'h', 'a', 't', 'e', 'n', 'c', 'r', 'y', 't'};

    /**
     * This method is used to encrpt the message,
     * first its is encrypted in level one and then level 2
     * @param message
     * @return twol level encrypted message
     */
    public static String encrypt(String message)
    {
        return level_2_Encrypt(level_1_Encript(message));
    }

    public static String decrypt(String message)
    {
        return level_1_Decript(level_2_Decrypt(message));
    }

    private static String level_2_Encrypt(String level_1_encrypted_data) {


        Cipher c = null;
        String encryptedValue = null;
        try {

            Key key = generateKey();
            c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(level_1_encrypted_data.getBytes());

            //Base-64 Encodeing String
            encryptedValue = Base64.encodeToString(encVal,1);
            return encryptedValue;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return encryptedValue;

    }

    private static String level_2_Decrypt(String encryptedData){
        Key key = generateKey();
        Cipher c = null;
        String decryptedValue = null;
        try {
            c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            //decoding the String to Bytes
            byte[] decordedValue = Base64.decode(encryptedData,1);
            byte[] decValue = c.doFinal(decordedValue);
            decryptedValue = new String(decValue);
            return decryptedValue;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return decryptedValue;

    }



    private static String encriptDigit(int number){
        return Integer.toBinaryString(number+(number%10))+"#"+number%10;
    }

    private static String level_1_Encript(String message){

        String temp="";
        for(int i=0; i<message.length(); i++){
            temp+=encriptDigit(message.charAt(i))+",";
        }
        return temp.substring(0,temp.length()-1);
    }

    private static String level_1_Decript(String message){

        String[] array = message.split(",");
        String temp="";
        for(String s : array){
            String[] aNum = s.split("#");
            temp+= (char) (Integer.parseInt(aNum[0],2)-Integer.parseInt(aNum[1]));
        }
        return temp;
    }


   /* public static void main(String[] args) {
        // Send this message to firebase
        System.out.println("Encripted message "+Encription.Encript("ABCD"));

        // you will retrive from firebase and decript it
        System.out.println(Encription.Decript(Encription.Encript("ABCD")));
    }*/

    private static Key generateKey(){
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

}
