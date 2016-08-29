package com.qianseit.westore.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.qianseit.westore.DoActivity;
import com.qianseit.westore.DoFragment;
import com.qianseit.westore.activity.account.AccountAddAttentionFragment;
import com.qianseit.westore.activity.account.AccountAddressAddFragment;
import com.qianseit.westore.activity.account.AccountAddressBookFragment;
import com.qianseit.westore.activity.account.AccountAttentionFragment;
import com.qianseit.westore.activity.account.AccountBackWebViewFragment;
import com.qianseit.westore.activity.account.AccountBalanceFragment;
import com.qianseit.westore.activity.account.AccountCheckoutFragment;
import com.qianseit.westore.activity.account.AccountEarningFragment;
import com.qianseit.westore.activity.account.AccountFansFragment;
import com.qianseit.westore.activity.account.AccountFavoriteGoodsFragment;
import com.qianseit.westore.activity.account.AccountInformationFragment;
import com.qianseit.westore.activity.account.AccountLoginFragment;
import com.qianseit.westore.activity.account.AccountLogisticsFragment;
import com.qianseit.westore.activity.account.AccountNicknameFragment;
import com.qianseit.westore.activity.account.AccountPersonalFragment;
import com.qianseit.westore.activity.account.AccountPhotographFragment;
import com.qianseit.westore.activity.account.AccountPraiseFragment;
import com.qianseit.westore.activity.account.AccountProfileFragment;
import com.qianseit.westore.activity.account.AccountRatingFragment;
import com.qianseit.westore.activity.account.AccountRegistFragment;
import com.qianseit.westore.activity.account.AccountResetPasswdFragment;
import com.qianseit.westore.activity.account.AccountSearchAttention;
import com.qianseit.westore.activity.account.AccountSettingFragment;
import com.qianseit.westore.activity.account.AccountSignatureFragment;
import com.qianseit.westore.activity.account.AccountTickertFragment;
import com.qianseit.westore.activity.account.AccountTotalOrdersFragment;
import com.qianseit.westore.activity.account.AcountBackApplyFragment;
import com.qianseit.westore.activity.account.AcountBackApplyFragment2;
import com.qianseit.westore.activity.account.AcountBackFragemnt;
import com.qianseit.westore.activity.account.AcountBackOrderDetailFragment;
import com.qianseit.westore.activity.account.AcountBackOrderFragment;
import com.qianseit.westore.activity.account.ApplyforChargeBackFragment;
import com.qianseit.westore.activity.account.ChargeCountFragment;
import com.qianseit.westore.activity.account.ChargeMethods;
import com.qianseit.westore.activity.account.ForgetPasswordFragment;
import com.qianseit.westore.activity.account.FragmentAddSinaFriends;
import com.qianseit.westore.activity.account.FragmentChargeCard;
import com.qianseit.westore.activity.account.FragmentCommentPraise;
import com.qianseit.westore.activity.account.FragmentSystemMessage;
import com.qianseit.westore.activity.account.GoodsOrderDetailFragment;
import com.qianseit.westore.activity.account.HistoryTicketFragment;
import com.qianseit.westore.activity.account.InvoiceEditorFragment;
import com.qianseit.westore.activity.account.MyAddresPickerFragment;
import com.qianseit.westore.activity.account.MyAddressPickerStreetFragment;
import com.qianseit.westore.activity.account.MyExchangeFragment;
import com.qianseit.westore.activity.account.MyMessageFragment;
import com.qianseit.westore.activity.account.MyScoreDetailFragment;
import com.qianseit.westore.activity.account.MyYingBangFragment;
import com.qianseit.westore.activity.account.OrderDetailFragment;
import com.qianseit.westore.activity.account.OrderRatingFragment;
import com.qianseit.westore.activity.account.SelectIDImageFragment;
import com.qianseit.westore.activity.account.TelPhoneFriendFragment;
import com.qianseit.westore.activity.account.UploadingIDImageFragement;
import com.qianseit.westore.activity.account.VipLevelFragment;
import com.qianseit.westore.activity.helpcentre.HelpArticleFragment;
import com.qianseit.westore.activity.helpcentre.HelpCentreFragment;
import com.qianseit.westore.activity.helpcentre.HelpNodeFragment;
import com.qianseit.westore.fragment.PhotoFilterFragment;
import com.qianseit.westore.imageloader.ClipPictureActivity;


