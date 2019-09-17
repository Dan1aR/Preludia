//
package sendGetMessage.encrypting

import java.security.MessageDigest

class Hashes
{
    fun hash(bytesString : String): String
    {
        val bytes = bytesString.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }.dropLast(32)
    }
}