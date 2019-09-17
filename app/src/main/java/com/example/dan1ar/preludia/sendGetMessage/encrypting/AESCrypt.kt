package com.example.dan1ar.preludia.sendGetMessage.encrypting

import android.os.Build
import android.support.annotation.RequiresApi
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESCrypt
{
    private var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    fun generateKey() : SecretKey
    {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(128)

        return keyGenerator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(secretKey : SecretKey, data : String?) : Array<String>
    {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val params = cipher.parameters

        val iv = params.getParameterSpec(IvParameterSpec::class.java).iv
        val ivString = Base64.getEncoder().encodeToString(iv)

        val cipherText = Base64.getEncoder().encodeToString(cipher.doFinal(data!!.toByteArray()))

        val array = Array(2) {""}
        array[0] = cipherText
        array[1] = ivString

        return array
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(secretKeyString : String, ivString: String, cipherText : String) : String
    {
        val decodedSecretKey = Base64.getDecoder().decode(secretKeyString)
        val secretKey = SecretKeySpec(decodedSecretKey, "AES")
        val iv = Base64.getDecoder().decode(ivString)

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        return String(cipher.doFinal(Base64.getDecoder().decode(cipherText)))
    }
}