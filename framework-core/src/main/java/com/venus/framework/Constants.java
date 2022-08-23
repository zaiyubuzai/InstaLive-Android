package com.venus.framework;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Common constant definitions
 *
 * Created by ywu on 16/2/2.
 */
public interface Constants {
    String EXTRA_REF = "referral_view";

    String EXTRA_REFERRAL_PAGE_NAME = "referral_page_name";

    String EXTRA_PAGE_NAME = "page_name";

    /**
     * App indexing使用的特殊设备型号
     */
    String DEVICE_APP_CRAWLER = "Calypso AppCrawler";

    /**
     * App状态数据,logout不会清理
     */
    String PREFS_APP_STATE = "app_state";

    /**
     * 用户私有状态数据,logout会清理
     */
    String PREFS_USER_STATE = "user_private_data";

    String PREF_KEY_DEVICE_ID = "device_id";

    //region Sharing template variables
    String SHARE_VAR_LINK = "\\{deeplink\\}";
    String SHARE_VAR_FIRST_NAME = "\\{first_name\\}";
    String SHARE_VAR_SHOP_NAME = "\\{shop_name\\}";
    String SHARE_VAR_PRICE = "\\{price\\}";
    String SHARE_VAR_LOCATION = "\\{location\\}";
    String SHARE_VAR_TITLE = "\\{title\\}";
    String SHARE_VAR_REVIEWER_FIRST_NAME = "\\{reviewer_first_name\\}";
    String SHARE_VAR_REVIEWEE_FIRST_NAME = "\\{reviewee_first_name\\}";
    String SHARE_VAR_REVIEW_CONTENT = "\\{review_content\\}";
    String SHARE_VAR_REVIEW_COUNT = "\\{review_count\\}";
    String SHARE_VAR_SCORE = "\\{score\\}";
    String SHARE_VAR_AVERAGE_SCORE = "\\{average_score\\}";
    String SHARE_VAR_LEVEL = "\\{level\\}";
    String SHARE_VAR_REFERRAL = "\\{referral\\}";
    //endregion

    //region Rating the app
    String PREF_KEY_APP_RATED = "app_already_rated";
    String PREF_KEY_APP_ENJOYED = "app_already_enjoyed";
    String PREF_KEY_APP_ENJOYED_VERSION = "app_enjoyed_version";
    String PREF_KEY_APP_ENJOYED_TIMES = "app_enjoyed_times";
    //endregion

    //region App upgrade check
    String PREF_KEY_LAST_UPDATE_CHECK_V = "update_check_app_version";
    String PREF_KEY_LAST_UPDATE_CHECK_TIME = "update_check_time";
    String PREF_KEY_HAS_UPDATE = "update_check_has_update";
    //endregion

    //region Command system (pushed from backend) definitions
    String ACT_FM_COMMAND_RECEIVED = "com.venus.fm_command_received";
    String EXTRA_FM_COMMANDS_JSON = "fm_commands_json";
    String EXTRA_FM_COMMAND_JSON = "fm_command_json";
    String EXTRA_OPEN_BY_FM_COMMAND = "open_by_fm_command";  // 标记当前view是否由后台指令打开
    String EXTRA_FM_COMMAND_CLOSABLE = "fm_command_closable";  // 根据后台指令标记当前view是否允许关闭
    //endregion

    //region Common list selection activity/dialog
    String EXTRA_SELECTION_DATA_LIST = "selection_data_list";
    String EXTRA_SELECTION_DATA_PARCELABLE_ARRAY_LIST = "selection_data_parcelable_array_list";
    String EXTRA_SELECTION_TITLE = "selection_title";
    String EXTRA_SELECTION_HINT_TITLE = "selection_hint_title";
    String EXTRA_SELECTION_CAPTION = "selection_caption";
    String EXTRA_SELECTION_DESC = "selection_description";
    String EXTRA_SELECTION_CHECKED_INDEX = "selection_checked_index";
    String EXTRA_SELECTION_CHECKED_LIST = "selection_checked_list";
    String EXTRA_SELECTION_SCREEN_NAME = "selection_screen_name";
    String EXTRA_SELECTION_USE_FAST_SCROLL = "selection_use_fast_scroll";
    String EXTRA_SELECTION_LINK_TEXT = "selection_link_text";
    String EXTRA_SELECTION_LINK_URI = "selection_link_uri";
    String RESULT_SELECTION_INDEX = "selection_index";
    String RESULT_SELECTION_ITEM = "selection_item";
    String RESULT_SELECTION_PARCELABLE_ITEM = "selection_parcelable_item";
    String RESULT_SELECTION_LIST = "selection_list";
    String RESULT_SELECTED_ACTION = "selection_action";
    //endregion

    //region App indexing
    String EXTRA_IS_FROM_GOOGLE_BOT = "is_from_google_bot";
    //endregion

    //region React Native view
    int RN_RESULT_OK = 0;
    int RN_RESULT_CANCELED = 1;

    String EXTRA_RN_JS_MODULE_PATH = "rn_js_module_path";
    String EXTRA_RN_JS_MODULE_NAME = "rn_js_module_name";
    String EXTRA_RN_JS_DATA = "rn_js_data";
    String EXTRA_RN_ACTION = "rn_action";
    String EXTRA_RN_AUTO_REFRESH = "rn_auto_refresh";  // refresh screen onResume
    String QUERY_PARAM_RN_MODULE = "rn_module";

    String RN_TRACK_TYPE_GA = "GATracking";
    String RN_TRACK_TYPE_FIREBASE = "FirebaseTracking";
    String RN_EVENT_TOP_CHANGE = "topChange";
    //endregion

    // -------------------------
    //region Screen names
    String VIEW_COUNTRY = "phoneselectcountry_view";
    String VIEW_RATE_POPUP = "rate_5miles_popup";
    //endregion

    Pattern EMAIL_PATTERN = Pattern.compile("^[-.a-z_+\\d]+@[-a-z_\\d]+(\\.[-a-z_\\d]+)+$",
            Pattern.CASE_INSENSITIVE);
    Pattern LINK_IN_TEXT_PATTERN = Pattern.compile("\\bhttps?://[\\S]+\\b");
    String IN_APP_DEEPLINK_URL_PREFIX = "fm-internal://deeplink/?uri=";

    Charset UTF_8 = Charset.forName("UTF-8");
}
