
package com.morestudio.littledot.doctor.api;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.RemoteException;

import com.morestudio.littledot.doctor.api.ISipService;

/**
 * Manage SIP application globally <br/>
 * Define intent, action, broadcast, extra constants <br/>
 * It also define authority and uris for some content holds by the internal
 * database
 */
public final class SipManager {
    // -------
    // Static constants
    // PERMISSION
    /**
     * Permission that allows to use sip : place call, control call etc.
     */
    public static final String PERMISSION_USE_SIP = "android.permission.USE_SIP";
    /**
     * Permission that allows to configure sip engine : preferences, accounts.
     */
    public static final String PERMISSION_CONFIGURE_SIP = "android.permission.CONFIGURE_SIP";

    public static final String INTENT_SIP_CONFIGURATION = "com.morestudio.littledot.doctor.service.SipConfiguration";
    /**
     * Bind sip service to control calls.<br/>
     * If you start the service using {@link android.content.Context#startService(android.content.Intent intent)}
     * , you may want to pass {@link #EXTRA_OUTGOING_ACTIVITY} to specify you
     * are starting the service in order to make outgoing calls. You are then in
     * charge to unregister for outgoing calls when user finish with your
     * activity or when you are not anymore in calls using
     * {@link #ACTION_OUTGOING_UNREGISTER}<br/>
     * If you actually make a call or ask service to do something but wants to
     * unregister, you must defer unregister of your activity using
     * {@link #ACTION_DEFER_OUTGOING_UNREGISTER}.
     * 
     * @see ISipService
     * @see #EXTRA_OUTGOING_ACTIVITY
     */
    public static final String INTENT_SIP_SERVICE = "com.morestudio.littledot.doctor.service.SipService";
        
    public static final String INTENT_SIP_ACCOUNT_ACTIVATE = "com.morestudio.littledot.doctor.accounts.activate";

    /**
     * Scheme for csip uri.
     */
    public static final String PROTOCOL_CSIP = "csip";
    /**
     * Scheme for sip uri.
     */
    public static final String PROTOCOL_SIP = "sip";
    /**
     * Scheme for sips (sip+tls) uri.
     */
    public static final String PROTOCOL_SIPS = "sips";
    public static final String ACTION_SIP_CALL_UI = "com.morestudio.littledot.doctor.phone.action.INCALL";
    /**
     * Action launched when the status icon clicked.<br/>
     * Should raise the dialer.
     */
    public static final String ACTION_SIP_DIALER = "com.morestudio.littledot.doctor.phone.action.DIALER";
    /**
     * Action launched when a missed call notification entry is clicked.<br/>
     * Should raise call logs list.
     */
    public static final String ACTION_SIP_CALLLOG = "com.morestudio.littledot.doctor.phone.action.CALLLOG";
    /**
     * Action launched when a sip message notification entry is clicked.<br/>
     * Should raise the sip message list.
     */
    public static final String ACTION_SIP_MESSAGES = "com.morestudio.littledot.doctor.phone.action.MESSAGES";
    /**
     * Action launched when user want to go in sip favorites.
     * Should raise the sip favorites view.
     */
    public static final String ACTION_SIP_FAVORITES = "com.morestudio.littledot.doctor.phone.action.FAVORITES";
    /**
     * Action launched to enter fast settings.<br/>
     */
    public static final String ACTION_UI_PREFS_FAST = "com.morestudio.littledot.doctor.ui.action.PREFS_FAST";
    /**
     * Action launched to enter global csipsimple settings.<br/>
     */
    public static final String ACTION_UI_PREFS_GLOBAL = "com.morestudio.littledot.doctor.ui.action.PREFS_GLOBAL";
    
    public static final String ACTION_SIP_CALL_LAUNCH = "com.morestudio.littledot.doctor.service.CALL_LAUNCHED";
    public static final String ACTION_SIP_CALL_CHANGED = "com.morestudio.littledot.doctor.service.CALL_CHANGED";
    public static final String ACTION_SIP_ACCOUNT_CHANGED = "com.morestudio.littledot.doctor.service.ACCOUNT_CHANGED";
    public static final String ACTION_SIP_ACCOUNT_DELETED = "com.morestudio.littledot.doctor.service.ACCOUNT_DELETED";

