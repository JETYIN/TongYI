package com.qianseit.westore;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import cn.shopex.ecstore.R;

public class LoginedUser {
	private final String EMPTY_URI = "http://localhost/";

	private static LoginedUser mLoginedUser;

	private boolean loginStatus = false;
	private JSONObject mUserInfo = null;
	private JSONObject mShopInfo = null;

	private int sex = 1;
	private String integral = "0";// 樱磅
	private String message = "0";// 消息
	private String remark;
	private String VipNum;
	private String userID = "";
	private String username = "";
	private String realname = "";
	private String balance = "0.00";
	private String freezeBalance = "0.00";
	private String gender = "";
	// private String email = "";
	private String city_id = "";
	private String contact_address = "";
	private String company_address = "";
	private String opening_bank = "";
	private String bank_number = "";
	private String bank_name = "";
	// private String phone = "";
	private String logo = "";
	private String offline_cardno;

	private LoginedUser() {
	}

	public static LoginedUser getInstance() {
		if (mLoginedUser == null)
			mLoginedUser = new LoginedUser();
		return mLoginedUser;
	}

	public void clearLoginedStatus() {
		this.mUserInfo = null;
		this.loginStatus = false;
		this.userID = "";
		this.username = "";
		this.realname = "";
		this.balance = "0.00";
		this.freezeBalance = "0.00";
		this.gender = "";
		// this.email = "";
		this.city_id = "";
		this.contact_address = "";
		this.company_address = "";
		this.opening_bank = "";
		this.bank_number = "";
		this.bank_name = "";
		// this.phone = "";
		this.logo = "";
		offline_cardno = "";
	}

	public boolean isLogined() {
		return loginStatus && mUserInfo != null;
	}

	public void setIsLogined(boolean loginStatus) {
		this.loginStatus = loginStatus;
	}

	public JSONObject getUserInfo() {
		return mUserInfo;
	}

	public void setUserInfo(JSONObject mUserInfo) {
		this.mUserInfo = mUserInfo;
		if (mUserInfo != null)
			mShopInfo = mUserInfo.optJSONObject("microshop_info");
	}

	public boolean hasShop() {
		return mShopInfo != null
				&& !TextUtils.isEmpty(mShopInfo.optString("shop_id"));
	}

	public String getShopId() {
		if (mShopInfo == null)
			return null;
		return mShopInfo.optString("shop_id");
	}

	public String getAvatarUri() {
		if (mUserInfo == null)
			return EMPTY_URI;
		return mUserInfo.optString("avatar");
	}

	public String getShopCoverUri() {
		if (mShopInfo == null)
			return EMPTY_URI;
		return mShopInfo.optString("cover");
	}

	public void setUserID(String uid) {
		if (TextUtils.isEmpty(uid))
			this.userID = Run.EMPTY_STR;
		else
			this.userID = uid;
	}

	public void setUsername(String un) {
		if (TextUtils.isEmpty(un))
			this.username = Run.EMPTY_STR;
		else
			this.username = un;
	}

	public void setRealname(String name) {
		if (TextUtils.isEmpty(name))
			this.realname = Run.EMPTY_STR;
		else
			this.realname = name;
	}

	public void setBalance(String b) {
		if (TextUtils.isEmpty(b))
			this.balance = Run.EMPTY_STR;
		else
			this.balance = b;
	}

	public void setFreezeBalance(String fb) {
		if (TextUtils.isEmpty(fb))
			this.freezeBalance = Run.EMPTY_STR;
		else
			this.freezeBalance = fb;
	}

	public String getUserID() {
		return userID;
	}

	public int getSex() {
		int sexValue;
		try {
			if (mUserInfo.isNull("sex"))
				return 1;
			sexValue = mUserInfo.optInt("sex");
			return sexValue;
		} catch (Exception e) {
			return 1;
		}
	}

	// 樱磅
	public String getIntegral() {
		try {
			if (mUserInfo.isNull("point"))
				return "0";
			return ("null".equals(mUserInfo.optString("point"))) ? "0"
					: mUserInfo.optString("point");
		} catch (Exception e) {
			return "0";
		}
	}

