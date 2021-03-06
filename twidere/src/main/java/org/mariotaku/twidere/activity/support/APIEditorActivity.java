/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity.support;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ThemeUtils;

import twitter4j.TwitterConstants;

import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.Utils.getNonEmptyString;
import static org.mariotaku.twidere.util.Utils.trim;

public class APIEditorActivity extends BaseSupportDialogActivity implements TwitterConstants, OnCheckedChangeListener,
        OnClickListener {

    private EditText mEditAPIUrlFormat;
    private CheckBox mEditSameOAuthSigningUrl, mEditNoVersionSuffix;
    private EditText mEditConsumerKey, mEditConsumerSecret;
    private RadioGroup mEditAuthType;
    private RadioButton mButtonOAuth, mButtonxAuth, mButtonBasic, mButtonTwipOMode;
    private TextView mAdvancedAPIConfigLabel;
    private View mAdvancedAPIConfigContainer;
    private View mAdvancedAPIConfigView;
    private Button mSaveButton;
    private View mAPIFormatHelpButton;

    @Override
    public void onCheckedChanged(final RadioGroup group, final int checkedId) {
        final int authType = getCheckedAuthType(checkedId);
        final boolean isOAuth = authType == Accounts.AUTH_TYPE_OAUTH || authType == Accounts.AUTH_TYPE_XAUTH;
        mAdvancedAPIConfigContainer.setVisibility(isOAuth ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.save: {
                if (checkUrlErrors()) return;
                final String apiUrlFormat = parseString(mEditAPIUrlFormat.getText());
                final int authType = getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId());
                if (authType == Accounts.AUTH_TYPE_TWIP_O_MODE && !apiUrlFormat.endsWith("/1.1/")) {
                    new TWIPNoticeDialogFragment().show(getFragmentManager(), "twip_o_mode_bug_notice");
                    return;
                }
                saveAndFinish();
                break;
            }
            case R.id.advanced_api_config_label: {
                final boolean isVisible = mAdvancedAPIConfigView.isShown();
                final int compoundRes = isVisible ? R.drawable.expander_close_holo : R.drawable.expander_open_holo;
                mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(compoundRes, 0, 0, 0);
                mAdvancedAPIConfigView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                break;
            }
            case R.id.api_url_format_help: {
                Toast.makeText(this, R.string.api_url_format_help, Toast.LENGTH_LONG).show();
                break;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mEditAPIUrlFormat = (EditText) findViewById(R.id.api_url_format);
        mEditAuthType = (RadioGroup) findViewById(R.id.auth_type);
        mButtonOAuth = (RadioButton) findViewById(R.id.oauth);
        mButtonxAuth = (RadioButton) findViewById(R.id.xauth);
        mButtonBasic = (RadioButton) findViewById(R.id.basic);
        mButtonTwipOMode = (RadioButton) findViewById(R.id.twip_o);
        mAdvancedAPIConfigContainer = findViewById(R.id.advanced_api_config_container);
        mAdvancedAPIConfigLabel = (TextView) findViewById(R.id.advanced_api_config_label);
        mAdvancedAPIConfigView = findViewById(R.id.advanced_api_config);
        mEditSameOAuthSigningUrl = (CheckBox) findViewById(R.id.same_oauth_signing_url);
        mEditNoVersionSuffix = (CheckBox) findViewById(R.id.no_version_suffix);
        mEditConsumerKey = (EditText) findViewById(R.id.consumer_key);
        mEditConsumerSecret = (EditText) findViewById(R.id.consumer_secret);
        mSaveButton = (Button) findViewById(R.id.save);
        mAPIFormatHelpButton = findViewById(R.id.api_url_format_help);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        final String apiUrlFormat = parseString(mEditAPIUrlFormat.getText());
        final int authType = getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId());
        final boolean sameOAuthSigningUrl = mEditSameOAuthSigningUrl.isChecked();
        final boolean noVersionSuffix = mEditNoVersionSuffix.isChecked();
        final String consumerKey = parseString(mEditConsumerKey.getText());
        final String consumerSecret = parseString(mEditConsumerSecret.getText());
        outState.putString(Accounts.API_URL_FORMAT, apiUrlFormat);
        outState.putInt(Accounts.AUTH_TYPE, authType);
        outState.putBoolean(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl);
        outState.putBoolean(Accounts.NO_VERSION_SUFFIX, noVersionSuffix);
        outState.putString(Accounts.CONSUMER_KEY, consumerKey);
        outState.putString(Accounts.CONSUMER_SECRET, consumerSecret);
        super.onSaveInstanceState(outState);
    }

    public void saveAndFinish() {
        final String apiUrlFormat = parseString(mEditAPIUrlFormat.getText());
        final int authType = getCheckedAuthType(mEditAuthType.getCheckedRadioButtonId());
        final boolean sameOAuthSigningUrl = mEditSameOAuthSigningUrl.isChecked();
        final boolean noVersionSuffix = mEditNoVersionSuffix.isChecked();
        final String consumerKey = parseString(mEditConsumerKey.getText());
        final String consumerSecret = parseString(mEditConsumerSecret.getText());
        final Intent intent = new Intent();
        intent.putExtra(Accounts.API_URL_FORMAT, apiUrlFormat);
        intent.putExtra(Accounts.AUTH_TYPE, authType);
        intent.putExtra(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl);
        intent.putExtra(Accounts.NO_VERSION_SUFFIX, noVersionSuffix);
        intent.putExtra(Accounts.CONSUMER_KEY, consumerKey);
        intent.putExtra(Accounts.CONSUMER_SECRET, consumerSecret);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_editor);

        String apiUrlFormat;
        int authType;
        boolean sameOAuthSigningUrl, noVersionSuffix;
        String consumerKey, consumerSecret;

        final SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final String prefApiUrlFormat = getNonEmptyString(pref, KEY_API_URL_FORMAT, DEFAULT_REST_BASE_URL);
        final int prefAuthType = pref.getInt(KEY_AUTH_TYPE, Accounts.AUTH_TYPE_OAUTH);
        final boolean prefSameOAuthSigningUrl = pref.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false);
        final boolean prefNoVersionSuffix = pref.getBoolean(KEY_NO_VERSION_SUFFIX, false);
        final String prefConsumerKey = getNonEmptyString(pref, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_3);
        final String prefConsumerSecret = getNonEmptyString(pref, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET_3);
        if (savedInstanceState != null) {
            apiUrlFormat = trim(savedInstanceState.getString(Accounts.API_URL_FORMAT, prefApiUrlFormat));
            authType = savedInstanceState.getInt(Accounts.AUTH_TYPE, prefAuthType);
            sameOAuthSigningUrl = savedInstanceState.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL,
                    prefSameOAuthSigningUrl);
            noVersionSuffix = savedInstanceState.getBoolean(Accounts.NO_VERSION_SUFFIX,
                    prefNoVersionSuffix);
            consumerKey = trim(savedInstanceState.getString(Accounts.CONSUMER_KEY, prefConsumerKey));
            consumerSecret = trim(savedInstanceState.getString(Accounts.CONSUMER_SECRET, prefConsumerSecret));
        } else {
            final Intent intent = getIntent();
            final Bundle extras = intent.getExtras();
            apiUrlFormat = trim(extras.getString(Accounts.API_URL_FORMAT, prefApiUrlFormat));
            authType = extras.getInt(Accounts.AUTH_TYPE, prefAuthType);
            sameOAuthSigningUrl = extras.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL, prefSameOAuthSigningUrl);
            noVersionSuffix = extras.getBoolean(Accounts.NO_VERSION_SUFFIX, prefNoVersionSuffix);
            consumerKey = trim(extras.getString(Accounts.CONSUMER_KEY, prefConsumerKey));
            consumerSecret = trim(extras.getString(Accounts.CONSUMER_SECRET, prefConsumerSecret));
        }

        mEditAuthType.setOnCheckedChangeListener(this);
        mAdvancedAPIConfigLabel.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        mAPIFormatHelpButton.setOnClickListener(this);

        mEditAPIUrlFormat.setText(apiUrlFormat);
        mEditSameOAuthSigningUrl.setChecked(sameOAuthSigningUrl);
        mEditNoVersionSuffix.setChecked(noVersionSuffix);
        mEditConsumerKey.setText(consumerKey);
        mEditConsumerSecret.setText(consumerSecret);

        mButtonOAuth.setChecked(authType == Accounts.AUTH_TYPE_OAUTH);
        mButtonxAuth.setChecked(authType == Accounts.AUTH_TYPE_XAUTH);
        mButtonBasic.setChecked(authType == Accounts.AUTH_TYPE_BASIC);
        mButtonTwipOMode.setChecked(authType == Accounts.AUTH_TYPE_TWIP_O_MODE);
        if (mEditAuthType.getCheckedRadioButtonId() == -1) {
            mButtonOAuth.setChecked(true);
        }
    }

    private boolean checkUrlErrors() {
        final boolean urlHasErrors = false;
        return urlHasErrors;
    }

    private int getCheckedAuthType(final int checkedId) {
        switch (checkedId) {
            case R.id.xauth: {
                return Accounts.AUTH_TYPE_XAUTH;
            }
            case R.id.basic: {
                return Accounts.AUTH_TYPE_BASIC;
            }
            case R.id.twip_o: {
                return Accounts.AUTH_TYPE_TWIP_O_MODE;
            }
            default: {
                return Accounts.AUTH_TYPE_OAUTH;
            }
        }
    }

    public static class TWIPNoticeDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener {

        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final Activity a = getActivity();
                    if (a instanceof APIEditorActivity) {
                        ((APIEditorActivity) a).saveAndFinish();
                    }
                    break;
                }
            }

        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Context wrapped = ThemeUtils.getThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(R.string.twip_api_version_notice_message);
            builder.setPositiveButton(R.string.save, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            return builder.create();
        }

    }
}