public class AgentActivity extends DoActivity {
	public static final int FRAGMENT_MAIN = 0x099;
	public static final int FRAGMENT_CATEGORY = 0x100;
	public static final int FRAGMENT_SCANNER = 0x101;
	public static final int FRAGMENT_GOODS_LIST = 0x102;
	public static final int FRAGMENT_GOODS_DETAIL = 0x103;
	public static final int FRAGMENT_GOODS_SEARCH = 0x104;
	public static final int FRAGMENT_ACCOUNT_CENTER = 0x105;
	public static final int FRAGMENT_ACCOUNT_LOGIN = 0x106;
	public static final int FRAGMENT_ACCOUNT_REGIST = 0x107;
	public static final int FRAGMENT_SHOPPING_CAR = 0x108;
	public static final int FRAGMENT_ADDRESS_BOOK = 0x109;
	public static final int FRAGMENT_ADDRESS_BOOK_EDITOR = 0x110;
	public static final int FRAGMENT_INVOICE_EDITOR = 0x111;
	public static final int FRAGMENT_TICKET_LIST = 0x112;
	public static final int FRAGMENT_TICKET_ADD = 0x113;
	public static final int FRAGMENT_ACCOUNT_SETTING = 0x114;
	public static final int FRAGMENT_ACCOUNT_RESET_PASSWD = 0x115;
	public static final int FRAGMENT_ACCOUNT_RESET_PROFILE = 0x116;
	public static final int FRAGMENT_ACCOUNT_ORDERS = 0x117;
	public static final int FRAGMENT_MY_ADDRESS_PICKER = 0x118;
	public static final int FRAGMENT_SUBMIT_SHOPPING_CAR = 0x120;
	public static final int FRAGMENT_PICK_EXPRESS = 0x121;
	public static final int FRAGMENT_CLEAR_CACHE = 0x122;
	public static final int FRAGMENT_ABOUT_US = 0x123;
	public static final int FRAGMENT_FEEDBACK = 0x124;
	public static final int FRAGMENT_ORDER_DETAIL = 0x125;
	public static final int FRAGMENT_ACCOUNT_EARNING = 0x129;
	public static final int FRAGMENT_ACCOUNT_CHECKOUT = 0x130;
	public static final int FRAGMENT_GROUP_BUY = 0x132;
	public static final int FRAGMENT_ARTICLE_READER = 0x133;
	public static final int FRAGMENT_FORGET_PASSWORD = 0x134;
	public static final int FRAGMENT_ORDER_RATING = 0x135;
	public static final int FRAGMENT_CATEGORY_THIRD = 0x136;
	public static final int FRAGMENT_CALL_SERVICE_PHONE = 0x137;
	public static final int FRAGMENT_ACCOUNT_RESET_AVATAR = 0x138;
	public static final int FRAGMENT_GOODS_DETAIL_MORE = 0x139;
	public static final int FRAGMENT_PAYMENT_PICKER = 0x140;
	public static final int FRAGMENT_FAVORITE_GOODS = 0x141;
	public static final int FRAGMENT_BALANCE_CHARGE = 0x142;
	public static final int FRAGMENT_ACCOUNT_BALANCE = 0x143;
	public static final int FRAGMENT_ACCOUNT_NICKNAME = 0x144;
	public static final int FRAGMENT_HELP_CENTRE = 0x145;
	public static final int FRAGMENT_HELP_USING = 0x146;
	public static final int FRAGMENT_HELP_ARTICLE = 0x148;
	public static final int FRAGMENT_EXCHAGNE_LIST = 0x149;
	public static final int FRAGMENT_EXCHAGNE_REASON = 0x150;
	public static final int FRAGMENT_MY_MASSAGE = 0x151;
	public static final int FRAGMENT_MYSCORE_DESTAIL = 0x152;
	public static final int FRAGMENT_SHOP_SERVICE = 0x153;
	public static final int FRAGMENT_PICKER_STREET = 0x154;
	public static final int FRAGMENT_PICKER_STRORE = 0x155;
	public static final int FRAGMENT_VIP_LEVEL = 0x156;
	public static final int FRAGMENT_COUDAN = 0x157;
	public static final int FRAGMENT_CAHARGE_METHODS = 0x158;
	public static final int FRAGMENT_CAHARGE_COUNT = 0x159;
	public static final int FRAGMENT_CATEGORY_YING = 0x160;
	public static final int FRAGMENT_GOODS_DETAIL_BRAND = 0x161;
	public static final int FRAGMENT_FLASH_SALE = 0x162;
	public static final int FRAGMENT_NEW_PRODUCT = 0x163;
	public static final int FRAGMENT_SEASON_SPECIAL = 0x164;
	public static final int FRAGMENT_MY_INFORMATION = 0x165;
	public static final int FRAGMENT_TEST = 0x166;
	public static final int FRAGMENT_HISTORY_TICKET = 0x167;
	public static final int FRAGMENT_UPLOADING_ID = 0x168;

