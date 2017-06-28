package com.saama.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.AsyncContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.saama.util.CryptoUtil;
import com.saama.util.PropertiyReader;

public class GetUserInfo implements Runnable {

	private final String CONST_STR_EMAIL = "email";
	private final String CONST_STR_CODE = "code";
	private final String CONST_STR_TOKEN = "token";
	private final String CONST_EMPTY_STR = new String("");
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private AsyncContext asyncCtx;

	public GetUserInfo(HttpServletRequest req, HttpServletResponse resp, AsyncContext asyncCtx) {
		this.req = req;
		this.resp = resp;
		this.asyncCtx = asyncCtx;
	}

	@Override
	public void run() {
		HttpSession sess = req.getSession();
		OAuthService service = (OAuthService) sess.getAttribute("oauth2Service");
		String code = req.getParameter(CONST_STR_CODE);
		Token token = service.getAccessToken(null, new Verifier(code));
		sess.setAttribute(CONST_STR_TOKEN, token);
		OAuthRequest oReq = new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v2/userinfo");
		
		service.signRequest(token, oReq);
		Response oResp = oReq.send();
		PropertiyReader propreader = new PropertiyReader("/credentials.properties");
		JsonReader reader = Json.createReader(new ByteArrayInputStream(oResp.getBody().getBytes()));
		JsonObject profile = reader.readObject();
		CryptoUtil cryptoUtil = new CryptoUtil();
		String cookieValue = CONST_EMPTY_STR;
		
		try {
			cookieValue = cryptoUtil.encrypt(propreader.readPropertyFile("CRYPTO_KEY"),
					profile.getString(CONST_STR_EMAIL).trim());
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | UnsupportedEncodingException | IllegalBlockSizeException
				| BadPaddingException e1) {
			cookieValue = CONST_EMPTY_STR;
		}
		Cookie myCookie = new Cookie(propreader.readPropertyFile("COOKIE_NAME").trim(), cookieValue);
		myCookie.setPath("/");
		myCookie.setSecure(true);
		myCookie.setHttpOnly(true);
		if (profile.getString(CONST_STR_EMAIL).contains(propreader.readPropertyFile("STRING_TO_BE_CHECKED"))) {
			try {
				this.resp.sendRedirect(propreader.readPropertyFile("WEBSITE_SSO_URL"));
				System.out.println(resp);
			} catch (IOException e) {
			}
		} else {
			try {
				eraseCookie(this.req, this.resp, propreader.readPropertyFile("COOKIE_NAME").trim());
				this.resp.addCookie(myCookie);

				this.resp.sendRedirect(propreader.readPropertyFile("WEBSITE_URL"));
			} catch (IOException e) {
			}
		}
		asyncCtx.complete();
	}

	private void eraseCookie(HttpServletRequest req, HttpServletResponse resp, String cookeName) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null)
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().trim().equalsIgnoreCase(cookeName.trim())) {
					cookies[i].setValue("");
					cookies[i].setPath("/");
					cookies[i].setMaxAge(0);
					resp.addCookie(cookies[i]);
				}
			}
	}
}