    public static final String ACTION_SIP_REGISTRATION_CHANGED = "com.morestudio.littledot.doctor.service.REGISTRATION_CHANGED";
    /**
     * Broadcast sent when the state of device media has been changed.
     */
    public static final String ACTION_SIP_MEDIA_CHANGED = "com.morestudio.littledot.doctor.service.MEDIA_CHANGED";
    /**
     * Broadcast sent when a ZRTP SAS
     */
    public static final String ACTION_ZRTP_SHOW_SAS = "com.morestudio.littledot.doctor.service.SHOW_SAS";
    /**
     * Broadcast sent when a message has been received.<br/>
     * By message here, we mean a SIP SIMPLE message of the sip simple protocol. Understand a chat / im message.
     */
    public static final String ACTION_SIP_MESSAGE_RECEIVED = "com.morestudio.littledot.doctor.service.MESSAGE_RECEIVED";

    public static final String ACTION_SIP_CALL_RECORDED = "com.morestudio.littledot.doctor.service.CALL_RECORDED";

    // REGISTERED BROADCASTS
    /**
     * Broadcast to send when the sip service can be stopped.
     */
    public static final String ACTION_SIP_CAN_BE_STOPPED = "com.morestudio.littledot.doctor.service.ACTION_SIP_CAN_BE_STOPPED";
    /**
     * Broadcast to send when the sip service should be restarted.
     */
    public static final String ACTION_SIP_REQUEST_RESTART = "com.morestudio.littledot.doctor.service.ACTION_SIP_REQUEST_RESTART";
    /**
     * Broadcast to send when your activity doesn't allow anymore user to make outgoing calls.<br/>
     * You have to pass registered {@link #EXTRA_OUTGOING_ACTIVITY} 
     * 
     * @see #EXTRA_OUTGOING_ACTIVITY
     */
    public static final String ACTION_OUTGOING_UNREGISTER = "com.morestudio.littledot.doctor.service.ACTION_OUTGOING_UNREGISTER";
    /**
     * Broadcast to send when you have launched a sip action (such as make call), but that your app will not anymore allow user to make outgoing calls actions.<br/>
     * You have to pass registered {@link #EXTRA_OUTGOING_ACTIVITY} 
     * 
     * @see #EXTRA_OUTGOING_ACTIVITY
     */
    public static final String ACTION_DEFER_OUTGOING_UNREGISTER = "com.morestudio.littledot.doctor.service.ACTION_DEFER_OUTGOING_UNREGISTER";

    // PLUGINS BROADCASTS
    /**
     * Plugin action for themes.
     */
    public static final String ACTION_GET_DRAWABLES = "com.morestudio.littledot.doctor.themes.GET_DRAWABLES";
    /**
     * Plugin action for call handlers.<br/>
     * You can expect {@link android.content.Intent#EXTRA_PHONE_NUMBER} as argument for the
     * number to call. <br/>
     * Your receiver must
     * {@link android.content.BroadcastReceiver#getResultExtras(boolean)} with parameter true to
     * fill response. <br/>
     * Your response contains :
     * <ul>
     * <li>{@link android.content.Intent#EXTRA_SHORTCUT_ICON} with
     * {@link android.graphics.Bitmap} (mandatory) : Icon representing the call
     * handler</li>
     * <li>{@link android.content.Intent#EXTRA_TITLE} with
     * {@link String} (mandatory) : Title representing the call
     * handler</li>
     * <li>{@link android.content.Intent#EXTRA_REMOTE_INTENT_TOKEN} with
     * {@link android.app.PendingIntent} (mandatory) : The intent to fire when
     * this action is choosen</li>
     * <li>{@link android.content.Intent#EXTRA_PHONE_NUMBER} with
     * {@link String} (optional) : Phone number if the pending intent
     * launch a call intent. Empty if the pending intent launch something not
     * related to a GSM call.</li>
     * </ul>
     */
    public static final String ACTION_GET_PHONE_HANDLERS = "com.morestudio.littledot.doctor.phone.action.HANDLE_CALL";

    public static final String ACTION_INCALL_PLUGIN = "com.morestudio.littledot.doctor.sipcall.action.HANDLE_CALL_PLUGIN";
    
