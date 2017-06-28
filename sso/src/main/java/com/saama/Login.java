package com.saama;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.scribe.builder.ServiceBuilder;
import org.scribe.oauth.OAuthService;

import com.saama.util.PropertiyReader;

@WebServlet("/Login")
public class Login extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public Login() {
		super();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		PropertiyReader reader = new PropertiyReader("/credentials.properties");
		ServiceBuilder builder = new ServiceBuilder();
		OAuthService service = builder.provider(Google2Api.class).apiKey(reader.readPropertyFile("CLIENT_ID"))
				.apiSecret(reader.readPropertyFile("CLIENT_SECRET")).callback(reader.readPropertyFile("CALLBACK_URL"))
				.scope("openid profile email " + "https://www.googleapis.com/auth/plus.login "
						+ "https://www.googleapis.com/auth/userinfo.profile "
						+ "https://www.googleapis.com/auth/userinfo.email ")
				.debug().build();
		HttpSession sess = req.getSession();
		sess.setAttribute("oauth2Service", service);
		res.sendRedirect(service.getAuthorizationUrl(null));
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}