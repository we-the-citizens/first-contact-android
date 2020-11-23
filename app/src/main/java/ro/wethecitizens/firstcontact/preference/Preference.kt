// Copyright (c) 2020 BlueTrace.io
// Copyright (c) 2020 Noi, Cetatenii

package ro.wethecitizens.firstcontact.preference

import android.content.Context
import android.content.SharedPreferences
import ro.wethecitizens.firstcontact.TracerApp

object Preference {

    private const val PREF_ID = "Tracer_pref"
    private const val IS_ONBOARDED = "IS_ONBOARDED"
    private const val LAST_PURGE_TIME = "LAST_PURGE_TIME"
    private const val INSTALL_DATE_TS = "INSTALL_DATE_TS"
    private const val ANNOUNCEMENT = "ANNOUNCEMENT"
    private const val IS_UPLOAD_COMPLETE = "IS_UPLOAD_COMPLETE"
    private const val IS_UPLOAD_SENT = "IS_UPLOAD_SENT"

    fun putIsOnBoarded(context: Context, value: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putBoolean(IS_ONBOARDED, value).apply()
    }

    fun isOnBoarded(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getBoolean(IS_ONBOARDED, false)
    }

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

    fun putIsUploadSent(context: Context, value: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putBoolean(IS_UPLOAD_SENT, value).apply()
    }

    fun isUploadSent(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getBoolean(IS_UPLOAD_SENT, false)
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