    public static final String EXTRA_SIP_CALL_MIN_STATE = "com.morestudio.littledot.doctor.sipcall.MIN_STATE";
    public static final String EXTRA_SIP_CALL_MAX_STATE = "com.morestudio.littledot.doctor.sipcall.MAX_STATE";
    public static final String EXTRA_SIP_CALL_CALL_WAY = "com.morestudio.littledot.doctor.sipcall.CALL_WAY";
    
    /**
     * Bitmask to keep media/call coming from outside
     */
    public final static int BITMASK_IN = 1 << 0;
    /**
     * Bitmask to keep only media/call coming from the app
     */
    public final static int BITMASK_OUT = 1 << 1;
    /**
     * Bitmask to keep all media/call whatever incoming/outgoing
     */
    public final static int BITMASK_ALL = BITMASK_IN | BITMASK_OUT;
    
    /**
     * Plugin action for rewrite numbers. <br/>     
     * You can expect {@link android.content.Intent#EXTRA_PHONE_NUMBER} as argument for the
     * number to rewrite. <br/>
     * Your receiver must
     * {@link android.content.BroadcastReceiver#getResultExtras(boolean)} with parameter true to
     * fill response. <br/>
     * Your response contains :
     * <ul>
     * <li>{@link android.content.Intent#EXTRA_PHONE_NUMBER} with
     * {@link String} (optional) : Rewritten phone number.</li>
     * </ul>
     */
    public final static String ACTION_REWRITE_NUMBER = "com.morestudio.littledot.doctor.phone.action.REWRITE_NUMBER";
    /**
     * Plugin action for audio codec.
     */
    public static final String ACTION_GET_EXTRA_CODECS = "com.morestudio.littledot.doctor.codecs.action.REGISTER_CODEC";
    /**
     * Plugin action for video codec.
     */
    public static final String ACTION_GET_EXTRA_VIDEO_CODECS = "com.morestudio.littledot.doctor.codecs.action.REGISTER_VIDEO_CODEC";
    /**
     * Plugin action for video.
     */
    public static final String ACTION_GET_VIDEO_PLUGIN = "com.morestudio.littledot.doctor.plugins.action.REGISTER_VIDEO";
    /**
     * Meta constant name for library name.
     */
    public static final String META_LIB_NAME = "lib_name";
    /**
     * Meta constant name for the factory name.
     */
    public static final String META_LIB_INIT_FACTORY = "init_factory";
    /**
     * Meta constant name for the factory deinit name.
     */
    public static final String META_LIB_DEINIT_FACTORY = "deinit_factory";

    // Content provider
    /**
     * Authority for regular database of the application.
     */
    public static final String AUTHORITY = "com.morestudio.littledot.doctor.db";
    /**
     * Base content type for csipsimple objects.
     */
    public static final String BASE_DIR_TYPE = "vnd.android.cursor.dir/vnd.csipsimple";
    /**
     * Base item content type for csipsimple objects.
     */
    public static final String BASE_ITEM_TYPE = "vnd.android.cursor.item/vnd.csipsimple";

