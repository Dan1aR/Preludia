//
package com.example.dan1ar.preludia.sendGetMessage.encrypting

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import java.io.File
import java.lang.Error
import java.nio.charset.StandardCharsets
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher

class RSACrypt
{

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun generateKeyPair(context: Context)
    {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(512, SecureRandom())

        val pair = generator.generateKeyPair()
        val publicKey = Base64.getEncoder().encodeToString(pair.public.encoded)
        val privateKey = Base64.getEncoder().encodeToString(pair.private.encoded)

        File(context.filesDir, "PrivateKey.pem").writeText(privateKey)
        File(context.filesDir, "PublicKey.pem").writeText(publicKey)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun encrypt(plainText: String, publicKey: PublicKey): String
    {
        val encryptCipher = Cipher.getInstance("RSA")
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val cipherText = encryptCipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        return Base64.getEncoder().encodeToString(cipherText)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    fun decrypt(cipherText: String, privateKey: PrivateKey): String?
    {
        try
        {
            val bytes = Base64.getDecoder().decode(cipherText)

            val decriptCipher = Cipher.getInstance("RSA")
            decriptCipher.init(Cipher.DECRYPT_MODE, privateKey)

            return String(decriptCipher.doFinal(bytes), StandardCharsets.UTF_8)
        }
        catch (e : Error)
        {
            return null
        }
    }

}