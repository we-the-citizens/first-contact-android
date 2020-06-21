// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Base64
import android.util.Log
import ro.wethecitizens.firstcontact.TracerApp
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.Arrays

/**
 * This is a helper class to generate your message hash to be included in your SMS message.
 *
 * Without the correct hash, your app won't recieve the message callback. This only needs to be
 * generated once per app and stored. Then you can remove this helper class from your code.
 *
 * [Source](https://github.com/googlearchive/android-credentials/blob/master/sms-verification/android/app/src/main/java/com/google/samples/smartlock/sms_verify/AppSignatureHelper.java)
 */
object AppSignatureHelper {
    private val TAG = AppSignatureHelper::class.java.simpleName
    private const val HASH_TYPE = "SHA-256"
    private const val NUM_HASHED_BYTES = 9
    private const val NUM_BASE64_CHAR = 11

    fun getAppHash() = appSignatures(TracerApp.AppContext).first()

// For each signature create a compatible hash
    /**
     * Get all the app signatures for the current package
     * @return
     */
    private fun appSignatures(context: Context): ArrayList<String> {
        val appCodes: ArrayList<String> = ArrayList()
        try { // Get all package signatures for the current package
            val packageName = context.packageName
            val packageManager = context.packageManager
            val signatures: Array<Signature> = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            ).signatures
            // For each signature create a compatible hash
            for (signature in signatures) {
                val hash =
                    hash(packageName, signature.toCharsString())
                if (hash != null) {
                    appCodes.add(String.format("%s", hash))
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Unable to find package to obtain hash.", e)
        }
        return appCodes
    }

    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature"
        try {
            val messageDigest: MessageDigest =
                MessageDigest.getInstance(HASH_TYPE)
            messageDigest.update(appInfo.byteInputStream(StandardCharsets.UTF_8).readBytes())
            var hashSignature: ByteArray = messageDigest.digest()
            // truncated into NUM_HASHED_BYTES
            hashSignature = Arrays.copyOfRange(
                hashSignature,
                0,
                NUM_HASHED_BYTES
            )
            // encode into Base64
            var base64Hash: String =
                Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR)
            Log.d(
                TAG,
                String.format("pkg: %s -- hash: %s", packageName, base64Hash)
            )
            return base64Hash
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "hash:NoSuchAlgorithm", e)
        }
        return null
    }
}