	public static final int FRAGMENT_GOODS_SHOOSEG = 0x169;
	public static final int FRAGMENT_GOODS_PHOTOS = 0x170;
	public static final int FRAGMENT_SELECT_ID = 0x171;
	public static final int FRAGMENT_GOODS_XIUJIAN = 0x172;
	public static final int FRAGMENT_PHOTO = 0x173;
	public static final int FRAGMENT_ACCORDING = 0x174;
	public static final int FRAGMENT_GOODS_ORDERS_DETAIL = 0x175;
	public static final int FRAGMENT_GOODS_LOGISTICS = 0x176;
	public static final int FRAGMENT_FRINENT = 0x177;
	public static final int FRAGMENT_PERSONAL_HOME = 0x178;
	public static final int FRAGMENT_GOODS_CHOOSE = 0x179;
	public static final int FRAGMENT_ATTENTION = 0x180;
	public static final int FRAGMENT_ADD_ATTENTION = 0x181;
	public static final int FRAGMENT_FANS = 0x182;
	public static final int FRAGMENT_SYSTEM_MSG = 0x183;
	public static final int FRAGMENT_PRAISE_COMMENT = 0x184;
	public static final int FRAGMENT_PRAISE_AECOMMEND = 0x187;
	public static final int FRAGMENT_SIGNATURE = 0x185;
	public static final int FRAGMENT_ORDERS_RATING = 0x186;
	public static final int FRAGMENT_ATTENTION_SEARCH = 0x188;
	public static final int FRAGMENT_ADD_WEBO_FRIENDS = 0x189;
	public static final int FRAGMENT_COMMEND= 0x90;
	public static final int FRAGMENT_PROMOTIONGS= 0x91;
	public static final int FRAGMENT_PRAISE= 0x92;

	public static final int FRAGMENT_ACCOUNT_BACK = 0x93;
	public static final int FRAGMENT_ACCOUNT_BACK_ORDER = 0x94;
	public static final int ACCOUNT_BACK_APPLY = 0x95;
	public static final int FRAGMENT_ACCOUNT_BACK_ORDER_DETAIL = 0x96;
	public static final int FRAGMENT_ACCOUNT_BACK_WEBVIEW_FRAGEMNT = 0x97;
	

	public static final String EXTRA_FRAGMENT = "extra_fragment";

