// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.preference

import android.content.Context
import android.content.SharedPreferences
import ro.wethecitizens.firstcontact.TracerApp

object Preference {

    private const val PREF_ID = "Tracer_pref"
    private const val IS_ONBOARDED = "IS_ONBOARDED"
    private const val PHONE_NUMBER = "PHONE_NUMBER"
    private const val CHECK_POINT = "CHECK_POINT"
    private const val HANDSHAKE_PIN = "HANDSHAKE_PIN"

//    private const val TEMP_ID = "TEMP_ID"
//    private const val NEXT_FETCH_TIME = "NEXT_FETCH_TIME"
//    private const val EXPIRY_TIME = "EXPIRY_TIME"
//    private const val LAST_FETCH_TIME = "LAST_FETCH_TIME"

    private const val LAST_PURGE_TIME = "LAST_PURGE_TIME"
    private const val INSTALL_DATE_TS = "INSTALL_DATE_TS"
    private const val PATIENT_ID_QR = "PATIENT_ID_QR"
    private const val ANNOUNCEMENT = "ANNOUNCEMENT"
    private const val IS_UPLOAD_COMPLETE = "IS_UPLOAD_COMPLETE"


    fun putHandShakePin(context: Context, value: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putString(HANDSHAKE_PIN, value).apply()
    }

    fun getHandShakePin(context: Context): String {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getString(HANDSHAKE_PIN, "AERTVC") ?: "AERTVC"
    }

    fun putIsOnBoarded(context: Context, value: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putBoolean(IS_ONBOARDED, value).apply()
    }

    fun isOnBoarded(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getBoolean(IS_ONBOARDED, false)
    }

    fun putPhoneNumber(context: Context, value: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putString(PHONE_NUMBER, value).apply()
    }

    fun getPhoneNumber(context: Context): String {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getString(PHONE_NUMBER, "") ?: ""
    }

    fun putCheckpoint(context: Context, value: Int) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putInt(CHECK_POINT, value).apply()
    }

    fun getCheckpoint(context: Context): Int {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getInt(CHECK_POINT, 0)
    }

//    fun putTempID(context: Context, tempID: String) {
//        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
//            .edit().putString(TEMP_ID, tempID).apply()
//    }
//
//    fun getTempID(context: Context): String {
//        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
//            .getString(TEMP_ID, "") ?: ""
//    }
//
//    fun getLastFetchTimeInMillis(context: Context): Long {
//        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
//            .getLong(
//                LAST_FETCH_TIME, 0
//            )
//    }
//
//    fun putLastFetchTimeInMillis(context: Context, time: Long) {
//        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
//            .edit().putLong(LAST_FETCH_TIME, time).apply()
//    }
//
//    fun putNextFetchTimeInMillis(context: Context, time: Long) {
//        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
//            .edit().putLong(NEXT_FETCH_TIME, time).apply()
//    }
//
//    fun getNextFetchTimeInMillis(context: Context): Long {
//        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
//            .getLong(
//                NEXT_FETCH_TIME, 0
//            )
//    }
//
//    fun putExpiryTimeInMillis(context: Context, time: Long) {
//        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
//            .edit().putLong(EXPIRY_TIME, time).apply()
//    }
//
//    fun getExpiryTimeInMillis(context: Context): Long {
//        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
//            .getLong(
//                EXPIRY_TIME, 0
//            )
//    }

    fun putAnnouncement(context: Context, announcement: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putString(ANNOUNCEMENT, announcement).apply()
    }

    fun getAnnouncement(context: Context): String {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getString(ANNOUNCEMENT, "") ?: ""
    }

    fun putLastPurgeTime(context: Context, lastPurgeTime: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(LAST_PURGE_TIME, lastPurgeTime).apply()
    }

    fun getLastPurgeTime(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getLong(LAST_PURGE_TIME, 0)
    }

    fun putInstallDateTS(context: Context, installDateTS: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(INSTALL_DATE_TS, installDateTS).apply()
    }

    fun getInstallDateTS(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getLong(INSTALL_DATE_TS, 0)
    }

    fun registerListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun putPatientIdQr(value: String) {
        TracerApp.AppContext.getSharedPreferences(
            PREF_ID, Context.MODE_PRIVATE)
            .edit().putString(PATIENT_ID_QR, value).apply()
    }

    fun getPatientIdQr(): String? {
        return TracerApp.AppContext.getSharedPreferences(
            PREF_ID, Context.MODE_PRIVATE)
            .getString(PATIENT_ID_QR, null)
    }


    fun putIsUploadComplete(context: Context, value: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putBoolean(IS_UPLOAD_COMPLETE, value).apply()
    }

    fun isUploadComplete(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getBoolean(IS_UPLOAD_COMPLETE, false)
    }
}