    // Content Provider - call logs
    /**
     * Table name for call logs.
     */
    public static final String CALLLOGS_TABLE_NAME = "calllogs";
    /**
     * Content type for call logs provider.
     */
    public static final String CALLLOG_CONTENT_TYPE = BASE_DIR_TYPE + ".calllog";
    /**
     * Item type for call logs provider.
     */
    public static final String CALLLOG_CONTENT_ITEM_TYPE = BASE_ITEM_TYPE + ".calllog";
    /**
     * Uri for call log content provider.
     */
    public static final Uri CALLLOG_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + AUTHORITY + "/"
            + CALLLOGS_TABLE_NAME);
    /**
     * Base uri for a specific call log. Should be appended with id of the call log.
     */
    public static final Uri CALLLOG_ID_URI_BASE = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + AUTHORITY + "/"
            + CALLLOGS_TABLE_NAME + "/");
    // -- Extra fields for call logs
    /**
     * The account used for this call
     */
    public static final String CALLLOG_PROFILE_ID_FIELD = "account_id";
    /**
     * The final latest status code for this call.
     */
    public static final String CALLLOG_STATUS_CODE_FIELD = "status_code";
    /**
     * The final latest status text for this call.
     */
    public static final String CALLLOG_STATUS_TEXT_FIELD = "status_text";

    // Content Provider - filter
    /**
     * Table name for filters/rewriting rules.
     */
    public static final String FILTERS_TABLE_NAME = "outgoing_filters";
    /**
     * Content type for filter provider.
     */
    public static final String FILTER_CONTENT_TYPE = BASE_DIR_TYPE + ".filter";
    /**
     * Item type for filter provider.
     */
    public static final String FILTER_CONTENT_ITEM_TYPE = BASE_ITEM_TYPE + ".filter";
    /**
     * Uri for filters provider.
     */
    public static final Uri FILTER_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + AUTHORITY + "/"
            + FILTERS_TABLE_NAME);
    /**
     * Base uri for a specific filter. Should be appended with filter id.
     */
    public static final Uri FILTER_ID_URI_BASE = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
            + AUTHORITY + "/"
            + FILTERS_TABLE_NAME + "/");

    public static final String EXTRA_CALL_INFO = "call_info";
    

    /**
     * Tell sip service that it's an user interface requesting for outgoing call.<br/>
     * It's an extra to add to sip service start as string representing unique key for your activity.<br/>
     * We advise to use your own component name {@link android.content.ComponentName} to avoid collisions.<br/>
     * Each activity is in charge unregistering broadcasting {@link #ACTION_OUTGOING_UNREGISTER} or {@link #ACTION_DEFER_OUTGOING_UNREGISTER}<br/>
     * 
     * @see android.content.ComponentName
     */
    public static final String EXTRA_OUTGOING_ACTIVITY = "outgoing_activity";
    
    /**
     * Extra key to contain an string to path of a file.<br/>
     * @see String
     */
    public static final String EXTRA_FILE_PATH = "file_path";
    
    /**
     * Target in a sip launched call.
     * @see #ACTION_SIP_CALL_LAUNCH
     */
    public static final String EXTRA_SIP_CALL_TARGET = "call_target";
    /**
     * Options of a sip launched call.
     * @see #ACTION_SIP_CALL_LAUNCH
     */
    public static final String EXTRA_SIP_CALL_OPTIONS = "call_options";

    public static final String EXTRA_FALLBACK_BEHAVIOR = "fallback_behavior";
    /**
     * Parameter for {@link #EXTRA_FALLBACK_BEHAVIOR}.
     * Prompt user with other choices without calling automatically.
     */
    public static final int FALLBACK_ASK = 0;
    /**
     * Parameter for {@link #EXTRA_FALLBACK_BEHAVIOR}.
     * Warn user about the fact current account not valid and exit.
     * WARNING : not yet implemented, will behaves just like {@link #FALLBACK_ASK} for now
     */
    public static final int FALLBACK_PREVENT = 1;
    /**
     * Parameter for {@link #EXTRA_FALLBACK_BEHAVIOR}
     * Automatically fallback to any other available account in case requested sip profile is not there.
     */
    public static final int FALLBACK_AUTO_CALL_OTHER = 2;
    
    // Constants
    /**
     * Constant for success return
     */
    public static final int SUCCESS = 0;
    /**
     * Constant for network errors return
     */
    public static final int ERROR_CURRENT_NETWORK = 10;

    /**
     * Possible presence status.
     */
    public enum PresenceStatus {
        /**
         * Unknown status
         */
        UNKNOWN,
        /**
         * Online status
         */
        ONLINE,
        /**
         * Offline status
         */
        OFFLINE,
        /**
         * Busy status
         */
        BUSY,
        /**
         * Away status
         */
        AWAY,
    }

    /**
     * Current api version number.<br/>
     * Major version x 1000 + minor version. <br/>
     * Major version are backward compatible.
     */
    public static final int CURRENT_API = 2005;

    /**
     * Ensure capability of the remote sip service to reply our requests <br/>
     * 
     * @param service the bound service to check
     * @return true if we can safely use the API
     */
    public static boolean isApiCompatible(ISipService service) {
        if (service != null) {
            try {
                int version = service.getVersion();
                return (Math.floor(version / 1000) == Math.floor(CURRENT_API % 1000));
            } catch (RemoteException e) {
                // We consider this is a bad api version that does not have
                // versionning at all
                return false;
            }
        }

        return false;
    }
}
