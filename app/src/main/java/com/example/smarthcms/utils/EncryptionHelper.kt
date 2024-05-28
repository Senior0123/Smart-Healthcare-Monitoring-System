package com.example.smarthcms.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionHelper(private val password: String) {

    fun encrypt(plainText: String): String {
        val salt = ByteArray(16)
        val iv = ByteArray(16)
        val random = SecureRandom()
        random.nextBytes(salt)
        random.nextBytes(iv)

        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val secretKeyTmp = secretKeyFactory.generateSecret(keySpec)
        val secretKey = SecretKeySpec(secretKeyTmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        val encryptedText = cipher.doFinal(plainText.toByteArray())

        val cipherData = ByteArray(salt.size + iv.size + encryptedText.size)
        System.arraycopy(salt, 0, cipherData, 0, salt.size)
        System.arraycopy(iv, 0, cipherData, salt.size, iv.size)
        System.arraycopy(encryptedText, 0, cipherData, salt.size + iv.size, encryptedText.size)

        return Base64.encodeToString(cipherData, Base64.DEFAULT)
    }

    fun decrypt(cipherText: String): String {
        val cipherData = Base64.decode(cipherText, Base64.DEFAULT)
        val salt = cipherData.sliceArray(0..15)
        val iv = cipherData.sliceArray(16..31)
        val encryptedText = cipherData.sliceArray(32 until cipherData.size)

        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val secretKeyTmp = secretKeyFactory.generateSecret(keySpec)
        val secretKey = SecretKeySpec(secretKeyTmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        val decryptedText = cipher.doFinal(encryptedText)

        return String(decryptedText)
    }
}