	public void setIntegral(String Integral) {
		try {
			if (mUserInfo.isNull("point"))
				return;
			mUserInfo.remove("point");
			mUserInfo.put("point", Integral);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 消息
	public String getMessage() {
		try {
			if (mUserInfo.isNull("messagecount"))
				return "0";
			return ("null".equals(mUserInfo.optString("messagecount"))) ? "0"
					: mUserInfo.optString("messagecount");
		} catch (Exception e) {
			return "0";
		}

	}

	public void setMessage(String message) {
		try {
			if (mUserInfo.isNull("messagecount"))
				mUserInfo.put("messagecount", message);
			else {
				mUserInfo.remove("messagecount");
				mUserInfo.put("messagecount", message);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getRemark() {
		try {
			if (mUserInfo.isNull("desc"))
				return "";
			return ("null".equals(mUserInfo.optString("desc"))) ? ""
					: mUserInfo.optString("desc");
		} catch (Exception e) {
			return "";
		}
	}

	public void setRemark(String desc) {
		try {
			if (mUserInfo.isNull("desc"))
				mUserInfo.put("desc", desc);
			mUserInfo.remove("desc");
			mUserInfo.put("desc", desc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setAddress(String address) {
		try {
			if (mUserInfo.isNull("addr"))
				mUserInfo.put("addr", address);
			mUserInfo.remove("addr");
			mUserInfo.put("addr", address);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 地址
	public String getAddress() {
		try {
			if (mUserInfo.isNull("addr"))
				return "";
			return ("null".equals(mUserInfo.optString("addr"))) ? ""
					: mUserInfo.optString("addr");
		} catch (Exception e) {
			return "";
		}
	}

	public String getVipNum() {
		return (mUserInfo == null) ? "LV.0" : "LV."
				+ mUserInfo.optString("member_lv_id");
	}

	public String getUsername() {
		return username;
	}

	public String getRealname() {
		if (TextUtils.isEmpty(realname) || "null".equals(realname))
			return "匿名";

		return realname;
	}

	public String getBalance() {
		return balance;
	}

	public String getFreezeBalance() {
		return freezeBalance;
	}

	public static LoginedUser getmLoginedUser() {
		return mLoginedUser;
	}

	public static void setmLoginedUser(LoginedUser mLoginedUser) {
		LoginedUser.mLoginedUser = mLoginedUser;
	}

	public boolean isLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(boolean loginStatus) {
		this.loginStatus = loginStatus;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		if (TextUtils.isEmpty(gender))
			this.gender = Run.EMPTY_STR;
		else
			this.gender = gender;
	}

	public String getEmail() {
		return mUserInfo.optString("email");
	}

	public void setEmail(String email) {
		try {
			mUserInfo.put("email", email);
		} catch (Exception e) {
		}
	}

	public String getCity_id() {
		if (TextUtils.isEmpty(city_id) || "null".equals(city_id))
			return "";

		return city_id;
	}

	public void setCity_id(String city_id) {
		if (TextUtils.isEmpty(city_id))
			this.city_id = Run.EMPTY_STR;
		else
			this.city_id = city_id;
	}

	public String getContact_address() {
		if (TextUtils.isEmpty(contact_address)
				|| "null".equals(contact_address))
			return "";

		return contact_address;
	}

	public void setContact_address(String contact_address) {
		if (TextUtils.isEmpty(contact_address))
			this.contact_address = Run.EMPTY_STR;
		else
			this.contact_address = contact_address;
	}

	public String getCompany_address() {
		if (TextUtils.isEmpty(company_address)
				|| "null".equals(company_address))
			return "";

		return company_address;
	}

	public void setCompany_address(String company_address) {
		if (TextUtils.isEmpty(company_address))
			this.company_address = Run.EMPTY_STR;
		else
			this.company_address = company_address;
	}

	public String getOpening_bank() {
		if (TextUtils.isEmpty(opening_bank) || "null".equals(opening_bank))
			return "";

		return opening_bank;
	}

	public void setOpening_bank(String opening_bank) {
		if (TextUtils.isEmpty(opening_bank))
			this.opening_bank = Run.EMPTY_STR;
		else
			this.opening_bank = opening_bank;
	}

	public String getBank_number() {
		if (TextUtils.isEmpty(bank_number) || "null".equals(bank_number))
			return "";

		return bank_number;
	}

	public void setBank_number(String bank_number) {
		if (TextUtils.isEmpty(bank_number))
			this.bank_number = Run.EMPTY_STR;
		else
			this.bank_number = bank_number;
	}

	public String getBank_name() {
		if (TextUtils.isEmpty(bank_name) || "null".equals(bank_name))
			return "";

		return bank_name;
	}

	public void setBank_name(String bank_name) {
		if (TextUtils.isEmpty(bank_name))
			this.bank_name = Run.EMPTY_STR;
		else
			this.bank_name = bank_name;
	}

	public String getPhone() {
		return !mUserInfo.isNull("mobile") ? mUserInfo.optString("mobile")
				: Run.EMPTY_STR;
	}

	public void setPhone(String phone) {
		try {
			mUserInfo.put("mobile", phone);
		} catch (Exception e) {
		}
	}

	public String getLogo() {
		if (TextUtils.isEmpty(logo) || "null".equals(logo))
			return "";

		return logo;
	}

	public void setLogo(String logo) {
		if (TextUtils.isEmpty(logo))
			this.logo = Run.EMPTY_STR;
		else
			this.logo = logo;
	}

	// =================================
	public String getMemberId() {
		try {
			return mUserInfo.optString("member_id");
		} catch (Exception e) {
			return "";
		}
	}

	public String getAgencyNo() {
		try {
			return mUserInfo.optString("agency_no");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 * @return 1:一般用户<br>
	 *         2:微店用户<br>
	 *         3:经销商<br>
	 */
	public int getMemberType() {
		try {
			return mUserInfo.optInt("member_type", 1);
		} catch (Exception e) {
			return 1;
		}
	}

	public String getNickName(Context context) {
		try {
			String nickname = context.getString(R.string.account_header_noname);
			if (mUserInfo.isNull("name"))
				return nickname;
			nickname = mUserInfo.optString("name");
			nickname = TextUtils.isEmpty(nickname) ? context
					.getString(R.string.account_header_noname) : nickname;
			return nickname;
		} catch (Exception e) {
			return context.getString(R.string.account_header_noname);
		}
	}

	public void setNickName(String name) {
		try {
			if (mUserInfo.isNull("name"))
				mUserInfo.put("name", name);
			mUserInfo.remove("name");
			mUserInfo.put("name", name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取最原始的昵称
	 * 
	 * @param context
	 * @return
	 */
	public String getRealNickName(Context context) {
		try {
			if (mUserInfo.isNull("name"))
				return null;
			return mUserInfo.optString("name");
		} catch (Exception e) {
			return null;
		}
	}

	public String getFansQuantity() {
		try {
			if (!mUserInfo.isNull("fans_num"))
				return mUserInfo.optString("fans_num");
		} catch (Exception e) {
		}
		return "0";
	}

	public void setSex(int sex) {
		try {
			if (mUserInfo.isNull("sex"))
				return;
			mUserInfo.remove("sex");
			mUserInfo.put("sex", sex);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFollowedQuantity() {
		try {
			if (!mUserInfo.isNull("follow_num"))
				return mUserInfo.optString("follow_num");
		} catch (Exception e) {
		}
		return "0";
	}

	public String getOfflineNum() {
		if (mUserInfo != null && !mUserInfo.isNull("offline_cardno")) {
			return mUserInfo.optString("offline_cardno");
		}
		return "NULL";
	}

	public int getPoint() {
		if (mUserInfo != null) {
			return mUserInfo.optInt("point");
		}
		return 0;
	}
}
