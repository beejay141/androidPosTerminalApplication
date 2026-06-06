package com.iisysgroup.androidlite;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by Agbede on 2/7/2018.
 */

public class SettingsFragment extends PreferenceFragment

{
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        final String KEY_TERMINAL_ID =  getResources().getString(R.string.key_terminal_id);
        final String KEY_IP_ADDRESS = getResources().getString(R.string.key_ip_address);
        final String KEY_PORT = getResources().getString(R.string.key_pref_port);
        final String KEY_HOST_TYPE = getResources().getString(R.string.key_host_type);
        final String KEY_PORT_TYPE = getResources().getString(R.string.key_pref_port_type);
        final String KEY_PLATFORM = getString(R.string.key_platform);

        Preference terminal_key = findPreference(KEY_TERMINAL_ID);
        String terminal_key_string = terminal_key.getSharedPreferences().getString(KEY_TERMINAL_ID, null);
        terminal_key.setSummary(terminal_key_string);

        Preference ip_address_key = findPreference(KEY_IP_ADDRESS);
        String ip_address_key_string = terminal_key.getSharedPreferences().getString(KEY_IP_ADDRESS, null);
        ip_address_key.setSummary(ip_address_key_string);

        Preference key_port_key = findPreference(KEY_PORT);
        String key_port_key_string = terminal_key.getSharedPreferences().getString(KEY_PORT, null);
        key_port_key.setSummary(key_port_key_string);

        Preference key_host_type_key = findPreference(KEY_HOST_TYPE);
        String key_host_type_key_string = terminal_key.getSharedPreferences().getString(KEY_HOST_TYPE, null);
        key_host_type_key.setSummary(key_host_type_key_string);

        Preference key_port_type_key = findPreference(KEY_PORT_TYPE);
        String key_port_type_key_string = terminal_key.getSharedPreferences().getString(KEY_PORT_TYPE, null);
        key_port_type_key.setSummary(key_port_type_key_string);

        Preference key_platform = findPreference(KEY_PLATFORM);
        String key_platform_string = terminal_key.getSharedPreferences().getString(KEY_PLATFORM, null);
        key_platform.setSummary(key_platform_string);

    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (getActivity() != null){
            final String KEY_TERMINAL_ID =  getResources().getString(R.string.key_terminal_id);
            final String KEY_IP_ADDRESS = getResources().getString(R.string.key_ip_address);
            final String KEY_PORT = getResources().getString(R.string.key_pref_port);
            final String KEY_HOST_TYPE = getResources().getString(R.string.key_host_type);
            final String KEY_PORT_TYPE = getResources().getString(R.string.key_pref_port_type);


            final String KEY_PLATFORM = getString(R.string.key_platform);

            if (key.equals(KEY_TERMINAL_ID))
            {
                EditTextPreference editTextPreference = (EditTextPreference) findPreference(key);
                String pref_changed_element = sharedPreferences.getString(key, null);
                if (pref_changed_element != null)
                    editTextPreference.setSummary(pref_changed_element);
            } else if (key.equals(KEY_IP_ADDRESS)){
                EditTextPreference editTextPreference = (EditTextPreference) findPreference(key);
                String pref_changed_element = sharedPreferences.getString(key, null);
                if (pref_changed_element != null)
                    editTextPreference.setSummary(pref_changed_element);

            } else if (key.equals(KEY_PORT)){
                EditTextPreference editTextPreference = (EditTextPreference) findPreference(key);
                String pref_changed_element = sharedPreferences.getString(key, null);
                if (pref_changed_element != null)
                    editTextPreference.setSummary(pref_changed_element);

            } else if (key.equals(KEY_HOST_TYPE)){
                ListPreference listPreference = (ListPreference) findPreference(key);
                String pref_changed_element = sharedPreferences.getString(key, null);
                if (pref_changed_element != null)
                    listPreference.setSummary(pref_changed_element);

            } else if (key.equals(KEY_PORT_TYPE)){
                ListPreference listPreference = (ListPreference) findPreference(key);
                String pref_changed_element = sharedPreferences.getString(key, null);
                if (pref_changed_element != null)
                    listPreference.setSummary(pref_changed_element);
            }

            else if (key.equals(KEY_PLATFORM)) {
                ListPreference listPreference = (ListPreference) findPreference(key);
                String pref_changed_element = sharedPreferences.getString(key, null);
                if (pref_changed_element != null)
                    listPreference.setSummary(pref_changed_element);
            }
        }

        }
    };
}