	DoFragment fragment = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		int fragmentid = getIntent().getIntExtra(EXTRA_FRAGMENT, -1);
		switch (fragmentid) {
		case FRAGMENT_MAIN:
			fragment = new MainFragment();
			break;
		case FRAGMENT_CATEGORY:
			fragment = new CategoryFragment();
			break;
		case FRAGMENT_GOODS_SEARCH:
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			fragment = new OrderRatingFragment();
			fragment = new SearchFragment();
			break;
		case FRAGMENT_GOODS_LIST:
			fragment = new GoodsListFragment();
			break;
		case FRAGMENT_GOODS_DETAIL:
//			fragment = new GoodsDetailFragment();
			fragment = new TestGoodsDetail();
			break;
		case FRAGMENT_ACCOUNT_CENTER:
			fragment = new SearchFragment();
			break;
		case FRAGMENT_ACCOUNT_LOGIN:
			fragment = new AccountLoginFragment();
			break;
		case FRAGMENT_ACCOUNT_REGIST:
			fragment = new AccountRegistFragment();
			break;
		case FRAGMENT_SHOPPING_CAR:
			fragment = new TestShoppingCarFragment();
			break;
		case FRAGMENT_ADDRESS_BOOK:
			fragment = new AccountAddressBookFragment();
			break;
		case FRAGMENT_ADDRESS_BOOK_EDITOR:
			// fragment = new MyAddressBookEditorFragment();
			fragment = new AccountAddressAddFragment();
			break;
		case FRAGMENT_INVOICE_EDITOR:
			fragment = new InvoiceEditorFragment();
			break;
		case FRAGMENT_TICKET_LIST:
			fragment = new AccountTickertFragment();
			break;
		case FRAGMENT_TICKET_ADD:
			fragment = new TicketAddFragment();
			break;
		case FRAGMENT_ACCOUNT_SETTING:
			fragment = new AccountSettingFragment();
			break;
		case FRAGMENT_ACCOUNT_RESET_PASSWD:
			fragment = new AccountResetPasswdFragment();
			break;
		case FRAGMENT_ACCOUNT_RESET_PROFILE:
			fragment = new AccountProfileFragment();
			break;
		case FRAGMENT_ACCOUNT_ORDERS:
			fragment = new AccountTotalOrdersFragment();
			// fragment = new AccountOrdersFragment();
			break;
		case FRAGMENT_MY_ADDRESS_PICKER:
			fragment = new MyAddresPickerFragment();
			break;
		case FRAGMENT_SUBMIT_SHOPPING_CAR:
			fragment = new ConfirmOrderFragment();
			break;
		case FRAGMENT_PICK_EXPRESS:
			fragment = new ExpressPickerFragment();
			break;
		case FRAGMENT_ABOUT_US:
			fragment = new AboutFragment();
			break;
		case FRAGMENT_FEEDBACK:
			fragment = new FeedbackFragment();
			break;
		case FRAGMENT_ORDER_DETAIL:
			fragment = new OrderDetailFragment();
			break;
		case FRAGMENT_ACCOUNT_EARNING:
			fragment = new AccountEarningFragment();
			break;
		case FRAGMENT_ACCOUNT_CHECKOUT:
			fragment = new AccountCheckoutFragment();
			break;
		case FRAGMENT_GROUP_BUY:
			fragment = new GroupBuyNewFragment();
			break;
		case FRAGMENT_ARTICLE_READER:
			fragment = new ArticleReaderFragment();
			break;
		case FRAGMENT_FORGET_PASSWORD:
			fragment = new ForgetPasswordFragment();
			break;
		case FRAGMENT_ORDER_RATING: {
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
			fragment = new OrderRatingFragment();
		}
			break;
		case FRAGMENT_CATEGORY_THIRD: {
			fragment = new CategoryThirdFragment();
		}
			break;
		case FRAGMENT_GOODS_DETAIL_MORE: {
			fragment = new GoodsDetailMoreFragment();
		}
			break;
		case FRAGMENT_PAYMENT_PICKER: {
			fragment = new PaymentPickerFragment();
		}
			break;
		case FRAGMENT_FAVORITE_GOODS: {
			fragment = new AccountFavoriteGoodsFragment();
		}
			break;
		case FRAGMENT_BALANCE_CHARGE: {
			fragment = new FragmentChargeCard();
		}
			break;
		case FRAGMENT_ACCOUNT_BALANCE: {
			fragment = new AccountBalanceFragment();
		}
			break;
		case FRAGMENT_ACCOUNT_NICKNAME: {
			fragment = new AccountNicknameFragment();
		}
			break;
		case FRAGMENT_HELP_CENTRE:
			fragment = new HelpCentreFragment();
			break;
		case FRAGMENT_HELP_USING:
			fragment = new HelpNodeFragment();
			break;
		case FRAGMENT_HELP_ARTICLE:
			fragment = new HelpArticleFragment();
			break;
		case FRAGMENT_EXCHAGNE_LIST:
			fragment = new MyExchangeFragment();
			break;
		case FRAGMENT_EXCHAGNE_REASON:
			fragment = new ApplyforChargeBackFragment();
			break;
		case FRAGMENT_MY_MASSAGE:
			fragment = new MyMessageFragment();
			break;
		case FRAGMENT_MYSCORE_DESTAIL:
			fragment = new MyScoreDetailFragment();
			break;
		case FRAGMENT_SHOP_SERVICE:
			fragment = new MyYingBangFragment();
			break;
		case FRAGMENT_PICKER_STREET:
			fragment = new MyAddressPickerStreetFragment();
			break;
		case FRAGMENT_PICKER_STRORE:
			fragment = new PickerSelftStoreFragment();
			break;
		case FRAGMENT_VIP_LEVEL:
			fragment = new VipLevelFragment();
			break;
		case FRAGMENT_COUDAN:
			fragment = new CoudanFragment();
			break;
		case FRAGMENT_CAHARGE_METHODS:
			fragment = new ChargeMethods();
			break;
		case FRAGMENT_CAHARGE_COUNT:
			fragment = new ChargeCountFragment();
			break;
		case FRAGMENT_CATEGORY_YING:
			fragment = new CategoryYtFragment();
			break;
		case FRAGMENT_GOODS_DETAIL_BRAND:
			fragment = new GoodsDetailBrandFragment();
			break;
		case FRAGMENT_FLASH_SALE:
			fragment = new FlashSaleFragment();
			break;
		case FRAGMENT_NEW_PRODUCT:
			fragment = new NewProductFragment();
			break;
		case FRAGMENT_SEASON_SPECIAL:
			fragment = new SeasonSpecialFragment();
			break;
		case FRAGMENT_MY_INFORMATION:
			fragment = new AccountInformationFragment();
			break;
		case FRAGMENT_TEST:
			fragment = new TestShoppingCarFragment();
			break;
		case FRAGMENT_HISTORY_TICKET:
			fragment = new HistoryTicketFragment();
			break;
		case FRAGMENT_UPLOADING_ID:
			fragment = new UploadingIDImageFragement();
			break;
		case FRAGMENT_SELECT_ID:
			fragment = new SelectIDImageFragment();
			break;
		case FRAGMENT_GOODS_XIUJIAN:
			fragment = new ClipPictureActivity();
			break;
		case FRAGMENT_PHOTO:
			fragment = new AccountPhotographFragment();
			break;
		case FRAGMENT_GOODS_SHOOSEG:
			fragment = new AccountPhotographFragment();
			break;
		case FRAGMENT_ACCORDING:
			fragment = new PhotoFilterFragment();
			break;
		case FRAGMENT_FRINENT:
			fragment = new TelPhoneFriendFragment();
			break;
		case FRAGMENT_GOODS_ORDERS_DETAIL:
			fragment = new GoodsOrderDetailFragment();
			break;
		case FRAGMENT_GOODS_LOGISTICS:
			fragment = new AccountLogisticsFragment();
			break;
		case FRAGMENT_GOODS_CHOOSE:
			fragment = new ChooseNewFreagment();
			break;
		case FRAGMENT_PERSONAL_HOME:
			fragment = new AccountPersonalFragment();
			break;
		case FRAGMENT_ATTENTION:
			fragment = new AccountAttentionFragment();
			break;
		case FRAGMENT_ADD_ATTENTION:
			fragment = new AccountAddAttentionFragment();
			break;
		case FRAGMENT_FANS:
			fragment = new AccountFansFragment();
			break;
		case FRAGMENT_SYSTEM_MSG:
			fragment = new FragmentSystemMessage();
			break;
		case FRAGMENT_PRAISE_COMMENT:
			fragment = new FragmentCommentPraise();
			break;
		case FRAGMENT_PRAISE_AECOMMEND:
			fragment = new AecommendedLanguageActivity();
			break;
		case FRAGMENT_SIGNATURE:
			fragment = new AccountSignatureFragment();
			break;
		case FRAGMENT_ORDERS_RATING:
			fragment = new AccountRatingFragment();
			break;
		case FRAGMENT_ATTENTION_SEARCH:
			fragment = new AccountSearchAttention();
			break;
		case FRAGMENT_ADD_WEBO_FRIENDS:
			fragment = new FragmentAddSinaFriends();
			break;
		case FRAGMENT_COMMEND:
			fragment = new GoodsCommentFragment();
			break;
		case FRAGMENT_PROMOTIONGS:
			fragment = new PromotionsFragment();
			break;
		case FRAGMENT_PRAISE:
			fragment = new AccountPraiseFragment();
			break;
			case FRAGMENT_ACCOUNT_BACK:
				fragment = new AcountBackFragemnt();
				break;
			case FRAGMENT_ACCOUNT_BACK_ORDER:
				fragment = new AcountBackOrderFragment();
				break;
			case ACCOUNT_BACK_APPLY:
				//fragment = new AcountBackApplyFragment();
				fragment = new AcountBackApplyFragment2();
				break;
			case FRAGMENT_ACCOUNT_BACK_ORDER_DETAIL:
				fragment = new AcountBackOrderDetailFragment();
				break;
			case FRAGMENT_ACCOUNT_BACK_WEBVIEW_FRAGEMNT:
				fragment = new AccountBackWebViewFragment();
				break;

		default:
			finish();
			break;
		}

		// 插入Fragment
		if (fragment != null) {
			fragment.setArguments(bundle);
			setMainFragment(fragment);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 返回打开Fragment的Intent
	 * 
	 * @param context
	 * @param fragment
	 * @return
	 */
	public static Intent intentForFragment(Context context, int fragment) {
		Intent intent = new Intent(context, AgentActivity.class);
		intent.putExtra(EXTRA_FRAGMENT, fragment);
		intent.putExtra(DoActivity.EXTRA_SHOW_BACK, true);
		return intent;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (fragment.onKeyDown(keyCode, event)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		fragment.onWindowFocusChanged(hasFocus);
	}
}